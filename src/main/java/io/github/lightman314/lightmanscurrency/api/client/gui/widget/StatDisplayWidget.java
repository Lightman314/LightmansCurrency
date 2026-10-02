package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatViewer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class StatDisplayWidget extends FancyWidget implements IScrollable, IScrollListener {

    private static final int SPACING = 12;

    private float scroll = 0f;
    private int scrollableHeight = 0;

    private final Supplier<StatViewer> statSource;

    protected StatDisplayWidget(Builder builder) {
        super(builder);
        this.statSource = builder.statViewer;
        if(builder.oldWidget != null) {
            this.scroll = builder.oldWidget.scroll;
            this.scrollableHeight = builder.oldWidget.scrollableHeight;
        }
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {

        StatViewer stats = this.statSource.get();

        boolean inside = area.isInArea(gui.getMousePos());
        int mouseX = gui.getMousePos().x - area.x;
        int mouseY = gui.getMousePos().y - area.y;

        List<StatKey<?,?>> allKeys = stats == null ? List.of() : stats.getSortedKeys();
        //Recalculate the scrollable height based on the number of stats that should be displayed
        this.scrollableHeight = Math.max((allKeys.size() * SPACING) - 2 - area.height,0);
        //Validate the current scroll to ensure everything is rendered at the correct position
        this.validateScroll();
        //If no stats are present, render the empty stats text instead
        if(allKeys.isEmpty() || stats == null) {
            List<FormattedCharSequence> lines = gui.getFont().split(StatViewer.GUI_STATS_EMPTY.get(),area.width);
            int centerX = area.halfWidth();
            int centerY = area.halfHeight();
            int yPos = centerY - (lines.size() * (SPACING / 2));
            for(FormattedCharSequence line : lines) {
                gui.centeredText(line,centerX,yPos,0xFF404040,false);
                yPos += SPACING;
            }
            return;
        }

        //Enable the scissor area
        gui.enableScissor(area.atPosition(0,0));
        int yPos = Math.round(this.scroll) * -1;
        for(StatKey<?,?> key : allKeys) {
            int bottom = yPos + SPACING;
            if(bottom <= 0) {
                yPos += SPACING;
                continue;
            }
            //Label
            Component label = key.getLabel();
            int labelWidth = gui.getFont().width(label);
            gui.text(label,0,yPos,0xFF404040,false);
            //Value Text
            Component value = stats.getStatValueText(key);
            gui.textWithScrollingOverflow(value,labelWidth,yPos,area.width - labelWidth,0xFF404040,false);
            //Check for tooltips
            if(inside && mouseY >= yPos && mouseY < bottom) {
                List<Component> tooltip;
                if(mouseX < labelWidth)
                    tooltip = key.getTooltip();
                else
                    tooltip = stats.getStatValueTooltip(key);
                if(tooltip != null)
                    gui.renderTooltipAtMouse(tooltip);
            }
            //Increment the render position and check if we should cut the loop off early
            yPos += SPACING;
            if(yPos >= area.height)
                return;
        }
        //Clear the scissor now that we're done rendering
        gui.disableScissor();
    }

    @Override
    @Nullable
    protected List<Component> collectTooltips(ScreenPosition mousePos) { return null; }

    @Override
    public int getScroll() { return Math.round(this.scroll); }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return this.scrollableHeight; }

    public static Builder builder() { return new Builder(); }

    @Override
    public boolean onMouseScrolled(int mouseX,int mouseY,double deltaX,double deltaY) {
        if(this.getArea().isInArea(mouseX,mouseY) && this.isVisible()) {
            this.scroll = IScrollable.handlePreciseScrolling(this.scroll,this.scrollableHeight,(float)deltaY);
            return true;
        }
        return false;
    }

    public static final class Builder extends FlexibleSizeBuilder<Builder,StatDisplayWidget> {

        private Builder() { super(100,100); }

        private Supplier<StatViewer> statViewer = () -> null;
        @Nullable
        private StatDisplayWidget oldWidget = null;

        public Builder forStats(StatViewer statViewer) { return this.forStats(() -> statViewer); }
        public Builder forStats(Supplier<StatViewer> statViewer) { this.statViewer = statViewer; return this; }

        public Builder withOldWidget(@Nullable StatDisplayWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public StatDisplayWidget build() { return new StatDisplayWidget(this); }

    }

}
