package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends SavedData{
	
	private static final String DATA_NAME = LightmansCurrency.MODID + "_trading_office";
	
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	
	public TradingOffice()
	{
		//super(DATA_NAME);
	}
	
	public TradingOffice(CompoundTag compound)
	{
		this.load(compound);
	}

	//@Override //No longer an override, as it's loaded via function
	public void load(CompoundTag compound) {
		
		universalTraderMap.clear();
		if(compound.contains("UniversalTraders", Constants.NBT.TAG_LIST))
		{
			ListTag universalTraderDataList = compound.getList("UniversalTraders", Constants.NBT.TAG_COMPOUND);
			universalTraderDataList.forEach(nbt ->{
				CompoundTag traderNBT = (CompoundTag)nbt;
				UUID traderID = traderNBT.getUUID("ID");
				UniversalTraderData data = IUniversalDataDeserializer.Deserialize(traderNBT);
				universalTraderMap.put(traderID, data);
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
	
	public static List<UniversalTraderData> getTraders(ServerPlayer player)
	{
		TradingOffice office = get(player.server);
		return office.universalTraderMap.values().stream().collect(Collectors.toList());
	}
	
	private static void MarkDirty()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			get(server).setDirty();
	}
	
	public static void MarkDirty(UUID traderID)
	{
		MarkDirty();
		UniversalContainer.onDataModified(traderID);
	}
	
	public static void registerTrader(UUID traderID, UniversalTraderData data)
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
		}
	}
	
	public static void removeTrader(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office.universalTraderMap.containsKey(traderID))
		{
			office.universalTraderMap.remove(traderID);
			office.setDirty();
			LightmansCurrency.LogInfo("Successfully removed the universal trader with id '" + traderID + "'!");
		}
	}
	
	//Need to determine how to add to the save file at this point.
	private static TradingOffice get(MinecraftServer server)
    {
        ServerLevel world = server.overworld();
        return world.getDataStorage().computeIfAbsent(deserializer, () -> new TradingOffice(), DATA_NAME);
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
				ServerLevel level = server.getLevel(traderData.getWorld());
				if(level != null && level.isAreaLoaded(pos, 0))
				{
					BlockEntity blockEntity = level.getBlockEntity(pos);
					if(blockEntity instanceof UniversalTraderBlockEntity)
					{
						UniversalTraderBlockEntity traderEntity = (UniversalTraderBlockEntity)blockEntity;
						return traderEntity.getTraderID() == null || !traderEntity.getTraderID().equals(traderData.getTraderID());
					}
					return true;
				}
				return false;
			});
		}
	}
	
	private static final Deserializer deserializer = new Deserializer();
	
	private static class Deserializer implements Function<CompoundTag,TradingOffice>
	{
		@Override
		public TradingOffice apply(CompoundTag input) {
			return new TradingOffice(input);
		}
	}
	
}
