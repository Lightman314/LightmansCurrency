package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class MultiTraderPeripheral extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_multi_trader";

    public MultiTraderPeripheral() {}

    @Override
    public Set<String> getAdditionalTypes() { return Set.of(BASE_TYPE); }

    @Nonnull
    protected abstract List<TraderData> getAccessibleTraders();

    protected abstract boolean stillAccessible(TraderData trader);

    @Override
    protected boolean childStillValid(IPeripheral child) {
        if(child instanceof TraderPeripheral<?,?> tp)
        {
            TraderData trader = tp.safeGetTrader();
            if(trader == null)
                return false;
            return this.stillAccessible(trader);
        }
        return true;
    }

    @LuaFunction(mainThread = true)
    public Long[] getTraderIDs() {
        List<Long> list = new ArrayList<>();
        for(TraderData trader : this.getAccessibleTraders())
            list.add(trader.getID());
        return list.toArray(Long[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable getBasicTraderInfo() { return collectTraderInfo(this.getAccessibleTraders()); }

    @LuaFunction(mainThread = true)
    public LCLuaTable searchTraders(String searchText) { return collectTraderInfo(TraderAPI.API.FilterTraders(this.getAccessibleTraders(),searchText)); }

    private static LCLuaTable collectTraderInfo(List<TraderData> traders)
    {
        LCLuaTable table = new LCLuaTable();
        for(TraderData trader : traders)
        {
            LCLuaTable entry = new LCLuaTable();
            entry.put("Name",trader.getName().getString());
            entry.put("Owner",trader.getOwner().getName().getString());
            IPeripheral peripheral = LCComputerHelper.getPeripheral(trader);
            List<String> types = new ArrayList<>();
            types.add(peripheral.getType());
            types.addAll(peripheral.getAdditionalTypes());
            entry.put("Types",types.toArray(String[]::new));
            table.put(trader.getID(),entry);
        }
        return table;
    }

    @LuaFunction(mainThread = true)
    public Object[] getTrader(long traderID) throws LuaException
    {
        TraderData trader = TraderAPI.API.GetTrader(false,traderID);
        if(trader == null || !this.stillAccessible(trader))
            return new Object[] { null, new LuaException("Trader could not be located") };
        IPeripheral result = LCComputerHelper.getPeripheral(trader);
        if(result instanceof AccessTrackingPeripheral at)
            at.setParent(this);
        return new Object[] { result, null };
    }

}
