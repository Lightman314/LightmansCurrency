package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.network.message.notifications.CPacketOpenNotifications;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class NotificationButton extends InventoryButton {



	private static NotificationButton lastButton = null;

	public static final int SIZE = 9;

	public static final ScreenPosition OFFSET = ScreenPosition.of(10,0);

	public static final Sprite SPRITE_NORMAL = Sprite.SimpleSprite(NotificationScreen.GUI_TEXTURE, 200, 0, SIZE, SIZE);
	public static final Sprite SPRITE_UNSEEN = Sprite.SimpleSprite(NotificationScreen.GUI_TEXTURE, 200 + SIZE, 0, SIZE, SIZE);

	public NotificationButton(AbstractContainerScreen<?> inventoryScreen) {
		super(inventoryScreen, CPacketOpenNotifications::sendToServer, NotificationButton::getSprite);
		lastButton = this;
	}

	@Override
	protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return isCreativeScreen ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get().offset(OFFSET) : LCConfig.CLIENT.notificationAndTeamButtonPosition.get().offset(OFFSET); }

	private static Sprite getSprite() { return ClientNotificationData.GetNotifications().unseenNotification() ? SPRITE_UNSEEN : SPRITE_NORMAL; }

	public static void tryRenderTooltip(EasyGuiGraphics gui) {
		if(lastButton != null && lastButton.isMouseOver(gui.mousePos))
			gui.renderTooltip(LCText.TOOLTIP_NOTIFICATION_BUTTON.get());
	}

}