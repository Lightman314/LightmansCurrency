package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.menus.slots.easy.EasyMultiBGSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class InteractionSlot extends EasyMultiBGSlot {
	
	public final List<InteractionSlotData> slotData;
	
	public InteractionSlot(List<InteractionSlotData> slotData, int x, int y) {
		super(new SimpleContainer(1), 0, x, y);
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
	public boolean mayPlace(@NotNull ItemStack stack) {
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
