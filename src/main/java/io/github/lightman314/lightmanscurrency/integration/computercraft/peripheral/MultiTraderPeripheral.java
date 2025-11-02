package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MultiTraderPeripheral extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_multi_trader";

    public MultiTraderPeripheral() {}

    @Override
    public Set<String> getAdditionalTypes() { return Set.of(BASE_TYPE); }

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

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        registration.register(PeripheralMethod.builder("getTraderIDs").simpleArray(this::getTraderIDs));
        registration.register(PeripheralMethod.builder("getBasicTraderInfo").simple(this::getBasicTraderInfo));
        registration.register(PeripheralMethod.builder("searchTraders").withArgs(this::searchTraders));
        registration.register(PeripheralMethod.builder("getTrader").withArgsArray(this::getTrader));
    }

    public Long[] getTraderIDs() {
        List<Long> list = new ArrayList<>();
        for(TraderData trader : this.getAccessibleTraders())
            list.add(trader.getID());
        return list.toArray(Long[]::new);
    }

    public LCLuaTable getBasicTraderInfo() { return collectTraderInfo(this.getAccessibleTraders()); }

    public LCLuaTable searchTraders(IArguments args) throws LuaException { return collectTraderInfo(TraderAPI.getApi().FilterTraders(this.getAccessibleTraders(),args.getString(0))); }

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

    public Object[] getTrader(IArguments args) throws LuaException
    {
        TraderData trader = TraderAPI.getApi().GetTrader(false,args.getInt(0));
        if(trader == null || !this.stillAccessible(trader))
            return new Object[] { null, new LuaException("Trader could not be located") };
        IPeripheral result = LCComputerHelper.getPeripheral(trader);
        if(result instanceof AccessTrackingPeripheral at)
            at.setParent(this);
        return new Object[] { result, null };
    }

}