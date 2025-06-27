package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;
import net.minecraft.core.Direction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PaygateTradeWrapper extends TradeWrapper<PaygateTradeData> {

    public PaygateTradeWrapper(Supplier<PaygateTradeData> tradeSource, Supplier<TraderData> trader) {
        super(tradeSource, trader);
    }

    @Override
    public String getType() { return "lc_trade_paygate"; }

    @Override
    @LuaFunction(mainThread = true)
    public LCLuaTable getPrice() throws LuaException {
        PaygateTradeData trade = this.getTrade();
        if(trade.isTicketTrade())
        {
            LCLuaTable table = new LCLuaTable();
            table.put("TicketID",trade.getTicketID());
            table.put("TicketColor",Integer.toHexString(trade.getTicketColor()));
            table.put("TicketItem", ForgeRegistries.ITEMS.getKey(trade.getTicketItem()).toString());
            return table;
        }
        else
            return super.getPrice();
    }

    @LuaFunction(mainThread = true)
    public boolean storesTicketStubs() throws LuaException { return this.getTrade().shouldStoreTicketStubs(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable getDuration() throws LuaException {
        int duration = this.getTrade().getDuration();
        LCLuaTable table = new LCLuaTable();
        table.put("ticks",duration);
        table.put("text",PaygateTradeData.formatDurationDisplay(duration));
        return table;
    }

    @LuaFunction(mainThread = true)
    public int getRedstoneLevel() throws LuaException { return this.getTrade().getRedstoneLevel(); }

    @LuaFunction(mainThread = true)
    public String getDescription() throws LuaException { return this.getTrade().getDescription(); }

    @LuaFunction(mainThread = true)
    public String[] getTooltip() throws LuaException {
        String tooltip = this.getTrade().getTooltip();
        if(tooltip.isBlank())
            return new String[]{};
        List<String> lines = new ArrayList<>(Arrays.asList(tooltip.split("\\\\n")));
        return lines.toArray(String[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable getOutputSides() throws LuaException {
        PaygateTradeData trade = this.getTrade();
        LCLuaTable table = new LCLuaTable();
        for(Direction side : Direction.values())
            table.put(side.toString(),trade.allowOutputSide(side));
        return table;
    }



}