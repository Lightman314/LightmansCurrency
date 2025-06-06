package io.github.lightman314.lightmanscurrency.client.gui.widget.scroll;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ScrollBarWidget extends EasyWidget implements IMouseListener, IPreRender {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource( "textures/gui/scroll.png");
	
	public static final int WIDTH = 8;
	public static final int KNOB_HEIGHT = 29;
	public static final int SMALL_KNOB_HEIGHT = 9;
	
	private final IScrollable scrollable;

	private final boolean smallKnob;
	
	public boolean isDragging = false;
	
	private int getKnobHeight() { return this.smallKnob ? SMALL_KNOB_HEIGHT : KNOB_HEIGHT; }

	private ScrollBarWidget(@Nonnull Builder builder)
	{
		super(builder);
		this.scrollable = builder.scrollable;
		this.smallKnob = builder.smallKnob;
	}

	/**
	 * @deprecated Use {@link Builder#onRight(EasyWidget)} instead
	 */
	@Deprecated
	@Nonnull
	public static <T extends EasyWidget & IScrollable> ScrollBarWidget createOnRight(@Nonnull T widget) { return builder().onRight(widget).build(); }

	public boolean visible() { return this.visible && this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }

	@Override
	protected void renderTick() {
		if(!this.visible() && this.isDragging)
			this.isDragging = false;
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		if(!this.visible())
			return;

		gui.resetColor();
		//Render the top
		gui.blit(GUI_TEXTURE, 0, 0, 0, 0, WIDTH, 8);
		//Render the middle
		int yOffset = 8;
		while(yOffset < this.height - 8)
		{
			int yPart = Math.min(this.height - 8 - yOffset, 240);
			gui.blit(GUI_TEXTURE, 0, yOffset, 0, 8, WIDTH, yPart);
			yOffset += yPart;
		}
		//Render the bottom
		gui.blit(GUI_TEXTURE, 0, this.height - 8, 0, 248, WIDTH, 8);
		
		int knobPosition;
		if(this.isDragging)
			knobPosition = MathUtil.clamp(gui.mousePos.y - this.getY() - (this.getKnobHeight() / 2), 0, this.height - this.getKnobHeight());
		else
			knobPosition = this.getNaturalKnobPosition();
		
		//Render the knob
		gui.blit(GUI_TEXTURE, 0, knobPosition, this.smallKnob ? WIDTH * 2 : WIDTH, 0, WIDTH, this.getKnobHeight());
		
	}

	@Override
	public void preRender(@Nonnull EasyGuiGraphics gui) {
		if(this.isDragging)
			this.dragKnob(gui.mousePos.y);
	}

	private int getNaturalKnobPosition() {
		int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(notches <= 0)
			return 0;
		double spacing = (double)(this.height - this.getKnobHeight()) / (double)notches;
		int scroll = this.scrollable.currentScroll() - this.scrollable.getMinScroll();
		return (int)Math.round(scroll * spacing);
	}

	protected void dragKnob(double mouseY) {
		//Cannot do anything if the scrollable cannot be scrolled
		if(!this.visible())
		{
			this.isDragging = false;
			return;
		}
		
		//Calculate the y offset
		int scroll = this.getScrollFromMouse(mouseY);
		
		if(this.scrollable.currentScroll() != scroll)
			this.scrollable.setScroll(scroll);
		
	}
	
	private int getScrollFromMouse(double mouseY) {
		
		mouseY -= (double)this.getKnobHeight() / 2d;
		//Check if the mouse is out of bounds, upon which return the max/min scroll respectively
		if(mouseY <= this.getY())
			return this.scrollable.getMinScroll();
		if(mouseY >= this.getY() + this.height - this.getKnobHeight())
			return this.scrollable.getMaxScroll();
		
		//Calculate the scroll based on the mouse position
		int deltaScroll = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(deltaScroll <= 0)
			return Integer.MIN_VALUE;
		
		double sectionHeight = (double)(this.height - this.getKnobHeight()) / (double)deltaScroll;
		double yPos = (double)this.getY() - (sectionHeight / 2d);
		
		for(int i = this.scrollable.getMinScroll(); i <= this.scrollable.getMaxScroll(); ++i)
		{
			if(mouseY >= yPos && mouseY < yPos + sectionHeight)
				return i;
			yPos += sectionHeight;
		}
		//Somehow didn't find the scroll from the scroll bar.
		LightmansCurrency.LogWarning("Error getting scroll from mouse position.");
		return this.scrollable.getMinScroll();
	}

	//Deprecated as this should only be called by the IMouseListener callers
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.isDragging = false;
		if(this.isMouseOver(mouseX, mouseY) && this.visible() && button == 0)
		{
			this.isDragging = true;
			this.dragKnob(mouseY);
		}
		return false;
	}

	//Deprecated as this should only be called by the IMouseListener callers (and thus not care that if it's a ScrollBarWidget or not)
	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) {
		if(this.isDragging && this.visible() && button == 0)
		{
			//One last drag calculation
			this.dragKnob(mouseY);
			this.isDragging = false;
		}
		return false;
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(WIDTH,20); }
		@Override
		protected Builder getSelf() { return this; }

		private boolean smallKnob = false;
		private IScrollable scrollable = null;

		public Builder height(int height) { this.changeHeight(height); return this; }
		public <T extends EasyWidget & IScrollable> Builder onLeft(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(-1 * WIDTH,0)).height(widget.getHeight()); }
		public <T extends EasyWidget & IScrollable> Builder onRight(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(widget.getWidth(),0)).height(widget.getHeight()); }
		public Builder scrollable(IScrollable scrollable) { this.scrollable = scrollable; return this; }
		public Builder smallKnob() { this.smallKnob = true; return this; }

		public ScrollBarWidget build() { return new ScrollBarWidget(this); }

	}
	
}
