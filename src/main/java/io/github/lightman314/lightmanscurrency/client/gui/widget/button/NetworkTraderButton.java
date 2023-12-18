package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class NetworkTraderButton extends EasyButton {
	
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universaltraderbuttons.png");
	
	public static final int WIDTH = 146;
	public static final int HEIGHT = 30;
	
	TraderData data;
	public TraderData getData() { return this.data; }
	
	public boolean selected = false;
	
	public NetworkTraderButton(ScreenPosition pos, Consumer<EasyButton> pressable) { this(pos.x, pos.y, pressable); }
	public NetworkTraderButton(int x, int y, Consumer<EasyButton> pressable) { super(x, y, WIDTH, HEIGHT, pressable); }

	@Override
	public NetworkTraderButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	/**
	 * Updates the trader data for this buttons trade.
	 */
	public void SetData(TraderData data) { this.data = data; }
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		//Set active status
		this.active = this.data != null && !this.selected;
		//Render nothing if there is no data
		if(this.data == null)
			return;

		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);
		
		int offset = 0;
		if(this.isHovered || this.selected)
			offset = HEIGHT;
		//Draw Button BG
		gui.blit(BUTTON_TEXTURES, 0,0, 0, offset, WIDTH, HEIGHT);
		
		//Draw the icon
		this.data.getIcon().render(gui, 4, 7);
		
		//Draw the name & owner of the trader
		Style style = this.data.isCreative() ? Style.EMPTY.applyFormat(ChatFormatting.GREEN) : Style.EMPTY;
		gui.drawString(TextRenderUtil.fitString(this.data.getName(), this.width - 26, style), 24, 6, 0x404040);
		gui.drawString(TextRenderUtil.fitString(this.data.getOwner().getOwnerName(true), this.width - 26), 24, 16, 0x404040);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

	}

}
