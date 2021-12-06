package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugUtil {

	public static String getItemDebug(ItemStack item)
	{
		return item.getCount() + "x " + item.getItem().getRegistryName();
	}
	
	public static String getSideText(Entity entity)
	{
		return getSideText(entity.level);
	}
	
	public static String getSideText(Level level)
	{
		return level.isClientSide ? "client" : "server";
	}
	
}
