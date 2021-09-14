package io.github.lightman314.lightmanscurrency.containers.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WalletSlot extends Slot{
	
	public static final int EMPTY_SLOT_X = 0;
	public static final int EMPTY_SLOT_Y = 0;
	
	public WalletSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof WalletItem;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawEmptyWalletSlots(Screen screen, Container container, MatrixStack matrix, int startX, int startY)
	{
		screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
		for(Slot slot : container.inventorySlots)
		{
			if(slot instanceof WalletSlot)
			{
				if(!slot.getHasStack())
				{
					screen.blit(matrix, startX + slot.xPos, startY + slot.yPos, EMPTY_SLOT_X, EMPTY_SLOT_Y, 16, 16);
				}
			}
		}
	}

}
