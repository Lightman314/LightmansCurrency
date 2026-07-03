package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TradeButton extends FancyButton {

    private final Supplier<TradeData> trade;
    private final Supplier<TradeContext.Builder> context;
    private final ITradeInteractionHandler interactions;
    protected TradeButton(Builder builder) {
        super(builder);
        this.trade = builder.trade;
        this.context = builder.context;
        this.interactions = builder.interactions;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        TradeDisplayResults display = this.getDisplayEntries();
        if(display.isEmpty())
            return;
        TradeData trade = this.trade.get();
        if(trade == null)
            return;
        int nextX = TradeDisplay.FULL_SPACER;
        boolean isHovered = this.isHovered;
        int mouseX = gui.getMousePos().x - this.getX();
        List<Component> tooltips = new ArrayList<>();
        @Nullable
        TradeDisplayEntry.ManualTooltipRender manualTooltipRender = null;
        ITradeInteractionHandler handler = this.interactions == null ? ITradeInteractionHandler.NULL : this.interactions;
        try(TradeContext context = this.context.get().build())
        {
            //Change this buttons width to match the width defined in the display
            this.setWidth(display.getButtonWidth(trade,context,handler));
            for(TradeSlotType slot : TradeSlotType.values())
            {
                //Add centering spacer
                int slotWidth = display.getSlotWidth(trade,slot,context,handler);
                int currentX = nextX + display.getCenteringSpacer(slot,slotWidth);
                //Render Displays
                for(TradeDisplayEntry entry : display.get(slot))
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
                    currentX += entry.desiredWidth() + TradeDisplay.MINI_SPACER;
                }
                //Set up the next x position
                nextX += TradeDisplay.FULL_SPACER + slotWidth;
            }
            //Collect the remaining tooltips
            display.appendGlobalTooltips(context,handler,tooltips);
            //Now render them
            if(!tooltips.isEmpty() || manualTooltipRender != null)
            {
                if(manualTooltipRender != null)
                    manualTooltipRender.renderCustomTooltip(gui,tooltips);
                else
                    gui.renderPositionedTooltip(tooltips,this.getTooltipPositioner());
            }
        }
    }

    private TradeDisplayResults getDisplayEntries() {
        TradeData trade = this.trade.get();
        if(trade == null)
            return TradeDisplayResults.EMPTY;
        TradeContext.Builder contextBuilder = this.context.get();
        if(contextBuilder == null)
            return TradeDisplayResults.EMPTY;
        //Assemble the trade context and collect the display entries
        TradeDisplay display = TradeDisplay.REGISTRY.getValue(trade.getType());
        Map<TradeSlotType,List<TradeDisplayEntry>> entries = new HashMap<>();
        try(TradeContext context = contextBuilder.build())
        {
            for(TradeSlotType type : TradeSlotType.values())
            {
                List<TradeDisplayEntry> e = display.getDisplayEntries(trade,type,context,this.isHoveredOrFocused(),this.interactions);
                entries.put(type,new ArrayList<>(e));
            }
        }
        return new TradeDisplayResults(display,entries);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.interactions != null)
        {
            TradeData trade = this.trade.get();
            if(trade != null)
            {
                ScreenPosition localPosition = ScreenPosition.of(event.x() - this.getX(),event.y() - this.getY());
                TradeSlot slot = this.getHoveredDisplay(localPosition);
                this.interactions.onTradeSlotClick(trade,slot,event.button(),createContext(event,localPosition,this.interactions));
                return true;
            }
            return false;
        }
        else
            return super.mouseClicked(event,doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if(this.interactions != null)
        {
            TradeData trade = this.trade.get();
            if(trade != null)
            {
                ScreenPosition mousePos = ScreenPosition.of(x - this.getX(),y - this.getY());
                TradeSlot slot = this.getHoveredDisplay(mousePos);
                this.interactions.onTradeSlotScroll(trade,slot,(float)scrollY,createContext(null,mousePos,this.interactions));
            }
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    public static TradeEditContext createContext(@Nullable InputWithModifiers input,ScreenPosition mousePos, ITradeInteractionHandler handler) {
        return new TradeEditContext(input != null && input.hasShiftDown(),input != null && input.hasControlDown(),input != null && input.hasAltDown(),mousePos,handler);
    }

    private TradeSlot getHoveredDisplay(ScreenPosition mousePos) {
        if(mousePos.y < 0 || mousePos.y > this.height)
            return TradeSlot.NONE;
        TradeDisplayResults display = this.getDisplayEntries();
        if(display == null)
            return TradeSlot.NONE;
        TradeData trade = this.trade.get();
        if(trade == null)
            return TradeSlot.NONE;
        int nextX = TradeDisplay.FULL_SPACER;
        try(TradeContext context = this.context.get().build())
        {
            //Change this buttons width to match the width defined in the display
            this.setWidth(display.getButtonWidth(trade,context,this.interactions));
            for(TradeSlotType slot : TradeSlotType.values())
            {
                //Add centering spacer
                int slotWidth = display.getSlotWidth(trade,slot,context,this.interactions);
                //Check if mouse is in the general area
                if(mousePos.x >= nextX && mousePos.x < nextX + slotWidth)
                {
                    //If it is, then check the slots one by one
                    int currentX = nextX + display.getCenteringSpacer(slot,slotWidth);
                    List<TradeDisplayEntry> list = display.get(slot);
                    for(int i = 0; i < list.size(); ++i)
                    {
                        TradeDisplayEntry entry = list.get(i);
                        if(mousePos.x >= currentX && mousePos.x < currentX + entry.desiredWidth())
                            return new TradeSlot(slot,i);
                        currentX += entry.desiredWidth() + TradeDisplay.MINI_SPACER;
                    }
                }
                //Set up the next x position
                nextX += TradeDisplay.FULL_SPACER + slotWidth;
            }
        }
        return TradeSlot.NONE;
    }

    public static class Builder extends ButtonBuilder<Builder,TradeButton>
    {
        private Supplier<TradeData> trade = () -> null;
        private Supplier<TradeContext.Builder> context = () -> null;
        @Nullable
        private ITradeInteractionHandler interactions = null;

        private Builder() { super(10,18); }

        public Builder withTrade(Supplier<TradeData> trade) { this.trade = trade; return this; }
        public Builder withContext(Supplier<TradeContext.Builder> trade) { this.context = trade; return this; }

        public Builder withInteractionHandler(ITradeInteractionHandler handler) { this.interactions = handler; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TradeButton build() { return new TradeButton(this); }
    }

    private record TradeDisplayResults(TradeDisplay display,Map<TradeSlotType,List<TradeDisplayEntry>> entries) {

        public static final TradeDisplayResults EMPTY = new TradeDisplayResults(null,ImmutableMap.of());

        public boolean isEmpty() { return this.display == null; }

        public List<TradeDisplayEntry> get(TradeSlotType slot) { return this.entries.get(slot); }

        public int getButtonWidth(TradeData trade,TradeContext context,ITradeInteractionHandler handler) { return this.display.getButtonWidth(trade,context,handler); }

        public int getSlotWidth(TradeData trade,TradeSlotType slot,TradeContext context,ITradeInteractionHandler handler) { return this.display.getSlotWidth(trade,slot,context,handler); }

        public int getCenteringSpacer(TradeSlotType slot, int slotWidth) {
            int entryWidth = 0;
            for(TradeDisplayEntry entry : this.get(slot))
                entryWidth += entry.desiredWidth();
            return Math.max(0,(slotWidth - entryWidth) / 2);
        }

        public void appendGlobalTooltips(TradeContext context,ITradeInteractionHandler handler,List<Component> tooltips) {

        }

    }

}