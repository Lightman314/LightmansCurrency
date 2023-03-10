package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class MarkAsSeenButton extends Button {

	public static final int HEIGHT = 11;
	
	public MarkAsSeenButton(int rightPos, int yPos, ITextComponent text, IPressable onPress) {
		super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, onPress);
	}
	
	private static int getWidth(ITextComponent text) { return TextRenderUtil.getFont().width(text) + 4; }
	
	@Override
	public void renderButton(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		RenderUtil.bindTexture(WIDGETS_LOCATION);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		int i = this.getYImage(this.isHovered());
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
		drawCenteredString(pose, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}
	
}
