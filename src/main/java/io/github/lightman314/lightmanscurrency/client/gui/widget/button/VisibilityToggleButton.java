package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;

import javax.annotation.Nonnull;

public class VisibilityToggleButton extends PlainButton {
	
	public static final int U_OFFSET = 28;
	public static final int V_OFFSET = 0;
	
	public static final int SIZE = 6;
	
	private final ContainerScreen<?> parent;
	private final int xOffset;
	private final int yOffset;
	
	public VisibilityToggleButton(ContainerScreen<?> parent, int x, int y, IPressable pressable) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, SIZE, SIZE, pressable, ClientEvents.WALLET_SLOT_TEXTURE, 0, 0);
		this.parent = parent;
		this.xOffset = x;
		this.yOffset = y;
	}
	
	private static boolean isWalletVisible() {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
		return walletHandler != null && walletHandler.visible();
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.setResource(ClientEvents.WALLET_SLOT_TEXTURE, U_OFFSET + (isWalletVisible() ? SIZE : 0), V_OFFSET);
		this.x = this.parent.getGuiLeft() + this.xOffset;
		this.y = this.parent.getGuiTop() + this.yOffset;
		if(this.parent instanceof CreativeScreen) {
			CreativeScreen creativeScreen = (CreativeScreen)this.parent;
			this.active = this.visible = creativeScreen.getSelectedTab() == ItemGroup.TAB_INVENTORY.getId();
		}
		super.render(pose, mouseX, mouseY, partialTicks);
	}

}
