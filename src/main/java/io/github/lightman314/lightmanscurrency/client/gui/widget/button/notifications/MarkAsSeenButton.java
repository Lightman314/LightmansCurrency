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
		super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, onPress);
	}
	
	private static int getWidth(Component text) { return TextRenderUtil.getFont().width(text) + 4; }
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		int i = this.getYImage(this.isHoveredOrFocused());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		int topSize = this.height / 2;
		int bottomSize = topSize;
		if(this.height % 2 != 0)
			bottomSize++;
		this.blit(pose, this.x, this.y, 0, 46 + i * 20, this.width / 2, topSize);
		this.blit(pose, this.x, this.y + topSize, 0, 66 - bottomSize + i * 20, this.width / 2, bottomSize);
		this.blit(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);
		this.blit(pose, this.x + this.width / 2, this.y + topSize, 200 - this.width / 2, 66 - bottomSize + i * 20, this.width / 2, bottomSize);
		this.renderBg(pose, minecraft, mouseX, mouseY);
		int j = getFGColor();
		drawCenteredString(pose, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
}
