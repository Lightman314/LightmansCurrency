package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradeData implements ISidedContext {

    public static final Codec<TradeData> CODEC = LCRegistries.Trader.TRADE_DATA_TYPE.byNameCodec()
            .dispatch(TradeData::getType,TradeDataType::codec);

    public abstract TradeDataType<?> getType();

    private TradingNode<?> parent;
    public TradingNode<?> getHolder() { return this.parent; }
    public final void initialize(TradingNode<?> node) { this.parent = node; }

    @Override
    public final boolean isClient() { return this.parent == null || this.parent.isClient(); }

    private TradePrice price = new MoneyPrice(MoneyValue.empty());
    public TradePrice getPrice() { return this.price; }

    /**
     * Calls the {@link io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent.Cost }
     * @param context The compiled Trade Context for the potential trade interaction
     * @return The fully modified Trade Price for this trade
     */
    public final TradePrice getPrice(TradeContext context) {
        TradeEvent.Cost event = new TradeEvent.Cost(context,this.parent,this,this.getPrice());
        //TODO post to rules
        NeoForge.EVENT_BUS.post(event);
        return event.getCostResult();
    }
    public void setPrice(TradePrice newPrice) { this.price = newPrice; this.setChanged(builder -> builder.set("price", LCFancyPacketTypes.TRADE_PRICE,this.price)); }

    public abstract TradeDirection getDirection();

    protected TradeData() {}
    protected TradeData(TradePrice price) { this.price = price; }

    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet) { this.setChanged(packet,Predicates.alwaysTrue()); }
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,UUID player) { this.setChanged(packet,c -> c.isValidTarget(player));}
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,PlayerReference player) { this.setChanged(packet,c -> c.isValidTarget(player.id));}
    public final void setChanged(Consumer<FancyPacketMap.Mutable> packet,Predicate<ISyncingContext> targetFilter) {
        if(this.parent == null)
            return;
        this.parent.setTradeChanged(this,packet,targetFilter);
    }

    public abstract void getFullPacket(FancyPacketMap.Mutable packet,ISyncingContext context);

    public abstract void handlePacket(FancyPacketMap packet);

    public final boolean hasStock(TradeContext context) { return this.getStock(context) > 0; }
    public abstract long getStock(TradeContext context);

    public abstract boolean processTradeClick(Player player,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context);
    public abstract boolean processTradeScroll(Player player,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context);

}