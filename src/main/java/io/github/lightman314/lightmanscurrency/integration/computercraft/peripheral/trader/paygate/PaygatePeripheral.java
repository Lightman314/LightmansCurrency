package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaygatePeripheral extends TraderPeripheral<PaygateBlockEntity,PaygateTraderData> {

    public PaygatePeripheral(PaygateBlockEntity paygateBlockEntity) { super(paygateBlockEntity); }

    @Override
    public String getType() { return "lc_trader_paygate"; }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getTicketStubStorage() throws LuaException {
        List<LCLuaTable> list = new ArrayList<>();
        for(ItemStack stub : this.getTrader().getTicketStubStorage())
            list.add(LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(stub)));
        return list.toArray(LCLuaTable[]::new);
    }

    private Supplier<PaygateTradeData> tradeSource(int index)
    {
        return () -> {
            PaygateTraderData trader = this.safeGetTrader();
            if(trader != null && index >= 0 && index < trader.getTradeCount())
                return trader.getTrade(index);
            return null;
        };
    }

    @Nullable
    @Override
    protected IPeripheral wrapTrade(TradeData trade) throws LuaException {
        int index = this.getTrader().indexOfTrade(trade);
        return new PaygateTradeWrapper(this.tradeSource(index),this::safeGetTrader);
    }

    @LuaFunction(mainThread = true)
    public PaygateTradeWrapper[] getTrades() throws LuaException {
        List<PaygateTradeWrapper> list = new ArrayList<>();
        PaygateTraderData trader = this.getTrader();
        for(int i = 0; i < trader.getTradeCount(); ++i)
            list.add(new PaygateTradeWrapper(this.tradeSource(i),this::safeGetTrader));
        return list.toArray(PaygateTradeWrapper[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable getRedstoneState() throws LuaException {
        PaygateBlockEntity be = this.getBlockEntity();
        if(be == null)
            throw new LuaException("An unexpected error occurred attempting to access the Paygate!");
        LCLuaTable table = new LCLuaTable();
        CompoundTag tag = new CompoundTag();
        be.saveRedstoneData(tag);
        Set<Direction> unusedSides = Arrays.stream(Direction.values()).collect(Collectors.toSet());
        for(var data :PaygateBlockEntity.parseVisibilityData(tag))
        {
            LCLuaTable entry = new LCLuaTable();
            if(data.name() != null)
                entry.put("Name",data.name());
            entry.put("Power",data.power());
            entry.put("Timer",data.timer());
            for(Direction side : data.sides())
            {
                table.putTable(side.toString(),entry);
                unusedSides.remove(side);
            }
        }
        for(Direction side : unusedSides)
            table.put(side.toString(),new LCLuaTable());
        return table;
    }

}