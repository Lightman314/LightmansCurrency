package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import com.google.common.primitives.Ints;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SimpleSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;

public class VerticalScrollBar extends FancyWidget implements IMouseListener {

    public static final Identifier DEFAULT_BACKGROUND = LCApi.id("widget/scrollbar_vert_background");
    public static final SizedSprite DEFAULT_KNOB = new SimpleSizedSprite(LCApi.id("widget/scrollbar_vert_knob"),8,29);
    public static final SizedSprite SMALL_KNOB = new SimpleSizedSprite(LCApi.id("widget/scrollbar_vert_smallknob"),8,9);

    private final IScrollable scrollable;
    private final Identifier backgroundSprite;
    private final SizedSprite knobSprite;
    private final boolean alwaysShow;

    private boolean isDragging = false;

    protected VerticalScrollBar(Builder builder) {
        super(builder);
        this.scrollable = builder.scrollable;
        this.backgroundSprite = builder.backgroundSprite;
        this.knobSprite = builder.knobSprite;
        this.alwaysShow = builder.alwaysShow;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        if(this.isDragging)
            this.dragKnob(mousePos.y);
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
        gui.blitSprite(this.backgroundSprite,0,0,this.width,this.height);
        //Render the kob
        if(showKnob)
        {
            int knobPosition;
            if(this.isDragging)
                knobPosition = Math.clamp(gui.getMousePos().y - this.getY() - (this.knobSprite.height() / 2),0,this.height - this.knobSprite.height());
            else
                knobPosition = this.getNaturalKnobPosition();
            gui.blitSprite(this.knobSprite,0,knobPosition);
        }
    }


    private void dragKnob(double mouseY) {
        if(this.scrollable.getMinScroll() >= this.scrollable.getMaxScroll())
        {
            this.isDragging = false;
            return;
        }

        //Calculate the y offset
        int scroll = this.getScrollFromMouse(mouseY);
        if(this.scrollable.getScroll() != scroll)
        {
            this.scrollable.setScroll(scroll);
            this.scrollable.validateScroll();
        }
    }

    private int getScrollFromMouse(double mouseY) {
        mouseY -= (double)this.knobSprite.height() / 2d;
        //Check if the mouse is out of bounds, upon which return the max/min scroll respectively
        if(mouseY <= this.getY())
            return this.scrollable.getMinScroll();
        if(mouseY >= this.getY() + this.height - this.knobSprite.height())
            return this.scrollable.getMaxScroll();

        //Calculate the scroll based on the mouse position
        int deltaScroll = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
        if(deltaScroll <= 0)
            return this.scrollable.getMinScroll();

        double sectionHeight = (double)(this.height - this.knobSprite.height()) / (double)deltaScroll;
        double relativeMouseY = mouseY - this.getY();
        int section = Ints.saturatedCast(Math.round(Math.floor(relativeMouseY / sectionHeight)));
        return this.scrollable.getMinScroll() + section;

    }

    private int getNaturalKnobPosition() {
        int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
        if(notches <= 0)
            return 0;
        double spacing = (double)(this.height - this.knobSprite.height()) / (double)notches;
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
            this.dragKnob(event.y());
        return false;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent event) {
        if(this.isDragging && event.button() == 0)
        {
            //One last drag calculation
            this.dragKnob(event.y());
            this.isDragging = false;
        }
        return false;
    }

    public static Builder builder(IScrollable scrollable) { return new Builder(scrollable); }

    public static class Builder extends AbstractBuilder<Builder,VerticalScrollBar> {

        private Builder(IScrollable scrollable) { super(8,100); this.scrollable = scrollable; }

        private Identifier backgroundSprite = DEFAULT_BACKGROUND;
        private SizedSprite knobSprite = DEFAULT_KNOB;
        private boolean alwaysShow = false;
        private final IScrollable scrollable;

        public Builder leftOf(LayoutElement element) { return this.atPos(element.getX() - this.getArea().width,element.getY()).ofHeight(element.getHeight()); }
        public Builder rightOf(LayoutElement element) { return this.atPos(element.getX() + element.getWidth(),element.getY()).ofHeight(element.getHeight()); }
        public Builder ofHeight(int height) { this.setHeight(height); return this; }

        public Builder withBackground(Identifier backgroundSprite) { this.backgroundSprite = backgroundSprite; return this; }
        public Builder withKnob(SizedSprite sprite) { this.knobSprite = sprite; this.setWidth(sprite.width()); return this; }

        public Builder alwaysShow() { this.alwaysShow = true; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public VerticalScrollBar build() { return new VerticalScrollBar(this); }

    }

}
