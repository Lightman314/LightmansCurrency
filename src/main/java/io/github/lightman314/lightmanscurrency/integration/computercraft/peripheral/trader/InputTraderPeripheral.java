package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InputTraderPeripheral<BE extends TraderBlockEntity<T>,T extends InputTraderData> extends TraderPeripheral<BE,T> {

    public InputTraderPeripheral(BE be) { super(be); }
    public InputTraderPeripheral(T trader) { super(trader); }

    public static InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData> createSimpleInput(TraderBlockEntity<InputTraderData> blockEntity) { return new Simple(blockEntity); }
    public static InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData> createSimpleInput(InputTraderData trader) { return new Simple(trader); }

    @Override
    public Set<String> getAdditionalTypes() {
        Set<String> set = new HashSet<>(super.getAdditionalTypes());
        set.add("lc_trader_input");
        return set;
    }

    @LuaFunction(mainThread = true)
    public boolean allowsInputs() throws LuaException { return this.getTrader().allowInputs(); }

    @LuaFunction(mainThread = true)
    public boolean allowInputSide(String sideName) throws LuaException
    {
        Direction side = EnumUtil.enumFromString(sideName,Direction.values(),null);
        if(side == null)
            return false;
        return this.getTrader().allowInputSide(side);
    }

    @LuaFunction(mainThread = true)
    public String[] getInputSides() throws LuaException
    {
        InputTraderData trader = this.getTrader();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(trader.allowInputSide(side))
                sides.add(side.toString());
        }
        return sides.toArray(String[]::new);
    }

    @LuaFunction
    public boolean allowsOutputs() throws LuaException { return this.getTrader().allowOutputs(); }

    @LuaFunction(mainThread = true)
    public boolean allowOutputSide(String sideName) throws LuaException
    {
        Direction side = EnumUtil.enumFromString(sideName,Direction.values(),null);
        if(side == null)
            return false;
        return this.getTrader().allowOutputSide(side);
    }

    @LuaFunction(mainThread = true)
    public String[] getOutputSides() throws LuaException
    {
        InputTraderData trader = this.getTrader();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(trader.allowOutputSide(side))
                sides.add(side.toString());
        }
        return sides.toArray(String[]::new);
    }

    private static final class Simple extends InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData>
    {

        private Simple(TraderBlockEntity<InputTraderData> blockEntity) { super(blockEntity); }
        private Simple(InputTraderData trader) { super(trader); }

        @Nullable
        @Override
        protected IPeripheral wrapTrade(TradeData trade) throws LuaException {
            int index = this.getTrader().indexOfTrade(trade);
            return TradeWrapper.createSimple(() -> {
                TraderData trader = this.safeGetTrader();
                if(trader != null)
                {
                    if(index < 0 || index >= trader.getTradeCount())
                        return null;
                    return trader.getTrade(index);
                }
                return null;
            },this::safeGetTrader);
        }

        @Override
        public String getType() {
            return "";
        }
    }

}
