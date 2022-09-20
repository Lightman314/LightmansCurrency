package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientNotificationData {

	private static NotificationData myNotifications = new NotificationData();
	
	public static NotificationData GetNotifications() { return myNotifications; }
	
	public static void UpdateNotifications(NotificationData data) {
		myNotifications = data;
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof NotificationScreen)
			((NotificationScreen)mc.screen).reinit();
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		//Reset notifications
		myNotifications = new NotificationData();
	}
	
}