package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UniversalTraderButton extends Button{
	
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universaltraderbuttons.png");
	
	public static final int WIDTH = 146;
	public static final int HEIGHT = 30;
	
	UniversalTraderData data;
	
	FontRenderer font;
	
	public UniversalTraderButton(int x, int y, IPressable pressable, FontRenderer font)
	{
		super(x, y, WIDTH, HEIGHT, ITextComponent.getTextComponentOrEmpty(""), pressable);
		this.font = font;
	}
	
	/**
	 * Updates the trader data for this buttons trade.
	 */
	public void SetData(UniversalTraderData data)
	{
		this.data = data;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set active status
		this.active = this.data != null;
		//Render nothing if there is no data
		if(this.data == null)
			return;
		
		Minecraft.getInstance().getTextureManager().bindTexture(BUTTON_TEXTURES);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		int offset = 0;
		if(this.isHovered)
			offset = HEIGHT;
		//Draw Button BG
		this.blit(matrixStack, this.x, this.y, 0, offset, WIDTH, HEIGHT);
		
		//Draw the icon
		this.data.getIcon().render(matrixStack, this, this.font, this.x + 4, this.y + 7);
		
		//Draw the name & owner of the trader
		this.font.drawString(matrixStack, this.data.getName().getString(), this.x + 24f, this.y + 6f, 0x404040);
		this.font.drawString(matrixStack, this.data.getCoreSettings().getOwnerName(), this.x + 24f, this.y + 16f, 0x404040);
		
	}

}
