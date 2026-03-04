package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;

import java.util.List;

public interface IClientSettingsTabProvider {

    void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder);

    static List<SettingsSubTab> getSettingsTabs(TraderData trader, TraderSettingsClientTab tab)
    {
        //Set up defailt tabs
        SettingsTabBuilder builder = new SettingsTabBuilder();
        //Add tabs from client attachments
        TraderClientHooks.forEach(trader,IClientSettingsTabProvider.class,provider -> provider.addSettingsTabs(trader,tab,builder));
        return builder.getResult();
    }

}
