package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import com.google.common.primitives.Ints;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.HorizontalSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;

public class HorizontalScrollBar extends FancyWidget implements IMouseListener {

    public static final HorizontalSizedSprite DEFAULT_BACKGROUND = new HorizontalSizedSprite.Simple(LCApi.id("widget/scrollbar_horiz_background"),8);
    public static final SizedSprite DEFAULT_KNOB = new SizedSprite.Simple(LCApi.id("widget/scrollbar_horiz_knob"),29,8);
    public static final SizedSprite SLIDER = new SizedSprite.Simple(LCApi.id("widget/scrollbar_horiz_slider"),5,12);

    private final IScrollable scrollable;
    private final HorizontalSizedSprite backgroundSprite;
    private final SizedSprite knobSprite;
    private final boolean alwaysShow;

    private boolean isDragging = false;

    protected HorizontalScrollBar(Builder builder) {
        super(builder);
        this.scrollable = builder.scrollable;
        this.backgroundSprite = builder.backgroundSprite;
        this.knobSprite = builder.knobSprite;
        this.alwaysShow = builder.alwaysShow;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        if(this.isDragging)
            this.dragKnob(mousePos.x);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        boolean showKnob = this.scrollable.showScrollability();
        if(!this.alwaysShow && !showKnob)
        {
            //If it isn't visible, then it isn't dragging
            this.isDragging = false;
            return;
        }
        //Render the background
        gui.blitSprite(this.backgroundSprite,0,0,this.width);
        //Render the kob
        if(showKnob)
        {
            int knobPosition;
            if(this.isDragging)
                knobPosition = Math.clamp(gui.getMousePos().x - this.getX() - (this.knobSprite.width() / 2),0,this.height - this.knobSprite.height());
            else
                knobPosition = this.getNaturalKnobPosition();
            gui.blitSprite(this.knobSprite,knobPosition,(this.height - this.knobSprite.height()) / 2);
        }
    }


    private void dragKnob(double mouseX) {
        if(this.scrollable.getMinScroll() >= this.scrollable.getMaxScroll())
        {
            this.isDragging = false;
            return;
        }

        //Calculate the x offset
        int scroll = this.getScrollFromMouse(mouseX);
        if(this.scrollable.getScroll() != scroll)
        {
            this.scrollable.setScroll(scroll);
            this.scrollable.validateScroll();
        }
    }

    private int getScrollFromMouse(double mouseX) {
        mouseX -= (double)this.knobSprite.width() / 2d;
        //Check if the mouse is out of bounds, upon which return the maxUses/min scroll respectively
        if(mouseX <= this.getX())
            return this.scrollable.getMinScroll();
        if(mouseX >= this.getX() + this.width - this.knobSprite.width())
            return this.scrollable.getMaxScroll();

        //Calculate the scroll based on the mouse position
        int deltaScroll = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
        if(deltaScroll <= 0)
            return this.scrollable.getMinScroll();

        double sectionWidth = (double)(this.width - this.knobSprite.width()) / (double)deltaScroll;
        double relativeMouseX = mouseX - this.getX();
        int section = Ints.saturatedCast(Math.round(Math.floor(relativeMouseX / sectionWidth)));
        return this.scrollable.getMinScroll() + section;

    }

    private int getNaturalKnobPosition() {
        int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
        if(notches <= 0)
            return 0;
        double spacing = (double)(this.width - this.knobSprite.width()) / (double)notches;
        int scroll = this.scrollable.getScroll() - this.scrollable.getMinScroll();
        return (int)Math.round(scroll * spacing);
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.isDragging = false;
        if(event.button() == 0 && this.getArea().isInArea(event.x(),event.y()) && this.scrollable.getMaxScroll() > this.scrollable.getMinScroll())
        {
            this.isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseDragged(MouseButtonEvent event,double dx,double dy) {
        if(this.isDragging && event.button() == 0)
            this.dragKnob(event.x());
        return false;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent event) {
        if(this.isDragging && event.button() == 0)
        {
            //One last drag calculation
            this.dragKnob(event.x());
            this.isDragging = false;
        }
        return false;
    }

    public static Builder builder(IScrollable scrollable) { return new Builder(scrollable); }

    public static class Builder extends AbstractBuilder<Builder, HorizontalScrollBar> {

        private Builder(IScrollable scrollable) { super(100,8); this.scrollable = scrollable; }

        private HorizontalSizedSprite backgroundSprite = DEFAULT_BACKGROUND;
        private SizedSprite knobSprite = DEFAULT_KNOB;
        private boolean alwaysShow = false;
        private final IScrollable scrollable;

        public Builder above(LayoutElement element) { return this.atPos(element.getX(),element.getY() - this.getHeight()).ofWidth(element.getWidth()); }
        public Builder below(LayoutElement element) { return this.atPos(element.getX(),element.getY() + element.getHeight()).ofWidth(element.getWidth()); }
        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withBackground(Identifier background) { return this.withBackground(new HorizontalSizedSprite.Simple(background,this.getHeight())); }
        public Builder withBackground(HorizontalSizedSprite backgroundSprite) { this.backgroundSprite = backgroundSprite; this.setHeight(this.backgroundSprite.height()); return this; }
        public Builder withKnob(SizedSprite sprite) { this.knobSprite = sprite; this.setWidth(sprite.width()); return this; }

        public Builder alwaysShow() { this.alwaysShow = true; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public HorizontalScrollBar build() { return new HorizontalScrollBar(this); }

    }

}
