package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemTraderPeripheral extends InputTraderPeripheral<ItemTraderBlockEntity,ItemTraderData> {

    public ItemTraderPeripheral(ItemTraderBlockEntity blockEntity) { super(blockEntity); }
    public ItemTraderPeripheral(ItemTraderData trader) { super(trader); }

    @Override
    public String getType() { return "lc_trader_item"; }

    @LuaFunction(mainThread = true)
    public int getStorageStackLimit() throws LuaException { return this.getTrader().getStorageStackLimit(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getStorageItems() throws LuaException {
        TraderItemStorage storage = this.getTrader().getStorage();
        List<LCLuaTable> list = new ArrayList<>();
        for(ItemStack item : new ArrayList<>(storage.getContents()))
            list.add(LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(item)));
        return list.toArray(LCLuaTable[]::new);
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

    @LuaFunction(mainThread = true)
    public ItemTradeWrapper[] getTrades() throws LuaException {
        List<ItemTradeWrapper> list = new ArrayList<>();
        ItemTraderData trader = this.getTrader();
        for(int i = 0; i < trader.getTradeCount(); ++i)
            list.add(new ItemTradeWrapper(this.tradeSource(i),this::safeGetTrader));
        return list.toArray(ItemTradeWrapper[]::new);
    }

}