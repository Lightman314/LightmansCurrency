package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientEjectionData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.SPacketSyncEjectionData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class EjectionSaveData extends WorldSavedData {

	private final List<EjectionData> emergencyEjectionData = new ArrayList<>();
	
	private EjectionSaveData() { super("lightmanscurrency_ejection_data"); }
	
	public CompoundNBT save(CompoundNBT compound) {
		
		ListNBT ejectionData = new ListNBT();
		this.emergencyEjectionData.forEach(data -> ejectionData.add(data.save()));
		compound.put("EmergencyEjectionData", ejectionData);
		
		return compound;
	}

	public void load(CompoundNBT compound) {
		ListNBT ejectionData = compound.getList("EmergencyEjectionData", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < ejectionData.size(); ++i)
		{
			try {
				EjectionData e = EjectionData.loadData(ejectionData.getCompound(i));
				if(e != null && !e.isEmpty())
					this.emergencyEjectionData.add(e);
			} catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
		}
		LightmansCurrency.LogDebug("Server loaded " + this.emergencyEjectionData.size() + " ejection data entries from file.");
	}
	
	private static EjectionSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerWorld level = server.overworld();
			if(level != null)
				return level.getDataStorage().computeIfAbsent(EjectionSaveData::new, "lightmanscurrency_ejection_data");
		}
		return null;
	}
	
	public static List<EjectionData> GetEjectionData(boolean isClient) {
		if(isClient)
		{
			return ClientEjectionData.GetEjectionData();
		}
		else
		{
			EjectionSaveData esd = get();
			if(esd != null)
				return new ArrayList<>(esd.emergencyEjectionData);
		}
		return new ArrayList<>();
	}
	
	public static List<EjectionData> GetValidEjectionData(boolean isClient, PlayerEntity player)
	{
		List<EjectionData> ejectionData = GetEjectionData(isClient);
		if(ejectionData != null)
			return ejectionData.stream().filter(e -> e.canAccess(player)).collect(Collectors.toList());
		return new ArrayList<>();
	}

	/** @deprecated Use only to transfer ejection data from the old Trading Office. */
	@Deprecated
	public static void GiveOldEjectionData(EjectionData data) {
		EjectionSaveData esd = get();
		if(esd != null && data != null && !data.isEmpty())
		{
			esd.emergencyEjectionData.add(data);
			MarkEjectionDataDirty();
		}
	}
	
	public static void HandleEjectionData(World level, BlockPos pos, EjectionData data) {
		if(level.isClientSide)
			return;
		Objects.requireNonNull(data);
		
		if(Config.SERVER.safelyEjectIllegalBreaks.get())
		{
			EjectionSaveData esd = get();
			if(esd != null)
			{
				esd.emergencyEjectionData.add(data);
				MarkEjectionDataDirty();
			}
		}
		else
			InventoryUtil.dumpContents(level, pos, data);
	}
	
	public static void RemoveEjectionData(EjectionData data) {
		EjectionSaveData esd = get();
		if(esd != null)
		{
			Objects.requireNonNull(data);
			if(esd.emergencyEjectionData.contains(data))
			{
				esd.emergencyEjectionData.remove(data);
				MarkEjectionDataDirty();
			}
		}
	}
	
	public static void MarkEjectionDataDirty() {
		EjectionSaveData esd = get();
		if(esd != null)
		{
			esd.setDirty();
			//Send update packet to all connected clients
			CompoundNBT compound = new CompoundNBT();
			ListNBT ejectionList = new ListNBT();
			esd.emergencyEjectionData.forEach(data -> {
				ejectionList.add(data.save());
			});
			compound.put("EmergencyEjectionData", ejectionList);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new SPacketSyncEjectionData(compound));
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		PacketDistributor.PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
		EjectionSaveData esd = get();
		
		//Send ejection data
		CompoundNBT compound = new CompoundNBT();
		ListNBT ejectionList = new ListNBT();
		esd.emergencyEjectionData.forEach(data -> {
			ejectionList.add(data.save());
		});
		compound.put("EmergencyEjectionData", ejectionList);
		LightmansCurrencyPacketHandler.instance.send(target, new SPacketSyncEjectionData(compound));
	}
	
	
}