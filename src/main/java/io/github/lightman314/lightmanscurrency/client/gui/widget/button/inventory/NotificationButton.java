package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import io.github.lightman314.lightmanscurrency.network.message.notifications.CPacketOpenNotifications;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NotificationButton extends InventoryButton {

    private static NotificationButton lastButton = null;

    public static final int SIZE = 9;

    public static final ScreenPosition OFFSET = ScreenPosition.of(10,0);

    public static final FixedSizeSprite SPRITE_NORMAL = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_notifications"),9,9);
    public static final FixedSizeSprite SPRITE_UNSEEN = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_notifications_unseen"),9,9);

    public NotificationButton(AbstractContainerScreen<?> inventoryScreen) {
        super(inventoryScreen, CPacketOpenNotifications::sendToServer, NotificationButton::getSprite);
        lastButton = this;
    }

    @Override
    protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return isCreativeScreen ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get().offset(OFFSET) : LCConfig.CLIENT.notificationAndTeamButtonPosition.get().offset(OFFSET); }

    private static FixedSizeSprite getSprite() { return NotificationDataCache.TYPE.get(true).getNotifications(Minecraft.getInstance().player).unseenNotification() ? SPRITE_UNSEEN : SPRITE_NORMAL; }

    public static void tryRenderTooltip(EasyGuiGraphics gui) {
        if(lastButton != null && lastButton.isMouseOver(gui.mousePos))
            gui.renderTooltip(LCText.TOOLTIP_NOTIFICATION_BUTTON.get());
    }

}