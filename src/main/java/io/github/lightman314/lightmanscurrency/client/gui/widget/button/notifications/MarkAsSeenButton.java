package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class MarkAsSeenButton extends Button {

	public static final int HEIGHT = 11;
	
	public MarkAsSeenButton(int rightPos, int yPos, Component text, OnPress onPress) {
		super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, onPress, Button.DEFAULT_NARRATION);
	}
	
	private static int getWidth(Component text) { return TextRenderUtil.getFont().width(text) + 4; }

	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}

		return 46 + i * 20;
	}

	@Override
	public void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		int topSize = this.height / 2;
		int bottomSize = topSize;
		if(this.height % 2 != 0)
			bottomSize++;
		blit(pose, this.getX(), this.getY(), 0, this.getTextureY(), this.width / 2, topSize);
		blit(pose, this.getX(), this.getY() + topSize, 0, 20 - bottomSize + this.getTextureY(), this.width / 2, bottomSize);
		blit(pose, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, this.getTextureY(), this.width / 2, this.height / 2);
		blit(pose, this.getX() + this.width / 2, this.getY() + topSize, 200 - this.width / 2, 20 - bottomSize + this.getTextureY(), this.width / 2, bottomSize);
		int j = getFGColor();
		drawCenteredString(pose, font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
}
