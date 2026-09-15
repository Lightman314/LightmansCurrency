package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntryBundle;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

/**
 * Enum declaring the intended interaction direction of the trade<br>
 * {@link #SALE} indicates that the trade is selling the product in exchange for the given {@link TradePrice}<br>
 * {@link #PURCHASE} indicates that the trade is buying the product in exchange for the given {@link TradePrice}<br>
 * {@link #OTHER} indicates that the trade is doing some form of custom interaction that either doesn't involve the {@link TradePrice} or is doing some form of unknown or abnormal trade interaction
 */
public enum TradeDirection {
    SALE,PURCHASE,OTHER;

    public static final Codec<TradeDirection> CODEC = EnumHelper.buildCodec(TradeDirection.class,"Trade Direction");
    public static final StreamCodec<ByteBuf,TradeDirection> STREAM_CODEC = EnumHelper.buildStreamCodec(TradeDirection.class,"Trade Direction");

    public static final ImmutableSet<TradeDirection> SET = ImmutableSet.copyOf(values());

    public static final TextEntryBundle<TradeDirection> NAME_TEXT = TextEntryBundle.of(TradeDirection.values(),"gui.lightmanscurrency.trade_direction");
    public static final TextEntryBundle<TradeDirection> ACTION_TEXT = TextEntryBundle.of(TradeDirection.values(),"gui.lightmanscurrency.trade_direction.action");

    public boolean isSale() { return this == SALE; }
    public boolean isPurchase() { return this == PURCHASE; }
    public boolean isOther() { return this == OTHER; }

    public Component getName() { return NAME_TEXT.getComponent(this); }
    public Component getAction() { return ACTION_TEXT.getComponent(this); }

}