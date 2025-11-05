package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachinePeripheral extends TraderPeripheral<SlotMachineTraderBlockEntity, SlotMachineTraderData> {

    public SlotMachinePeripheral(SlotMachineTraderData trader) {
        super(trader);
    }

    public SlotMachinePeripheral(SlotMachineTraderBlockEntity slotMachineTraderBlockEntity) {
        super(slotMachineTraderBlockEntity);
    }

    @Nullable
    @Override
    protected LCPeripheral wrapTrade(TradeData trade) { return this; }

    @Override
    public String getType() { return "lc_trader_slot_machine"; }

    public int getStorageStackLimit() throws LuaException { return this.getTrader().getStorageStackLimit(); }

    public Object getStorage(IComputerAccess computer) { return wrapInventory(computer,() -> this.hasPermissions(computer, Permissions.OPEN_STORAGE),this::safeGetStorage); }

    private IItemHandler safeGetStorage()
    {
        SlotMachineTraderData trader = this.safeGetTrader();
        if(trader != null)
            return trader.getStorage();
        return null;
    }

    public LCLuaTable getPrice() throws LuaException { return LCLuaTable.fromMoney(this.getTrader().getPrice()); }

    public boolean setPrice(IComputerAccess computer, IArguments args) throws LuaException
    {
        MoneyValue newPrice = LCArgumentHelper.parseMoneyValue(args,0,true);
        SlotMachineTraderData trader = this.getTrader();
        if(this.hasPermissions(computer,Permissions.EDIT_TRADES))
        {
            trader.setPrice(newPrice);
            return true;
        }
        return false;
    }

    public Object[] getEntries(IComputerAccess computer) throws LuaException{
        List<Object> list = new ArrayList<>();
        SlotMachineTraderData trader = this.getTrader();
        List<SlotMachineEntry> entries = trader.getAllEntries();
        for(int i = 0; i < entries.size(); ++i)
            list.add(this.wrapEntry(i).asTable(computer));
        return list.toArray(Object[]::new);
    }

    private SlotMachineEntryWrapper wrapEntry(int index) {
        return new SlotMachineEntryWrapper(this.entrySource(index),this::safeGetTrader);
    }

    private Supplier<SlotMachineEntry> entrySource(int index)
    {
        return () -> {
            SlotMachineTraderData trader = this.safeGetTrader();
            if(trader != null)
            {
                List<SlotMachineEntry> entry = trader.getAllEntries();
                if(index >= 0 && index < entry.size())
                    return entry.get(index);
            }
            return null;
        };
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getStorageStackLimit").simple(this::getStorageStackLimit));
        registration.register(LCPeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
        registration.register(LCPeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(LCPeripheralMethod.builder("setPrice").withContext(this::setPrice));
        registration.register(LCPeripheralMethod.builder("getEntries").withContextOnlyArray(this::getEntries));
    }
}
