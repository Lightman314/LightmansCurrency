package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TicketSlot extends Slot {
	
	public static final ResourceLocation EMPTY_TICKET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_ticket_slot");
	
	public TicketSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return InventoryUtil.ItemHasTag(stack, TicketItem.TICKET_TAG);
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return Pair.of(PlayerContainer.BLOCK_ATLAS, EMPTY_TICKET_SLOT); }
	

}
