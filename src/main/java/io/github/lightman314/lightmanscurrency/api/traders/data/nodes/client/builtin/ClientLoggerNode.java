package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.widgets.ToggleOption;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientInfoTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientMiscTabAddonProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.core.TraderLogClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.core.TraderStatsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.LoggerNode;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientLoggerNode extends ClientTraderNode<LoggerNode> implements IClientInfoTabProvider, IClientPermissionProvider, IClientMiscTabAddonProvider {

    public ClientLoggerNode(LoggerNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.VIEW_LOGS);
    }

    @Override
    public void addInfoTabs(TraderData trader, TraderInfoClientTab tab, List<InfoSubTab> tabs) {
        tabs.add(new TraderLogClientTab(tab,false));
        tabs.add(new TraderStatsClientTab(tab));
        tabs.add(new TraderLogClientTab(tab,true));
    }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new LoggerMiscAddon());
    }

    private static class LoggerMiscAddon extends MiscTabAddon
    {

        @Override
        public void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen) {
            this.addWidget(ToggleOption.builder()
                    .width(DEFAULT_WIDTH)
                    .currentValue(this::notificationsEnabled)
                    .handler(this::ToggleNotifications)
                    .label(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_ENABLED)
                    .build());
            this.addWidget(ToggleOption.builder()
                    .width(DEFAULT_WIDTH)
                    .currentValue(this::notificationsToChat)
                    .handler(this::ToggleChatNotifications)
                    .label(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT)
                    .build());

            //Team Notification Level Button
            this.addLabel(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT);
            this.addWidget(EasyTextButton.builder()
                    .width(DEFAULT_WIDTH)
                    .text(this::TeamLevelText)
                    .pressAction(this::ToggleTeamNotificationLevel)
                    .addon(EasyAddonHelper.visibleCheck(this::teamLevelVisible))
                    .build());

        }

        private boolean notificationsEnabled() {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            return node != null && node.getNotificationsEnabled();
        }

        private boolean notificationsToChat() {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            return node != null && node.getNotificationsToChat();
        }

        private boolean teamLevelVisible() {
            TraderData trader = this.getTrader();
            return trader != null && trader.getOwner().getValidOwner().hasNotificationLevels();
        }

        private Component TeamLevelText()
        {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            int level = node == null ? 0 : node.getTeamNotificationLevel();
            return LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_TARGET.get(Owner.getOwnerLevelBlurb(level));
        }

        private void ToggleTeamNotificationLevel() {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            if(node == null)
                return;
            this.sendMessage(this.builder().setInt("TeamNotificationLevel", Team.NextBankLimit(node.getTeamNotificationLevel())));
        }

        private void ToggleNotifications() {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            if(node == null)
                return;
            this.sendMessage(this.builder().setBoolean("Notifications", !node.getNotificationsEnabled()));
        }

        private void ToggleChatNotifications() {
            LoggerNode node = this.getNode(LoggerNode.TYPE);
            if(node == null)
                return;
            this.sendMessage(this.builder().setBoolean("NotificationsToChat", !node.getNotificationsToChat()));
        }

    }

}
