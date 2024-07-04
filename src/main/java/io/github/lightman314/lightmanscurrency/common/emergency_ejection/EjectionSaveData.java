package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientEjectionData;
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
import net.minecraft.world.entity.player.Player;
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
				EjectionData e = EjectionData.loadData(ejectionData.getCompound(i),lookup);
				if(e != null && !e.isEmpty())
					this.emergencyEjectionData.add(e);
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
	
	public static List<EjectionData> GetValidEjectionData(boolean isClient, Player player)
	{
		List<EjectionData> ejectionData = GetEjectionData(isClient);
		return ejectionData.stream().filter(e -> e.canAccess(player)).collect(Collectors.toList());
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
	
	public static void HandleEjectionData(Level level, BlockPos pos, EjectionData data) {
		if(level.isClientSide)
			return;
		Objects.requireNonNull(data);
		if(data.getContainerSize() == 0)
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
			InventoryUtil.dumpContents(level, pos, data);
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
			//Send update packet to all connected clients
			CompoundTag compound = new CompoundTag();
			ListTag ejectionList = new ListTag();
			esd.emergencyEjectionData.forEach(data -> ejectionList.add(data.save(LookupHelper.getRegistryAccess(false))));
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
