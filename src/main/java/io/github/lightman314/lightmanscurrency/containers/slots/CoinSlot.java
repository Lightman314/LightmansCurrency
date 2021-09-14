package io.github.lightman314.lightmanscurrency.containers.slots;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
//import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoinSlot extends Slot{
	
	public static final int EMPTY_SLOT_X = 16;
	public static final int EMPTY_SLOT_Y = 0;
	
	private boolean acceptHiddenCoins = true;
	
	public CoinSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	public CoinSlot(Container inventory, int index, int x, int y, boolean acceptHiddenCoins)
	{
		super(inventory, index, x, y);
		this.acceptHiddenCoins = acceptHiddenCoins;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		if(acceptHiddenCoins)
			return MoneyUtil.isCoin(stack.getItem());
		else
			return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawEmptyCoinSlots(Screen screen, AbstractContainerMenu container, PoseStack poseStack, int startX, int startY)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, LightmansCurrency.EMPTY_SLOTS);
		
		for(Slot slot : container.slots)
		{
			if(slot instanceof CoinSlot)
			{
				if(!slot.hasItem())
				{
					//CurrencyMod.LOGGER.info("Drawing empty coin slot at ATM slot index " + this.container.inventorySlots.indexOf(slot));
					screen.blit(poseStack, startX + slot.x, startY + slot.y, EMPTY_SLOT_X, EMPTY_SLOT_Y, 16, 16);
				}
			}
		}
	}

}
