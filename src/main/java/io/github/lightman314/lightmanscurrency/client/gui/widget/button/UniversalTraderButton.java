package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UniversalTraderButton extends Button{
	
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universaltraderbuttons.png");
	
	public static final int WIDTH = 146;
	public static final int HEIGHT = 30;
	
	TraderData data;
	public TraderData getData() { return this.data; }
	
	Font font;
	
	public boolean selected = false;
	
	public UniversalTraderButton(int x, int y, OnPress pressable, Font font)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.font = font;
	}
	
	/**
	 * Updates the trader data for this buttons trade.
	 */
	public void SetData(TraderData data) { this.data = data; }
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set active status
		this.active = this.data != null && !this.selected;
		//Render nothing if there is no data
		if(this.data == null)
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, BUTTON_TEXTURES);
		if(this.active)
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0F);
		
		int offset = 0;
		if(this.isHovered || this.selected)
			offset = HEIGHT;
		//Draw Button BG
		this.blit(poseStack, this.x, this.y, 0, offset, WIDTH, HEIGHT);
		
		//Draw the icon
		this.data.getIcon().render(poseStack, this, this.font, this.x + 4, this.y + 7);
		
		//Draw the name & owner of the trader
		Style style = this.data.isCreative() ? Style.EMPTY.applyFormat(ChatFormatting.GREEN) : Style.EMPTY;
		this.font.draw(poseStack, TextRenderUtil.fitString(this.data.getName(), this.width - 26, style), this.x + 24f, this.y + 6f, 0x404040);
		this.font.draw(poseStack, TextRenderUtil.fitString(this.data.getOwner().getOwnerName(true), this.width - 26), this.x + 24f, this.y + 16f, 0x404040);
		
	}

}