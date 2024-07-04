package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TicketSlot extends Slot{
	
	public static final ResourceLocation EMPTY_TICKET_SLOT = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "item/empty_ticket_slot");
	
	public TicketSlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) { return InventoryUtil.ItemHasTag(stack, LCTags.Items.TICKETS); }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_TICKET_SLOT); }
	

}
