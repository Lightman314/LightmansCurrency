package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
    public LCLuaTable getPrice() throws LuaException {
        PaygateTradeData trade = this.getTrade();
        if(trade.isTicketTrade())
        {
            LCLuaTable table = new LCLuaTable();
            table.put("TicketID",trade.getTicketID());
            table.put("TicketColor",Integer.toHexString(trade.getTicketColor()));
            table.put("TicketItem", BuiltInRegistries.ITEM.getKey(trade.getTicketItem()).toString());
            return table;
        }
        else
            return super.getPrice();
    }

    public boolean setTicketPrice(IComputerAccess computer,IArguments args) throws LuaException
    {
        long ticketID = args.getLong(0);
        int color = args.getInt(1);
        Item ticket;
        try {
            Item item = BuiltInRegistries.ITEM.get(VersionUtil.parseResource(args.getString(2)));
            if(item instanceof TicketItem && InventoryUtil.ItemHasTag(new ItemStack(item),LCTags.Items.TICKETS_MASTER))
                ticket = item;
            else
                throw new Exception("Pass to bad argument exception!");
        } catch (Exception exception) {
            throw LuaValues.badArgumentOf(args,2,"master_ticket");
        }
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setTicket(TicketItem.CreateTicket(ticket,ticketID,color));
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public boolean storesTicketStubs() throws LuaException { return this.getTrade().shouldStoreTicketStubs(); }
    public boolean setStoresTicketStubs(IComputerAccess computer,IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.shouldStoreTicketStubs() != newState)
        {
            trade.setStoreTicketStubs(newState);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public LCLuaTable getDuration() throws LuaException {
        int duration = this.getTrade().getDuration();
        LCLuaTable table = new LCLuaTable();
        table.put("ticks",duration);
        table.put("text",PaygateTradeData.formatDurationDisplay(duration));
        return table;
    }
    public boolean setDuration(IComputerAccess computer,IArguments args) throws LuaException
    {
        int newDuration = args.getInt(0);
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            if(trade.getDuration() != newDuration)
            {
                trade.setDuration(newDuration);
                this.markTradeDirty();
                return true;
            }
        }
        return false;
    }

    public int getRedstoneLevel() throws LuaException { return this.getTrade().getRedstoneLevel(); }
    public boolean setRedstoneLevel(IComputerAccess computerAccess,IArguments args) throws LuaException
    {
        int newLevel = args.getInt(0);
        ArgumentHelpers.assertBetween(newLevel,1,15,"Redstone level is not in range (%s)");
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computerAccess))
        {
            if(trade.getRedstoneLevel() != newLevel)
            {
                trade.setRedstoneLevel(newLevel);
                this.markTradeDirty();
                return true;
            }
        }
        return false;
    }

    public String getDescription() throws LuaException { return this.getTrade().getDescription(); }
    public boolean setDescription(IComputerAccess computer,IArguments args) throws LuaException
    {
        String newDescription = args.getString(0);
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            if(!trade.getDescription().equals(newDescription))
            {
                trade.setDescription(newDescription);
                this.markTradeDirty();
                return true;
            }
        }
        return false;
    }

    public String[] getTooltip() throws LuaException {
        String tooltip = this.getTrade().getTooltip();
        if(tooltip.isBlank())
            return new String[]{};
        List<String> lines = new ArrayList<>(Arrays.asList(tooltip.split("\\\\n")));
        return lines.toArray(String[]::new);
    }
    public boolean setTooltip(IComputerAccess computer,IArguments args) throws LuaException
    {
        StringBuilder tooltipBuilder = new StringBuilder();
        for(int i = 0; i < args.count(); ++i)
        {
            if(!tooltipBuilder.isEmpty())
                tooltipBuilder.append("\\n");
            tooltipBuilder.append(args.getString(i));
        }
        String newTooltip = tooltipBuilder.toString();
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            if(!trade.getTooltip().equals(newTooltip))
            {
                trade.setTooltip(newTooltip);
                this.markTradeDirty();
                return true;
            }
        }
        return false;
    }

    public LCLuaTable getOutputSides() throws LuaException {
        PaygateTradeData trade = this.getTrade();
        LCLuaTable table = new LCLuaTable();
        for(Direction side : Direction.values())
            table.put(side.toString(),trade.allowOutputSide(side));
        return table;
    }
    public boolean setOutputSide(IComputerAccess computer,IArguments args) throws LuaException
    {
        Direction side = LCArgumentHelper.parseEnum(args,0,Direction.class);
        boolean newState = args.getBoolean(1);
        PaygateTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.allowOutputSide(side) != newState)
        {
            trade.getOutputSides().setState(side,newState ? DirectionalSettingsState.OUTPUT : DirectionalSettingsState.NONE);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        //Override of getPrice method to allow for
        //registration.register(PeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(LCPeripheralMethod.builder("setTicketPrice").withContext(this::setTicketPrice));
        registration.register(LCPeripheralMethod.builder("storesTicketStubs").simple(this::storesTicketStubs));
        registration.register(LCPeripheralMethod.builder("setStoresTicketStubs").withContext(this::setStoresTicketStubs));
        registration.register(LCPeripheralMethod.builder("getDuration").simple(this::getDuration));
        registration.register(LCPeripheralMethod.builder("setDuration").withContext(this::setDuration));
        registration.register(LCPeripheralMethod.builder("getRedstoneLevel").simple(this::getRedstoneLevel));
        registration.register(LCPeripheralMethod.builder("setRedstoneLevel").withContext(this::setRedstoneLevel));
        registration.register(LCPeripheralMethod.builder("getDescription").simple(this::getDescription));
        registration.register(LCPeripheralMethod.builder("setDuration").withContext(this::setDuration));
        registration.register(LCPeripheralMethod.builder("getTooltip").simpleArray(this::getTooltip));
        registration.register(LCPeripheralMethod.builder("setTooltip").withContext(this::setTooltip));
        registration.register(LCPeripheralMethod.builder("getOutputSides").simple(this::getOutputSides));
        registration.register(LCPeripheralMethod.builder("setOutputSides").withContext(this::setOutputSide));
    }
}
