package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

public class WalletButton extends PlainButton{
	
	private final AbstractContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public WalletButton(AbstractContainerScreen<?> parent, int x, int y, OnPress pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, pressable, ClientEvents.WALLET_SLOT_TEXTURE, 18, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		if(shouldHide())
			return;
		
		//Reposition the button based on the containers top/left most position
		this.x = this.parent.getGuiLeft() + this.xOffset;
		this.y = this.parent.getGuiTop() + this.yOffset;
		
		if(this.parent instanceof CreativeModeInventoryScreen cs)
			this.visible = cs.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
		
		super.render(pose, mouseX, mouseY, partialTicks);
		
	}
	
	private static boolean shouldHide() {
		Minecraft mc = Minecraft.getInstance();
		assert mc.player != null;
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(mc.player);
		return walletHandler == null || walletHandler.getWallet().isEmpty();
	}

}
