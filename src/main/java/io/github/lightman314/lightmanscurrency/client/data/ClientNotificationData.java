package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientNotificationData {

	private static NotificationData myNotifications = new NotificationData();
	
	public static NotificationData GetNotifications() { return myNotifications; }
	
	public static void UpdateNotifications(NotificationData data) {
		myNotifications = data;
		myNotifications.flagAsClient();
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof NotificationScreen screen)
			screen.reinit();
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		//Reset notifications
		myNotifications = new NotificationData();
	}
	
}
