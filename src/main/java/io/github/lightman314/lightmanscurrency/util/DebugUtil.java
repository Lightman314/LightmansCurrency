package io.github.lightman314.lightmanscurrency.util;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class DebugUtil {

	public static String getItemDebug(ItemStack item) { return item.getCount() + "x " + ForgeRegistries.ITEMS.getKey(item.getItem()); }
	
	public static String getSideText(Entity entity) { return getSideText(entity.level()); }
	public static String getSideText(Level level) { return getSideText(level.isClientSide); }
	public static String getSideText(IClientTracker tracker) { return getSideText(tracker.isClient()); }
	public static String getSideText(boolean isClient) { return isClient ? "client" : "server"; }

}
