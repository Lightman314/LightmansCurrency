package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEjectionData {

	private static final List<EjectionData> emergencyEjectionData = new ArrayList<>();
	
	public static List<EjectionData> GetEjectionData() { return new ArrayList<>(emergencyEjectionData); }
	
	public static List<EjectionData> GetValidEjectionData() {
		Minecraft mc = Minecraft.getInstance();
		return emergencyEjectionData.stream().filter(e -> e.canAccess(mc.player)).collect(Collectors.toList());
	}
	
	public static void UpdateEjectionData(CompoundTag compound) {
		emergencyEjectionData.clear();
		ListTag ejectionList = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
		for(int i = 0; i < ejectionList.size(); ++i)
		{
			try {
				emergencyEjectionData.add(EjectionData.loadData(ejectionList.getCompound(i)));
			} catch(Throwable t) { t.printStackTrace(); }
		}
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		emergencyEjectionData.clear();
	}
	
}
