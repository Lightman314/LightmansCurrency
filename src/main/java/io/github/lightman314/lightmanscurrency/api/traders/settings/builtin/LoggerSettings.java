package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.LoggerNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class LoggerSettings extends EasyTraderNodeSettings<TraderData,LoggerNode> {

    public LoggerSettings(TraderData trader, LoggerNode node) { super("notifications", trader, node); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_LOGGER.get(); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("notificationsEnabled",this.node.getNotificationsEnabled());
        data.setBooleanValue("notificationsToChat",this.node.getNotificationsToChat());
        data.setIntValue("teamNotificationLevel",this.node.getTeamNotificationLevel());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.node.setNotificationsEnabled(null,data.getBooleanValue("notificationsEnabled"));
        this.node.setNotificationsToChat(null,data.getBooleanValue("notificationsToChat"));
        this.node.setTeamNotificationLevel(null,data.getIntValue("teamNotificationLevel"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_ENABLED.get(),data.getBooleanValue("notificationsEnabled")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_TO_CHAT.get(),data.getBooleanValue("notificationsToChat")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TEAM_NOTIFICATION_LEVEL.get(),data.getIntValue("teamNotificationLevel")));
    }

}
