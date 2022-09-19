package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
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
	
	public static void UpdateEjectionData(CompoundTag compound) {
		emergencyEjectionData.clear();
		ListTag ejectionList = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
		for(int i = 0; i < ejectionList.size(); ++i)
		{
			try {
				EjectionData e = EjectionData.loadData(ejectionList.getCompound(i));
				if(e != null)
				{
					emergencyEjectionData.add(e);
					e.flagAsClient();
				}
				else
					throw new RuntimeException("EmergencyEjectionData entry " + i + " loaded as null.");
			} catch(Throwable t) { t.printStackTrace(); }
		}
		LightmansCurrency.LogDebug("Client loaded " + emergencyEjectionData.size() + " ejection data entries from the server.");
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		emergencyEjectionData.clear();
	}
	
}
