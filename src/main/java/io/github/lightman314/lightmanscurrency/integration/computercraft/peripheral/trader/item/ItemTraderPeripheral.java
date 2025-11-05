package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ticket.TicketItemTradeWrapper;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemTraderPeripheral extends InputTraderPeripheral<ItemTraderBlockEntity,ItemTraderData> {

    public ItemTraderPeripheral(ItemTraderBlockEntity blockEntity) { super(blockEntity); }
    public ItemTraderPeripheral(ItemTraderData trader) { super(trader); }

    @Override
    public String getType() { return "lc_trader_item"; }

    public int getStorageStackLimit() throws LuaException { return this.getTrader().getStorageStackLimit(); }

    public Object getStorage(IComputerAccess computer) { return wrapInventory(computer,() -> this.hasPermissions(computer,Permissions.OPEN_STORAGE),this::safeGetStorage); }

    private IItemHandler safeGetStorage()
    {
        ItemTraderData trader = this.safeGetTrader();
        if(trader != null)
            return trader.getStorage();
        return null;
    }

    private Supplier<ItemTradeData> tradeSource(int index)
    {
        return () -> {
            ItemTraderData trader = this.safeGetTrader();
            if(trader != null && index >= 0 && index < trader.getTradeCount())
                return trader.getTrade(index);
            return null;
        };
    }

    @Nullable
    @Override
    protected LCPeripheral wrapTrade(TradeData trade) throws LuaException {
        int index = this.getTrader().indexOfTrade(trade);
        if(trade instanceof TicketItemTrade)
            return new TicketItemTradeWrapper(this.tradeSource(index),this::safeGetTrader);
        return new ItemTradeWrapper<>(this.tradeSource(index),this::safeGetTrader);
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getStorageStackLimit").simple(this::getStorageStackLimit));
        registration.register(LCPeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
        this.registerGetTrade(registration);
    }

}
