package io.github.lightman314.lightmanscurrency.api.trader.event;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.message.TradeMessage;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All events involved within the lifecycle of a customer trade interaction.
 */
public abstract class TradeEvent extends Event {

    private final TradeContext context;
    public final TradeContext getContext() { return this.context; }
    public final boolean isEditingView() { return this.context.isEditingView(); }
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

    /**
     * This event is called on both the logical server and the logical client in order to determine if the customer is allowed to attempt the given trade interaction.<br>
     * This event collects a set of {@link TradeMessage messages} that will be displayed on trade's button on the client, explaining why it failed (or if it didn't fail, other helpful info such as informing them of a discount).<br>
     * Event is {@link ICancellableEvent cancellable}, and if cancelled the trade attempt will be aborted immediately.
     * Calling {@link #addDenial(Component)} will automatically cancel the event in addition to collecting the given denial reason.
     */
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
        public void addHidden(Component message) { this.addMessage(TradeMessage.hidden(message),false); }

    }


    /**
     * This event is posted on both the logical server and the logical client in order to determine the trades base price.<br>
     * Automatically posted when {@link TradeData#getPrice(TradeContext)} is called.<br>
     * Called before {@link Cost} event is called, and this only allows you to modify the base cost of the trade
     * **before** any percentage-based multipliers are applied.
     */
    public static final class BaseCost extends TradeEvent {

        private final TradePrice originalBaseCost;
        public TradePrice getOriginalBaseCost() { return this.originalBaseCost; }
        private TradePrice baseCost;
        public TradePrice getBaseCost() { return this.baseCost; }
        public void setBaseCost(MoneyPrice newCost) { this.baseCost = newCost; }
        public BaseCost(TradeContext context,TradingNode<?> activeNode,TradeData trade,TradePrice baseCost) {
            super(context, activeNode, trade);
            this.originalBaseCost = this.baseCost = baseCost;
        }

    }

    /**
     * This event is posted on both the logical server and the logical client in order to determine the trades intended price.<br>
     * Automatically posted when {@link TradeData#getPrice(TradeContext)} is called.
     * Allows outside sources to modify the price via percentage, or for the price to be directly overridden/replaced if you, but do so with caution.
     */
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

    /**
     * Posted on the logical server after a successful trade.<br>
     * Contains information about the completed trade, such as the price paid, how many taxes were paid, and what product was actually given to the customer.<br>
     * The product information is provided as a list of objects
     */
    public static final class Post extends TradeEvent
    {
        private final TradePrice pricePaid;
        public TradePrice getPricePaid() { return this.pricePaid; }
        private final TradePrice.TransferResult priceResult;
        public MoneyValue getTaxesPaid() { return this.priceResult.getTaxesPaid(); }
        public TradePriceReceipt getPriceReceipt() { return this.priceResult.getReceipt(); }
        private final List<?> product;
        public List<?> getProduct() { return this.product; }
        private final Optional<Notification> tradeNotification;
        public Optional<Notification> getTradeNotification() { return this.tradeNotification; }

        /**
         * Should only be constructed via {@link TradingNode#finishSuccessfulTrade(TradeContext,TradeData,TradePrice,TradePrice.TransferResult,List,Notification)}
         */
        @ApiStatus.Internal
        public Post(TradeContext context,TradingNode<?> activeNode,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult,List<?> product,Optional<Notification> tradeNotification) {
            super(context,activeNode,trade);
            this.pricePaid = pricePaid;
            this.priceResult = priceResult;
            this.product = ImmutableList.copyOf(product);
            this.tradeNotification = tradeNotification;
        }

    }


}