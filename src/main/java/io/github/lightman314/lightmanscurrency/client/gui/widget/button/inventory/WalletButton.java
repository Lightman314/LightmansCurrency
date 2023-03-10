package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.item.ItemGroup;

import javax.annotation.Nonnull;

public class WalletButton extends PlainButton{
	
	private final ContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public WalletButton(ContainerScreen<?> parent, int x, int y, IPressable pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, pressable, ClientEvents.WALLET_SLOT_TEXTURE, 18, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		if(shouldHide())
			return;
		
		//Reposition the button based on the containers top/left most position
		this.x = this.parent.getGuiLeft() + this.xOffset;
		this.y = this.parent.getGuiTop() + this.yOffset;
		
		if(this.parent instanceof CreativeScreen) {
			CreativeScreen creativeScreen = (CreativeScreen)this.parent;
			boolean isInventoryTab = creativeScreen.getSelectedTab() == ItemGroup.TAB_INVENTORY.getId();
			this.active = isInventoryTab;
			
			//Hide if we're not in the inventory tab of the creative screen
			if(!isInventoryTab) {
				return;
			}
		}
		
		super.render(pose, mouseX, mouseY, partialTicks);
		
	}
	
	private static boolean shouldHide() {
		Minecraft mc = Minecraft.getInstance();
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(mc.player);
		return walletHandler == null || walletHandler.getWallet().isEmpty();
	}

}
