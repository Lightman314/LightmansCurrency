package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.items.WalletItem;

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

public class WalletSlot extends Slot{
	
	public static final int EMPTY_SLOT_X = 0;
	public static final int EMPTY_SLOT_Y = 0;
	
	public WalletSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof WalletItem;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawEmptyWalletSlots(Screen screen, AbstractContainerMenu container, PoseStack matrix, int startX, int startY)
	{
		//screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
		RenderSystem.setShaderTexture(1, LightmansCurrency.EMPTY_SLOTS);
		for(Slot slot : container.slots)
		{
			if(slot instanceof WalletSlot)
			{
				if(!slot.hasItem())
				{
					screen.blit(matrix, startX + slot.x, startY + slot.y, EMPTY_SLOT_X, EMPTY_SLOT_Y, 16, 16);
				}
			}
		}
	}

}
