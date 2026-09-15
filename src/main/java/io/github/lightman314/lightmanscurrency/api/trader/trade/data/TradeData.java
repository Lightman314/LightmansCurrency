package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradeData implements ISidedContext {

    public static final Codec<TradeData> CODEC = LCRegistries.Trader.TRADE_DATA_TYPE.byNameCodec()
            .dispatch(TradeData::getType,TradeDataType::codec);

    public static final TextEntry TOOLTIP_TRADE_INFO_TITLE = TextEntry.tooltip(LCApi.MODID,"trade.info.title");
    public static final TextEntry TOOLTIP_TRADE_INFO_STOCK = TextEntry.tooltip(LCApi.MODID,"trade.info.stock");
    public static final TextEntry TOOLTIP_TRADE_INFO_STOCK_INFINITE = TextEntry.tooltip(LCApi.MODID,"trade.info.stock.infinite");

    public static final TextEntry TOOLTIP_OUT_OF_STOCK = TextEntry.tooltip(LCApi.MODID,"out_of_stock");
    public static final TextEntry TOOLTIP_OUT_OF_SPACE = TextEntry.tooltip(LCApi.MODID,"out_of_space");
    public static final TextEntry TOOLTIP_CANNOT_AFFORD = TextEntry.tooltip(LCApi.MODID,"cannot_afford");

    public abstract TradeDataType<?> getType();

    private TradingNode<?> parent;
    public final TradingNode<?> getHolder() { return this.parent; }
    public final TraderData getTrader() { return this.parent != null ? this.parent.getTrader() : null; }
    public final void initialize(TradingNode<?> node) { this.parent = node; this.afterInit(); }
    protected void afterInit() {}

    @Override
    public final boolean isClient() { return this.parent == null || this.parent.isClient(); }

    private TradePrice price = TradePrice.empty().withListener(this::setPriceChanged);
    public TradePrice getInternalPrice() { return this.price; }
    public void validatePrice(TraderData trader) {
        if(trader != null && !this.price.currentlySupportsTrade(trader,this))
            this.setPrice(TradePrice.empty());
    }

    /**
     * Calls the {@link io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent.Cost }
     * @param context The compiled Trade Context for the potential trade interaction
     * @return The fully modified Trade Price for this trade
     */
    public final TradePrice getPrice(TradeContext context) {
        if(this.parent == null || context.isEditingView())
            return this.getInternalPrice();
        //Calculate the base cost
        TradeEvent.BaseCost baseCost = new TradeEvent.BaseCost(context,this.parent,this,this.getInternalPrice());
        TradeRule.tradeBaseCost(this.parent,this,baseCost);
        NeoForge.EVENT_BUS.post(baseCost);
        //Now calculate the modified cost
        TradeEvent.Cost event = new TradeEvent.Cost(context,this.parent,this,baseCost.getBaseCost());
        TradeRule.tradeCost(this.parent,this,event);
        NeoForge.EVENT_BUS.post(event);
        return event.getCostResult();
    }
    public void setPrice(TradePrice newPrice) {
        this.price = newPrice.copyWithListener(this::setPriceChanged);
        this.setPriceChanged();
    }

    protected final void setPriceChanged() {
        this.setChanged(builder -> builder.set("price",LCFancyPacketTypes.TRADE_PRICE,this.price));
    }

    public abstract TradeDirection getDirection();

    public final boolean isSale() { return this.getDirection().isSale(); }
    public final boolean isPurchase() { return this.getDirection().isPurchase(); }
    public final boolean isOther() { return this.getDirection().isOther(); }

    protected TradeData() {}
    protected TradeData(TradePrice price) { this.price = price.withListener(this::setPriceChanged); }

    public abstract boolean isCustomerReady();

    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet) { this.setChanged(packet,Predicates.alwaysTrue()); }
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,UUID player) { this.setChanged(packet,c -> c.isValidTarget(player));}
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,PlayerReference player) { this.setChanged(packet,c -> c.isValidTarget(player.id));}
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,Predicate<ISyncingContext> targetFilter) {
        if(this.parent == null)
            return;
        this.parent.setTradeChanged(this,packet,targetFilter);
    }

    public void getFullPacket(FancyPacketMap.Mutable packet,ISyncingContext context) {
        packet.set("price",LCFancyPacketTypes.TRADE_PRICE,this.price);
        this.getAdditionalFullPacket(packet,context);
    }
    protected abstract void getAdditionalFullPacket(FancyPacketMap.Mutable packet,ISyncingContext context);

    public void handlePacket(FancyPacketMap message) {
        if(message.contains("price"))
            this.price = message.get("price",LCFancyPacketTypes.TRADE_PRICE).withListener(this::setPriceChanged);
        this.handleAdditionalPacket(message);
    }
    protected abstract void handleAdditionalPacket(FancyPacketMap message);

    public final boolean hasStock(TradeContext context) { return this.getStock(context) > 0; }
    public abstract long getStock(TradeContext context);

    public final boolean outOfStock(TradeContext context) { return !this.hasStock(context); }

    public abstract boolean showOutOfSpaceWarning(TradeContext context);

    public final boolean predictHasStock(TraderData trader,@Nullable TransactionContext transaction) { return this.predictStock(trader,transaction) > 0; }
    public long predictStock(TraderData trader,@Nullable TransactionContext transaction) {
        //Create a customer-free trade context and use that to calculate the trades stock
        try(TradeContext context = TradeContext.builder(trader).build(transaction)) { return this.getStock(context); }
    }

    public abstract boolean processTradeClick(Player player,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context);
    public abstract boolean processTradeScroll(Player player,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context);

}