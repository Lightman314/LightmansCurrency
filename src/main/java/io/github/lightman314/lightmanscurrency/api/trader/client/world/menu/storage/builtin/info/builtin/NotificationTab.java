package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationFilter;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NotificationNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;

public class NotificationTab extends InfoClientSubTab {

    private static final IconData SETTINGS_ICON = SpriteIcon.of(LCApi.id("icon/settings"));
    private static final IconData LOGGER_ICON = ItemIcon.of(Items.WRITABLE_BOOK);

    private final boolean settingsOnly;
    private final NotificationFilter filter;
    public NotificationTab(InfoClientTab tab,boolean settingsOnly) {
        super(tab);
        this.settingsOnly = settingsOnly;
        this.filter = this.settingsOnly ? NotificationNode.FILTER_SETTINGS : NotificationNode.FILTER_NORMAL;
    }
    @Override
    public IconData getIcon() { return this.settingsOnly ? SETTINGS_ICON : LOGGER_ICON; }
    @Override
    public Component getName() { return this.settingsOnly ? NotificationNode.TOOLTIP_TRADER_LOGS_SETTINGS.get() : NotificationNode.TOOLTIP_TRADER_LOGS.get(); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        this.addChild(NotificationDisplayWidget.builder()
                .atPos(area.pos.offset(15,10))
                .ofWidth(area.width - 300)
                .withRows(5)
                .withNotifications(this::getNotifications)
                .deletionHandler(this::deleteNotification,this::canDeleteNotification)
                .build());
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

    private List<NotificationStack> getNotifications() {
        NotificationNode node = this.getNode(NotificationNode.TYPE);
        if(node != null)
            return node.getNotifications(this.filter);
        return List.of();
    }

    private boolean canDeleteNotification() { return this.getPermission(BuiltInPermissions.VIEW_LOGS).hasHigherPermission(); }

    private void deleteNotification(int index) {
        this.sendSettingRequest(NotificationNode.TYPE,FancyPacketMap.map()
                .setMap("deleteNotification",FancyPacketMap.map()
                        .setInt("index",index)
                        .setBoolean("settingsView",this.settingsOnly)));
    }

}