package io.github.lightman314.lightmanscurrency.integration.computercraft.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.*;

public class LCArgumentHelper {

    public static FlexibleMoneyValue parseFlexibleMoneyValue(IArguments args, int index) throws LuaException
    {
        if(Objects.equals(args.getType(index),"string"))
            return FlexibleMoneyValue.positive(parseMoneyValue(args,index,true));
        else
        {
            //Try to parse from map
            Map<?,?> table = args.getTable(index);
            boolean negative = Objects.equals(table.get("negative"),true);
            MoneyValue val = parseMoneyValue(args,index,true);
            return FlexibleMoneyValue.of(negative,val);
        }
    }

    public static MoneyValue parseMoneyValue(IArguments args,int index, boolean allowEmpty) throws LuaException
    {
        if(Objects.equals(args.getType(index), "string"))
        {
            try {
                //Try parse from money value argument
                return MoneyValueParser.parse(new StringReader(args.getString(index)),allowEmpty);
            } catch (CommandSyntaxException ignored) { throw LuaValues.badArgumentOf(args,index,"money_value"); }
        }
        else
        {
            Map<?,?> table = args.getTable(index);
            //Get the "data" entry from a Money Value result if it's present
            if(table.get("data") instanceof Map<?,?> other)
                table = other;
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(table);
            MoneyValue result = MoneyValue.load(tag);
            if(result == null)
                throw LuaValues.badArgumentOf(args,index,"money_value");
            return result;
        }
    }

    public static <T extends Enum<T>> T parseEnum(IArguments args, int index, Class<T> clazz) throws LuaException { return LuaValues.checkEnum(index,clazz,args.getString(index)); }

    public static ResourceLocation parseResourceLocation(IArguments args, int index) throws LuaException
    {
        try { return VersionUtil.parseResource(args.getString(index));
        } catch (ResourceLocationException e) { throw LuaValues.badArgumentOf(args,index,"id"); }
    }

    public static ItemStack parseItem(IArguments args, int index) throws LuaException
    {
        if(Objects.equals(args.getType(index), "string"))
        {
            ResourceLocation itemID = parseResourceLocation(args,index);
            if(BuiltInRegistries.ITEM.containsKey(itemID))
                return new ItemStack(BuiltInRegistries.ITEM.get(itemID));
            throw LuaValues.badArgumentOf(args,index,"item");
        }
        else
        {
            Map<?,?> table = args.getTable(index);
            if(isBasicItemTable(table))
                return parseBasicItem(args,index,table);
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(args.getTable(index));
            Optional<ItemStack> item = ItemStack.parse(LookupHelper.getRegistryAccess(),tag);
            if(item.isEmpty())
                throw LuaValues.badArgumentOf(args,index,"item");
            return item.get();
        }
    }

    public static FluidStack parseFluid(IArguments args, int index) throws LuaException
    {
        if(Objects.equals(args.getType(index), "string"))
        {
            ResourceLocation fluidID = parseResourceLocation(args,index);
            if(BuiltInRegistries.FLUID.containsKey(fluidID))
                return new FluidStack(BuiltInRegistries.FLUID.get(fluidID),FluidType.BUCKET_VOLUME);
            throw LuaValues.badArgumentOf(args,index,"fluid");
        }
        else
        {
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(args.getTable(index));
            Optional<FluidStack> fluid = FluidStack.parse(LookupHelper.getRegistryAccess(),tag);
            if(fluid.isEmpty())
                throw LuaValues.badArgumentOf(args,index,"fluid");
            return fluid.get();
        }
    }

    public static boolean isBasicItemTable(Map<?,?> table) { return table.containsKey("name") && table.containsKey("count"); }

    public static List<ItemStack> parseBasicItems(IArguments args, int index) throws LuaException
    {
        Map<?,?> table = args.getTable(0);
        List<ItemStack> list = new ArrayList<>();
        if(table.containsKey("name") && table.containsKey("count"))
            list.add(parseBasicItem(args,index,table));
        else for(Object key : table.keySet())
        {
            if(table.get(key) instanceof Map<?,?> entry)
                list.add(parseBasicItem(args,index,entry));
            else
                throw LuaValues.badArgumentOf(args,index,"item");
        }
        return list;
    }

    public static ItemStack parseBasicItem(IArguments args, int index) throws LuaException { return parseBasicItem(args,index,args.getTable(index)); }
    public static ItemStack parseBasicItem(IArguments args, int index, Map<?,?> table) throws LuaException
    {
        try {
            Item item = BuiltInRegistries.ITEM.get(VersionUtil.parseResource((String)table.get("name")));
            int count = ((Number)table.get("count")).intValue();
            ItemStack stack = new ItemStack(item,count);
            LCComputerHelper.modifyItemParsing(stack,table);
            return stack;
        } catch (ClassCastException | NullPointerException | ResourceLocationException e) {
            //LightmansCurrency.LogDebug("Failed parse",e);
            throw LuaValues.badArgumentOf(args,index,"item");
        }
    }

}
