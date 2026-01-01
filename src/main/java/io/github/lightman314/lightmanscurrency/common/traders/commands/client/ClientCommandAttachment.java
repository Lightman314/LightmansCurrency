package io.github.lightman314.lightmanscurrency.common.traders.commands.client;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.command.CommandSettingsAddon;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;

import java.util.List;

public class ClientCommandAttachment extends ClientTraderAttachment {

    public static ClientTraderAttachment INSTANCE = new ClientCommandAttachment();
    private ClientCommandAttachment() {}

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new CommandSettingsAddon());
    }

}