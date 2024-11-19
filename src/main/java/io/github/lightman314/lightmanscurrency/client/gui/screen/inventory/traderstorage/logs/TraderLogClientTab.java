package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.logs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.logs.TraderLogTab;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TraderLogClientTab extends TraderStorageClientTab<TraderLogTab> {


    public TraderLogClientTab(Object screen, TraderLogTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_LOGS.get(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        NotificationDisplayWidget notificationWidget = this.addChild(NotificationDisplayWidget.builder()
                .position(screenArea.pos.offset(15,10))
                .width(screenArea.width - 30)
                .rowCount(5)
                .notificationSource(this::getNotifications)
                .build());
        notificationWidget.setDeletionHandler(this.commonTab::DeleteNotification, this.commonTab::canDeleteNotification);

        this.addChild(ScrollBarWidget.builder()
                .onRight(notificationWidget)
                .build());

        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    @Override
    public void closeAction() { this.menu.SetCoinSlotsActive(true); }

    private List<Notification> getNotifications()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getNotifications();
        return new ArrayList<>();
    }

}
