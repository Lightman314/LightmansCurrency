package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.*;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageInitializeClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageUpdateClientBank;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageInitializeClientTeams;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageInitializeClientTraders;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRemoveClientTrader;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateClientData;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends WorldSavedData{
	
	public static final String PERSISTENT_TRADER_FILENAME = "config/lightmanscurrency/persistentTraders.json";
	
	private static final Map<ResourceLocation,Supplier<? extends UniversalTraderData>> registeredDeserializers = Maps.newHashMap();
	
	public static final void RegisterDataType(ResourceLocation key, Supplier<? extends UniversalTraderData> source)
	{
		if(registeredDeserializers.containsKey(key))
		{
			LightmansCurrency.LogError("A universal trader type of key " + key + " has already been registered.");
			return;
		}
		else
			registeredDeserializers.put(key, source);
	}
	
	private static final String DATA_NAME = LightmansCurrency.MODID + "_trading_office";
	
	private static List<UUID> adminPlayers = new ArrayList<>();
	
	private Map<UUID, UniversalTraderData> persistentTraderMap = new HashMap<>();
	private Map<UUID, String> persistentTraderIDs = new HashMap<>();
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	private Map<UUID, Team> playerTeams = new HashMap<>();
	private Map<UUID, BankAccount> playerBankAccounts = new HashMap<>();
	//Store persistent data locally, so that it doesn't get lost if the persistent trader file is malformed
	ListNBT persistentData = new ListNBT();
	
	public TradingOffice()
	{
		super(DATA_NAME);
	}

	@SuppressWarnings("deprecation")
	public static UniversalTraderData Deserialize(CompoundNBT compound)
	{
		ResourceLocation thisType = new ResourceLocation(compound.getString("type"));
		//New method
		if(registeredDeserializers.containsKey(thisType))
		{
			UniversalTraderData data = registeredDeserializers.get(thisType).get();
			data.read(compound);
			return data;
		}
		//Fall back onto the old method to allow older addon mods
		return IUniversalDataDeserializer.ClassicDeserialize(compound);
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
	
	@Override
	public void read(CompoundNBT compound) {
		
		if(compound.contains("UniversalTraders", Constants.NBT.TAG_LIST))
		{
			this.universalTraderMap.clear();
			ListNBT universalTraderDataList = compound.getList("UniversalTraders", Constants.NBT.TAG_COMPOUND);
			universalTraderDataList.forEach(nbt ->{
				CompoundNBT traderNBT = (CompoundNBT)nbt;
				//UUID traderID = traderNBT.getUniqueId("ID");
				//UniversalTraderData data = IUniversalDataDeserializer.Deserialize(traderNBT);
				UniversalTraderData data = Deserialize(traderNBT);
				if(data != null)
					universalTraderMap.put(data.getTraderID(), data);
			});
		}
		
		if(compound.contains("Teams", Constants.NBT.TAG_LIST))
		{
			this.playerTeams.clear();
			ListNBT teamList = compound.getList("Teams", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < teamList.size(); ++i)
			{
				Team team = Team.load(teamList.getCompound(i));
				if(team != null)
					this.playerTeams.put(team.getID(), team);
			}
		}
		
		if(compound.contains("BankAccounts", Constants.NBT.TAG_LIST))
		{
			this.playerBankAccounts.clear();
			ListNBT bankAccountList = compound.getList("BankAccounts", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < bankAccountList.size(); ++i)
			{
				CompoundNBT accountCompound = bankAccountList.getCompound(i);
				try {
					UUID owner = accountCompound.getUniqueId("Player");
					BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(owner), accountCompound);
					if(owner != null && bankAccount != null)
						this.playerBankAccounts.put(owner, bankAccount);
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		if(compound.contains("PersistentTraderData", Constants.NBT.TAG_LIST))
			this.persistentData = compound.getList("PersistentTraderData", Constants.NBT.TAG_COMPOUND);
		
		if(compound.contains("PersistentTraderIDs", Constants.NBT.TAG_LIST))
		{
			this.persistentTraderIDs.clear();
			ListNBT persistentIDs = compound.getList("PersistentTraderIDs", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < persistentIDs.size(); ++i)
			{
				try {
					CompoundNBT idData = persistentIDs.getCompound(i);
					UUID uuid = idData.getUniqueId("UUID");
					String traderID = idData.getString("TraderID");
					if(uuid != null && traderID != null)
						this.persistentTraderIDs.put(uuid, traderID);
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		this.loadPersistentTraders();
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT universalTraderDataList = new ListNBT();
		this.universalTraderMap.forEach((traderID, traderData) ->
		{
			if(traderData != null)
			{
				CompoundNBT traderNBT = traderData.write(new CompoundNBT());
				traderNBT.putUniqueId("ID", traderID);
				universalTraderDataList.add(traderNBT);
			}
		});
		compound.put("UniversalTraders", universalTraderDataList);
		
		ListNBT teamList = new ListNBT();
		this.playerTeams.forEach((teamID, team) ->{
			if(team != null)
				teamList.add(team.save());
		});
		compound.put("Teams", teamList);
		
		ListNBT bankAccountList = new ListNBT();
		this.playerBankAccounts.forEach((playerID, account) ->{
			CompoundNBT accountNBT = account.save();
			accountNBT.putUniqueId("Player", playerID);
			bankAccountList.add(accountNBT);
		});
		compound.put("BankAccounts", bankAccountList);
		
		this.persistentTraderMap.forEach((ID, traderData) ->{
			if(traderData != null && this.persistentTraderIDs.containsKey(ID))
			{
				CompoundNBT data = traderData.getPersistentData();
				String traderID = this.persistentTraderIDs.get(ID);
				data.putString("traderID", traderID);
				this.setPersistentData(traderID, data);
			}
		});
		compound.put("PersistentTraderData", this.persistentData);

		ListNBT persistentTraderIDs = new ListNBT();
		this.persistentTraderIDs.forEach((uuid, traderID) ->{
			CompoundNBT idData = new CompoundNBT();
			idData.putUniqueId("UUID", uuid);
			idData.putString("TraderID", traderID);
		});
		compound.put("PersistentTraderIDs", persistentTraderIDs);
		
		
		return compound;
	}
	
	public static void reloadPersistentTraders() {
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		office.resendTraderData();
	}

	private void loadPersistentTraders() {
		LightmansCurrency.LogInfo("Begining to load persistent traders.");
		//Get JSON file
		File ptf = new File(PERSISTENT_TRADER_FILENAME);
		if(!ptf.exists())
		{
			this.createPersistentTraderFile(ptf);
		}
		try { 
			JsonObject fileData = JSONUtils.fromJson(Files.toString(ptf, StandardCharsets.UTF_8));
			this.loadPersistentTraders(fileData);
		} catch(Throwable e) {
			LightmansCurrency.LogError("Error loading Persistent Traders.", e);
		}
		LightmansCurrency.LogInfo("Finished loading persistent traders.");
	}

	private void loadPersistentTraders(JsonObject fileData) throws Exception {
		if(fileData.has("Traders"))
		{
			this.persistentTraderMap.clear();
			List<String> loadedIDs = new ArrayList<>();
			JsonArray traderList = fileData.get("Traders").getAsJsonArray();
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
						this.markDirty();
					}
					data.initTraderID(id);

					this.persistentTraderMap.put(id, data);
					loadedIDs.add(traderID);
					LightmansCurrency.LogInfo("Successfully loaded persistent trader '" + traderID + "' with UUID " + id.toString() + ".");

				} catch(Throwable e) { LightmansCurrency.LogError("Error loading Persistent Trader at index " + i, e); }
			}
		}
		else
			throw new Exception("Json Data has no 'Traders' entry.");
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

	private CompoundNBT getPersistentData(String traderID) {
		for(int i = 0; i < this.persistentData.size(); ++i) {
			CompoundNBT thisData = this.persistentData.getCompound(i);
			if(thisData.getString("traderID").contentEquals(traderID))
				return thisData;
		}
		return new CompoundNBT();
	}
	
	private void setPersistentData(String traderID, CompoundNBT data) {
		for(int i = 0; i < this.persistentData.size(); ++i) {
			CompoundNBT thisData = this.persistentData.getCompound(i);
			if(thisData.getString("traderID").contentEquals(traderID))
			{
				this.persistentData.set(i, data);
				return;
			}
		}
		this.persistentData.add(data);
	}

	private void createPersistentTraderFile(File ptf) {
		File dir = new File(ptf.getParent());
		if(!dir.exists())
			dir.mkdirs();
		if(dir.exists())
		{
			try {

				ptf.createNewFile();

				FileUtil.writeStringToFile(ptf, "{\n    \"Traders\":[]\n}");

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
			if(entry.getCoreSettings().getOwner().lastKnownName().toLowerCase().contains(searchText))
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
			get(server).markDirty();
			//Send update packet to all connected clients
			UniversalTraderData data = getData(traderID);
			if(data != null)
			{
				CompoundNBT compound = data.write(new CompoundNBT());
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			}
		}
	}
	
	public static void MarkDirty(UUID traderID, CompoundNBT updateMessage)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).markDirty();
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
			get(server).markDirty();
			//Send update packet to all connected clients
			Team team = getTeam(teamID);
			if(team != null)
			{
				CompoundNBT compound = team.save();
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
	
	public static UUID registerTrader(UniversalTraderData data, PlayerEntity owner)
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
			office.markDirty();
			//Send update packet to the connected clients
			CompoundNBT compound = data.write(new CompoundNBT());
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			//Post Universal Trader Create Event
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
			office.markDirty();
			LightmansCurrency.LogInfo("Successfully removed the universal trader with id '" + traderID + "'!");
			//Send update packet to the connected clients
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTrader(traderID));
			MinecraftForge.EVENT_BUS.post(new UniversalTradeRemoveEvent(traderID, removedData));
		}
	}
	
	public static Team registerTeam(PlayerEntity owner, String teamName)
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
				office.markDirty();
				//Send update packet to the connected clients
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTeam(teamID));
			}
		}
	}
	
	/**
	 * Gets the bank account for the given player.
	 * If no account exists for that player, a new one will be created and synced to clients.
	 */
	public static BankAccount getBankAccount(PlayerEntity player) {
		return getBankAccount(player.getUniqueID());
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
			//Create a new bank account for the player
			BankAccount newAccount = new BankAccount(() -> MarkBankAccountDirty(playerID));
			office.playerBankAccounts.put(playerID, newAccount);
			office.markDirty();
			MarkBankAccountDirty(playerID);
		}
		return null;
	}
	
	public static void MarkBankAccountDirty(PlayerEntity player) { if(player.world.isRemote) return; MarkBankAccountDirty(player.getUniqueID()); }
	
	public static void MarkBankAccountDirty(UUID playerID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).markDirty();
			//Send update packet to all connected clients
			BankAccount bankAccount = getBankAccount(playerID);
			CompoundNBT compound = bankAccount.save();
			compound.putUniqueId("Player", playerID);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientBank(compound));
			
		}
	}
	
	//Modified to load persistent traders on intial creation
	private static TradingOffice get(MinecraftServer server)
    {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        TradingOffice office = world.getSavedData().get(TradingOffice::new, DATA_NAME);
        if(office == null)
        {
        	office = world.getSavedData().getOrCreate(TradingOffice::new, DATA_NAME);
        	office.loadPersistentTraders();
        }
        return office;
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
			PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
			TradingOffice office = get(server);
			//Send update message to the connected clients
			CompoundNBT compound = new CompoundNBT();
			ListNBT traderList = new ListNBT();
			office.universalTraderMap.forEach((id, trader)-> traderList.add(trader.write(new CompoundNBT())) );
			office.persistentTraderMap.forEach((id, trader) -> traderList.add(trader.write(new CompoundNBT())) );
			compound.put("Traders", traderList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTraders(compound));
			
			CompoundNBT compound2 = new CompoundNBT();
			ListNBT teamList = new ListNBT();
			office.playerTeams.forEach((id,team) -> teamList.add(team.save()));
			compound2.put("Teams", teamList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTeams(compound2));
			
			//Confirm the presence of the loading players bank account
			getBankAccount(event.getPlayer());
			
			CompoundNBT compound3 = new CompoundNBT();
			ListNBT bankList = new ListNBT();
			office.playerBankAccounts.forEach((id,team) ->{
				CompoundNBT tag = team.save();
				tag.putUniqueId("Player", id);
				bankList.add(tag);
			});
			compound3.put("BankAccounts", bankList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientBank(compound3));
		}
	}
	
	/**
	 * Clean up invalid traders
	 */
	@SubscribeEvent
	public static void onTick(TickEvent.WorldTickEvent event)
	{
		
		if(event.side != LogicalSide.SERVER)
			return;
		
		if(event.phase != TickEvent.Phase.START)
			return;
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null && server.getTickCounter() % 1200 == 0)
		{
			TradingOffice office = get(server);
			office.universalTraderMap.values().removeIf(traderData ->{
				BlockPos pos = traderData.getPos();
				ServerWorld world = server.getWorld(traderData.getWorld());
				if(world.isAreaLoaded(pos, 0))
				{
					TileEntity tileEntity = world.getTileEntity(pos);
					if(tileEntity instanceof UniversalTraderTileEntity)
					{
						UniversalTraderTileEntity traderEntity = (UniversalTraderTileEntity)tileEntity;
						return traderEntity.getTraderID() == null || !traderEntity.getTraderID().equals(traderData.getTraderID());
					}
					return true;
				}
				return false;
			});
		}
		
	}
	
	private void resendTraderData() {
		CompoundNBT compound = new CompoundNBT();
		ListNBT traderList = new ListNBT();
		this.universalTraderMap.forEach((id, trader)-> traderList.add(trader.write(new CompoundNBT())) );
		this.persistentTraderMap.forEach((id, trader) -> traderList.add(trader.write(new CompoundNBT())) );
		compound.put("Traders", traderList);
		LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageInitializeClientTraders(compound));
	}
	
	public static boolean isAdminPlayer(PlayerEntity player)
	{
		return adminPlayers.contains(player.getUniqueID()) && player.hasPermissionLevel(2);
	}
	
	public static void toggleAdminPlayer(PlayerEntity player)
	{
		UUID playerID = player.getUniqueID();
		if(adminPlayers.contains(playerID))
		{
			adminPlayers.remove(playerID);
			if(!player.world.isRemote)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
		else
		{
			adminPlayers.add(playerID);
			if(!player.world.isRemote)
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
