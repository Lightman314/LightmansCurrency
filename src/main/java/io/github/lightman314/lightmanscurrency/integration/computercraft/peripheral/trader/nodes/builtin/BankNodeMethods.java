package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.BankNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;

import javax.annotation.Nullable;

public class BankNodeMethods extends TraderNodeMethods<BankNode> {

    public BankNodeMethods(TraderPeripheral peripheral) { super(peripheral,BankNode.TYPE); }

    public boolean hasBankAccount() throws LuaException { return this.getNode().hasBankAccount(); }

    @Nullable
    public Object getLinkedAccount() throws LuaException {
        if(!this.hasBankAccount())
            return null;
        BankNode node = this.getNode();
        BankReference br = node.getBankReference();
        return LCLuaTable.fromCustom(br,BankReference.CODEC,this.dataContext());
    }

    public boolean setLinkedToBankAccount(IComputerAccess computer, IArguments args) throws LuaException {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer, Permissions.BANK_LINK))
        {
            BankNode node = this.getNode();
            boolean linkedToBankAccount = node.isLinkedToBank();
            if(linkedToBankAccount != newState)
            {
                //Confirm that we're *allowed* to link the bank account
                if(newState && !node.canLinkBankAccount())
                    return false;
                return node.setLinkedToBank(this.getFakePlayer(computer),newState);
            }
        }
        return false;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("hasBankAccount").simple(this::hasBankAccount));
        registration.register(LCPeripheralMethod.builder("getLinkedAccount").simple(this::getLinkedAccount));
        registration.register(LCPeripheralMethod.builder("setLinkedToBankAccount").withContext(this::setLinkedToBankAccount));
    }
}
