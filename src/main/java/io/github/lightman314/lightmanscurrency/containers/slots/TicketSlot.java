package io.github.lightman314.lightmanscurrency.containers.slots;


import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TicketSlot extends Slot{
	
	public static final int EMPTY_SLOT_X = 32;
	public static final int EMPTY_SLOT_Y = 0;
	
	public TicketSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().getTags().contains(new ResourceLocation(LightmansCurrency.MODID, "ticket" ));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawEmptyTicketSlots(Screen screen, Container container, MatrixStack matrix, int startX, int startY)
	{
		screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
		for(Slot slot : container.inventorySlots)
		{
			if(slot instanceof TicketSlot)
			{
				if(!slot.getHasStack())
				{
					//CurrencyMod.LOGGER.info("Drawing empty coin slot at ATM slot index " + this.container.inventorySlots.indexOf(slot));
					screen.blit(matrix, startX + slot.xPos, startY + slot.yPos, EMPTY_SLOT_X, EMPTY_SLOT_Y, 16, 16);
				}
			}
		}
	}

}
