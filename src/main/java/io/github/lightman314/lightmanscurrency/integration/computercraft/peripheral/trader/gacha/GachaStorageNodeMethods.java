package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.neoforged.neoforge.items.IItemHandler;

public class GachaStorageNodeMethods extends TraderNodeMethods<GachaStorageNode> {

    public GachaStorageNodeMethods(TraderPeripheral peripheral) { super(peripheral,GachaStorageNode.TYPE); }

    public int getStorageCount() throws LuaException { return this.getNode().getStorage().getItemCount(); }

    public int getStorageCapacity() throws LuaException { return this.getNode().getStorageCapacity(); }

    public Object getStorage(IComputerAccess computer) throws LuaException { return LCPeripheral.wrapInventory(computer,() -> this.hasPermissions(computer, Permissions.OPEN_STORAGE),this::safeGetStorage,() -> {},this.peripheral); }

    private IItemHandler safeGetStorage()
    {
        GachaStorageNode node = this.safeGetNode();
        if(node != null)
            return node.getStorageWrapper();
        return null;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getStorageCount").simple(this::getStorageCount));
        registration.register(LCPeripheralMethod.builder("getStorageCapacity").simple(this::getStorageCapacity));
        registration.register(LCPeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
    }
}
