package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.*;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends WorldSavedData{
	
	private static final String DATA_NAME = LightmansCurrency.MODID + "_trading_office";
	
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	
	public TradingOffice()
	{
		super(DATA_NAME);
	}
	
	public TradingOffice(String name)
	{
		super(name);
	}

	@Override
	public void read(CompoundNBT compound) {
		
		universalTraderMap.clear();
		if(compound.contains("UniversalTraders", Constants.NBT.TAG_LIST))
		{
			ListNBT universalTraderDataList = compound.getList("UniversalTraders", Constants.NBT.TAG_COMPOUND);
			universalTraderDataList.forEach(nbt ->{
				CompoundNBT traderNBT = (CompoundNBT)nbt;
				UUID traderID = traderNBT.getUniqueId("ID");
				UniversalTraderData data = IUniversalDataDeserializer.Deserialize(traderNBT);
				universalTraderMap.put(traderID, data);
			});
		}
		
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
		return compound;
	}
	
	/*public static UUID getTraderID(UniversalTraderData data)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		AtomicReference<UUID> traderID = new AtomicReference<>();
		TradingOffice office = get(server);
		office.universalTraderMap.forEach((trader, traderData) -> {
			if(traderData == data)
				traderID.set(trader);
		});
		return traderID.get();
	}*/
	
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
	
	public static List<UniversalTraderData> getTraders(ServerPlayerEntity player)
	{
		TradingOffice office = get(player.server);
		return office.universalTraderMap.values().stream().collect(Collectors.toList());
	}
	
	private static void MarkDirty()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			get(server).markDirty();
	}
	
	public static void MarkDirty(UUID traderID)
	{
		MarkDirty();
		UniversalContainer.onDataModified(traderID);
	}
	
	public static void registerTrader(UUID traderID, UniversalTraderData data, PlayerEntity owner)
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
			office.markDirty();
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
			office.markDirty();
			LightmansCurrency.LogInfo("Successfully removed the universal trader with id '" + traderID + "'!");
			MinecraftForge.EVENT_BUS.post(new UniversalTradeRemoveEvent(traderID, removedData));
		}
	}
	
	private static TradingOffice get(MinecraftServer server)
    {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        return world.getSavedData().getOrCreate(TradingOffice::new, DATA_NAME);
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
	
}
