package io.github.lightman314.lightmanscurrency.api.bank_account.notifications;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.templates.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class LowBalanceNotification extends SingleLineNotification {

    private static final MapCodec<LowBalanceNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
            MoneyValue.NON_EMPTY_OR_FREE_CODEC.fieldOf("amount").forGetter(n -> n.value)
    ).apply(builder,LowBalanceNotification::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,LowBalanceNotification> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,n -> n.accountName,
            MoneyValue.STREAM_CODEC,n -> n.value,
            LowBalanceNotification::new);

    public static final NotificationType<LowBalanceNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    private final Component accountName;
    private final MoneyValue value;

    public LowBalanceNotification(Component accountName,MoneyValue value) { this.accountName = accountName; this.value = value; }

    @Override
    protected Component getMessage() { return TEXT.get(this.value.getText()); }
    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    protected boolean equals(Notification other) { return other instanceof LowBalanceNotification n && n.accountName.equals(this.accountName) && n.value.equals(this.value); }

    @Override
    protected int hash() { return Objects.hash(this.accountName,this.value); }

}