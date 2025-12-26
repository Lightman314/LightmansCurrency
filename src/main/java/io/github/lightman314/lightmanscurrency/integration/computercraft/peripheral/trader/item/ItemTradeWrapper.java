package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ItemTradeWrapper<T extends ItemTradeData> extends TradeWrapper<T> {

    public ItemTradeWrapper(Supplier<T> tradeSource, Supplier<TraderData> trader) { super(tradeSource,trader); }

    @Override
    public String getType() { return "lc_trade_item"; }

    public boolean setDirection(IComputerAccess computer,IArguments args) throws LuaException
    {
        TradeDirection direction = LCArgumentHelper.parseEnum(args,0,TradeDirection.class);
        if(direction == TradeDirection.OTHER)
            throw LuaValues.badArgumentOf(args,0,"tradedirection");
        ItemTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.getTradeDirection() != direction)
        {
            trade.setTradeType(direction);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public String getRestrictionType() throws LuaException { return ItemTradeRestriction.getId(this.getTrade().getRestriction()).toString(); }

    public LCLuaTable[] getSellItems() throws LuaException {
        List<LCLuaTable> list = new ArrayList<>();
        ItemTradeData trade = this.getTrade();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = trade.getSellItem(i);
            LCLuaTable entry = new LCLuaTable(VanillaDetailRegistries.ITEM_STACK.getBasicDetails(item));
            if(!item.isEmpty())
                addTradeData(entry,trade,i,trade.getCustomName(i));
            list.add(entry);
        }
        return list.toArray(LCLuaTable[]::new);
    }

    @Nullable
    public LCLuaTable getSellItemDetails(IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot out of range (%s)");
        ItemTradeData trade = this.getTrade();
        ItemStack item = trade.getSellItem(slot - 1);
        LCLuaTable entry = new LCLuaTable(VanillaDetailRegistries.ITEM_STACK.getDetails(item));
        if(!item.isEmpty())
            addTradeData(entry,trade,slot - 1,trade.getCustomName(slot - 1));
        return entry;
    }

    public boolean setSellItem(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot out of range (%s)");
        ItemStack newItem = LCArgumentHelper.paseBasicItem(args,1);
        Optional<Integer> count = args.optInt(2);
        if(count.isPresent())
        {
            int c = count.get();
            ArgumentHelpers.assertBetween(c,1,newItem.getMaxStackSize(),"Item count out of range (%s)");
            newItem.setCount(c);
        }
        ItemTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setItem(newItem,slot - 1);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

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
                addTradeData(entry,trade,i + 2,"");
            list.add(entry);
        }
        return list.toArray(LCLuaTable[]::new);
    }

    public LCLuaTable getBarterItemDetails(IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot out of range (%s)");
        ItemTradeData trade = this.getTrade();
        ItemStack item = trade.getBarterItem(slot - 1);
        LCLuaTable entry = new LCLuaTable(VanillaDetailRegistries.ITEM_STACK.getDetails(item));
        if(!item.isEmpty())
            addTradeData(entry,trade,slot + 1,"");
        return entry;
    }

    public boolean setBarterItem(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,2,"Slot out of range (%s)");
        ItemStack newItem = LCArgumentHelper.paseBasicItem(args,1);
        Optional<Integer> count = args.optInt(2);
        if(count.isPresent())
        {
            int c = count.get();
            ArgumentHelpers.assertBetween(c,1,newItem.getMaxStackSize(),"Item count out of range (%s)");
            newItem.setCount(c);
        }
        ItemTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setItem(newItem,slot + 1);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public boolean setEnforceNBT(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,4,"Slot out of range (%s)");
        boolean newState = args.getBoolean(1);
        ItemTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.getEnforceNBT(slot - 1) != newState)
        {
            trade.setEnforceNBT(slot - 1,newState);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    private static void addTradeData(LCLuaTable table,ItemTradeData trade, int slot, String customName)
    {
        table.put("EnforceNBT",trade.getEnforceNBT(slot));
        if(!customName.isEmpty())
            table.put("CustomName",customName);
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("setDirection").withContext(this::setDirection));
        registration.register(LCPeripheralMethod.builder("getRestrictionType").simple(this::getRestrictionType));
        //Sell Item Methods
        registration.register(LCPeripheralMethod.builder("getSellItems").simpleArray(this::getSellItems));
        registration.register(LCPeripheralMethod.builder("getSellItemDetails").withArgs(this::getSellItemDetails));
        registration.register(LCPeripheralMethod.builder("setSellItem").withContext(this::setSellItem));
        //Barter Item Methods
        registration.register(LCPeripheralMethod.builder("getBarterItems").simpleArray(this::getBarterItems));
        registration.register(LCPeripheralMethod.builder("getBarterItemDetails").withArgs(this::getBarterItemDetails));
        registration.register(LCPeripheralMethod.builder("setBarterItem").withContext(this::setBarterItem));
        //Non Slot Specific
        registration.register(LCPeripheralMethod.builder("setEnforceNBT").withContext(this::setEnforceNBT));
    }
}
