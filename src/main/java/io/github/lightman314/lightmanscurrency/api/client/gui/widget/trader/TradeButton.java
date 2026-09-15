package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IGhostSlotProvider;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TradeButton extends FancyButton implements IGhostSlotProvider, IScrollListener {

    public static final int HEIGHT = 18;

    private final Supplier<TradeData> trade;
    private final Supplier<TradeContext.Builder> context;
    private final ITradeInteractionHandler interactions;
    private final boolean externalControl;
    protected TradeButton(Builder builder) {
        super(builder);
        this.trade = builder.trade;
        this.context = builder.context;
        this.interactions = builder.interactions;
        this.externalControl = builder.externalControl;
    }

    private TradeDisplayResults cachedDisplay = TradeDisplayResults.EMPTY;
    public void cacheResults(TradeData trade,TradeContext context,ScreenPosition mousePos) {
        this.cachedDisplay = this.calculateDisplayEntries(trade,context,mousePos);
        this.setWidth(this.cachedDisplay.totalWidth);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        //Render Background Sprite
        gui.blitSprite(this.getBackgroundSprite().get(this.active,this.isHovered()),0,0,this.getWidth(),this.getHeight());
        //Get the trade display
        if(this.cachedDisplay.isEmpty())
            return;
        int nextX = TradeButtonDisplay.FULL_SPACER;
        boolean isHovered = this.isHovered();
        boolean isActive = this.isActive();
        int mouseX = gui.getMousePos().x - this.getX();
        TradeTooltipBuilder tooltips = TradeTooltipBuilder.create();
        @Nullable
        TradeDisplayEntry.ManualTooltipRender manualTooltipRender = null;
        ITradeInteractionHandler handler = this.interactions == null ? ITradeInteractionHandler.NULL : this.interactions;

        for(TradeSlotType slot : TradeSlotType.values())
        {
            //Add centering spacer
            int slotWidth = this.cachedDisplay.getSlotWidth(slot);
            int currentX = nextX + this.cachedDisplay.getCenteringSpacer(slot,slotWidth);
            //Render Displays
            for(TradeDisplayEntry entry : this.cachedDisplay.get(slot))
            {
                entry.extractContents(gui,currentX,0);
                //Check for tooltips to extract
                if(isHovered && mouseX >= currentX && mouseX < currentX + entry.desiredWidth())
                {
                    entry.appendTooltip(tooltips);
                    //Also cache the fancy tooltip renderer if we need special rendering logic for things like items
                    if(entry instanceof TradeDisplayEntry.ManualTooltipRender mtr)
                        manualTooltipRender = mtr;
                }
                currentX += entry.desiredWidth() + TradeButtonDisplay.MINI_SPACER;
            }
            //Set up the next x position
            nextX += TradeButtonDisplay.FULL_SPACER + slotWidth;
        }
        //Collect the remaining tooltips
        tooltips.merge(this.cachedDisplay.globalTooltips);
        TradeTooltipBuilder.Results results = tooltips.build();
        //Now render them
        if(!results.isEmpty() || (manualTooltipRender != null && manualTooltipRender.shouldRenderTooltip()))
        {
            ClientTooltipPositioner positioner = this.getTooltipPositioner();
            if(manualTooltipRender != null && manualTooltipRender.shouldRenderTooltip())
                manualTooltipRender.renderCustomTooltip(gui,results,positioner);
            else {
                gui.renderPositionedTooltip(results.getAllLines(),positioner);
            }
        }
    }

    public void renderSmallArrowAtSlot(FancyGuiExtractor gui,TradeSlot targetSlot) { this.renderSmallArrowAtSlot(gui,targetSlot,LCSprites.SMALL_ARROW_DOWN,-10); }
    public void renderSmallArrowAtSlot(FancyGuiExtractor gui,TradeSlot targetSlot,SizedSprite arrowSprite,int yOffset) {
        if(targetSlot == TradeSlot.NONE || !this.cachedDisplay.display.shouldShowSmallArrow(targetSlot))
            return;
        int nextX = TradeButtonDisplay.FULL_SPACER;
        TradeDisplayResults display = this.cachedDisplay;
        for(TradeSlotType slot : TradeSlotType.values())
        {
            //Add centering spacer
            int slotWidth = display.getSlotWidth(slot);
            int currentX = nextX + display.getCenteringSpacer(slot,slotWidth);
            boolean thisSlot = targetSlot.type() == slot;
            List<TradeDisplayEntry> displays = display.get(slot);
            for(int i = 0; i < displays.size(); ++i)
            {
                TradeDisplayEntry entry = displays.get(i);
                if(thisSlot && i == targetSlot.slot())
                {
                    //Render the arrow
                    int x = this.getX() + currentX + (entry.desiredWidth() / 2) - (arrowSprite.width() / 2);
                    int y = this.getY() + yOffset;
                    //Push position to zero since we're calculating based on absolute cords not relative cords
                    gui.pushZero()
                            .blitSprite(arrowSprite,x,y);
                    gui.pop();
                    return;
                }
                currentX += entry.desiredWidth() + TradeButtonDisplay.MINI_SPACER;
            }
            nextX += TradeButtonDisplay.FULL_SPACER + slotWidth;
        }
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        if(!this.externalControl && this.visible)
        {
            TradeContext.Builder builder = this.context.get();
            if(builder == null){
                this.cachedDisplay = TradeDisplayResults.EMPTY;
                return;
            }
            try(TradeContext context = builder.build()) {
                this.cachedDisplay = this.calculateDisplayEntries(this.trade.get(),context,mousePos);
            }
        }
    }

    private WidgetSprites getBackgroundSprite() { return LCSprites.BUTTON_GRAY; }

    private TradeDisplayResults calculateDisplayEntries(@Nullable TradeData trade, TradeContext context, ScreenPosition mousePos) {
        if(trade == null)
            return TradeDisplayResults.EMPTY;
        //Assemble the trade context and collect the display entries
        TradeButtonDisplay display = TradeButtonDisplay.REGISTRY.getValue(trade.getType());
        Map<TradeSlotType,List<TradeDisplayEntry>> entries = new HashMap<>();
        ImmutableMap.Builder<TradeSlotType,Integer> sectionWidth = ImmutableMap.builderWithExpectedSize(TradeSlotType.values().length);
        for(TradeSlotType type : TradeSlotType.values())
        {
            List<TradeDisplayEntry> e = display.getDisplayEntries(trade,type,context,this.isMouseOver(mousePos.x,mousePos.y),this.interactions);
            entries.put(type,new ArrayList<>(e));
            sectionWidth.put(type,display.getSlotWidth(trade,type,context,this.interactions));
        }
        int totalWidth = display.getButtonWidth(trade,context,this.interactions);
        TradeTooltipBuilder builder = TradeTooltipBuilder.create();
        if(this.getArea().isInArea(mousePos))
            display.appendTooltips(builder,trade,context,mousePos.relativeTo(this.getArea().pos),this.interactions);
        this.setWidth(totalWidth);
        return new TradeDisplayResults(trade,display,entries,totalWidth,sectionWidth.build(),builder.build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.interactions != null)
        {
            TradeData trade = this.cachedDisplay.trade;
            if(trade != null)
            {
                ScreenPosition mousePos = ScreenHelper.getMousePos(event);
                ScreenPosition localPosition = mousePos.relativeTo(this.getPosition());
                TradeSlot slot = this.getHoveredDisplay(localPosition);
                //LightmansCurrency.LogDebug("Sending mouse click at " + localPosition + " to trade slot " + slot);
                this.interactions.onTradeSlotClick(trade,slot,event.button(),createContext(event,localPosition,this.interactions));
                return true;
            }
            //LightmansCurrency.LogDebug("Cannot send mouse click since the trade is null!");
            return false;
        }
        else
            return super.mouseClicked(event,doubleClick);
    }

    @Override
    public boolean onMouseScrolled(int mouseX,int mouseY,double scrollX,double scrollY) {
        if(this.interactions != null && this.interactions.allowsScrollInteractions() && this.isMouseOver(mouseX,mouseY))
        {
            TradeData trade = this.trade.get();
            if(trade != null)
            {
                ScreenPosition mousePos = ScreenPosition.of(mouseX - this.getX(),mouseY - this.getY());
                TradeSlot slot = this.getHoveredDisplay(mousePos);
                this.interactions.onTradeSlotScroll(trade,slot,(float)scrollY,createContext(null,mousePos,this.interactions));
                return true;
            }
        }
        return false;
    }

    public static TradeEditContext createContext(@Nullable InputWithModifiers input,ScreenPosition mousePos, ITradeInteractionHandler handler) {
        return new TradeEditContext(input != null && input.hasShiftDown(),input != null && input.hasControlDown(),input != null && input.hasAltDown(),mousePos,handler);
    }

    private TradeSlot getHoveredDisplay(ScreenPosition localMousePos) {
        if(localMousePos.y < 0 || localMousePos.y > this.height)
            return TradeSlot.NONE;
        int nextX = TradeButtonDisplay.FULL_SPACER;
        //Change this buttons width to match the width defined in the display
        this.setWidth(this.cachedDisplay.totalWidth);
        for(TradeSlotType slot : TradeSlotType.values())
        {
            //Add centering spacer
            int slotWidth = this.cachedDisplay.getSlotWidth(slot);
            //Check if mouse is in the general area
            if(localMousePos.x >= nextX && localMousePos.x < nextX + slotWidth)
            {
                //If it is, then check the slots one by one
                int currentX = nextX + this.cachedDisplay.getCenteringSpacer(slot,slotWidth);
                List<TradeDisplayEntry> list = this.cachedDisplay.get(slot);
                for(int i = 0; i < list.size(); ++i)
                {
                    TradeDisplayEntry entry = list.get(i);
                    if(localMousePos.x >= currentX && localMousePos.x < currentX + entry.desiredWidth())
                        return new TradeSlot(slot,i);
                    currentX += entry.desiredWidth() + TradeButtonDisplay.MINI_SPACER;
                }
            }
            //Set up the next x position
            nextX += TradeButtonDisplay.FULL_SPACER + slotWidth;
        }
        return TradeSlot.NONE;
    }

    public static Builder builder() { return new Builder(); }

    @Nullable
    @Override
    public List<GhostSlot<?>> getGhostSlots() {
        //TODO allow ghost slot interactions
        return List.of();
    }

    public static class Builder extends ButtonBuilder<Builder,TradeButton>
    {
        private Supplier<TradeData> trade = () -> null;
        private Supplier<TradeContext.Builder> context = () -> null;
        @Nullable
        private ITradeInteractionHandler interactions = null;
        private boolean externalControl = false;

        private Builder() { super(10,HEIGHT); }

        public Builder withTrade(Supplier<TradeData> trade) { this.trade = trade; return this; }
        public Builder withContext(Supplier<TradeContext.Builder> trade) { this.context = trade; return this; }
        public Builder withExternalControl() { this.externalControl = true; return this; }

        public Builder withInteractionHandler(ITradeInteractionHandler handler) { this.interactions = handler; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TradeButton build() { return new TradeButton(this); }
    }

    private record TradeDisplayResults(TradeData trade, TradeButtonDisplay display, Map<TradeSlotType,List<TradeDisplayEntry>> entries, int totalWidth, Map<TradeSlotType,Integer> slotWidth,TradeTooltipBuilder.Results globalTooltips) {

        public static final TradeDisplayResults EMPTY = new TradeDisplayResults(null,null,ImmutableMap.of(),100,ImmutableMap.of(),TradeTooltipBuilder.create().build());

        public boolean isEmpty() { return this.display == null; }

        public List<TradeDisplayEntry> get(TradeSlotType slot) { return this.entries.getOrDefault(slot,new ArrayList<>()); }

        public int getSlotWidth(TradeSlotType slot) { return this.slotWidth.getOrDefault(slot,0); }

        public int getCenteringSpacer(TradeSlotType slot,int slotWidth) {
            int entryWidth = 0;
            for(TradeDisplayEntry entry : this.get(slot))
                entryWidth += entry.desiredWidth();
            return Math.max(0,(slotWidth - entryWidth) / 2);
        }

    }

}