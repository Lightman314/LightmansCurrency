package io.github.lightman314.lightmanscurrency.client.gui.widget.scroll;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleWidthSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.HorizontalSliceSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HorizScrollBarWidget extends EasyWidget implements IMouseListener, IPreRender {

    public static final FlexibleWidthSprite BACKGROUND_SPRITE = new HorizontalSliceSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_horiz_background"),128,8),8);
    public static final FixedSizeSprite KNOB_SPRITE = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_horiz_knob"),29,8));
    public static final FixedSizeSprite SMALL_KNOB_SPRITE = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_horiz_smallknob"),9,8));
    public static final FixedSizeSprite SLIDER_SPRITE = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_horiz_slider"),5,12));

	public static final int HEIGHT = 8;

	private final IScrollable scrollable;

	private final FixedSizeSprite knobSprite;

	public boolean isDragging = false;

	private HorizScrollBarWidget(Builder builder)
	{
		super(builder);
		this.scrollable = builder.scrollable;
		this.knobSprite = builder.knobSprite;
	}

	public boolean visible() { return this.visible && this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }

	@Override
	protected void renderTick() {
		if(!this.visible() && this.isDragging)
			this.isDragging = false;
	}

	@Override
	public void renderWidget(EasyGuiGraphics gui) {
		
		if(!this.visible())
			return;

		gui.resetColor();
		//Render the background
        BACKGROUND_SPRITE.render(gui,0,0,this.width);
		
		int knobPosition;
		if(this.isDragging)
			knobPosition = MathUtil.clamp(gui.mousePos.x - this.getX() - (this.getKnobWidth() / 2), 0, this.width - this.getKnobWidth());
		else
			knobPosition = this.getNaturalKnobPosition();
		
		//Render the knob
        int knobOffset = (HEIGHT - this.knobSprite.getHeight()) / 2;
        this.knobSprite.render(gui,knobPosition,knobOffset);
		
	}

	@Override
	public void preRender(EasyGuiGraphics gui) {
		if(this.isDragging)
			this.dragKnob(gui.mousePos.x);
	}

    private int getKnobWidth() { return this.knobSprite.getWidth(); }

	private int getNaturalKnobPosition() {
		int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(notches <= 0)
			return 0;
		double spacing = (double)(this.width - this.getKnobWidth()) / (double)notches;
		int scroll = this.scrollable.currentScroll() - this.scrollable.getMinScroll();
		return (int)Math.round(scroll * spacing);
	}

	protected void dragKnob(double mouseX) {
		//Cannot do anything if the scrollable cannot be scrolled
		if(!this.visible())
		{
			this.isDragging = false;
			return;
		}
		
		//Calculate the y offset
		int scroll = this.getScrollFromMouse(mouseX);
		
		if(this.scrollable.currentScroll() != scroll)
			this.scrollable.setScroll(scroll);
		
	}
	
	private int getScrollFromMouse(double mouseX) {
		
		mouseX -= (double)this.getKnobWidth() / 2d;
		//Check if the mouse is out of bounds, upon which return the max/min scroll respectively
		if(mouseX <= this.getX())
			return this.scrollable.getMinScroll();
		if(mouseX >= this.getX() + this.width - this.getKnobWidth())
			return this.scrollable.getMaxScroll();
		
		//Calculate the scroll based on the mouse position
		int deltaScroll = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(deltaScroll <= 0)
			return Integer.MIN_VALUE;
		
		double sectionWidth = (double)(this.width - this.getKnobWidth()) / (double)deltaScroll;
		double xPos = (double)this.getX() - (sectionWidth / 2d);
		
		for(int i = this.scrollable.getMinScroll(); i <= this.scrollable.getMaxScroll(); ++i)
		{
			if(mouseX >= xPos && mouseX < xPos + sectionWidth)
				return i;
            xPos += sectionWidth;
		}
		//Somehow didn't find the scroll from the scroll bar.
		LightmansCurrency.LogWarning("Error getting scroll from mouse position.");
		return this.scrollable.getMinScroll();
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.isDragging = false;
		if(this.isMouseOver(mouseX, mouseY) && this.visible() && button == 0)
		{
			this.isDragging = true;
			this.dragKnob(mouseX);
		}
		return false;
	}

	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) {
		if(this.isDragging && this.visible() && button == 0)
		{
			//One last drag calculation
			this.dragKnob(mouseX);
			this.isDragging = false;
		}
		return false;
	}

	public static Builder builder() { return new Builder(); }

	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(20,HEIGHT); }
		@Override
		protected Builder getSelf() { return this; }

		private FixedSizeSprite knobSprite = KNOB_SPRITE;
		private IScrollable scrollable = null;

		public Builder width(int width) { this.changeWidth(width); return this; }
		public <T extends EasyWidget & IScrollable> Builder onTop(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(0,-1 * HEIGHT)).width(widget.getWidth()); }
		public <T extends EasyWidget & IScrollable> Builder onBottom(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(0,widget.getHeight())).width(widget.getWidth()); }
		public Builder scrollable(IScrollable scrollable) { this.scrollable = scrollable; return this; }
		public Builder smallKnob() { return this.customKnob(SMALL_KNOB_SPRITE); }
		public Builder sliderKnob() { return this.customKnob(SLIDER_SPRITE); }
        public Builder customKnob(FixedSizeSprite knobSprite) {
            this.knobSprite = knobSprite;
            return this;
        }

		public HorizScrollBarWidget build() { return new HorizScrollBarWidget(this); }

	}
	
}
