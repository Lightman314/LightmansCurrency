package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;

public class WalletButton extends PlainButton{

	public static final ResourceLocation WALLET_BUTTON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_button.png");
	
	private final ContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public WalletButton(ContainerScreen<?> parent, int x, int y, IPressable pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, pressable, WALLET_BUTTON_TEXTURE, 0, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		//Reposition the button based on the containers top/left most position
		this.x = this.parent.getGuiLeft() + this.xOffset;
		this.y = this.parent.getGuiTop() + this.yOffset;
		
		if(this.parent instanceof CreativeScreen) {
			CreativeScreen gui = (CreativeScreen)this.parent;
			boolean isInventoryTab = gui.getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex();
			this.active = isInventoryTab;
			
			//Hide if we're not in the inventory tab of the creative screen
			if(!isInventoryTab) {
				return;
			}
		}
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
	}

}
