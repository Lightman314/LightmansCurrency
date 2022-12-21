package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

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
		assert player != null;
		IWalletHandler walletHandler = WalletCapability.getWalletHandler(player).orElse(null);
		return walletHandler == null ? false : walletHandler.visible();
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.setResource(ClientEvents.WALLET_SLOT_TEXTURE, U_OFFSET + (isWalletVisible() ? SIZE : 0), V_OFFSET);
		this.setPosition(this.parent.getGuiLeft() + this.xOffset, this.parent.getGuiTop() + this.yOffset);
		if(this.parent instanceof CreativeModeInventoryScreen cs)
			this.visible = cs.isInventoryOpen();
		super.render(pose, mouseX, mouseY, partialTicks);
	}

}
