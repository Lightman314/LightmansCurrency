package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InteractionSlot extends Slot{
	
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
	public boolean mayPlace(ItemStack stack) {
		return InteractionSlotData.allowItemInSlot(this.slotData, stack);
	}
	
	public List<Pair<ResourceLocation,ResourceLocation>> getPossibleNoItemIcons() {
		List<Pair<ResourceLocation,ResourceLocation>> possibleBGs = new ArrayList<>();
		for(InteractionSlotData slot : this.slotData)
		{
			Pair<ResourceLocation,ResourceLocation> bg = slot.emptySlotBG();
			if(bg != null)
				possibleBGs.add(bg);
		}
		return possibleBGs;
	}
	
	@Override
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		Minecraft mc = Minecraft.getInstance();
		//Use the game time as a timer. Divide by 20 ticks to make the timer change the index once a second.
		int timer = (int)(mc.level.getGameTime() / 20);
		List<Pair<ResourceLocation,ResourceLocation>> bgs = this.getPossibleNoItemIcons();
		return bgs.get(timer % bgs.size());
	}
	
}
