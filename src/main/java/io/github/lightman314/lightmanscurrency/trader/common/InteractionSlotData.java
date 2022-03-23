package io.github.lightman314.lightmanscurrency.trader.common;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class InteractionSlotData {
	
	public final String type;
	protected InteractionSlotData(String type) { this.type = type; }
	public abstract boolean allowItemInSlot(ItemStack item);
	public Pair<ResourceLocation,ResourceLocation> emptySlotBG() { return null; }
	
}
