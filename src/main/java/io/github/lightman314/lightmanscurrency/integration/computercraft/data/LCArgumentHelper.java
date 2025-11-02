package io.github.lightman314.lightmanscurrency.integration.computercraft.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class LCArgumentHelper {

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
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(args.getTable(index));
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
            if(ForgeRegistries.ITEMS.containsKey(itemID))
                return new ItemStack(ForgeRegistries.ITEMS.getValue(itemID));
            throw LuaValues.badArgumentOf(args,index,"item");
        }
        else
        {
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(args.getTable(index));
            ItemStack item = ItemStack.of(tag);
            if(item.isEmpty())
                throw LuaValues.badArgumentOf(args,index,"item");
            return item;
        }
    }

    public static FluidStack parseFluid(IArguments args, int index) throws LuaException
    {
        if(Objects.equals(args.getType(index), "string"))
        {
            ResourceLocation fluidID = parseResourceLocation(args,index);
            if(ForgeRegistries.FLUIDS.containsKey(fluidID))
                return new FluidStack(ForgeRegistries.FLUIDS.getValue(fluidID),FluidType.BUCKET_VOLUME);
            throw LuaValues.badArgumentOf(args,index,"fluid");
        }
        else
        {
            //Try to parse from map
            CompoundTag tag = LCLuaTable.toTag(args.getTable(index));
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
            if(fluid.isEmpty())
                throw LuaValues.badArgumentOf(args,index,"fluid");
            return fluid;
        }
    }

}