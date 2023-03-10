package io.github.lightman314.lightmanscurrency.common.menus.slots;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasyMultiBGSlot;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class InteractionSlot extends EasyMultiBGSlot {
	
	public final List<InteractionSlotData> slotData;
	
	public InteractionSlot(List<InteractionSlotData> slotData, int x, int y) {
		super(new Inventory(1), 0, x, y);
		this.slotData = slotData;
	}
	
	public boolean isType(String type) {
		for(InteractionSlotData slot : this.slotData)
		{
			if(slot.type.contentEquals(type))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isActive() { return this.slotData.size() > 0; }
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return InteractionSlotData.allowItemInSlot(this.slotData, stack);
	}

	@Override
	protected List<Pair<ResourceLocation,ResourceLocation>> getPossibleNoItemIcons() {
		List<Pair<ResourceLocation,ResourceLocation>> possibleBGs = new ArrayList<>();
		for(InteractionSlotData slot : this.slotData)
		{
			Pair<ResourceLocation,ResourceLocation> bg = slot.emptySlotBG();
			if(bg != null)
				possibleBGs.add(bg);
		}
		return possibleBGs;
	}
	
}
