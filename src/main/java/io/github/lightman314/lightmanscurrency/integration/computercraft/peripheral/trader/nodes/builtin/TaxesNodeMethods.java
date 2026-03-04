package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.TaxesNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;

public class TaxesNodeMethods extends TraderNodeMethods<TaxesNode> {

    public TaxesNodeMethods(TraderPeripheral peripheral) { super(peripheral,TaxesNode.TYPE); }

    public int acceptableTaxRate() throws LuaException { return this.getNode().getAcceptableTaxRate(); }
    public boolean setAcceptableTaxRate(IComputerAccess computer, IArguments args) throws LuaException
    {
        int newValue = args.getInt(0);
        ArgumentHelpers.assertBetween(newValue,0,99,"Tax Rate is not in range (%s)");
        if(this.hasPermissions(computer, Permissions.EDIT_SETTINGS))
        {
            TaxesNode node = this.getNode();
            return node.setAcceptableTaxRate(this.getFakePlayer(computer),newValue);
        }
        return false;
    }
    public int currentTaxRate() throws LuaException { return this.getNode().getTotalTaxPercentage(); }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("acceptableTaxRate").simple(this::acceptableTaxRate));
        registration.register(LCPeripheralMethod.builder("setAcceptableTaxRate").withContext(this::setAcceptableTaxRate));
        registration.register(LCPeripheralMethod.builder("currentTaxRate").simple(this::currentTaxRate));
    }
}
