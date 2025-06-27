package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InputTraderPeripheral<BE extends TraderBlockEntity<T>,T extends InputTraderData> extends TraderPeripheral<BE,T> {

    public InputTraderPeripheral(BE be) { super(be); }
    public InputTraderPeripheral(T trader) { super(trader); }

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

}