package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemStorageNodeMethods extends TraderNodeMethods<ItemStorageNode> {

    public ItemStorageNodeMethods(TraderPeripheral peripheral) { super(peripheral,ItemStorageNode.TYPE); }

    public int getStorageStackLimit() throws LuaException { return this.getNode().getStorageCapacity(); }

    public Object getStorage(IComputerAccess computer) { return LCPeripheral.wrapInventory(computer,() -> this.hasPermissions(computer, Permissions.OPEN_STORAGE),this::safeGetStorage,() -> {},this.peripheral); }

    private IItemHandler safeGetStorage()
    {
        ItemStorageNode node = this.safeGetNode();
        if(node != null)
            return node.getStorage();
        return null;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getStorageStackLimit").simple(this::getStorageStackLimit));
        registration.register(LCPeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
    }

}
