package io.github.lightman314.lightmanscurrency.client.gui.widget.scroll;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleHeightSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.VerticalSliceSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScrollBarWidget extends EasyWidget implements IMouseListener, IPreRender {

    public static final FlexibleHeightSprite BACKGROUND_SPRITE = new VerticalSliceSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_vert_background"),8,128),8);
    public static final FixedSizeSprite KNOB_SPRITE = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_vert_knob"),8,29));
    public static final FixedSizeSprite SMALL_KNOB_SPRITE = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_vert_smallknob"),8,9));

    public static final int WIDTH = 8;

    private final IScrollable scrollable;

    private final boolean alwaysShow;
    private final FlexibleHeightSprite backgroundSprite;
    private final FixedSizeSprite knobSprite;

    public boolean isDragging = false;

    private ScrollBarWidget(Builder builder)
    {
        super(builder);
        this.alwaysShow = builder.alwaysShow;
        this.scrollable = builder.scrollable;
        this.backgroundSprite = builder.backgroundSprite;
        this.knobSprite = builder.knobSprite;
    }

    /**
     * @deprecated Use {@link Builder#onRight(EasyWidget)} instead
     */
    @Deprecated

    public static <T extends EasyWidget & IScrollable> ScrollBarWidget createOnRight(T widget) { return builder().onRight(widget).build(); }

    public boolean visible() { return this.visible && this.alwaysShow || this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }
    public boolean showKnob() { return this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }

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
        this.backgroundSprite.render(gui,0,0,this.height);

        if(this.showKnob())
        {
            int knobPosition;
            if(this.isDragging)
                knobPosition = MathUtil.clamp(gui.mousePos.y - this.getY() - (this.getKnobHeight() / 2), 0, this.height - this.getKnobHeight());
            else
                knobPosition = this.getNaturalKnobPosition();

            //Render the knob
            int knobOffset = (WIDTH - this.knobSprite.getWidth()) / 2;
            this.knobSprite.render(gui,knobOffset,knobPosition);
        }

    }

    @Override
    public void preRender(EasyGuiGraphics gui) {
        if(this.isDragging)
            this.dragKnob(gui.mousePos.y);
    }

    private int getKnobHeight() { return this.knobSprite.getHeight(); }

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

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(WIDTH,20); }
        @Override
        protected Builder getSelf() { return this; }

        private boolean alwaysShow = false;
        private FlexibleHeightSprite backgroundSprite = BACKGROUND_SPRITE;
        private FixedSizeSprite knobSprite = KNOB_SPRITE;
        private IScrollable scrollable = null;

        public Builder height(int height) { this.changeHeight(height); return this; }
        public <T extends EasyWidget & IScrollable> Builder onLeft(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(-1 * WIDTH,0)).height(widget.getHeight()); }
        public <T extends EasyWidget & IScrollable> Builder onRight(T widget) { return this.scrollable(widget).position(widget.getPosition().offset(widget.getWidth(),0)).height(widget.getHeight()); }
        public Builder scrollable(IScrollable scrollable) { this.scrollable = scrollable; return this; }
        public Builder smallKnob() { return this.customKnob(SMALL_KNOB_SPRITE); }
        public Builder customKnob(FixedSizeSprite knobSprite) {
            this.knobSprite = knobSprite;
            return this;
        }
        public Builder customBackground(FlexibleHeightSprite backgroundSprite) {
            this.backgroundSprite = backgroundSprite;
            this.changeWidth(this.backgroundSprite.getWidth());
            return this;
        }
        public Builder alwaysShow() { this.alwaysShow = true; return this; }

        public ScrollBarWidget build() { return new ScrollBarWidget(this); }

    }

}