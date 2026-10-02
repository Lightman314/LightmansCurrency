package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.ExternalInteractionsNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public interface IExternalInteractionDisplayProvider {

    default Component getExternalInteractionTabName() { return ExternalInteractionsNode.TOOLTIP_SETTINGS_DEFAULT.get(); }
    default IconData getExternalInteractionTabIcon() { return ItemIcon.of(Items.CHEST); }

    static Component getName(INodeAccess trader) {
        IExternalInteractionDisplayProvider p = ClientTraderNode.getFirstClientNode(trader, IExternalInteractionDisplayProvider.class);
        return p == null ? ExternalInteractionsNode.TOOLTIP_SETTINGS_DEFAULT.get() : p.getExternalInteractionTabName();
    }

    static IconData getIcon(INodeAccess trader) {
        IExternalInteractionDisplayProvider p = ClientTraderNode.getFirstClientNode(trader, IExternalInteractionDisplayProvider.class);
        return p == null ? ItemIcon.of(Items.CHEST) : p.getExternalInteractionTabIcon();
    }

}