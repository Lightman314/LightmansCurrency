package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.SPacketSyncEjectionData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class EjectionSaveData extends SavedData {

	private List<EjectionData> emergencyEjectionData = new ArrayList<>();
	
	private EjectionSaveData() {}
	private EjectionSaveData(CompoundTag compound) {
		
		ListTag ejectionData = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
		for(int i = 0; i < ejectionData.size(); ++i)
		{
			try {
				this.emergencyEjectionData.add(EjectionData.loadData(ejectionData.getCompound(i)));
			} catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
		}
		
	}
	
	public CompoundTag save(CompoundTag compound) {
		
		ListTag ejectionData = new ListTag();
		this.emergencyEjectionData.forEach(data -> ejectionData.add(data.save()));
		compound.put("EmergencyEjectionData", ejectionData);
		
		return compound;
	}
	
	private static EjectionSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(EjectionSaveData::new, EjectionSaveData::new, "lightmanscurrency_ejection_data");
		}
		return null;
	}
	
	public static List<EjectionData> GetEjectionData(boolean isClient) {
		if(isClient)
		{
			//TODO get from client
		}
		else
		{
			EjectionSaveData esd = get();
			if(esd != null)
				return new ArrayList<>(esd.emergencyEjectionData);
		}
		return new ArrayList<>();
	}
	
	@Deprecated /** @deprecated Use only to transfer ejection data from the old Trading Office. */
	public static void GiveOldEjectionData(EjectionData data) {
		EjectionSaveData esd = get();
		if(esd != null)
		{
			esd.emergencyEjectionData.add(data);
			MarkEjectionDataDirty();
		}
	}
	
	public static void HandleEjectionData(Level level, BlockPos pos, EjectionData data) {
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
			CompoundTag compound = new CompoundTag();
			ListTag ejectionList = new ListTag();
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
		PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
		EjectionSaveData esd = get();
		
		//Send ejection data
		CompoundTag compound = new CompoundTag();
		ListTag ejectionList = new ListTag();
		esd.emergencyEjectionData.forEach(data -> {
			ejectionList.add(data.save());
		});
		compound.put("EmergencyEjectionData", ejectionList);
		LightmansCurrencyPacketHandler.instance.send(target, new SPacketSyncEjectionData(compound));
	}
	
	
}
