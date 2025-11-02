package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TraderLogClientTab extends InfoSubTab {

    private final boolean settingsView;
    private final Predicate<Notification> filter;
    public TraderLogClientTab(TraderInfoClientTab tab, boolean settingsView) {
        super(tab);
        this.settingsView = settingsView;
        this.filter = this.settingsView ? TraderData.LOGS_SETTINGS_FILTER : TraderData.LOGS_NORMAL_FILTER;
    }

    @Nonnull
    @Override
    public IconData getIcon() { return this.settingsView ? IconUtil.ICON_SETTINGS : IconUtil.ICON_SHOW_LOGGER; }
    @Override
    public MutableComponent getTooltip() { return this.settingsView ? LCText.TOOLTIP_TRADER_LOGS_SETTINGS.get() : LCText.TOOLTIP_TRADER_LOGS.get(); }
    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.VIEW_LOGS) && (!this.settingsView || this.menu.hasPermission(Permissions.EDIT_SETTINGS)); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        NotificationDisplayWidget notificationWidget = this.addChild(NotificationDisplayWidget.builder()
                .position(screenArea.pos.offset(15,10))
                .width(screenArea.width - 30)
                .rowCount(5)
                .notificationSource(this::getNotifications)
                .build());
        notificationWidget.setDeletionHandler(this::DeleteNotification, this::canDeleteNotification);

        this.addChild(ScrollBarWidget.builder()
                .onRight(notificationWidget)
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private List<Notification> getNotifications()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getNotifications(this.filter);
        return new ArrayList<>();
    }

    public boolean canDeleteNotification() { return this.menu.hasPermission(Permissions.TRANSFER_OWNERSHIP); }

    public void DeleteNotification(int notificationIndex)
    {
        this.sendMessage(this.builder().setInt("DeleteNotification",notificationIndex).setBoolean("SettingsView",this.settingsView));
    }

}