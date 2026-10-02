package io.github.lightman314.lightmanscurrency.api.trader.client.trade;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeTooltipBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.EmptyDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.SpriteDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.StackedDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.message.TradeMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public abstract class TradeButtonDisplay {

    public static final ClientPairedRegistry<TradeDataType<?>,TradeButtonDisplay> REGISTRY = ClientPairedRegistry.builder(LCRegistries.Trader.TRADE_DATA_TYPE,TradeButtonDisplay.class)
            .throwIfUndefined().build();

    public static final int FULL_SPACER = 1;
    public static final int MINI_SPACER = 1;

    public static final Identifier SPRITE_ARROW_GRAY = LCApi.id("widget/arrow_gray");
    public static final Identifier SPRITE_ARROW_WHITE = LCApi.id("widget/arrow_white");
    public static final Identifier SPRITE_ALERT = LCApi.id("widget/alert");
    public static final Identifier EMPTY_ITEM = LCApi.id("container/slot/item");

    public static final int ARROW_WIDTH = 22;

    public boolean shouldShowSmallArrow(TradeSlot slot) { return slot.isInputOrOutput(); }

    public int getButtonWidth(TradeData trade,TradeContext context,ITradeInteractionHandler handler) {
        //Calculate based on input/output width
        int width = FULL_SPACER * 2;
        for(TradeSlotType slot : TradeSlotType.values())
        {
            int added = this.getSlotWidth(trade,slot,context,handler);
            if(added > 0)
                width += added + FULL_SPACER;
        }
        return width;
    }

    public abstract int getSlotWidth(TradeData trade,TradeSlotType section,TradeContext context,ITradeInteractionHandler handler);

    protected final int calculatedSlotWidth(TradeData trade,TradeSlotType slot,TradeContext context,ITradeInteractionHandler handler) {
        int width = 0;
        List<TradeDisplayEntry> list = this.getDisplayEntries(trade,slot,context,false,handler);
        if(list.isEmpty())
            return width;
        for(TradeDisplayEntry entry : list)
            width += entry.desiredWidth();
        width += (list.size()  - 1) * MINI_SPACER;
        return width;
    }

    public abstract List<TradeDisplayEntry> getDisplayEntries(TradeData trade,TradeSlotType section,TradeContext context,boolean hovered,ITradeInteractionHandler handler);

    public void addAdditionalMessages(TradeData trade,TradeContext context,TradeEvent.Pre event) {
        TradePrice price = trade.getPrice(context);
        if(!IAdminSettingProvider.hasInfiniteStock(context)) {
            //Check Stock
            if(trade.outOfStock(context))
                event.addWarning(TradeData.TOOLTIP_OUT_OF_STOCK.get());

            //Check Space (Purchase)
            if(trade.isPurchase() && trade.showOutOfSpaceWarning(context))
                event.addWarning(TradeData.TOOLTIP_OUT_OF_SPACE.get());

            //Check Price Space
            if(trade.isSale() && price.showOutOfSpaceWarning(context)) {
                event.addWarning(TradeData.TOOLTIP_OUT_OF_SPACE.get());
            }
        }
        //Check whether they can afford this trade
        if(trade.isSale() && price.showCannotAffordWarning(context))
            event.addWarning(TradeData.TOOLTIP_CANNOT_AFFORD.get());
    }

    public static List<TradeDisplayEntry> single(TradeDisplayEntry entry) { return Lists.newArrayList(entry); }

    public static List<TradeDisplayEntry> arrowWithMessages(boolean hovered,TradeData trade,TradeContext context,TradeButtonDisplay display) {
        return single(StackedDisplay.of(arrowEntry(hovered),messageEntry(hovered,trade,context,display)));
    }

    public static TradeDisplayEntry arrowEntry(boolean hovered) { return SpriteDisplay.of(hovered ? SPRITE_ARROW_WHITE : SPRITE_ARROW_GRAY, ARROW_WIDTH,18); }

    public static TradeDisplayEntry messageEntry(boolean hovered,TradeData trade,TradeContext context,TradeButtonDisplay display) {
        TradingNode<?> node = trade.getHolder();
        //Only actually add warnings if we're not in editing mode
        if(node != null && !context.isEditingView())
        {
            TradeEvent.Pre event = node.runPreTradeEvent(context,node.getTrades().indexOf(trade));
            display.addAdditionalMessages(trade,context,event);
            List<TradeMessage> messages = event.getMessages();
            messages.sort(TradeMessage.SORTER);
            List<Component> tooltips = TradeMessage.getFormattedMessages(messages);
            int alertColor = 0;
            if(!messages.isEmpty())
                alertColor = hovered ? messages.getFirst().type.hoverColor() : messages.getFirst().type.color();
            return SpriteDisplay.of(SPRITE_ALERT,ARROW_WIDTH,18,alertColor,tooltips);
        }
        //If the trade is not attached to its holder, then we cannot display any messages/alerts
        return EmptyDisplay.INSTANCE;
    }

    public static List<TradeDisplayEntry> forPrice(TradeData trade,TradeContext context,int width,ITradeInteractionHandler handler) {
        return ClientTradePrice.getTradeDisplay(trade,context,width,handler);
    }

    public void appendTooltips(TradeTooltipBuilder builder, TradeData trade, TradeContext context, ScreenPosition mousePos, ITradeInteractionHandler handler) { }

}