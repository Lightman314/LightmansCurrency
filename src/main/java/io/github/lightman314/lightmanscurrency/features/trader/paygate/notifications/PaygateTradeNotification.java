package io.github.lightman314.lightmanscurrency.features.trader.paygate.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TickTimeHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.TraderNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class PaygateTradeNotification extends TraderNotification.TaxableSingleLine {

    private static final MapCodec<PaygateTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradePriceReceipt.CODEC.fieldOf("cost").forGetter(n -> n.cost),
            Codec.INT.fieldOf("duration").forGetter(n -> n.duration),
            ComponentSerialization.CODEC.fieldOf("custoemr").forGetter(n -> n.customer),
            TraderCategory.TRADER_CATEGORY_CODEC.fieldOf("trader").forGetter(PaygateTradeNotification::getCategory),
            MoneyValue.CODEC.fieldOf("taxesPaid").forGetter(PaygateTradeNotification::getTaxesPaid)
    ).apply(builder,PaygateTradeNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,PaygateTradeNotification> STREAM_CODEC = StreamCodec.composite(
            TradePriceReceipt.STREAM_CODEC,n -> n.cost,
            ByteBufCodecs.INT,n -> n.duration,
            ComponentSerialization.STREAM_CODEC,n -> n.customer,
            TraderCategory.TRADER_CATEGORY_STREAM_CODEC,PaygateTradeNotification::getCategory,
            MoneyValue.STREAM_CODEC,PaygateTradeNotification::getTaxesPaid,
            PaygateTradeNotification::new);

    public static final NotificationType<PaygateTradeNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    private final TradePriceReceipt cost;
    private final int duration;
    private final Component customer;

    public PaygateTradeNotification(TradePriceReceipt price,int duration,Component customer,TraderCategory category,MoneyValue taxesPaid) {
        super(category,taxesPaid);
        this.cost = Objects.requireNonNull(price);
        this.duration = duration;
        this.customer = Objects.requireNonNull(customer);
    }

    @Override
    protected Component getMessage() {
        return TEXT.get(this.customer,this.cost.getText(),TickTimeHelper.formatDuration(this.duration));
    }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    protected boolean equals(Notification other) {
        return other instanceof PaygateTradeNotification n && n.cost.equals(this.cost) && this.duration == n.duration && this.customer.equals(n.customer) && super.parentDataMatches(n);
    }

    @Override
    protected int hash() { return Objects.hash(this.cost,this.duration,this.customer,this.hashParentData()); }

}