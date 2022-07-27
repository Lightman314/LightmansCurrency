package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.PersistentAuctionData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.*;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageInitializeClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageUpdateClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.SPacketSyncSelectedBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.SPacketSyncEjectionData;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageClientNotification;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageUpdateClientNotifications;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageInitializeClientTeams;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageClearClientTraders;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRemoveClientTrader;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateClientData;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends SavedData{
	
	public static final String PERSISTENT_TRADER_FILENAME = "config/lightmanscurrency/persistentTraders.json";
	
	public static final String PERSISTENT_TRADER_SECTION = "Traders";
	public static final String PERSISTENT_AUCTION_SECTION = "Auctions";
	
	private static TradingOffice activeOffice = null;
	
	private static JsonObject persistentTraderData = null;
	
	public static JsonObject getPersistentTraderJson() {
		//Force the Trading Office to be loaded.
		get(ServerLifecycleHooks.getCurrentServer());
		return persistentTraderData;
	}
	
	public static JsonArray getPersistentTraderJson(String section) {
		JsonObject json = getPersistentTraderJson();
		if(json != null)
		{
			if(!json.has(section))
			{
				JsonArray newSection = new JsonArray();
				json.add(section, newSection);
			}
			if(json.get(section).isJsonArray())
				return json.get(section).getAsJsonArray();
			else
				LightmansCurrency.LogError("Cannot get Persistent Data section '" + section + "' as it is not a JsonArray.");
		}
		return null;
	}
	
	public static void setPersistentTraderJson() { setPersistentTraderJson(getPersistentTraderJson()); }
	
	public static void setPersistentTraderJson(JsonObject newData) {
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		File ptf = new File(PERSISTENT_TRADER_FILENAME);
		try {
			office.loadPersistentTrader(newData);
		} catch(Exception e) {
			LightmansCurrency.LogError("Error loading modified Persistent Trader Data. Ignoring request.", e);
			return;
		}
		//Now that it's safely loaded, set the data and save to file
		persistentTraderData = newData;
		office.savePersistentTraderJson(ptf);
		office.resendTraderData();
	}
	
	public static void setPersistentTraderSection(String section, JsonArray newData) {
		JsonObject json = getPersistentTraderJson();
		json.add(section, newData);
		setPersistentTraderJson(json);
	}
	
	public TradingOffice() {
		cleanOldOffice(this);
		this.validateAuctionHouse();
		this.loadPersistentTraders();
	}
	
	public TradingOffice(CompoundTag tag) {
		cleanOldOffice(this);
		this.load(tag);
		this.validateAuctionHouse();
		this.loadPersistentTraders();
	}
	
	private static void cleanOldOffice(TradingOffice newOffice) {
		if(activeOffice != null)
		{
			activeOffice.persistentTraderMap.forEach((id,data) -> data.onRemoved());
			activeOffice.universalTraderMap.forEach((id,data) -> data.onRemoved());
		}
		activeOffice = newOffice;
	}
	
	public static void forceValidateAuctionHouse() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			get(server).validateAuctionHouse();
	}
	
	private void validateAuctionHouse() {
		if(!Config.SERVER.enableAuctionHouse.get())
		{
			LightmansCurrency.LogInfo("Will not create or validate the auction house as the auction house is disabled.");
			return;
		}
		AtomicBoolean hasAuctionHouse = new AtomicBoolean(false);
		this.universalTraderMap.forEach((id,data) -> {
			if(data instanceof AuctionHouseTrader)
				hasAuctionHouse.set(true);
		});
		if(!hasAuctionHouse.get())
		{
			//Create the auction house manually
			AuctionHouseTrader ah = new AuctionHouseTrader();
			ah.getCoreSettings().forceCreative();
			
			//Generate a trader ID
			UUID traderID = this.getValidTraderID();
			
			//Apply it to the trader
			ah.initTraderID(traderID);
			
			LightmansCurrency.LogInfo("Successfully created an auction house trader with id '" + traderID + "'!");
			this.universalTraderMap.put(traderID, ah);
			this.setDirty();
			//Send update packet to the connected clients
			CompoundTag compound = ah.write(new CompoundTag());
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			
		}
	}
	
	private static final Map<ResourceLocation,Supplier<? extends UniversalTraderData>> registeredDeserializers = Maps.newHashMap();
	
	public static final void RegisterDataType(ResourceLocation key, Supplier<? extends UniversalTraderData> source)
	{
		if(registeredDeserializers.containsKey(key))
			LightmansCurrency.LogError("A universal trader type of key " + key + " has already been registered.");
		else
			registeredDeserializers.put(key, source);
	}
	
	private static final String DATA_NAME = LightmansCurrency.MODID + "_trading_office";
	
	private static List<UUID> adminPlayers = new ArrayList<>();
	
	private Map<UUID, UniversalTraderData> persistentTraderMap = new HashMap<>();
	private Map<UUID, String> persistentTraderIDs = new HashMap<>();
	private List<PersistentAuctionData> persistentAuctionData = new ArrayList<>();
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	private Map<UUID, Team> playerTeams = new HashMap<>();
	private Map<UUID, BankAccount> playerBankAccounts = new HashMap<>();
	private Map<UUID, NotificationData> playerNotifications = new HashMap<>();
	private Map<UUID, AccountReference> lastSelectedAccounts = new HashMap<>();
	private List<EjectionData> emergencyEjectionData = new ArrayList<>();
	
	//Store persistent data locally, so that it doesn't get lost if the persistent trader file is malformed
	ListTag persistentData = new ListTag();
	
	public static UniversalTraderData Deserialize(CompoundTag compound)
	{
		ResourceLocation thisType = new ResourceLocation(compound.getString("type"));
		//New method
		if(registeredDeserializers.containsKey(thisType))
		{
			UniversalTraderData data = registeredDeserializers.get(thisType).get();
			data.read(compound);
			return data;
		}
		return null;
	}
	
	public static UniversalTraderData Deserialize(JsonObject json) throws Throwable
	{
		if(!json.has("type") || !json.get("type").isJsonPrimitive() || !json.get("type").getAsJsonPrimitive().isString())
			throw new Exception("No string 'type' entry for this trader.");
		ResourceLocation thisType = new ResourceLocation(json.get("type").getAsString());
		if(registeredDeserializers.containsKey(thisType))
		{
			UniversalTraderData data = registeredDeserializers.get(thisType).get();
			data.loadFromJson(json);
			return data;
		}
		throw new Exception("Trader type '" + thisType + "' is undefined.");
	}
	
	public void load(CompoundTag compound) {
		
		if(compound.contains("UniversalTraders", Tag.TAG_LIST))
		{
			this.universalTraderMap.forEach((id,data) -> data.onRemoved());
			this.universalTraderMap.clear();
			ListTag universalTraderDataList = compound.getList("UniversalTraders", Tag.TAG_COMPOUND);
			universalTraderDataList.forEach(nbt ->{
				CompoundTag traderNBT = (CompoundTag)nbt;
				//UUID traderID = traderNBT.getUniqueId("ID");
				//UniversalTraderData data = IUniversalDataDeserializer.Deserialize(traderNBT);
				UniversalTraderData data = Deserialize(traderNBT);
				if(data != null)
					universalTraderMap.put(data.getTraderID(), data);
			});
		}
		
		if(compound.contains("Teams", Tag.TAG_LIST))
		{
			this.playerTeams.clear();
			ListTag teamList = compound.getList("Teams", Tag.TAG_COMPOUND);
			for(int i = 0; i < teamList.size(); ++i)
			{
				Team team = Team.load(teamList.getCompound(i));
				if(team != null)
					this.playerTeams.put(team.getID(), team);
			}
		}
		
		if(compound.contains("BankAccounts", Tag.TAG_LIST))
		{
			this.playerBankAccounts.clear();
			ListTag bankAccountList = compound.getList("BankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < bankAccountList.size(); ++i)
			{
				CompoundTag accountCompound = bankAccountList.getCompound(i);
				try {
					UUID owner = accountCompound.getUUID("Player");
					BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(owner), accountCompound);
					if(owner != null && bankAccount != null)
					{
						//Generate notification consumer
						bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(owner));
						this.playerBankAccounts.put(owner, bankAccount);
						//Update owners name if the player has changed their name
						bankAccount.updateOwnersName(PlayerReference.of(owner, bankAccount.getOwnersName()).lastKnownName());
					}
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		if(compound.contains("PersistentTraderData", Tag.TAG_LIST))
			this.persistentData = compound.getList("PersistentTraderData", Tag.TAG_COMPOUND);
		
		if(compound.contains("PersistentTraderIDs", Tag.TAG_LIST))
		{
			this.persistentTraderIDs.clear();
			ListTag persistentIDs = compound.getList("PersistentTraderIDs", Tag.TAG_COMPOUND);
			for(int i = 0; i < persistentIDs.size(); ++i)
			{
				try {
					CompoundTag idData = persistentIDs.getCompound(i);
					UUID uuid = idData.getUUID("UUID");
					String traderID = idData.getString("TraderID");
					if(uuid != null && traderID != null)
						this.persistentTraderIDs.put(uuid, traderID);
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		if(compound.contains("PlayerNotifications", Tag.TAG_LIST))
		{
			this.playerNotifications.clear();
			ListTag notificationData = compound.getList("PlayerNotifications", Tag.TAG_COMPOUND);
			for(int i = 0; i < notificationData.size(); ++i)
			{
				CompoundTag notData = notificationData.getCompound(i);
				if(notData.contains("Player"))
				{
					UUID playerID = notData.getUUID("Player");
					NotificationData data = NotificationData.loadFrom(notData);
					if(playerID != null && data != null)
						this.playerNotifications.put(playerID, data);
				}
			}
		}
		
		if(compound.contains("LastSelectedBankAccounts", Tag.TAG_LIST))
		{
			this.lastSelectedAccounts.clear();
			ListTag selectedAccounts = compound.getList("LastSelectedBankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < selectedAccounts.size(); ++i)
			{
				CompoundTag accountData = selectedAccounts.getCompound(i);
				if(accountData.contains("Player"))
				{
					UUID playerID = accountData.getUUID("Player");
					AccountReference account = BankAccount.LoadReference(false, accountData);
					if(playerID != null && account != null)
						this.lastSelectedAccounts.put(playerID, account);
				}
			}
		}
		
		if(compound.contains("EmergencyEjectionData", Tag.TAG_LIST))
		{
			this.emergencyEjectionData.clear();
			ListTag ejectionData = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
			for(int i = 0; i < ejectionData.size(); ++i)
			{
				try {
					this.emergencyEjectionData.add(EjectionData.loadData(ejectionData.getCompound(i)));
				} catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
			}
		}
		
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		
		ListTag universalTraderDataList = new ListTag();
		this.universalTraderMap.forEach((traderID, traderData) ->
		{
			if(traderData != null)
			{
				CompoundTag traderNBT = traderData.write(new CompoundTag());
				traderNBT.putUUID("ID", traderID);
				universalTraderDataList.add(traderNBT);
			}
		});
		compound.put("UniversalTraders", universalTraderDataList);
		
		ListTag teamList = new ListTag();
		this.playerTeams.forEach((teamID, team) ->{
			if(team != null)
				teamList.add(team.save());
		});
		compound.put("Teams", teamList);
		
		ListTag bankAccountList = new ListTag();
		this.playerBankAccounts.forEach((playerID, account) ->{
			CompoundTag accountNBT = account.save();
			accountNBT.putUUID("Player", playerID);
			bankAccountList.add(accountNBT);
		});
		compound.put("BankAccounts", bankAccountList);
		
		this.persistentTraderMap.forEach((ID, traderData) ->{
			if(traderData != null && this.persistentTraderIDs.containsKey(ID))
			{
				CompoundTag data = traderData.getPersistentData();
				String traderID = this.persistentTraderIDs.get(ID);
				data.putString("traderID", traderID);
				this.setPersistentData(traderID, data);
			}
		});
		compound.put("PersistentTraderData", this.persistentData);
		
		ListTag persistentTraderIDs = new ListTag();
		this.persistentTraderIDs.forEach((uuid, traderID) ->{
			CompoundTag idData = new CompoundTag();
			idData.putUUID("UUID", uuid);
			idData.putString("TraderID", traderID);
			persistentTraderIDs.add(idData);
		});
		compound.put("PersistentTraderIDs", persistentTraderIDs);
		
		ListTag notficationData = new ListTag();
		this.playerNotifications.forEach((uuid, notificationData) ->{
			CompoundTag notData = notificationData.save();
			notData.putUUID("Player", uuid);
			notficationData.add(notData);
		});
		compound.put("PlayerNotifications", notficationData);
		
		ListTag selectedAccounts = new ListTag();
		this.lastSelectedAccounts.forEach((uuid,account) -> {
			CompoundTag accountData = account.save();
			accountData.putUUID("Player", uuid);
			selectedAccounts.add(accountData);
		});
		compound.put("LastSelectedBankAccounts", selectedAccounts);
		
		ListTag ejectionData = new ListTag();
		this.emergencyEjectionData.forEach(data -> ejectionData.add(data.save(new CompoundTag())));
		compound.put("EmergencyEjectionData", ejectionData);
		
		return compound;
	}
	
	public static void reloadPersistentTraders() {
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		office.loadPersistentTraders();
		office.resendTraderData();
	}
	
	private void loadPersistentTraders() {
		//Get JSON file
		File ptf = new File(PERSISTENT_TRADER_FILENAME);
		if(!ptf.exists())
		{
			persistentTraderData = this.generateDefaultPersistentTraderJson();
			this.savePersistentTraderJson(ptf);
		}
		try { 
			persistentTraderData = GsonHelper.parse(Files.readString(ptf.toPath()));
			this.loadPersistentTrader(persistentTraderData);
		} catch(Throwable e) {
			LightmansCurrency.LogError("Error loading Persistent Traders.", e);
			//If an error occurs while loading, set the data to default.
			persistentTraderData = this.generateDefaultPersistentTraderJson();
		}
	}
	
	private JsonObject generateDefaultPersistentTraderJson() {
		JsonObject fileData = new JsonObject();
		JsonArray traderList = new JsonArray();
		fileData.add("Traders", traderList);
		JsonArray auctions = new JsonArray();
		fileData.add("Auctions", auctions);
		return fileData;
	}
	
	private void loadPersistentTrader(JsonObject fileData) throws Exception {
		boolean hadNone = true;
		if(fileData.has(PERSISTENT_TRADER_SECTION))
		{
			hadNone = false;
			this.persistentTraderMap.forEach((id,data) -> data.onRemoved());
			this.persistentTraderMap.clear();
			List<String> loadedIDs = new ArrayList<>();
			JsonArray traderList = fileData.getAsJsonArray(PERSISTENT_TRADER_SECTION);
			for(int i = 0; i < traderList.size(); ++i)
			{
				try {
					
					//Load the trader
					JsonObject traderTag = traderList.get(i).getAsJsonObject();
					String traderID;
					if(traderTag.has("id"))
						traderID = traderTag.get("id").getAsString();
					else
						throw new Exception("Trader has no defined id.");
					if(loadedIDs.contains(traderID))
						throw new Exception("Trader with id '" + traderID + "' already exists. Cannot have duplicate ids.");
					UniversalTraderData data = Deserialize(traderTag);
					
					//Make the trader creative
					data.getCoreSettings().forceCreative();
					
					//Load the persistent data
					data.loadPersistentData(this.getPersistentData(traderID));
					
					//Match the persistent data with traders UUID
					UUID id = this.getPersistentTraderUUID(traderID);
					if(id == null) //If no ID has ever been generated for this persistent trader ID, generate one and add it to the list
					{
						id = this.getValidTraderID();
						this.persistentTraderIDs.put(id, traderID);
						this.setDirty();
					}
					data.initTraderID(id);
					
					this.persistentTraderMap.put(id, data);
					loadedIDs.add(traderID);
					LightmansCurrency.LogInfo("Successfully loaded persistent trader '" + traderID + "' with UUID " + id.toString() + ".");
					
				} catch(Throwable e) { LightmansCurrency.LogError("Error loading Persistent Trader at index " + i, e); }
			}
		}
		if(fileData.has(PERSISTENT_AUCTION_SECTION))
		{
			hadNone = false;
			this.persistentAuctionData.clear();
			List<String> loadedIDs = new ArrayList<>();
			JsonArray auctionList = fileData.getAsJsonArray(PERSISTENT_AUCTION_SECTION);
			for(int i = 0; i < auctionList.size(); ++i)
			{
				try {
					
					//Load the auction
					JsonObject auctionTag = auctionList.get(i).getAsJsonObject();
					PersistentAuctionData data = PersistentAuctionData.load(auctionTag);
					if(loadedIDs.contains(data.id))
						throw new Exception("Auction with id '" + data.id + "' already exists. Cannot have duplicate ids.");
					else
						loadedIDs.add(data.id);
					
					this.persistentAuctionData.add(data);
					
					LightmansCurrency.LogInfo("Successfully loaded persistent auction '" + data.id + "'");
					
				} catch(Throwable e) { LightmansCurrency.LogError("Error loading Persistent Auction at index " + i, e); }
				
			}
			
		}
		if(hadNone)
			throw new Exception("Json Data has no 'Traders' or 'Auctions' entry.");
	};
	
	private UUID getPersistentTraderUUID(String traderID) {
		AtomicReference<UUID> result = new AtomicReference<UUID>(null);
		if(this.persistentTraderIDs.containsValue(traderID))
		{
			this.persistentTraderIDs.forEach((uuid, id) ->{
				if(id.contentEquals(traderID))
					result.set(uuid);
			});
		}
		return result.get();
	}
	
	private CompoundTag getPersistentData(String traderID) {
		for(int i = 0; i < this.persistentData.size(); ++i)
		{
			CompoundTag thisData = this.persistentData.getCompound(i);
			if(thisData.getString("traderID").contentEquals(traderID))
				return thisData;
		}
		return new CompoundTag();
	}
	
	private void setPersistentData(String traderID, CompoundTag data) {
		for(int i = 0; i < this.persistentData.size(); ++i) {
			CompoundTag thisData = this.persistentData.getCompound(i);
			if(thisData.getString("traderID").contentEquals(traderID))
			{
				this.persistentData.set(i, data);
				return;
			}
		}
		this.persistentData.add(data);
	}
	
	private void savePersistentTraderJson(File ptf) {
		File dir = new File(ptf.getParent());
		if(!dir.exists())
			dir.mkdirs();
		if(dir.exists())
		{
			try {
				
				ptf.createNewFile();
				
				String jsonString = FileUtil.GSON.toJson(persistentTraderData);
				
				FileUtil.writeStringToFile(ptf, jsonString);
				
				LightmansCurrency.LogInfo("persistentTraders.json does not exist. Creating a fresh copy.");
				
			} catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'persistentTraders.json' file.", e); }
		}
	}
	
	public static UniversalTraderData getData(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.universalTraderMap.containsKey(traderID))
				return office.universalTraderMap.get(traderID);
			else if(office.persistentTraderMap.containsKey(traderID))
				return office.persistentTraderMap.get(traderID);
		}
		return null;
	}
	
	public static List<UniversalTraderData> getTraders()
	{
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		List<UniversalTraderData> traders = office.universalTraderMap.values().stream().collect(Collectors.toList());
		traders.addAll(office.persistentTraderMap.values());
		return traders;
	}
	
	public static List<UniversalTraderData> filterTraders(String searchFilter, List<UniversalTraderData> traders)
	{
		if(searchFilter.isEmpty())
			return traders;
		Stream<UniversalTraderData> stream = traders.stream().filter(entry ->{
			String searchText = searchFilter.toLowerCase().trim();
			//Search the display name of the traders
			if(entry.getName().getString().toLowerCase().contains(searchText))
				return true;
			//Search the owner name of the traders
			if(entry.getCoreSettings().getOwnerName().toLowerCase().contains(searchText))
				return true;
			//Search any custom filters
			return TraderSearchFilter.checkFilters(entry, searchText);
		});
		return stream.collect(Collectors.toList());
	}
	
	public static List<UniversalTraderData> getTraders(String searchFilter)
	{
		return filterTraders(searchFilter, getTraders());
	}
	
	public static void MarkDirty(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).setDirty();
			//Send update packet to all connected clients
			UniversalTraderData data = getData(traderID);
			if(data != null)
			{
				CompoundTag compound = data.write(new CompoundTag());
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			}
		}
	}
	
	public static void MarkDirty(UUID traderID, CompoundTag updateMessage)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).setDirty();
			//Send update packet to all connected clients
			UniversalTraderData data = getData(traderID);
			if(data != null)
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(updateMessage));
		}
	}
	
	public static List<Team> getTeams()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return get(server).playerTeams.values().stream().collect(Collectors.toList());
		return new ArrayList<>();
	}
	
	public static Team getTeam(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.playerTeams.containsKey(teamID))
				return office.playerTeams.get(teamID);
		}
		return null;
	}
	
	public static void MarkTeamDirty(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).setDirty();
			//Send update packet to all connected clients
			Team team = getTeam(teamID);
			if(team != null)
			{
				CompoundTag compound = team.save();
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientTeam(compound));
			}
		}
	}
	
	private UUID getValidTraderID() { 
		UUID traderID = UUID.randomUUID();
		while(this.universalTraderMap.containsKey(traderID) || this.persistentTraderIDs.containsKey(traderID))
			traderID = UUID.randomUUID();
		return traderID;
	}
	
	public static UUID registerTrader(UniversalTraderData data, @Nullable Player owner)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			//Generate a trader ID
			UUID traderID = office.getValidTraderID();
			
			//Apply it to the trader
			data.initTraderID(traderID);
			
			LightmansCurrency.LogInfo("Successfully registered the universal trader with id '" + traderID + "'!");
			office.universalTraderMap.put(traderID, data);
			office.setDirty();
			//Send update packet to the connected clients
			CompoundTag compound = data.write(new CompoundTag());
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			//Post Universal Trader Create Event
			if(owner != null)
				MinecraftForge.EVENT_BUS.post(new UniversalTradeCreateEvent(traderID, owner));
			
			return traderID;
		}
		return null;
		
	}
	
	public static void removeTrader(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office.universalTraderMap.containsKey(traderID))
		{
			UniversalTraderData removedData = office.universalTraderMap.get(traderID);
			office.universalTraderMap.remove(traderID);
			office.setDirty();
			removedData.onRemoved();
			LightmansCurrency.LogInfo("Successfully removed the universal trader with id '" + traderID + "'!");
			//Send update packet to the connected clients
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTrader(traderID));
			MinecraftForge.EVENT_BUS.post(new UniversalTradeRemoveEvent(traderID, removedData));
		}
	}
	
	public static Team registerTeam(Player owner, String teamName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			UUID teamID = UUID.randomUUID();
			while(office.playerTeams.containsKey(teamID))
				teamID = UUID.randomUUID();
			
			Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
			office.playerTeams.put(teamID, newTeam);
			
			//Send update packet to the connected clients
			MarkTeamDirty(newTeam.getID());
			
			return newTeam;
			
		}
		return null;
	}
	
	public static void removeTeam(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.playerTeams.containsKey(teamID))
			{
				office.playerTeams.remove(teamID);
				office.setDirty();
				//Send update packet to the connected clients
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTeam(teamID));
			}
		}
	}
	
	public static List<AccountReference> getPlayerBankAccounts() {
		List<AccountReference> list = new ArrayList<>();
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office != null)
		{
			office.playerBankAccounts.forEach((playerID, bankAccount) ->{
				list.add(BankAccount.GenerateReference(false, AccountType.Player, playerID));
			});
		}
		return list;
	}
	
	/**
	 * Gets the bank account for the given player.
	 * If no account exists for that player, a new one will be created and synced to clients.
	 */
	public static BankAccount getBankAccount(Player player) {
		return getBankAccount(player.getUUID());
	}
	
	/**
	 * Gets the bank account for the given player.
	 * If no account exists for that player, a new one will be created and synced to clients.
	 */
	public static BankAccount getBankAccount(UUID playerID) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office != null)
		{
			if(office.playerBankAccounts.containsKey(playerID))
				return office.playerBankAccounts.get(playerID);
			//Create a new bank account for the player;
			BankAccount newAccount = new BankAccount(() -> MarkBankAccountDirty(playerID));
			newAccount.updateOwnersName(PlayerReference.of(playerID, "Unknown").lastKnownName());
			office.playerBankAccounts.put(playerID, newAccount);
			MarkBankAccountDirty(playerID);
			return newAccount;
		}
		return null;
	}
	
	public static void MarkBankAccountDirty(Player player) { if(player.level.isClientSide) return; MarkBankAccountDirty(player.getUUID()); }
	
	public static void MarkBankAccountDirty(UUID playerID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).setDirty();
			//Send update packet to all connected clients
			BankAccount bankAccount = getBankAccount(playerID);
			CompoundTag compound = bankAccount.save();
			compound.putUUID("Player", playerID);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientBank(compound));
		}
	}
	
	public static NotificationData getNotifications(Player player) { return getNotifications(player.getUUID()); }
	
	public static NotificationData getNotifications(UUID playerID) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office != null)
		{
			if(office.playerNotifications.containsKey(playerID))
			{
				return office.playerNotifications.get(playerID);
			}
			//Create a new notification data set for the player;
			NotificationData newData = new NotificationData();
			office.playerNotifications.put(playerID, newData);
			MarkNotificationsDirty(playerID);
			return newData;
		}
		return null;
	}
	
	public static boolean pushNotification(UUID playerID, Notification notification) { return pushNotification(playerID, notification, true); }
	
	public static boolean pushNotification(UUID playerID, Notification notification, boolean pushToChat) {
		if(notification == null)
		{
			LightmansCurrency.LogError("Cannot push a null notification!");
			return false;
		}
		NotificationData data = getNotifications(playerID);
		if(data != null)
		{
			//Post event to see if we should sent the notification
			NotificationEvent.NotificationSent.Pre event = new NotificationEvent.NotificationSent.Pre(playerID, data, notification);
			if(MinecraftForge.EVENT_BUS.post(event))
				return false;
			//Passed the pre event, add the notification to the notification data
			data.addNotification(event.getNotification());
			//Mark the data as dirty
			MarkNotificationsDirty(playerID);
			//Run the post event to notify anyone who cares that the notification was created.
			MinecraftForge.EVENT_BUS.post(new NotificationEvent.NotificationSent.Post(playerID, data, event.getNotification()));
			
			//Send the notification message to the client so that it will be posted in chat
			if(pushToChat)
			{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if(server != null)
				{
					ServerPlayer player = server.getPlayerList().getPlayer(playerID);
					if(player != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageClientNotification(notification));
				}
			}
			
			return true;
		}
		return false;
	}
	
	public static void MarkNotificationsDirty(UUID playerID) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).setDirty();
			//Send update packet to the relevant player
			ServerPlayer player = server.getPlayerList().getPlayer(playerID);
			if(player != null)
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageUpdateClientNotifications(getNotifications(playerID)));
		}
	}
	
	public static AccountReference getSelectedBankAccount(Player player) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.lastSelectedAccounts.containsKey(player.getUUID()))
			{
				AccountReference account = office.lastSelectedAccounts.get(player.getUUID());
				if(!account.allowedAccess(player))
				{
					LightmansCurrency.LogInfo(player.getName().getString() + " is no longer allowed to access their selected bank account. Switching back to their personal account.");
					account = BankAccount.GenerateReference(player);
					setSelectedBankAccount(player, account);
				}
				return account;
			}
			//Generate default bank account for the player
			AccountReference account = BankAccount.GenerateReference(player);
			setSelectedBankAccount(player,account);
			return account;
		}
		return BankAccount.GenerateReference(player);
	}
	
	public static void setSelectedBankAccount(Player player, AccountReference account) {
		//Ignore if the account is null or the player isn't allowed to access it.
		if(account == null)
			return;
		if(!account.allowedAccess(player))
		{
			LightmansCurrency.LogInfo("Player does not have access to the selected account. Canceling selection.");
			return;
		}
			
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			office.lastSelectedAccounts.put(player.getUUID(), account);
			
			office.setDirty();
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketSyncSelectedBankAccount(account));
		}
	}
	
	public static List<EjectionData> getEjectionData() {
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		return office.emergencyEjectionData;
	}
	
	public static void handleEjectionData(Level level, BlockPos pos, EjectionData data) {
		Objects.requireNonNull(data);
		
		if(Config.SERVER.safelyEjectIllegalBreaks.get())
		{
			TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
			if(office != null)
			{
				office.emergencyEjectionData.add(data);
				MarkEjectionDataDirty();
			}
		}
		else
			InventoryUtil.dumpContents(level, pos, data);
		
	}
	
	public static void removeEjectionData(EjectionData data) {
		Objects.requireNonNull(data);
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		if(office != null && office.emergencyEjectionData.contains(data))
		{
			office.emergencyEjectionData.remove(data);
			MarkEjectionDataDirty();
		}
	}
	
	public static void MarkEjectionDataDirty() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			office.setDirty();
			//Send update packet to all connected clients
			CompoundTag compound = new CompoundTag();
			ListTag ejectionList = new ListTag();
			office.emergencyEjectionData.forEach(data -> {
				ejectionList.add(data.save(new CompoundTag()));
			});
			compound.put("EmergencyEjectionData", ejectionList);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new SPacketSyncEjectionData(compound));
		}
	}
	
	private static TradingOffice get(MinecraftServer server)
    {
		if(server == null)
			return null;
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        return world.getDataStorage().computeIfAbsent(TradingOffice::new, TradingOffice::new, DATA_NAME);
    }
	
	/**
	 * Sync traders with new players on login
	 */
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
			
			TradingOffice office = get(server);
			
			//Send the clear message
			LightmansCurrencyPacketHandler.instance.send(target, new MessageClearClientTraders());
			//Send update message to the newly connected client
			office.universalTraderMap.forEach((id, trader) -> LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientData(trader.write(new CompoundTag()))));
			office.persistentTraderMap.forEach((id, trader) -> LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientData(trader.write(new CompoundTag()))));
			
			CompoundTag compound2 = new CompoundTag();
			ListTag teamList = new ListTag();
			office.playerTeams.forEach((id, team) -> teamList.add(team.save()));
			compound2.put("Teams", teamList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTeams(compound2));
			
			//Confirm the presence of the loading players bank account
			getBankAccount(event.getEntity());
			
			CompoundTag compound3 = new CompoundTag();
			ListTag bankList = new ListTag();
			office.playerBankAccounts.forEach((id, team) -> {
				CompoundTag tag = team.save();
				tag.putUUID("Player", id);
				bankList.add(tag);
			});
			compound3.put("BankAccounts", bankList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientBank(compound3));
			
			//Only send their personal notifications
			NotificationData notifications = getNotifications(event.getEntity());
			LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientNotifications(notifications));
			
			//Update to let them know their selected bank account
			AccountReference selectedAccount = getSelectedBankAccount(event.getEntity());
			LightmansCurrencyPacketHandler.instance.send(target, new SPacketSyncSelectedBankAccount(selectedAccount));
			
			//Send ejection data
			CompoundTag compound4 = new CompoundTag();
			ListTag ejectionList = new ListTag();
			office.emergencyEjectionData.forEach(data -> {
				ejectionList.add(data.save(new CompoundTag()));
			});
			compound4.put("EmergencyEjectionData", ejectionList);
			LightmansCurrencyPacketHandler.instance.send(target, new SPacketSyncEjectionData(compound4));
			
		}
	}
	
	private void resendTraderData() {
		PacketTarget target = PacketDistributor.ALL.noArg();
		LightmansCurrencyPacketHandler.instance.send(target, new MessageClearClientTraders());
		this.universalTraderMap.forEach((id, trader) -> LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientData(trader.write(new CompoundTag()))));
		this.persistentTraderMap.forEach((id, trader) -> LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientData(trader.write(new CompoundTag()))));
	}
	
	/**
	 * Clean up invalid traders
	 */
	@SubscribeEvent
	public static void onTick(TickEvent.LevelTickEvent event)
	{
		if(event.phase != TickEvent.Phase.START || !event.side.isServer())
			return;
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(server.getTickCount() % 1200 == 0)
			{
				office.universalTraderMap.values().removeIf(traderData -> {
					if(traderData.shouldRemove(server))
					{
						traderData.onRemoved();
						return true;
					}
					return false;
				});
			}
			if(server.getTickCount() % 20 == 0 && office.persistentAuctionData.size() > 0)
			{
				List<UniversalTraderData> traders = office.universalTraderMap.values().stream().collect(Collectors.toList());
				AuctionHouseTrader ah = null;
				for(int i = 0; i < traders.size() && ah == null; ++i)
				{
					if(traders.get(i) instanceof AuctionHouseTrader)
						ah = (AuctionHouseTrader)traders.get(i);
				}
				if(ah != null)
				{
					for(PersistentAuctionData pad : office.persistentAuctionData)
					{
						if(!ah.hasPersistentAuction(pad.id))
						{
							AuctionTradeData trade = pad.createAuction();
							if(trade != null)
							{
								ah.addTrade(trade);
								LightmansCurrency.LogInfo("Successfully added Persistent Auction '" + pad.id + "' into the auction house.");
							}	
						}
					}
				}
			}
		}
	}
	
	public static boolean isAdminPlayer(Player player)
	{
		return adminPlayers.contains(player.getUUID()) && player.hasPermissions(2);
	}
	
	public static void toggleAdminPlayer(Player player)
	{
		UUID playerID = player.getUUID();
		if(adminPlayers.contains(playerID))
		{
			adminPlayers.remove(playerID);
			if(!player.level.isClientSide)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
		else
		{
			adminPlayers.add(playerID);
			if(!player.level.isClientSide)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
	}
	
	public static MessageSyncAdminList getAdminSyncMessage()
	{
		return new MessageSyncAdminList(adminPlayers);
	}
	
	public static void loadAdminPlayers(List<UUID> serverAdminList)
	{
		adminPlayers = serverAdminList;
	}
	
}
