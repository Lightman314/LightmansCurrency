package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
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
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends SavedData{
	
	public TradingOffice() { }
	
	public TradingOffice(CompoundTag tag) { this.load(tag); }
	
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
	
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	private Map<UUID, Team> playerTeams = new HashMap<>();
	private Map<UUID, BankAccount> playerBankAccounts = new HashMap<>();

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
	
	public void load(CompoundTag compound) {
		
		this.universalTraderMap.clear();
		if(compound.contains("UniversalTraders", Tag.TAG_LIST))
		{
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
		this.playerTeams.clear();
		if(compound.contains("Teams", Tag.TAG_LIST))
		{
			ListTag teamList = compound.getList("Teams", Tag.TAG_COMPOUND);
			for(int i = 0; i < teamList.size(); ++i)
			{
				Team team = Team.load(teamList.getCompound(i));
				if(team != null)
					this.playerTeams.put(team.getID(), team);
			}
		}
		this.playerBankAccounts.clear();
		if(compound.contains("BankAccounts", Tag.TAG_LIST))
		{
			ListTag bankAccountList = compound.getList("BankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < bankAccountList.size(); ++i)
			{
				CompoundTag accountCompound = bankAccountList.getCompound(i);
				try {
					UUID owner = accountCompound.getUUID("Player");
					BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(owner), accountCompound);
					if(owner != null && bankAccount != null)
						this.playerBankAccounts.put(owner, bankAccount);
				} catch(Exception e) { e.printStackTrace(); }
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
		
		return compound;
	}
	
	public static UniversalTraderData getData(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.universalTraderMap.containsKey(traderID))
			{
				return office.universalTraderMap.get(traderID);
			}
		}
		return null;
	}
	
	public static List<UniversalTraderData> getTraders()
	{
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		return office.universalTraderMap.values().stream().collect(Collectors.toList());
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
	
	public static UUID registerTrader(UniversalTraderData data, Player owner)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			//Generate a trader ID
			UUID traderID = UUID.randomUUID();
			while(office.universalTraderMap.containsKey(traderID))
				traderID = UUID.randomUUID();
			
			//Apply it to the trader
			data.initTraderID(traderID);
			
			LightmansCurrency.LogInfo("Successfully registered the universal trader with id '" + traderID + "'!");
			office.universalTraderMap.put(traderID, data);
			office.setDirty();
			//Send update packet to the connected clients
			CompoundTag compound = data.write(new CompoundTag());
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
			office.setDirty();
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
			office.playerBankAccounts.put(playerID, newAccount);
			MarkBankAccountDirty(playerID);
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
	
	private static TradingOffice get(MinecraftServer server)
    {
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        return world.getDataStorage().computeIfAbsent((compound) -> new TradingOffice(compound), () -> new TradingOffice(), DATA_NAME);
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
			CompoundTag compound = new CompoundTag();
			ListTag traderList = new ListTag();
			office.universalTraderMap.forEach((id, trader)-> traderList.add(trader.write(new CompoundTag())) );
			compound.put("Traders", traderList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTraders(compound));
			
			CompoundTag compound2 = new CompoundTag();
			ListTag teamList = new ListTag();
			office.playerTeams.forEach((id, team) -> teamList.add(team.save()));
			compound2.put("Teams", teamList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTeams(compound2));
			
			//Confirm the presence of the loading players bank account
			getBankAccount(event.getPlayer());
			
			CompoundTag compound3 = new CompoundTag();
			ListTag bankList = new ListTag();
			office.playerBankAccounts.forEach((id, team) -> {
				CompoundTag tag = team.save();
				tag.putUUID("Player", id);
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
		if(event.phase != TickEvent.Phase.START)
			return;
		
		if(event.side != LogicalSide.SERVER)
			return;
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null && server.getTickCount() % 1200 == 0)
		{
			TradingOffice office = get(server);
			office.universalTraderMap.values().removeIf(traderData ->{
				BlockPos pos = traderData.getPos();
				ServerLevel world = server.getLevel(traderData.getWorld());
				if(world.isLoaded(pos))
				{
					BlockEntity tileEntity = world.getBlockEntity(pos);
					if(tileEntity instanceof UniversalTraderBlockEntity)
					{
						UniversalTraderBlockEntity traderEntity = (UniversalTraderBlockEntity)tileEntity;
						return traderEntity.getTraderID() == null || !traderEntity.getTraderID().equals(traderData.getTraderID());
					}
					return true;
				}
				return false;
			});
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
