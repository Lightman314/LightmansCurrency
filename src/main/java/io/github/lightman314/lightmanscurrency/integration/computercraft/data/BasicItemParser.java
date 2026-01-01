package io.github.lightman314.lightmanscurrency.integration.computercraft.data;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface BasicItemParser {

    void modifyResult(ItemStack stack, Map<?,?> table) throws LuaException;

}