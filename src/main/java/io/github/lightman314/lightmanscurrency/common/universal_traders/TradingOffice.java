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
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.*;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageInitializeClientTraders;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRemoveClientTrader;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateClientData;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
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
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends SavedData{
	
	public TradingOffice() { }
	
	public TradingOffice(CompoundTag tag) { this.load(tag); }
	
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
	
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();

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
	
	//@Override
	public void load(CompoundTag compound) {
		
		universalTraderMap.clear();
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
			if(entry.getOwnerName().toLowerCase().contains(searchText))
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
	
	public static void registerTrader(UUID traderID, UniversalTraderData data, Player owner)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.universalTraderMap.containsKey(traderID))
			{
				LightmansCurrency.LogError("Attempted to register a universal trader with id '" + traderID + "', but one is already present.");
				return;
			}
			LightmansCurrency.LogInfo("Successfully registered the universal trader with id '" + traderID + "'!");
			office.universalTraderMap.put(traderID, data);
			office.setDirty();
			//Send update packet to the connected clients
			CompoundTag compound = data.write(new CompoundTag());
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			//Post Universal Trader Create Event
			MinecraftForge.EVENT_BUS.post(new UniversalTradeCreateEvent(traderID, owner));
		}
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
			TradingOffice office = get(server);
			//Send update message to the connected clients
			CompoundTag compound = new CompoundTag();
			ListTag traderList = new ListTag();
			office.universalTraderMap.forEach((id, trader)-> traderList.add(trader.write(new CompoundTag())) );
			compound.put("Traders", traderList);
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getPlayer()), new MessageInitializeClientTraders(compound));
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
