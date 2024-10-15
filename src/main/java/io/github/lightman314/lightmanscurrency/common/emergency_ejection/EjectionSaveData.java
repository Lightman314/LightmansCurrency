package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.SPacketSyncEjectionData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@EventBusSubscriber
public class EjectionSaveData extends SavedData {

	private final List<EjectionData> emergencyEjectionData = new ArrayList<>();
	
	private EjectionSaveData() {}
	private EjectionSaveData(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		ListTag ejectionData = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
		for(int i = 0; i < ejectionData.size(); ++i)
		{
			try {
				EjectionData e = SafeEjectionAPI.getApi().parseData(compound,lookup);
				if(e != null && !e.isEmpty())
					this.emergencyEjectionData.add(e);
				else
					LightmansCurrency.LogWarning("Loaded " + (e == null ? "null" : "empty") + " Ejection Data from file!");
			} catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
		}
		LightmansCurrency.LogDebug("Server loaded " + this.emergencyEjectionData.size() + " ejection data entries from file.");
		
	}
	
	@Nonnull
	public CompoundTag save(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		ListTag ejectionData = new ListTag();
		this.emergencyEjectionData.forEach(data -> ejectionData.add(data.save(lookup)));
		compound.put("EmergencyEjectionData", ejectionData);
		
		return compound;
	}
	
	private static EjectionSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(new Factory<>(EjectionSaveData::new, EjectionSaveData::new), "lightmanscurrency_ejection_data");
		}
		return null;
	}
	
	public static List<EjectionData> GetEjectionData() {
		EjectionSaveData esd = get();
		if(esd != null)
			return new ArrayList<>(esd.emergencyEjectionData);
		return new ArrayList<>();
	}
	
	public static void HandleEjectionData(Level level, BlockPos pos, EjectionData data) {
		if(level.isClientSide)
			return;
		Objects.requireNonNull(data);
		if(data.isEmpty())
			return;
		
		if(LCConfig.SERVER.safelyEjectMachineContents.get() && !LCConfig.SERVER.anarchyMode.get())
		{
			EjectionSaveData esd = get();
			if(esd != null)
			{
				esd.emergencyEjectionData.add(data);
				MarkEjectionDataDirty();
			}
		}
		else
		{
			//Split/dismantle the ejection data in anarchy mode, but leave it as the recoverable item if ejection is simply turned off/disabled
			if(data.canSplit() && LCConfig.SERVER.anarchyMode.get())
				data.splitContents();
			InventoryUtil.dumpContents(level, pos, data.getContents());
		}

		//Push notification to the data's owner(s)
		data.pushNotificationToOwner();
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
			//Remove empty entries
			esd.emergencyEjectionData.removeIf(EjectionData::isEmpty);
			//Send update packet to all connected clients
			CompoundTag compound = new CompoundTag();
			ListTag ejectionList = new ListTag();
			HolderLookup.Provider lookup = LookupHelper.getRegistryAccess(false);
			esd.emergencyEjectionData.forEach(data -> ejectionList.add(data.save(lookup)));
			compound.put("EmergencyEjectionData", ejectionList);
			new SPacketSyncEjectionData(compound).sendToAll();
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		EjectionSaveData esd = get();
		
		//Send ejection data
		CompoundTag compound = new CompoundTag();
		ListTag ejectionList = new ListTag();
		esd.emergencyEjectionData.forEach(data -> ejectionList.add(data.save(LookupHelper.getRegistryAccess(false))));
		compound.put("EmergencyEjectionData", ejectionList);
		new SPacketSyncEjectionData(compound).sendTo(event.getEntity());
	}
	
	
}
