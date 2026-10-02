package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.FancyRenderable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WidgetScrollingArea extends AbstractMultiWidget.LateChildren implements IPositionalWidgetHolder, IScrollable, IScrollListener {

    private final List<Child> children = new ArrayList<>();
    private float scroll = 0f;
    public float getPreciseScroll() { return this.scroll; }
    private int childHeight = 0;
    private int scrollableHeight = 0;
    private final boolean spawnScrollBar;
    private final boolean centerVertically;
    private final int bottomSpacing;

    public WidgetScrollingArea(Builder builder) {
        super(builder);
        this.spawnScrollBar = builder.createScrollBar;
        this.centerVertically = builder.centerVertically;
        this.bottomSpacing = builder.bottomSpacing;
        if(builder.old != null)
            this.scroll = builder.old.scroll;
    }

    @Override
    public int getLowestWidgetPoint() { return this.childHeight; }

    @Override
    protected void addLateChildren(ScreenArea area) {
        if(!this.spawnScrollBar)
            return;
        this.addChild(VerticalScrollBar.builder(this)
                .rightOf(this)
                .visible(this::isVisible)
                .build());
    }

    @Override
    protected void afterChildRemoved(Object child) {
        if(this.children.removeIf(c -> c.child == child)) {
            //Recalculate the total height of all children owned by this widget
            int lowestPoint = 0;
            for(Child c : this.children)
                lowestPoint = Math.max(lowestPoint,c.relativePosition.y + c.child.getHeight());
            this.childHeight = lowestPoint;
            this.scrollableHeight = Math.max(0,this.childHeight - this.height + this.bottomSpacing);
        }
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        ScreenArea area = this.getArea();
        ScreenPosition corner;
        if(this.centerVertically && this.childHeight < this.height) {
            int space = this.height - this.childHeight;
            corner = this.getPosition().offset(0,space / 2);
        }
        else
            corner = this.getPosition().offset(0,Math.round(this.scroll) * -1);
        for(Child child : this.children)
            child.renderTick(area,corner);
    }

    @Override
    public <T extends FancyRenderable> T addChild(ScreenPosition position, T child) {
        this.children.add(new Child(position,child));
        this.childHeight = Math.max(this.childHeight,position.y + child.getHeight());
        this.scrollableHeight = Math.max(this.childHeight - this.height + this.bottomSpacing,0);
        return this.addChild(child);
    }

    @Override
    public <T> T addUnpositionedChild(T child) { return this.addChild(child); }

    @Override
    public int getScroll() { return Math.round(this.scroll); }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return this.scrollableHeight; }
    @Override
    public IScrollListener buildScrollListener() { return this; }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        //Enable the scissor area
        gui.enableScissor(area.atPosition(0,0));
        //Render all child widgets
        FancyWidget.renderWidgetsInArea(gui,area,this.children.stream().map(Child::child).toList(),true);
        //Disable the scissor area
        gui.disableScissor();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    @Override
    public boolean onMouseScrolled(int mouseX, int mouseY, double deltaX, double deltaY) {
        if(this.getArea().isInArea(mouseX,mouseY))
            return this.handleScrolling(deltaY);
        return false;
    }

    public boolean handleScrolling(double deltaY) {
        if(deltaY != 0d && this.scrollableHeight > 0) {
            this.scroll = IScrollable.handlePreciseScrolling(this.scroll,this.scrollableHeight,(float)deltaY);
            return true;
        }
        return false;
    }

    public static final class Builder extends FlexibleSizeBuilder<Builder,WidgetScrollingArea> {

        private Builder() { super(100,100); }

        @Override
        protected Builder getSelf() { return this; }

        private boolean createScrollBar = true;
        private boolean centerVertically = false;
        private int bottomSpacing = 0;
        private WidgetScrollingArea old = null;

        public Builder withoutScrollBar() { this.createScrollBar = false; return this; }
        public Builder verticallyCentered() { return this.verticallyCentered(true); }
        public Builder verticallyCentered(boolean verticallyCentered) { this.centerVertically = verticallyCentered; return this; }

        public Builder withBottomPadding(int bottomPadding) { this.bottomSpacing = bottomPadding; return this; }

        public Builder withOldWidget(@Nullable WidgetScrollingArea old) { this.old = old; return this; }

        @Override
        public WidgetScrollingArea build() { return new WidgetScrollingArea(this); }

    }

    private record Child(ScreenPosition relativePosition,FancyRenderable child) {
        private void renderTick(ScreenArea scissorArea,ScreenPosition startPos) {
            this.child.setPosition(startPos.offset(this.relativePosition));
            this.child.setScissorArea(scissorArea);
        }
    }

}
