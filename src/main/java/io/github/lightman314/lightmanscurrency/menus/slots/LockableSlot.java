package io.github.lightman314.lightmanscurrency.menus.slots;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LockableSlot extends Slot {

	public static final ResourceLocation EMPTY_LOCKED_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_locked_slot");
	
	private final LockableContainer container;
	
	public LockableSlot(LockableContainer container, int index, int x, int y) {
		super(container.getContainer(), index, x, y);
		this.container = container;
	}
	
	@Override
	public boolean mayPlace(ItemStack item) {
		return this.container.getLockData(this.index).allow(item, this.container.shouldHonorFullLocks());
	}
	
	@Override
	public void set(@Nonnull ItemStack stack) {
		if(this.mayPlace(stack))
			super.set(stack);
	}
	
	@Nullable
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
		if(this.container.getLockData(this.getSlotIndex()).fullyLocked())
			return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_LOCKED_SLOT);
		return super.getNoItemIcon();
	}
	
}
