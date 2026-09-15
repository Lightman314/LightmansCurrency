package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.ArrayList;
import java.util.List;

public class WidgetScrollingArea extends AbstractMultiWidget implements IPositionalWidgetHolder, IScrollable, IScrollListener {

    private final List<Child> children = new ArrayList<>();
    private float scroll = 0f;
    private int scrollableHeight = 0;

    public WidgetScrollingArea(Builder builder) { super(builder); }

    @Override
    protected void addEarlyChildren(ScreenArea area) { }

    @Override
    protected void addLateChildren(ScreenArea area) {
        this.addChild(VerticalScrollBar.builder(this)
                .rightOf(this)
                .visible(this::isVisible)
                .build());
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        ScreenArea area = this.getArea();
        ScreenPosition corner = this.getPosition().offset(0,Math.round(this.scroll) * -1);
        for(Child child : this.children)
            child.renderTick(area,corner);
    }

    @Override
    public <T extends FancyWidget> T addChild(ScreenPosition position,T child) {
        this.children.add(new Child(position,child));
        this.scrollableHeight = Math.max(position.y + child.getHeight() - this.height,this.scrollableHeight);
        return this.addChild(child);
    }

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
        if(this.isMouseOver(mouseX,mouseY) && deltaY != 0f) {
            this.scroll = Math.clamp(this.scroll - ((float)deltaY * LCConfig.CLIENT.scrollMultiplier.get()),0f,(float)this.getMaxScroll());
            return true;
        }
        return false;
    }

    public static final class Builder extends FlexibleSizeBuilder<Builder,WidgetScrollingArea> {

        private Builder() { super(100,100); }

        @Override
        public WidgetScrollingArea build() { return new WidgetScrollingArea(this); }

        @Override
        protected Builder getSelf() { return this; }
    }

    private record Child(ScreenPosition relativePosition,FancyWidget child) {
        private void renderTick(ScreenArea scissorArea,ScreenPosition startPos) {
            this.child.setPosition(startPos.offset(this.relativePosition));
            this.child.setScissorArea(scissorArea);
        }
    }

}
