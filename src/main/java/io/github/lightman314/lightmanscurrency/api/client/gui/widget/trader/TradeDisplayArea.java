package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
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
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerMenu;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.*;

public class TradeDisplayArea extends AbstractMultiWidget.LateChildren implements IScrollable {

    public static final int LINE_SPACING = 4;
    public static final int TITLE_CUTOFF = 10;
    public static final int TITLE_HEIGHT = 12;

    private final Map<Long,TraderSection> sections = new HashMap<>();
    private final List<TraderSection> sectionsInOrder = new ArrayList<>();

    private final TraderSource traderSource;
    private final boolean showOwnerInTitle;
    @Nullable
    private final ITradeInteractionHandler interactionHandler;
    private final Consumer3<TraderData,TradingNode<?>,TradeData> onPress;
    private final ContextBuilder contextBuilder;
    private final BiPredicate<TradeData,TradeContext> filter;
    private final BooleanSupplier showSearchBox;

    private float scroll = 0f;
    private int scrollableHeight = 0;
    private String lastSearch = "";

    private TextBoxWrapper<String> searchBox = null;
    private SpriteButton addTradeButton = null;
    private SpriteButton removeTradeButton = null;
    private TradingNode<?> relevantNode = null;
    private boolean searchBoxVisibleThisFrame = false;
    private boolean addRemoveVisibleThisFrame = false;

    protected TradeDisplayArea(Builder builder) {
        super(builder);
        this.traderSource = builder.trader;
        this.showOwnerInTitle = builder.showOwnerInTitle;
        this.onPress = builder.onPress;
        this.interactionHandler = builder.interactionHandler;
        this.contextBuilder = builder.contextBuilder;
        this.filter = builder.filter;
        this.showSearchBox = builder.searchBox;
        if(builder.oldWidget != null) {
            this.lastSearch = builder.oldWidget.lastSearch;
            this.searchBox = builder.oldWidget.searchBox;
        }
    }

