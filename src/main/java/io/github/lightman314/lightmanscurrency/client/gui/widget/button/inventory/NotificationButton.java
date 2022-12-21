package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class NotificationButton extends PlainButton {
	
	private static NotificationButton lastButton = null;
	
	public static final int SIZE = 9;
	
	public static final int OFFSET = 10;
	
	private final AbstractContainerScreen<?> screen;
	
	public NotificationButton(AbstractContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), SIZE, SIZE, button -> LightmansCurrency.PROXY.openNotificationScreen(), NotificationScreen.GUI_TEXTURE, 0, 0);
		this.screen = screen;
		lastButton = this;
	}
	
	private static int getXPosition(AbstractContainerScreen<?> screen) {
		return (screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationAndTeamButtonXCreative.get() : Config.CLIENT.notificationAndTeamButtonX.get()) + OFFSET + screen.getGuiLeft();
	}
	
	private static int getYPosition(AbstractContainerScreen<?> screen) {
		return (screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationAndTeamButtonYCreative.get() : Config.CLIENT.notificationAndTeamButtonY.get()) + screen.getGuiTop();
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.setPosition(getXPosition(this.screen), getYPosition(this.screen));
		
		//Change icon based on whether there's an active notification or not.
		this.setResource(NotificationScreen.GUI_TEXTURE, ClientNotificationData.GetNotifications().unseenNotification() ? 200 + SIZE : 200, 0);
		//Change visibility based on whether the correct tab is open
		if(this.screen instanceof CreativeModeInventoryScreen cs)
			this.visible = cs.isInventoryOpen();
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	public static void tryRenderTooltip(PoseStack pose, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			lastButton.screen.renderTooltip(pose, Component.translatable("tooltip.button.notification"), mouseX, mouseY);
	}
	
}
