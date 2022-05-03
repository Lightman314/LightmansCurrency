package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;

public class VisibilityToggleButton extends PlainButton {
	
	public static final int U_OFFSET = 28;
	public static final int V_OFFSET = 0;
	
	public static final int SIZE = 6;
	
	private final AbstractContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public VisibilityToggleButton(AbstractContainerScreen<?> parent, int x, int y, OnPress pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, SIZE, SIZE, pressable, ClientEvents.WALLET_SLOT_TEXTURE, 0, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	private static boolean isWalletVisible() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		IWalletHandler walletHandler = WalletCapability.getWalletHandler(player).orElse(null);
		return walletHandler == null ? false : walletHandler.visible();
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.setResource(ClientEvents.WALLET_SLOT_TEXTURE, U_OFFSET + (isWalletVisible() ? SIZE : 0), V_OFFSET);
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
		super.render(pose, mouseX, mouseY, partialTicks);
	}

}
