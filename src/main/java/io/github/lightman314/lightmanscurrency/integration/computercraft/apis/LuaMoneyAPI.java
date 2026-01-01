package io.github.lightman314.lightmanscurrency.integration.computercraft.apis;

import dan200.computercraft.api.lua.*;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;

public class LuaMoneyAPI implements ILuaAPI {

    private static final ILuaAPI INSTANCE = new LuaMoneyAPI();
    public static final ILuaAPIFactory FACTORY = c -> INSTANCE;
    private LuaMoneyAPI() { }
    @Override
    public String[] getNames() { return new String[] { "lc_money" }; }

    @LuaFunction
    public LCLuaTable addMoney(IArguments args) throws LuaException
    {
        FlexibleMoneyValue val1 = LCArgumentHelper.parseFlexibleMoneyValue(args,0);
        FlexibleMoneyValue val2 = LCArgumentHelper.parseFlexibleMoneyValue(args,1);
        if(!val1.sameType(val2))
            throw new LuaException("Cannot add money of incompatible types!");
        return LCLuaTable.fromMoney(val1.addValue(val2));
    }

    @LuaFunction
    public LCLuaTable subtractMoney(IArguments args) throws LuaException
    {
        FlexibleMoneyValue val1 = LCArgumentHelper.parseFlexibleMoneyValue(args,0);
        FlexibleMoneyValue val2 = LCArgumentHelper.parseFlexibleMoneyValue(args,1);
        if(!val1.sameType(val2))
            throw new LuaException("Cannot add money of incompatible types!");
        return LCLuaTable.fromMoney(val1.subtractValue(val2));
    }

    @LuaFunction
    public LCLuaTable percentageOfMoney(IArguments args) throws LuaException
    {
        FlexibleMoneyValue val = LCArgumentHelper.parseFlexibleMoneyValue(args,0);
        int percentage = args.getInt(1);
        boolean roundUp = args.optBoolean(2,false);
        return LCLuaTable.fromMoney(val.percentageOfValue(percentage,roundUp));
    }

    @LuaFunction
    public LCLuaTable multiplyMoney(IArguments args) throws LuaException
    {
        FlexibleMoneyValue val = LCArgumentHelper.parseFlexibleMoneyValue(args,0);
        double mult = args.getFiniteDouble(1);
        return LCLuaTable.fromMoney(val.multiplyValue(mult));
    }

    @LuaFunction
    public LCLuaTable readInventoryValues(IArguments args) throws LuaException
    {
        Container container = InventoryUtil.buildInventory(LCArgumentHelper.parseBasicItems(args,0));
        IMoneyViewer view = MoneyAPI.getApi().GetContainersMoneyHandler(container,s -> {}, IClientTracker.forServer());
        return LCLuaTable.fromMoney(view);
    }

}