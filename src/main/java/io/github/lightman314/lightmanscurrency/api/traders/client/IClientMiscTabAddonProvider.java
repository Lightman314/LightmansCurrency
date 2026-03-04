package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;

import java.util.ArrayList;
import java.util.List;

public interface IClientMiscTabAddonProvider {

    void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons);

    static List<MiscTabAddon> getMiscTabAddons(TraderData trader)
    {
        //Add addons from deprecated methods
        List<MiscTabAddon> addons = new ArrayList<>();
        //Add tabs from client attachments
        TraderClientHooks.forEach(trader, IClientMiscTabAddonProvider.class,attachment -> attachment.addMiscTabAddons(trader,addons));
        return addons;
    }

}
