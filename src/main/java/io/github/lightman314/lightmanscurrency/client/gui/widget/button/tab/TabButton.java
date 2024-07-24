package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TabButton extends EasyButton implements ITooltipSource {
	
	public static final ResourceLocation GUI_TEXTURE = IconAndButtonUtil.WIDGET_TEXTURE;
	
	public static final int SIZE = 25;

	public boolean hideTooltip = false;
	
	public final ITab tab;
	
	private int rotation = 0;
	
	public TabButton(Consumer<EasyButton> pressable, ITab tab)
	{
		super(0, 0, SIZE, SIZE, pressable);
		this.tab = tab;
	}

	@Override
	public TabButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	public void reposition(ScreenPosition pos, int rotation) { this.reposition(pos.x, pos.y, rotation); }
	public void reposition(int x, int y, int rotation)
	{
		this.setPosition(x, y);
		this.rotation = MathUtil.clamp(rotation, 0, 3);
	}
	
	@Override
	public void renderWidget(@NotNull EasyGuiGraphics gui)
	{
		//Set the texture & color for the button
		gui.setColor(this.getColor(), 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
		gui.blit(GUI_TEXTURE, 0, 0, 200 + xOffset, yOffset, this.width, this.height);

		float m = this.active ? 1f : 0.5f;
		gui.setColor(m,m,m);
        this.tab.getIcon().render(gui, 4, 4);

		gui.resetColor();

	}
	
	protected int getColor() { return this.tab.getColor(); }

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) {
		if(this.hideTooltip || !this.isVisible())
			return null;
		if(this.getArea().isMouseInArea(mouseX,mouseY))
			return ImmutableList.of(this.tab.getTooltip());
		return null;
	}
}
