package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
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
    protected IPeripheral wrapTrade(TradeData trade) { return this; }

    @Override
    public String getType() { return "lc_trader_slot_machine"; }

    public int getStorageStackLimit() throws LuaException { return this.getTrader().getStorageStackLimit(); }

    public Object getStorage(IComputerAccess computer)
    {
        return wrapInventory(() -> this.hasPermissions(computer, Permissions.OPEN_STORAGE),this::safeGetStorage);
    }

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

    public Object[] getEntries() throws LuaException{
        List<Object> list = new ArrayList<>();
        SlotMachineTraderData trader = this.getTrader();
        List<SlotMachineEntry> entries = trader.getAllEntries();
        for(int i = 0; i < entries.size(); ++i)
            list.add(this.wrapEntry(i));
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
    protected void registerMethods(PeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(PeripheralMethod.builder("getStorageStackLimit").simple(this::getStorageStackLimit));
        registration.register(PeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
        registration.register(PeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(PeripheralMethod.builder("setPrice").withContext(this::setPrice));
        registration.register(PeripheralMethod.builder("getEntries").simpleArray(this::getEntries));
    }
}
