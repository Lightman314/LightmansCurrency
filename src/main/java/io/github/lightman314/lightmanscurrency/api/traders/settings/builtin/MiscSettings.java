package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class MiscSettings extends EasyTraderSettingsNode<TraderData> {

    public MiscSettings(TraderData trader) { super("misc", trader); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_MISC.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_SETTINGS; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("notificationsEnabled",this.trader.notificationsEnabled());
        data.setBooleanValue("notificationsToChat",this.trader.notificationsToChat());
        data.setIntValue("teamNotificationLevel",this.trader.teamNotificationLevel());
        data.setBooleanValue("alwaysShowSearchBox",this.trader.alwaysShowSearchBox());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.trader.setNotificationsEnabled(data.getBooleanValue("notificationsEnabled"));
        this.trader.setNotificationsToChat(data.getBooleanValue("notificationsToChat"));
        this.trader.setTeamNotificationLevel(data.getIntValue("teamNotificationLevel"));
        this.trader.setAlwaysShowSearchBox(data.getBooleanValue("alwaysShowSearchBox"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_ENABLED.get(),data.getBooleanValue("notificationsEnabled")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_TO_CHAT.get(),data.getBooleanValue("notificationsToChat")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TEAM_NOTIFICATION_LEVEL.get(),data.getIntValue("teamNotificationLevel")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_ALWAYS_SHOW_SEARCH_BOX.get(),data.getBooleanValue("alwaysShowSearchBox")));
    }
}
