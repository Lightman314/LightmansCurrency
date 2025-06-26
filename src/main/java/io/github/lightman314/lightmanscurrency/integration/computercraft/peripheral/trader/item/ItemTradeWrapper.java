package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemTradeWrapper extends TradeWrapper<ItemTradeData> {

    public ItemTradeWrapper(Supplier<ItemTradeData> tradeSource, Supplier<TraderData> trader) { super(tradeSource,trader); }

    @Override
    public String getType() { return "lc_trade_item"; }

    @LuaFunction(mainThread = true)
    public String getRestrictionType() throws LuaException { return ItemTradeRestriction.getId(this.getTrade().getRestriction()).toString(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getSaleItems() throws LuaException {
        List<LCLuaTable> list = new ArrayList<>();
        ItemTradeData trade = this.getTrade();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = trade.getSellItem(i);
            LCLuaTable entry = LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(item,this.registryAccess()));
            if(!item.isEmpty())
            {
                entry.put("enforceNBT",trade.getEnforceNBT(i));
                if(trade.hasCustomName(i))
                    entry.put("CustomName",trade.getCustomName(i));
            }
            list.add(entry);
        }
        return list.toArray(LCLuaTable[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getBarterItems() throws LuaException {
        List<LCLuaTable> list = new ArrayList<>();
        ItemTradeData trade = this.getTrade();
        if(!trade.isBarter())
            return list.toArray(LCLuaTable[]::new);
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = trade.getBarterItem(i);
            LCLuaTable entry = LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(item,this.registryAccess()));
            if(!item.isEmpty())
                entry.put("enforceNBT",trade.getEnforceNBT(i + 2));
            list.add(entry);
        }
        return list.toArray(LCLuaTable[]::new);
    }


}
