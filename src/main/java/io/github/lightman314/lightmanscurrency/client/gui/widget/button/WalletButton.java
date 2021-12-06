package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class WalletButton extends PlainButton{

	public static final ResourceLocation WALLET_BUTTON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_button.png");
	
	private final AbstractContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public WalletButton(AbstractContainerScreen<?> parent, int x, int y, OnPress pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, pressable, WALLET_BUTTON_TEXTURE, 0, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Reposition the button based on the containers top/left most position
		this.x = this.parent.getGuiLeft() + this.xOffset;
		this.y = this.parent.getGuiTop() + this.yOffset;
		
		if(this.parent instanceof CreativeModeInventoryScreen) {
			CreativeModeInventoryScreen creativeScreen = (CreativeModeInventoryScreen)this.parent;
			boolean isInventoryTab = creativeScreen.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
			this.active = isInventoryTab;
			
			//Hide if we're not in the inventory tab of the creative screen
			if(!isInventoryTab) {
				return;
			}
		}
		
		super.render(poseStack, mouseX, mouseY, partialTicks);
		
	}

}
