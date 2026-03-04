package io.github.lightman314.lightmanscurrency.common.traders.item.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTradeOfferNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.input.IInputClientNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class ClientItemTradeNode extends ClientTradeOfferNode<ItemTradeNode> implements IInputClientNode {

    public ClientItemTradeNode(ItemTradeNode node) { super(node); }

    @Override
    public IconData getSettingsTabIcon(TraderData trader) { return ItemIcon.ofItem(Items.HOPPER); }
    @Override
    public Component getSettingsTabTooltip(TraderData trader) { return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get(); }

}
