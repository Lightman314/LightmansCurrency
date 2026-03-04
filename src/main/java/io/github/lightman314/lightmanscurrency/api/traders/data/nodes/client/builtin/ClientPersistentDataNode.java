package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientSettingsTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.client.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.PersistentTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.PersistentDataNode;

public class ClientPersistentDataNode extends ClientTraderNode<PersistentDataNode> implements IClientSettingsTabProvider {

    public ClientPersistentDataNode(PersistentDataNode node) { super(node); }

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder) {
        builder.add(new PersistentTab(tab),-85);
    }
}
