package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ticket;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemTradeWrapper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class TicketItemTradeWrapper extends ItemTradeWrapper<TicketItemTrade> {

    public TicketItemTradeWrapper(Supplier<ItemTradeData> tradeSource, Supplier<TraderData> trader) {
        super(wrapSource(tradeSource), trader);
    }

    private static Supplier<TicketItemTrade> wrapSource(Supplier<ItemTradeData> tradeSource)
    {
        return () -> {
            if(tradeSource.get() instanceof TicketItemTrade trade)
                return trade;
            return null;
        };
    }

    @Override
    public String getType() { return "lc_trade_item_ticket"; }

    @Override
    public Set<String> getAdditionalTypes() {
        Set<String> set = new HashSet<>(super.getAdditionalTypes());
        set.add("lc_trade_item");
        return set;
    }

    //Ticket Trade Methods
    public String getTicketRecipe(IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        TicketItemTrade trade = this.getTrade();
        return trade.getTicketData(slot - 1).getRecipe().toString();
    }

    public boolean setTicketRecipe(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        ResourceLocation recipe = LCArgumentHelper.parseResourceLocation(args,1);
        TicketItemTrade trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.getTicketData(slot - 1).setRecipe(recipe);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    @Nullable
    public String getTicketCode(IComputerAccess computer,IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        TicketItemTrade trade = this.getTrade();
        if(this.hasPermission(computer))
            return trade.getTicketData(slot - 1).getCode();
        return null;
    }

    public boolean setTicketCode(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        String newCode = args.getString(1);
        TicketItemTrade trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            boolean success = trade.getTicketData(slot - 1).setCode(newCode);
            if(success)
                this.markTradeDirty();
            return success;
        }
        return false;
    }

    public int getTicketDurability(IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        TicketItemTrade trade = this.getTrade();
        return trade.getTicketData(slot - 1).getDurability();
    }

    public boolean setTicketDurability(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot not in range (%s)");
        int newDurability = args.getInt(1);
        TicketItemTrade trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.getTicketData(slot - 1).setDurability(newDurability);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getTicketRecipe").withArgs(this::getTicketRecipe));
        registration.register(LCPeripheralMethod.builder("setTicketRecipe").withContext(this::setTicketRecipe));
        registration.register(LCPeripheralMethod.builder("getTicketCode").withContext(this::getTicketCode));
        registration.register(LCPeripheralMethod.builder("setTicketCode").withContext(this::setTicketCode));
        registration.register(LCPeripheralMethod.builder("getTicketDurability").withArgs(this::getTicketDurability));
        registration.register(LCPeripheralMethod.builder("setTicketDurability").withContext(this::setTicketDurability));
    }
}