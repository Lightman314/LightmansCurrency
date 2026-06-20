package io.github.lightman314.lightmanscurrency.api.trader.event;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.message.TradeMessage;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class TradeEvent extends Event {

    private final TradeContext context;
    public final TradeContext getContext() { return this.context; }
    public final TraderData getTrader() { return this.context.getTrader(); }
    public final TradeCustomer getCustomer() { return this.context.getCustomer(); }
    private final TradingNode<?> activeNode;
    public final TradingNode<?> getActiveNode() { return this.activeNode; }
    private final TradeData trade;
    public final TradeData getTrade() { return this.trade; }

    public TradeEvent(TradeContext context,TradingNode<?> activeNode,TradeData trade)
    {
        this.context = context;
        this.activeNode = activeNode;
        this.trade = trade;
    }

    public static final class Pre extends TradeEvent implements ICancellableEvent
    {
        private final List<TradeMessage> messages = new ArrayList<>();
        public List<TradeMessage> getMessages() { return new ArrayList<>(this.messages); }

        /**
         * Should only be constructed via {@link TradingNode#runPreTradeEvent(TradeContext, int)}
         */
        @ApiStatus.Internal
        public Pre(TradeContext context, TradingNode<?> activeNode, TradeData trade) { super(context, activeNode, trade); }

        public void addMessage(TradeMessage message,boolean cancelTrade)
        {
            this.messages.add(message);
            if(cancelTrade)
                this.setCanceled(true);
        }

        public void addHelpful(Component message) { this.addMessage(TradeMessage.helpful(message),false); }
        public void addNeutral(Component message) { this.addMessage(TradeMessage.neutral(message),false); }
        public void addWarning(Component message) { this.addMessage(TradeMessage.warn(message),false); }
        public void addError(Component message) { this.addMessage(TradeMessage.error(message),false); }
        public void addDenial(Component message) { this.addMessage(TradeMessage.error(message),true); }
        public void addHoverOnly(Component message) { this.addMessage(TradeMessage.invis(message),false); }

    }

    public static final class Cost extends TradeEvent
    {
        private boolean forceFree = false;
        public boolean forcedFree() { return this.forceFree; }
        public void makeFree() { this.forceFree = true; }
        public void makeNotFree() { this.forceFree = false; }

        private int pricePercentage = 100;
        public int getPricePercentage() { return this.pricePercentage; }
        public void setPricePercentage(int pricePercentage) { this.pricePercentage = pricePercentage; }
        public void giveDiscount(int percentage) { this.pricePercentage -= percentage; }
        public void hikePrice(int percentage) { this.pricePercentage += percentage; }

        private final TradePrice baseCost;
        public boolean getCostResultIsFree() { return this.forceFree || this.pricePercentage <= 0 || this.baseCost.isFree(); }
        public TradePrice getCostResult() { return this.getCostResultIsFree() ? new MoneyPrice(MoneyValue.free()) : this.baseCost.percentageOfValue(this.pricePercentage); }

        /**
         * Should only be constructed via {@link TradeData#getPrice(TradeContext)}
         */
        @ApiStatus.Internal
        public Cost(TradeContext context, TradingNode<?> activeNode, TradeData trade, TradePrice baseCost) {
            super(context,activeNode,trade);
            this.baseCost = baseCost;
        }

    }

    public static final class Post extends TradeEvent
    {
        private final MoneyValue pricePaid;
        public MoneyValue getPricePaid() { return this.pricePaid; }
        private final MoneyValue taxesPaid;
        public MoneyValue getTaxesPaid() { return this.taxesPaid; }
        private final List<Object> product;
        public List<Object> getProduct() { return this.product; }

        /**
         * Should only be constructed via {@link TradingNode#finishSuccessfulTrade(TradeContext, TradeData, MoneyValue, MoneyValue,List)}
         */
        @ApiStatus.Internal
        public Post(TradeContext context,TradingNode<?> activeNode,TradeData trade,MoneyValue pricePaid,MoneyValue taxesPaid,List<Object> product) {
            super(context,activeNode,trade);
            this.pricePaid = pricePaid;
            this.taxesPaid = taxesPaid;
            this.product = ImmutableList.copyOf(product);
        }

    }


}