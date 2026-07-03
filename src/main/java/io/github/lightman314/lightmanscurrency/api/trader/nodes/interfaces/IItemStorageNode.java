package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface IItemStorageNode {

    ResourceHandler<ItemResource> getStorage();

}