    @Override
    protected void addLateChildren(ScreenArea area) {
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width,TITLE_CUTOFF))
                .ofHeight(area.height - TITLE_CUTOFF)
                .build());
        this.addChild(ScrollArea.builder()
                .ofArea(area)
                .withListener(this.buildScrollListener())
                .build());
        //Search Box
        this.searchBox = this.addChild(TextBoxWrapper.stringBuilder()
                .withOldWidget(this.searchBox)
                .atPos(area.pos.offset(area.width - 90,0).offset(2,0))
                .ofSize(80,10)
                .noBorder()
                .visible(() -> this.searchBoxVisibleThisFrame)
                .withHandler(s -> this.lastSearch = s)
                .build());
        //Add/Remove trade buttons
        this.addTradeButton = this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(area.width - 130,-1))
                .withSprite(LCSprites.SIGN_PLUS)
                .onPress(this::tryAddTrade)
                .active(this::canAddTrade)
                .visible(() -> this.addRemoveVisibleThisFrame)
                .build());
        this.removeTradeButton = this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(area.width - 110,-1))
                .withSprite(LCSprites.SIGN_MINUS)
                .onPress(this::tryRemoveTrade)
                .active(this::canRemoveTrade)
                .visible(() -> this.addRemoveVisibleThisFrame)
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
        ScreenArea widgetArea = this.getArea().lowered(TITLE_CUTOFF);
        this.relevantNode = null;
        for(TraderData trader : this.traderSource.getTraders())
        {
            long id = trader.getID();
            //Ignore duplicate traders
            if(foundSections.contains(id))
                continue;
            foundSections.add(id);
            TraderSection section = this.sections.computeIfAbsent(id,i -> new TraderSection(trader,this));
            this.sectionsInOrder.add(section);
            TradingNode<?> temp = section.prepare(trader,yPos,this::tradeVisible,mousePos,widgetArea);
            if(temp != null)
                this.relevantNode = temp;
            totalHeight += section.height;
            yPos += section.height;
        }
        //Clear unused sections
        for(long key : this.sections.keySet().stream().filter(key -> !foundSections.contains(key)).toList())
        {
            this.sections.get(key).clear();
            this.sections.remove(key);
        }
        //Remove the last spacer from the scrollable height
        this.scrollableHeight = Math.max(0,totalHeight - this.getHeight() - LINE_SPACING);
        //Validate the scroll value
        if(this.scroll > this.scrollableHeight)
            this.scroll = this.scrollableHeight;
        //Reposition the add/remove buttons depending on the search box's availability.
        this.searchBoxVisibleThisFrame = this.showSearchBox.getAsBoolean();
        this.addRemoveVisibleThisFrame = this.relevantNode != null && this.interactionHandler != null && this.interactionHandler.allowQuantityEdit(this.relevantNode);
        if(this.addRemoveVisibleThisFrame && this.addTradeButton != null && this.removeTradeButton != null) {
            int startX = widgetArea.right() - 20;
            if(this.searchBoxVisibleThisFrame)
                startX -= 92;
            this.addTradeButton.setX(startX);
            this.removeTradeButton.setX(startX + 10);
        }
    }

    private boolean tradeVisible(TradeData trade,TradeContext context) {
        if(!this.filter.test(trade,context))
            return false;
        return !this.showSearchBox.getAsBoolean() || this.lastSearch.isBlank() || LCApi.getTraderAPI().filterTrade(trade,this.lastSearch,context.getTransaction());
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
        boolean checkForTitle = title == null;
        //Offset scissor slightly below the actual top to leave space for the title
        ScreenArea widgetArea = area.lowered(TITLE_CUTOFF);
        gui.enableScissor(widgetArea.atPosition(0,TITLE_CUTOFF));
        gui.push(widgetArea.pos);
        for(TraderSection section : new ArrayList<>(this.sectionsInOrder))
        {
            Component t = section.extractRenderState(gui,yPos,widgetArea);
            //Collect the title for this section unless the yPos is below the actual title area
            if(checkForTitle && yPos < 0)
                title = t;
            yPos += section.height;
        }
        //Disable the scissor now, as the title will be drawn above
        gui.disableScissor();
        gui.pop();
        if(title == null && !this.sectionsInOrder.isEmpty())
            title = this.sectionsInOrder.getFirst().title;

        int titleWidth = area.width;
        //Render the search box BG
        if(this.searchBoxVisibleThisFrame){
            gui.blitSprite(LCSprites.SEARCH_FIELD,area.width - 90,-2,90);
            titleWidth -= 92;
        }
        if(this.addRemoveVisibleThisFrame)
            titleWidth -= 22;
        //Render the overall title
        if(title != null)
        {
            gui.textWithScrollingOverflow(title,0,0,titleWidth,0xFF404040,false);
            //Render tooltip if title is hovered
            if(ScreenArea.of(area.pos,this.width,9).isInArea(gui.getMousePos()))
                gui.renderTooltipAtMouse(title);
        }

    }

    private boolean canAddTrade() {
        return this.relevantNode != null && this.interactionHandler != null && this.interactionHandler.canAddTrade(this.relevantNode);
    }

    private boolean canRemoveTrade() {
        return this.relevantNode != null && this.interactionHandler != null && this.interactionHandler.canRemoveTrade(this.relevantNode);
    }

    private void tryAddTrade() {
        if(this.relevantNode != null && this.interactionHandler != null)
            this.interactionHandler.addTrade(this.relevantNode);
    }
    private void tryRemoveTrade() {
        if(this.relevantNode != null && this.interactionHandler != null)
            this.interactionHandler.removeTrade(this.relevantNode);
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
                this.scroll = IScrollable.handlePreciseScrolling(this.scroll,this.scrollableHeight,(float)dy);
                return true;
            }
            return false;
        };
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends FlexibleSizeBuilder<Builder,TradeDisplayArea> {

        private Builder() { super(100,100); }

        @Nullable
        private TradeDisplayArea oldWidget = null;

        private TraderSource trader = TraderSource.EMPTY;
        private boolean showOwnerInTitle = true;

        private Consumer3<TraderData,TradingNode<?>,TradeData> onPress = (t,n,d) -> {};
        @Nullable
        private ITradeInteractionHandler interactionHandler = null;
        private BooleanSupplier searchBox = () -> false;

        private ContextBuilder contextBuilder = ContextBuilder.NULL;

        private BiPredicate<TradeData,TradeContext> filter = (t,c) -> true;

        public Builder withOldWidget(@Nullable TradeDisplayArea oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder onPress(Consumer<TradeIndexes> action) { return this.onPress((trader,node,trade) -> TradeIndexes.lookup(this.trader,trader,node,trade).ifPresent(action)); }
        public Builder onPress(Consumer3<TraderData,TradingNode<?>,TradeData> action) { this.onPress = action; return this; }

        public Builder withInteractionHandler(ITradeInteractionHandler interactionHandler) { this.interactionHandler = interactionHandler; return this; }

        public Builder forTrader(TraderSource trader) { this.trader = trader; return this; }
        public Builder hideTraderOwner() { this.showOwnerInTitle = false; return this; }
        public Builder withContext(ContextBuilder builder) { this.contextBuilder = builder; return this; }
        public Builder withFilter(Predicate<TradeData> filter) { return this.withFilter((t,c) -> filter.test(t)); }
        public Builder withFilter(BiPredicate<TradeData,TradeContext> filter) { this.filter = filter; return this; }
        public Builder withSearchBox(BooleanSupplier searchBox) { this.searchBox = searchBox; return this; }

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

        @Nullable
        public TradingNode<?> prepare(TraderData trader,int startY,BiPredicate<TradeData,TradeContext> filter,ScreenPosition mousePos,ScreenArea widgetArea) {
            this.trader = trader;
            this.height = TITLE_HEIGHT;
            int yPos = startY + TITLE_HEIGHT;
            Component name = IDisplayNode.getTraderName(trader);
            if(this.parent.showOwnerInTitle)
                this.title = TraderCustomerMenu.GUI_TRADER_TITLE.get(name,trader.getOwner().getName());
            else
                this.title = name;
            List<TradingNode<?>> nodes = this.trader.getTradingNodes();
            TradingNode<?> relevantNode = null;
            TradeContext.Builder contextBuilder = this.parent.contextBuilder.buildTradeContext(trader);
            boolean anyNodeHeight = false;
            try(TradeContext context = contextBuilder.build()) {
                final int width = this.parent.getWidth();
                List<TradingNode<?>> tradingNodes = this.trader.getTradingNodes();
                this.showSectionTitles = tradingNodes.size() > 1;
                int relevantHeight = this.parent.getY() + TITLE_CUTOFF + (this.showSectionTitles ? 0 : TITLE_HEIGHT);
                ListHelper.forceListSize(this.sections,tradingNodes.size(),() -> new NodeSection(this), NodeSection::clear);
                for(int i = 0; i < tradingNodes.size(); ++i) {
                    TradingNode<?> node = tradingNodes.get(i);
                    NodeSection section = this.sections.get(i);
                    section.prepare(node,yPos,width,filter,context,mousePos,widgetArea);
                    if(yPos < relevantHeight)
                        relevantNode = node;
                    yPos += section.height;
                    this.height += section.height;
                    if(section.height > 0)
                        anyNodeHeight = true;
                }
            }
            if(!anyNodeHeight)
                this.height = 0;
            return relevantNode;
        }

        public Component extractRenderState(FancyGuiExtractor gui, int startY, ScreenArea area) {
            //If no trades are visible for this section, don't render anything
            if(this.height == 0)
                return null;
            int yPos = startY;
            if(ScreenArea.overlaps(0,area.height,yPos,yPos + 9))
            {
                gui.textWithScrollingOverflow(this.title,0,yPos,area.width,0xFF404040,false);
                //Render title tooltip if it's hovered
                if(ScreenArea.of(0,yPos,area.width,9).isInArea(gui.getMousePos().relativeTo(area.pos)))
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
            public boolean hasAddRemoveButtons = false;
            private Pair<SpriteButton,SpriteButton> addRemoveButtons = null;

            private NodeSection(TraderSection parent) {
                this.parent = parent;
            }

            public void prepare(TradingNode<?> node, int startY, int width, BiPredicate<TradeData,TradeContext> tradeFilter, TradeContext context, ScreenPosition mousePos, ScreenArea widgetArea) {
                this.node = node;
                this.height = 0;

                int x = this.parent.parent.getX();
                int yPos = startY;
                this.hasAddRemoveButtons = this.getInteractionValue(ITradeInteractionHandler::allowQuantityEdit);
                if(this.parent.showSectionTitles)
                {
                    this.title = node.getSetLabel();
                    this.height += TITLE_HEIGHT;
                    this.updateAddRemoveButtons(x,width,yPos);
                    yPos += TITLE_HEIGHT;
                }
                else
                    this.hasAddRemoveButtons = false;
                //Cache the trade button data (which will also calculate the buttons size)
                this.cacheTradeButtonData(node,context,mousePos,widgetArea);
                //Position the buttons
                int pendingWidth = 0;

                List<TradeButton> line = new ArrayList<>();
                this.visibleButtons.clear();
                for(ButtonEntry entry : this.buttons)
                {
                    if(tradeFilter.test(entry.trade,context))
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
                if(this.visibleButtons.isEmpty())
                    this.height = 0;
            }

            private void updateAddRemoveButtons(int x,int width,int yPos) {
                if(this.hasAddRemoveButtons) {
                    if(this.addRemoveButtons == null) {
                        SpriteButton add = this.addChild(SpriteButton.builder()
                                .withSprite(LCSprites.SIGN_PLUS)
                                .onPress(this::tryAddTrade)
                                .active(this::canAddTrade)
                                .visible(() -> this.hasAddRemoveButtons)
                                .build());
                        SpriteButton remove = this.addChild(SpriteButton.builder()
                                .withSprite(LCSprites.SIGN_MINUS)
                                .onPress(this::tryRemoveTrade)
                                .active(this::canRemoveTrade)
                                .visible(() -> this.hasAddRemoveButtons)
                                .build());
                        this.addRemoveButtons = Pair.of(add,remove);
                    }
                    //Position the buttons
                    SpriteButton add = this.addRemoveButtons.getFirst();
                    SpriteButton remove = this.addRemoveButtons.getSecond();
                    int y = this.parent.showSectionTitles ? yPos : yPos - 10;
                    add.setPosition(x + width - 20,y);
                    remove.setPosition(x + width - 10,y);
                }
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

            private void cacheTradeButtonData(TradingNode<?> node,TradeContext context,ScreenPosition mousePos,ScreenArea widgetArea) {
                List<? extends TradeData> trades = node.getTrades();
                ListHelper.forceListSize(this.buttons,trades.size(),() -> this.createEntry(widgetArea),this::removeEntry);
                for(int i = 0; i < trades.size(); ++i)
                {
                    ButtonEntry entry = this.buttons.get(i);
                    TradeData trade = trades.get(i);
                    entry.trade = trade;
                    entry.button.setScissorArea(widgetArea);
                    entry.button.cacheResults(trade,context,mousePos);
                }
            }

            private ButtonEntry createEntry(ScreenArea widgetArea) {
                ButtonEntry entry = new ButtonEntry();
                entry.button = this.addChild(TradeButton.builder()
                        .onPress(() -> this.parent.parent.onPress.accept(this.parent.trader,this.node,entry.trade))
                        .withInteractionHandler(this.parent.parent.interactionHandler)
                        .withExternalControl().build());
                return entry;
            }

            public void extractRenderState(FancyGuiExtractor gui, ScreenArea area, int startY) {
                //Render the Title
                if(this.title != null && ScreenArea.overlaps(0,area.height,startY,startY + 9))
                    gui.centeredText(this.title,area.centerX(),startY,0xFF404040,false);
                //Render the buttons
                FancyWidget.renderWidgetsInArea(gui,area,this.visibleButtons,true);
            }

            private boolean getInteractionValue(BiFunction<ITradeInteractionHandler,TradingNode<?>,Boolean> getter) {
                ITradeInteractionHandler handler = this.parent.parent.interactionHandler;
                return handler != null && getter.apply(handler,this.node);
            }

            private boolean canAddTrade() { return this.getInteractionValue(ITradeInteractionHandler::canAddTrade); }

            private boolean canRemoveTrade() { return this.getInteractionValue(ITradeInteractionHandler::canRemoveTrade); }

            private void performInteraction(BiConsumer<ITradeInteractionHandler,TradingNode<?>> action) {
                ITradeInteractionHandler handler = this.parent.parent.interactionHandler;
                if(handler != null)
                    action.accept(handler,this.node);
            }

            private void tryAddTrade() { this.performInteraction(ITradeInteractionHandler::addTrade); }

            private void tryRemoveTrade() { this.performInteraction(ITradeInteractionHandler::removeTrade); }

            private void clear() {
                this.buttons.forEach(this::removeEntry);
                this.buttons.clear();
                if(this.addRemoveButtons != null) {
                    this.removeChild(this.addRemoveButtons.getFirst());
                    this.removeChild(this.addRemoveButtons.getSecond());
                    this.addRemoveButtons = null;
                }
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