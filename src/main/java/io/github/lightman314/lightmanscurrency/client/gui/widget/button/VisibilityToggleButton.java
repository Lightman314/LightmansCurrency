package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import net.minecraftforge.common.util.NonNullSupplier;

public class VisibilityToggleButton extends PlainButton {
	
	public static final int U_OFFSET = 176 + 16;
	public static final int V_OFFSET = 0;
	
	public static final int SIZE = 6;
	
	private final NonNullSupplier<Boolean> visible;
	
	public VisibilityToggleButton(int x, int y, NonNullSupplier<Boolean> visible, OnPress pressable ) {
		super(x, y, SIZE, SIZE, pressable, WalletScreen.GUI_TEXTURE, 0, 0);
		this.visible = visible;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.setResource(WalletScreen.GUI_TEXTURE, U_OFFSET + (this.visible.get() ? SIZE : 0), V_OFFSET);
		super.render(pose, mouseX, mouseY, partialTicks);
	}

}
