package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.core.ModItems;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TicketSlot extends Slot{
	
	public static final int EMPTY_SLOT_X = 32;
	public static final int EMPTY_SLOT_Y = 0;
	
	public TicketSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() == ModItems.TICKET;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawEmptyTicketSlots(Screen screen, AbstractContainerMenu container, PoseStack matrix, int startX, int startY)
	{
		//screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
		RenderSystem.setShaderTexture(0, LightmansCurrency.EMPTY_SLOTS);
		for(Slot slot : container.slots)
		{
			if(slot instanceof TicketSlot)
			{
				if(!slot.hasItem())
				{
					//CurrencyMod.LOGGER.info("Drawing empty coin slot at ATM slot index " + this.container.inventorySlots.indexOf(slot));
					screen.blit(matrix, startX + slot.x, startY + slot.y, EMPTY_SLOT_X, EMPTY_SLOT_Y, 16, 16);
				}
			}
		}
	}

}
