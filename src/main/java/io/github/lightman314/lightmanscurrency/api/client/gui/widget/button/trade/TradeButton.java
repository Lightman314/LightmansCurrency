package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import net.minecraft.client.input.MouseButtonEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TradeButton extends EasyButton {

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

    }

    private Map<TradeSlotType,List<TradeDisplayEntry>> getDisplayEntries() {
        Map<TradeSlotType,List<TradeDisplayEntry>> entries = new HashMap<>();
        TradeData trade = this.trade.get();
        if(trade == null)
            return entries;
        TradeContext.Builder contextBuilder = this.context.get();
        if(contextBuilder == null)
            return entries;
        //Assemble the trade context and collect the display entries
        try(TradeContext context = contextBuilder.build())
        {
            TradeDisplay display = TradeDisplay.REGISTRY.getValue(trade.getType());
            for(TradeSlotType type : TradeSlotType.values())
            {
                List<TradeDisplayEntry> e = display.getDisplayEntries(trade,type,context,this.getArea());
                entries.put(type,new ArrayList<>(e));
            }
        }
        return entries;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.interactions != null)
        {
            //local position
            ScreenPosition localPosition = ScreenPosition.of(event.x() - this.getX(),event.y() - this.getY());
            return true;
        }
        else
            return super.mouseClicked(event,doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if(this.interactions != null)
        {

        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    public static class Builder extends ButtonBuilder<Builder,TradeButton>
    {
        private Supplier<TradeData> trade = () -> null;
        private Supplier<TradeContext.Builder> context = () -> null;
        @Nullable
        private ITradeInteractionHandler interactions = null;

        public Builder withTrade(Supplier<TradeData> trade) { this.trade = trade; return this; }
        public Builder withContext(Supplier<TradeContext.Builder> trade) { this.context = trade; return this; }

        public Builder withInteractionHandler(ITradeInteractionHandler handler) { this.interactions = handler; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TradeButton build() { return new TradeButton(this); }
    }

}