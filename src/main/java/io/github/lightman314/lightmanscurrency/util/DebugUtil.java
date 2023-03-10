package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DebugUtil {

	public static String getItemDebug(ItemStack item)
	{
		return item.getCount() + "x " + item.getItem().getRegistryName();
	}
	
	public static String getSideText(Entity entity)
	{
		return getSideText(entity.level);
	}
	
	public static String getSideText(World level)
	{
		return level.isClientSide ? "client" : "server";
	}
	
}
