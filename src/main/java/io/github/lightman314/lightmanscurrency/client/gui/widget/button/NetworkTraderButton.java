package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NetworkTraderButton extends EasyButton implements ITooltipWidget {
	
	public static final ResourceLocation BUTTON_TEXTURES = VersionUtil.lcResource("textures/gui/universaltraderbuttons.png");
	
	public static final int WIDTH = 146;
	public static final int HEIGHT = 30;
	
	TraderData data;
	public TraderData getData() { return this.data; }
	
	public boolean selected = false;

	private NetworkTraderButton(@Nonnull Builder builder) { super(builder); }

	/**
	 * Updates the trader data for this buttons trade.
	 */
	public void SetData(TraderData data) { this.data = data; }

	@Override
	protected void renderTick() {
		//Set as not visible if no data is present
		if((this.data == null) == this.visible)
			this.setVisible(this.data != null);
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		//Render nothing if there is no data
		if(this.data == null)
			return;

		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);
		
		int offset = 0;
		if(this.isHovered)
			offset = HEIGHT;
		if(this.selected)
			offset += HEIGHT * 2;
		//Draw Button BG
		gui.blit(BUTTON_TEXTURES, 0,0, 0, offset, WIDTH, HEIGHT);
		
		//Draw the icon
		this.data.getDisplayIcon().render(gui, 4, 7);
		
		//Draw the name & owner of the trader
		int color = this.data.getTerminalTextColor();
		gui.drawString(TextRenderUtil.fitString(this.data.getName(), this.width - 26), 24, 6, color);
		gui.drawString(TextRenderUtil.fitString(this.data.getOwner().getName(), this.width - 26), 24, 16, 0x404040);

		gui.resetColor();

	}

	@Override
	public List<Component> getTooltipText() {
		TraderData trader = this.getData();
		if(trader == null)
			return new ArrayList<>();
		return trader.getTerminalInfo(Minecraft.getInstance().player);
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private Builder() { super(WIDTH,HEIGHT); }
		@Override
		protected Builder getSelf() { return this; }
		public NetworkTraderButton build() { return new NetworkTraderButton(this); }
	}
}
