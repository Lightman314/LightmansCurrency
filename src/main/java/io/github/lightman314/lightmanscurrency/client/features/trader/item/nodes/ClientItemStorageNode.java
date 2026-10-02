package io.github.lightman314.lightmanscurrency.client.features.trader.item.nodes;

import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IExternalInteractionDisplayProvider;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;
import net.minecraft.network.chat.Component;

public class ClientItemStorageNode extends ClientTraderNode implements IExternalInteractionDisplayProvider {

    public static final ClientItemStorageNode INSTANCE = new ClientItemStorageNode();
    private ClientItemStorageNode() {}

    @Override
    public Component getExternalInteractionTabName() { return ItemStorageNode.TOOLTIP_INPUT_SETTINGS.get(); }

}
