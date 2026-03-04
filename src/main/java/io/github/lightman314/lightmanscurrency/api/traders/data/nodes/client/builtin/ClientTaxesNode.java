package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientInfoTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientSettingsTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.client.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.core.TaxInfoClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.TaxSettingsTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.TaxesNode;

import java.util.List;

public class ClientTaxesNode extends ClientTraderNode<TaxesNode> implements IClientInfoTabProvider, IClientSettingsTabProvider {

    public ClientTaxesNode(TaxesNode node) { super(node); }

    @Override
    public void addInfoTabs(TraderData trader, TraderInfoClientTab tab, List<InfoSubTab> tabs) {
        tabs.add(new TaxInfoClientTab(tab));
    }

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder) {
        builder.add(new TaxSettingsTab(tab));
    }
}
