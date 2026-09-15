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

public class BankInterestNotification extends SingleLineNotification {

    private static final MapCodec<BankInterestNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
            MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount)
    ).apply(builder,BankInterestNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,BankInterestNotification> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,n -> n.accountName,
            MoneyValue.STREAM_CODEC,n -> n.amount,
            BankInterestNotification::new);

    public static final NotificationType<BankInterestNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    protected final Component accountName;
    protected final MoneyValue amount;
    public BankInterestNotification(Component accountName,MoneyValue amount) {
        this.accountName = accountName;
        this.amount = amount;
    }
    @Override
    protected Component getMessage() { return TEXT.get(this.amount.getText()); }
    @Override
    public NotificationType<?> getType() { return TYPE; }
    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }
    @Override
    public boolean canMerge(Notification other) { return false; }
    @Override
    protected boolean equals(Notification other) { return other instanceof BankInterestNotification bin && bin.accountName.equals(this.accountName) && bin.amount.equals(this.amount); }
    @Override
    protected int hash() { return Objects.hash(this.accountName,this.amount); }

}
