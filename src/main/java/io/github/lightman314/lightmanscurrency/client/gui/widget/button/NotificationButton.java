package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public class NotificationButton extends PlainButton {
	
	public NotificationButton(AbstractContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), 20, 20, button -> LightmansCurrency.PROXY.openNotificationScreen(), NotificationScreen.GUI_TEXTURE, 0, 0);
	}
	
	private static int getXPosition(AbstractContainerScreen<?> screen) {
		int xPos = screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationButtonCreativeX.get() : Config.CLIENT.notificationButtonX.get();
		return Config.CLIENT.notificationButtonCorner.get().getOffset(screen).getFirst() + xPos;
	}
	
	private static int getYPosition(AbstractContainerScreen<?> screen) {
		int yPos = screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationButtonCreativeY.get() : Config.CLIENT.notificationButtonY.get();
		return Config.CLIENT.notificationButtonCorner.get().getOffset(screen).getSecond() + yPos;
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		//Change icon based on whether there's an active notification or not.
		this.setResource(NotificationScreen.GUI_TEXTURE, ClientTradingOffice.getNotifications().unseenNotification() ? 220 : 200, 0);
		super.renderButton(pose, mouseX, mouseY, partialTicks);
	}
	
}
