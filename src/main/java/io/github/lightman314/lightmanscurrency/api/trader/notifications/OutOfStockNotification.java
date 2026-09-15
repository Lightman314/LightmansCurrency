package io.github.lightman314.lightmanscurrency.api.trader.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class OutOfStockNotification extends TraderNotification.SingleLine {

    private static final MapCodec<OutOfStockNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TraderCategory.TRADER_CATEGORY_CODEC.fieldOf("trader").forGetter(OutOfStockNotification::getCategory),
            Codec.INT.fieldOf("trade").forGetter(n -> n.tradeSlot)
    ).apply(builder,OutOfStockNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,OutOfStockNotification> STREAM_CODEC = StreamCodec.composite(
            TraderCategory.TRADER_CATEGORY_STREAM_CODEC,OutOfStockNotification::getCategory,
            ByteBufCodecs.VAR_INT,n -> n.tradeSlot,
            OutOfStockNotification::new);
    public static final NotificationType<OutOfStockNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);
    public static final TextEntry TEXT_INDEXLESS = TextEntry.notification(TYPE,"indexless");

    private final int tradeSlot;
    public OutOfStockNotification(TraderData trader,int tradeIndex) { this(new TraderCategory(trader),tradeIndex); }
    public OutOfStockNotification(TraderCategory category,int tradeIndex) {
        super(category);
        this.tradeSlot = tradeIndex;
    }

    @Override
    protected Component getMessage() {
        return this.tradeSlot > 0 ?
                TEXT.get(this.category.getName(),this.tradeSlot) :
                TEXT_INDEXLESS.get(this.category.getName()); }
    @Override
    public NotificationType<?> getType() { return TYPE; }
    @Override
    protected boolean equals(Notification other) { return other instanceof OutOfStockNotification n && this.parentDataMatches(n) && this.tradeSlot == n.tradeSlot; }
    @Override
    protected int hash() { return Objects.hash(this.category,this.tradeSlot); }

}
