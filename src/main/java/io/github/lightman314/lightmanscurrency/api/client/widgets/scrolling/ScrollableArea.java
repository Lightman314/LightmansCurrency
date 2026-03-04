package io.github.lightman314.lightmanscurrency.api.client.widgets.scrolling;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.gui.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.layouts.LayoutElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollableArea extends EasyWidgetWithChildren implements IScrollListener, IScrollable {

    private final Consumer<Object> widgetConsumer;
    private final boolean showScrollBar;
    private final float sensitivity;

    private final List<PositionedWidget> widgets = new ArrayList<>();

    private boolean updateWidgets = true;

    private float yOffset = 0f;
    private int scrollablePixels = -1;

    protected ScrollableArea(Builder builder) {
        super(builder);
        this.widgetConsumer = builder.widgetConsumer;
        this.sensitivity = builder.sensitivity;
        this.showScrollBar = builder.showScrollBar;
        if(builder.oldWidget != null)
            this.yOffset = builder.oldWidget.yOffset;
    }

    public <T extends LayoutElement> T addWidget(T widget) { return this.addWidget(widget,ScreenPosition.of(widget.getX(),widget.getY()).offset(this.getPosition().inverted())); }
    public <T extends LayoutElement> T addWidget(T widget,ScreenPosition relativePosition)
    {
        this.widgetConsumer.accept(widget);
        PositionedWidget w = new PositionedWidget(widget,relativePosition);
        this.widgets.add(w);
        //Reset widget flags
        this.scrollablePixels = -1;
        this.updateWidgets = true;
        return widget;
    }

    @Override
    public void addChildren(ScreenArea area) {
        if(this.showScrollBar)
        {
            this.addChild(ScrollBarWidget.builder()
                    .onRight(this).build());
        }
    }

    private void checkScrollablePixels()
    {
        if(this.scrollablePixels < 0)
        {
            int lowestPoint = 0;
            for(PositionedWidget w : new ArrayList<>(this.widgets))
            {
                int l = w.position.y + w.widget.getHeight();
                if(l > lowestPoint)
                    lowestPoint = l;
            }
            this.scrollablePixels = Math.max(lowestPoint - this.getHeight(),0);
        }
    }

    @Override
    protected void renderTick() {
        if(this.updateWidgets)
        {
            this.checkScrollablePixels();
            this.yOffset = Math.clamp(this.yOffset,0,this.scrollablePixels);
            ScreenPosition corner = this.getPosition().offset(0,(int)this.yOffset * -1);
            for(PositionedWidget w : new ArrayList<>(this.widgets))
                w.update(corner);
            this.updateWidgets = false;
        }
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) { }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double old = this.yOffset;
        this.yOffset = Math.clamp(this.yOffset - (float)(delta * this.sensitivity),0,this.scrollablePixels);
        if(this.yOffset != old)
            this.updateWidgets = true;
        return this.yOffset != old;
    }

    @Override
    public int currentScroll() { return (int)this.yOffset; }

    @Override
    public void setScroll(int newScroll) { this.yOffset = Math.clamp(newScroll,0,this.scrollablePixels); }

    @Override
    public int getMaxScroll() { return this.scrollablePixels; }

    public Builder builder() { return new Builder(); }

    public static class Builder extends EasySizableBuilder<Builder>
    {

        private Consumer<Object> widgetConsumer = o -> {};
        private float sensitivity = 1f;
        private boolean showScrollBar = true;
        @Nullable
        private ScrollableArea oldWidget = null;

        public Builder widgetConsumer(Consumer<Object> widgetConsumer) { this.widgetConsumer = widgetConsumer; return this; }

        public Builder sensitivity(float sensitivity) { this.sensitivity = sensitivity; return this; }

        public Builder hideScrollBar() { this.showScrollBar = false; return this; }

        public Builder oldWidget(@Nullable ScrollableArea oldWidget) { this.oldWidget = oldWidget; return this; }

        @Override
        protected Builder getSelf() { return this; }

        public ScrollableArea build() { return new ScrollableArea(this); }

    }

    private record PositionedWidget(LayoutElement widget,ScreenPosition position)
    {
        void update(ScreenPosition corner)
        {
            ScreenPosition pos = corner.offset(this.position);
            this.widget.setPosition(pos.x,pos.y);
        }
    }

}
