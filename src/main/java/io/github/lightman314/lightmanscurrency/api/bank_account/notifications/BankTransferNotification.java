package io.github.lightman314.lightmanscurrency.api.bank_account.notifications;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
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

public abstract class BankTransferNotification extends SingleLineNotification {

    protected static <T extends BankTransferNotification> MapCodec<T> buildCodec(Function4<PlayerReference,MoneyValue,Component,Component,T> factory) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
                ComponentSerialization.CODEC.fieldOf("sending").forGetter(n -> n.sendingAccount),
                ComponentSerialization.CODEC.fieldOf("receiving").forGetter(n -> n.receivingAccount)
        ).apply(builder,factory));
    }
    protected static <T extends BankTransferNotification> StreamCodec<RegistryFriendlyByteBuf,T> buildStreamCodec(Function4<PlayerReference,MoneyValue,Component,Component,T> factory) {
        return StreamCodec.composite(
                PlayerReference.STREAM_CODEC,n -> n.player,
                MoneyValue.STREAM_CODEC,n -> n.amount,
                ComponentSerialization.STREAM_CODEC,n -> n.sendingAccount,
                ComponentSerialization.STREAM_CODEC,n -> n.receivingAccount,
                factory);
    }
    protected static <T extends BankTransferNotification> NotificationType<T> buildType(Function4<PlayerReference,MoneyValue,Component,Component,T> factory) { return new NotificationType<>(buildCodec(factory),buildStreamCodec(factory)); }

    protected final PlayerReference player;
    protected final MoneyValue amount;
    protected final Component sendingAccount;
    protected final Component receivingAccount;

    protected BankTransferNotification(PlayerReference player, MoneyValue amount,Component sendingAccount,Component receivingAccount) {
        this.player = player;
        this.amount = amount;
        this.sendingAccount = sendingAccount;
        this.receivingAccount = receivingAccount;
    }

    public static BankTransferNotification sentNotification(PlayerReference player,MoneyValue amount,Component sendingAccount,Component receivingAccount) { return new Sent(player,amount,sendingAccount,receivingAccount); }
    public static BankTransferNotification receivedNotification(PlayerReference player,MoneyValue amount,Component sendingAccount,Component receivingAccount) { return new Received(player,amount,sendingAccount,receivingAccount); }

    @Override
    protected Component getMessage() { return this.getMessageText().get(this.player.getName(this),this.amount.getText(),this.sendingAccount,this.receivingAccount); }

    protected abstract TextEntry getMessageText();

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.getMyAccountName()); }

    protected abstract Component getMyAccountName();

    @Override
    protected boolean equals(Notification other) {
        return other instanceof BankTransferNotification btn && other.getClass() == this.getClass() && btn.player.equals(this.player) && btn.amount.equals(this.amount) && btn.sendingAccount.equals(this.sendingAccount) && btn.receivingAccount.equals(this.sendingAccount);
    }

    @Override
    protected int hash() { return Objects.hash(this.player,this.amount,this.sendingAccount,this.receivingAccount); }

    public static final class Sent extends BankTransferNotification {

        public static final NotificationType<Sent> TYPE = buildType(Sent::new);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private Sent(PlayerReference player,MoneyValue amount,Component sendingAccount,Component receivingAccount) { super(player,amount,sendingAccount,receivingAccount); }
        @Override
        protected TextEntry getMessageText() { return TEXT; }
        @Override
        protected Component getMyAccountName() { return this.sendingAccount; }
        @Override
        public NotificationType<?> getType() { return TYPE; }
    }

    public static final class Received extends BankTransferNotification {

        public static final NotificationType<Received> TYPE = buildType(Received::new);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private Received(PlayerReference player,MoneyValue amount,Component sendingAccount,Component receivingAccount) { super(player,amount,sendingAccount,receivingAccount); }
        @Override
        protected TextEntry getMessageText() { return TEXT; }
        @Override
        protected Component getMyAccountName() { return this.receivingAccount; }
        @Override
        public NotificationType<?> getType() { return TYPE; }
    }

}
