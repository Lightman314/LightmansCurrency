package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.templates.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class TaxesPaidNotification extends SingleLineNotification {

    private static final MapCodec<TaxesPaidNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
            NotificationCategory.CODEC.fieldOf("category").forGetter(TaxesPaidNotification::getCategory)
    ).apply(builder,TaxesPaidNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,TaxesPaidNotification> STREAM_CODEC = StreamCodec.composite(
            MoneyValue.STREAM_CODEC,n -> n.amount,
            NotificationCategory.STREAM_CODEC,TaxesPaidNotification::getCategory,
            TaxesPaidNotification::new);

    public static final NotificationType<TaxesPaidNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    private final MoneyValue amount;
    private final NotificationCategory category;

    public TaxesPaidNotification(MoneyValue amount,NotificationCategory category) {
        this.amount = amount;
        this.category = category;
    }

    @Override
    protected Component getMessage() { return TEXT.get(this.amount.getText()); }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    protected boolean equals(Notification other) {
        if(other instanceof TaxesPaidNotification n)
            return n.amount.equals(this.amount) && n.category.equals(this.category);
        return false;
    }

    @Override
    protected int hash() { return Objects.hash(this.amount,this.category); }

}