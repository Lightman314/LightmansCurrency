package io.github.lightman314.lightmanscurrency.containers.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TicketSlot extends Slot{
	
	public static final ResourceLocation EMPTY_TICKET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_ticket_slot");
	
	public TicketSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().getTags().contains(TicketItem.TICKET_TAG);
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_TICKET_SLOT);
	}
	

}
