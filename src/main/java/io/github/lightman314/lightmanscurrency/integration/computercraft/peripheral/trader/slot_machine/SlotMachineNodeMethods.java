package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineDummyTrade;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TradeOfferNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine.entry.SlotMachineEntryWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineNodeMethods extends TradeOfferNodeMethods<SlotMachineDummyTrade,SlotMachineNode> {

    public SlotMachineNodeMethods(TraderPeripheral peripheral) { super(peripheral,SlotMachineNode.TYPE); }

    @Override
    public AccessTrackingPeripheral wrapTrade(TradeData trade) throws LuaException { return this.peripheral; }

    public LCLuaTable getPrice() throws LuaException { return LCLuaTable.fromMoney(this.getNode().getPrice()); }

    public boolean setPrice(IComputerAccess computer, IArguments args) throws LuaException
    {
        MoneyValue newPrice = LCArgumentHelper.parseMoneyValue(args,0,true);
        SlotMachineNode node = this.getNode();
        if(this.hasPermissions(computer, Permissions.EDIT_TRADES))
        {
            node.setPrice(newPrice);
            return true;
        }
        return false;
    }

    public int getEntryCount() throws LuaException { return this.getNode().getAllEntries().size(); }
    public int getValidEntryCount() throws LuaException { return this.getNode().getValidEntries().size(); }

    public Object getEntry(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        SlotMachineNode node = this.getNode();
        ArgumentHelpers.assertBetween(slot,1,node.getAllEntries().size(),"Entry Slot is out of bounds (%s)");
        return this.wrapEntry(slot - 1).asTable(computer);
    }

    public LCLuaTable getEntries(IComputerAccess computer) throws LuaException
    {
        SlotMachineNode node = this.getNode();
        List<Object> results = new ArrayList<>();
        for(int i = 0; i < node.getAllEntries().size(); ++i)
            results.add(this.wrapEntry(i).asTable(computer));
        return LCLuaTable.fromList(results);
    }

    public double getFailOdds() throws LuaException { return this.getNode().getFailOdds(); }

    private SlotMachineEntryWrapper wrapEntry(int index) {
        return new SlotMachineEntryWrapper(this.entrySource(index),this::safeGetTrader);
    }

    private Supplier<SlotMachineEntry> entrySource(int index)
    {
        return () -> {
            SlotMachineNode node = this.safeGetNode();
            if(node != null)
            {
                List<SlotMachineEntry> entry = node.getAllEntries();
                if(index >= 0 && index < entry.size())
                    return entry.get(index);
            }
            return null;
        };
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(LCPeripheralMethod.builder("setPrice").withContext(this::setPrice));
        registration.register(LCPeripheralMethod.builder("getEntryCount").simple(this::getEntryCount));
        registration.register(LCPeripheralMethod.builder("getValidEntryCount").simple(this::getValidEntryCount));
        registration.register(LCPeripheralMethod.builder("getEntry").withContext(this::getEntry));
        registration.register(LCPeripheralMethod.builder("getEntries").withContextOnly(this::getEntries));
        registration.register(LCPeripheralMethod.builder("getFailOdds").simple(this::getFailOdds));
    }

}
