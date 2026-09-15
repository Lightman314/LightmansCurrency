package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.system.Consumer3;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.ContextBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeIndexes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TradeDisplayArea extends AbstractMultiWidget implements IScrollable {

    public static final int LINE_SPACING = 4;
    public static final int TITLE_CUTOFF = 10;
    public static final int TITLE_HEIGHT = 12;

    private final Map<Long,TraderSection> sections = new HashMap<>();
    private final List<TraderSection> sectionsInOrder = new ArrayList<>();

    private final TraderSource traderSource;
    private final ITradeInteractionHandler interactionHandler;
    private final Consumer3<TraderData,TradingNode<?>,TradeData> onPress;
    private final ContextBuilder contextBuilder;
    private final Predicate<TradeData> filter;

    private float scroll = 0f;
    private int scrollableHeight = 0;

    protected TradeDisplayArea(Builder builder) {
        super(builder);
        this.traderSource = builder.trader;
        this.onPress = builder.onPress;
        this.interactionHandler = builder.interactionHandler;
        this.contextBuilder = builder.contextBuilder;
        this.filter = builder.filter;
    }

    @Override
    protected void addEarlyChildren(ScreenArea area) { }
    @Override
    protected void addLateChildren(ScreenArea area) {
        this.addChild(VerticalScrollBar.builder(this)
                .rightOf(this)
                .build());
    }

    private void updateSectionCache(ScreenPosition mousePos) {
        this.sectionsInOrder.clear();
        Set<Long> foundSections = new HashSet<>();
        int yPos = this.getY() - Math.round(this.scroll);
        int totalHeight = 0;
        if(this.traderSource.getNameOverride() != null)
        {
            yPos += TITLE_HEIGHT;
            totalHeight += TITLE_HEIGHT;
        }
        for(TraderData trader : this.traderSource.getTraders())
        {
            long id = trader.getID();
            //Ignore duplicate traders
            if(foundSections.contains(id))
                continue;
            foundSections.add(id);
            TraderSection section = this.sections.computeIfAbsent(id,i -> new TraderSection(trader,this));
            this.sectionsInOrder.add(section);
            section.prepare(trader,yPos,this::tradeVisible,mousePos);
            totalHeight += section.height;
        }
        //Clear unused sections
        for(long key : this.sections.keySet().stream().filter(key -> !foundSections.contains(key)).toList())
        {
            this.sections.get(key).clear();
            this.sections.remove(key);
        }
        this.scrollableHeight = Math.max(0,totalHeight - this.getHeight());
    }

    private boolean tradeVisible(TradeData trade) {
        if(!this.filter.test(trade))
            return false;
        //TODO have search functionality
        return true;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        //Collect trades and cache them for all future rendering shenanigans
        this.updateSectionCache(mousePos);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        int yPos = (Math.round(this.scroll) * -1) - TITLE_CUTOFF;
        Component title = this.traderSource.getNameOverride();
        //Offset scissor slightly below the actual top to leave space for the title
        ScreenArea widgetArea = area.lowered(TITLE_CUTOFF);
        gui.enableScissor(widgetArea.atPosition(0,0));
        gui.push(widgetArea.pos);
        for(TraderSection section : new ArrayList<>(this.sectionsInOrder))
        {
            Component t = section.extractRenderState(gui,yPos,widgetArea);
            //Collect the title for this section
            if(title == null && yPos >= 0)
                title = t;
            yPos += section.height;
        }
        gui.pop();
        if(title == null && !this.sectionsInOrder.isEmpty())
            title = this.sectionsInOrder.getFirst().title;
        //Render the overall title
        if(title != null)
        {
            gui.text(gui.fitTextToWidth(title,area.width),0,0,0xFF404040,false);
            //Render tooltip if title is hovered
            if(ScreenArea.of(area.pos,this.width,9).isInArea(gui.getMousePos()))
                gui.renderTooltipAtMouse(title);
        }
        gui.disableScissor();
    }

    @Override
    public int getScroll() { return Math.round(this.scroll); }

    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }

    @Override
    public int getMaxScroll() { return this.scrollableHeight; }

    @Override
    public IScrollListener buildScrollListener() {
        return (x,y,dx,dy) -> {
            if(dy != 0f)
            {
                this.scroll = Math.clamp(this.scroll - ((float)dy * LCConfig.CLIENT.scrollMultiplier.get()),0f,(float)this.getMaxScroll());
                return true;
            }
            return false;
        };
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends FlexibleSizeBuilder<Builder,TradeDisplayArea> {

        private Builder() { super(100,100); }

        @Nullable
        private TraderSource trader = TraderSource.EMPTY;

        private Consumer3<TraderData,TradingNode<?>,TradeData> onPress = (t,n,d) -> {};
        @Nullable
        private ITradeInteractionHandler interactionHandler = null;

        private ContextBuilder contextBuilder = ContextBuilder.NULL;

        private Predicate<TradeData> filter = Predicates.alwaysTrue();

        public Builder onPress(Consumer<TradeIndexes> action) { return this.onPress((trader,node,trade) -> TradeIndexes.lookup(this.trader,trader,node,trade).ifPresent(action)); }
        public Builder onPress(Consumer3<TraderData,TradingNode<?>,TradeData> action) { this.onPress = action; return this; }

        public Builder withInteractionHandler(ITradeInteractionHandler interactionHandler) { this.interactionHandler = interactionHandler; return this; }

        public Builder forTrader(TraderSource trader) { this.trader = trader; return this; }
        public Builder withContext(ContextBuilder builder) { this.contextBuilder = builder; return this; }
        public Builder withFilter(Predicate<TradeData> filter) { this.filter = filter; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TradeDisplayArea build() { return new TradeDisplayArea(this); }

    }

    private static class TraderSection implements IWidgetHolder {

        public final List<NodeSection> sections = new ArrayList<>();
        public int height = 0;

        private TraderData trader;
        private Component title = Component.empty();
        private final TradeDisplayArea parent;
        private boolean showSectionTitles;

        private TraderSection(TraderData trader,TradeDisplayArea parent) {
            this.parent = parent;
            this.trader = trader;
        }

        public <T> T addChild(T button) { return this.parent.addChild(button); }
        public void removeChild(Object button) { this.parent.removeChild(button); }
        public void removeAllChildren() { this.parent.removeAllChildren(); }

        public void prepare(TraderData trader,int startY,Predicate<TradeData> filter,ScreenPosition mousePos) {
            this.trader = trader;
            this.height = TITLE_HEIGHT;
            startY += TITLE_HEIGHT;
            this.title = IDisplayNode.getTraderName(trader);
            List<TradingNode<?>> nodes = this.trader.getTradingNodes();
            TradeContext.Builder contextBuilder = this.parent.contextBuilder.buildTradeContext(trader);
            if(contextBuilder != null)
            {
                try(TradeContext context = contextBuilder.build()) {
                    final int width = this.parent.getWidth();
                    List<TradingNode<?>> tradingNodes = this.trader.getTradingNodes();
                    this.showSectionTitles = tradingNodes.size() > 1;
                    ListHelper.forceListSize(this.sections,tradingNodes.size(),() -> new NodeSection(this), NodeSection::clear);
                    int yPos = startY + TITLE_HEIGHT;
                    for(int i = 0; i < tradingNodes.size(); ++i) {
                        TradingNode<?> node = tradingNodes.get(i);
                        NodeSection section = this.sections.get(i);
                        section.prepare(node,startY,width,filter,context,mousePos);
                        yPos += section.height;
                    }
                }
            }
        }

        public Component extractRenderState(FancyGuiExtractor gui, int startY, ScreenArea area) {
            int yPos = startY;
            if(ScreenArea.overlaps(0,area.height,yPos,yPos + 9))
            {
                gui.text(gui.fitTextToWidth(this.title,area.width),0,yPos,0xFF404040,false);
                //Render title tooltip if it's hovered
                if(ScreenArea.of(0,yPos,area.width,9).isInArea(gui.getMousePos()))
                    gui.renderTooltipAtMouse(this.title);
            }
            for(NodeSection section : this.sections)
            {
                section.extractRenderState(gui,area,yPos);
                yPos += section.height;
            }
            //Return the title if the yPos is positive at the bottom of this section
            if(yPos + this.height > 0)
                return this.title;
            return null;
        }

        public void clear() {
            for(NodeSection section : this.sections)
                section.clear();
        }

        private static class NodeSection implements IWidgetHolder {

            private final TraderSection parent;
            private TradingNode<?> node;
            private Component title = null;
            private final List<ButtonEntry> buttons = new ArrayList<>();
            private final List<TradeButton> visibleButtons = new ArrayList<>();
            public int height;

            private NodeSection(TraderSection parent) {
                this.parent = parent;
            }

            public void prepare(TradingNode<?> node,int startY,int width,Predicate<TradeData> tradeFilter,TradeContext context,ScreenPosition mousePos) {
                this.node = node;
                this.height = 0;
                int yPos = startY;
                if(this.parent.showSectionTitles)
                {
                    this.title = node.getSetLabel();
                    this.height += TITLE_HEIGHT;
                    yPos += TITLE_HEIGHT;
                }
                //Cache the trade button data (which will also calculate the buttons size)
                this.cacheTradeButtonData(node,context,mousePos);
                //Position the buttons
                int pendingWidth = 0;
                int x = this.parent.parent.getX();
                List<TradeButton> line = new ArrayList<>();
                this.visibleButtons.clear();
                for(ButtonEntry entry : this.buttons)
                {
                    if(tradeFilter.test(entry.trade))
                    {
                        int entryWidth = entry.getWidth();
                        //LightmansCurrency.LogDebug("Button has width of " + entryWidth + ". Total width is " + width);
                        if(pendingWidth + entryWidth >= width)
                        {
                            if(pendingWidth <= 0)
                            {
                                line.add(entry.button);
                                this.visibleButtons.add(entry.button);
                                yPos = this.positionLine(line,x,yPos,entryWidth,width);
                                //Skip to the next loop since we already processed adding this entry
                                continue;
                            }
                            else
                            {
                                yPos = this.positionLine(line,x,yPos,pendingWidth,width);
                                pendingWidth = 0;
                            }
                        }
                        //Add button to the line
                        line.add(entry.button);
                        this.visibleButtons.add(entry.button);
                        pendingWidth += entryWidth;
                    }
                    else //Hide the button, and don't bother moving it
                        entry.hide();
                }
                if(!line.isEmpty())
                    this.positionLine(line,x,yPos,pendingWidth,width);
            }

            private int positionLine(List<TradeButton> line,int x,int yPos,int buttonWidth,int availableWidth) {
                this.height += TradeButton.HEIGHT + LINE_SPACING;
                int emptySpace = availableWidth - buttonWidth;
                int xOffset = 0;
                int spacing = 0;
                if(line.size() == 1) {
                    xOffset = emptySpace / 2;
                } else if(line.size() > 1){
                    //Spacing between each button should be the "empty space" divided by the button count - 1
                    spacing = emptySpace / (line.size() - 1);
                }
                for(TradeButton button : line) {
                    button.setPosition(x + xOffset,yPos);
                    xOffset += button.getWidth() + spacing;
                }
                //Clear the line cache
                line.clear();
                return yPos + TradeButton.HEIGHT + LINE_SPACING;
            }

            private void cacheTradeButtonData(TradingNode<?> node,TradeContext context,ScreenPosition mousePos) {
                List<? extends TradeData> trades = node.getTrades();
                ListHelper.forceListSize(this.buttons,trades.size(),this::createEntry,this::removeEntry);
                for(int i = 0; i < trades.size(); ++i)
                {
                    ButtonEntry entry = this.buttons.get(i);
                    TradeData trade = trades.get(i);
                    entry.trade = trade;
                    entry.button.cacheResults(trade,context,mousePos);
                }
            }

            private ButtonEntry createEntry() {
                ButtonEntry entry = new ButtonEntry();
                entry.button = this.addChild(TradeButton.builder()
                        .onPress(() -> this.parent.parent.onPress.accept(this.parent.trader,this.node,entry.trade))
                        .withInteractionHandler(this.parent.parent.interactionHandler)
                        .withExternalControl().build());
                entry.button.setScissorArea(this.parent.parent.getArea());
                return entry;
            }

            public void extractRenderState(FancyGuiExtractor gui, ScreenArea area, int startY) {
                //Render the Title
                if(this.title != null && ScreenArea.overlaps(0,area.height,startY,startY + 9))
                    gui.centeredText(this.title,area.centerX(),startY,0xFF404040,false);
                //Render the buttons
                FancyWidget.renderWidgetsInArea(gui,area,this.visibleButtons,true);
            }

            private void clear() {
                this.buttons.forEach(this::removeEntry);
                this.buttons.clear();
            }

            @Override
            public <T> T addChild(T child) { return this.parent.addChild(child); }
            @Override
            public void removeChild(Object child) { this.parent.removeChild(child); }
            @Override
            public void removeAllChildren() { this.parent.removeAllChildren(); }
            private void removeEntry(ButtonEntry entry) { this.removeChild(entry.button); }

            private static class ButtonEntry {
                public TradeButton button;
                public TradeData trade;

                public int getWidth() { return this.button.getWidth(); }
                public void hide() { this.button.visible = false; }
            }

        }

    }

}