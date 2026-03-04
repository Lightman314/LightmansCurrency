package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.TraderInfoClientTab;

import java.util.ArrayList;
import java.util.List;

public interface IClientInfoTabProvider {

    void addInfoTabs(TraderData trader, TraderInfoClientTab tab, List<InfoSubTab> tabs);

    static List<InfoSubTab> getInfoSubtabs(TraderData trader, TraderInfoClientTab tab)
    {
        //Add built-in tabs
        List<InfoSubTab> tabs = new ArrayList<>();
        //Add tabs from client attachments
        TraderClientHooks.forEach(trader,IClientInfoTabProvider.class,attachment -> attachment.addInfoTabs(trader,tab,tabs));
        return tabs;
    }

}
