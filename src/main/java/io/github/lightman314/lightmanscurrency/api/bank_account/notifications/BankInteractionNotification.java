package io.github.lightman314.lightmanscurrency.api.bank_account.notifications;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart3;
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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public abstract class BankInteractionNotification extends SingleLineNotification {

    public static final TextEntry TEXT_DEPOSIT = TextEntry.notification(LCApi.id("bank_interaction.deposit"));
    public static final TextEntry TEXT_WITHDRAW = TextEntry.notification(LCApi.id("bank_interaction.withdraw"));
    public static final TextEntry TEXT_INTERACTION_SERVER = TextEntry.notification(LCApi.id("bank_interaction.server"));

    protected final Component accountName;
    protected final boolean isDeposit;
    protected final MoneyValue amount;

    protected BankInteractionNotification(Component accountName,boolean isDeposit,MoneyValue amount) {
        this.accountName = accountName;
        this.isDeposit = isDeposit;
        this.amount = amount;
    }

    public static BankInteractionNotification forPlayer(Component accountName,boolean isDeposit,MoneyValue amount,PlayerReference player) { return new ForPlayer(accountName,isDeposit,amount,player); }
    public static BankInteractionNotification forMachine(Component accountName, boolean isDeposit, MoneyValue amount, Component machineName) { return new ForMachine(accountName,isDeposit,amount,machineName); }
    public static BankInteractionNotification forServer(Component accountName, boolean isDeposit, MoneyValue amount) { return new ForServer(accountName,isDeposit,amount); }

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    protected abstract Component getName();

    @Override
    protected Component getMessage() {
        TextEntry text = this.isDeposit ? TEXT_DEPOSIT : TEXT_WITHDRAW;
        return text.get(this.getName(),this.amount.getText());
    }

    protected boolean parentMatches(BankInteractionNotification other) { return other.accountName.equals(this.accountName) && other.isDeposit == this.isDeposit && other.amount.equals(this.amount); }

    protected int hashParent() { return Objects.hash(this.accountName,this.isDeposit,this.amount); }

    public static <T extends BankInteractionNotification> Products.P3<RecordCodecBuilder.Mu<T>,Component,Boolean,MoneyValue> binFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                Codec.BOOL.fieldOf("deposit").forGetter(n -> n.isDeposit),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount)
        );
    }

    public static <T extends BankInteractionNotification> SPart3<RegistryFriendlyByteBuf,T,Component,Boolean,MoneyValue> binStreamFields(Class<T> clazz) { return binStreamFields(); }
    public static <T extends BankInteractionNotification> SPart3<RegistryFriendlyByteBuf,T,Component,Boolean,MoneyValue> binStreamFields() {
        return new SPart3<>(ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                ByteBufCodecs.BOOL,n -> n.isDeposit,
                MoneyValue.STREAM_CODEC,n -> n.amount);
    }

    public static class ForPlayer extends BankInteractionNotification {
        private static final MapCodec<ForPlayer> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> binFields(builder)
                .and(PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player))
                .apply(builder,ForPlayer::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,ForPlayer> STREAM_CODEC = StreamHelper.combine(
                binStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ForPlayer::new);
        public static final NotificationType<ForPlayer> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        private final PlayerReference player;
        private ForPlayer(Component accountName, boolean isDeposit,MoneyValue amount,PlayerReference player) {
            super(accountName, isDeposit, amount);
            this.player = player;
        }

        @Override
        protected Component getName() { return this.player.getNameComponent(this); }
        @Override
        public NotificationType<?> getType() { return TYPE; }
        @Override
        protected boolean equals(Notification other) { return other instanceof ForPlayer o && this.parentMatches(o) && this.player.equals(o.player); }
        @Override
        protected int hash() { return Objects.hash(this.hashParent(),this.player); }
    }

    public static class ForMachine extends BankInteractionNotification {

        private static final MapCodec<ForMachine> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> binFields(builder)
                .and(ComponentSerialization.CODEC.fieldOf("machine").forGetter(n -> n.machineName))
                .apply(builder, ForMachine::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ForMachine> STREAM_CODEC = StreamHelper.combine(
                binStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.machineName,
                ForMachine::new);
        public static final NotificationType<ForMachine> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        private final Component machineName;
        protected ForMachine(Component accountName,boolean isDeposit,MoneyValue amount,Component machineName) {
            super(accountName, isDeposit, amount);
            this.machineName = machineName;
        }
        @Override
        protected Component getName() { return this.machineName; }
        @Override
        public NotificationType<?> getType() { return TYPE; }
        @Override
        protected boolean equals(Notification other) { return other instanceof ForMachine o && this.parentMatches(o) && o.machineName.equals(this.machineName); }
        @Override
        protected int hash() { return Objects.hash(this.hashParent(),this.machineName); }
    }

    public static class ForServer extends BankInteractionNotification {

        private static final MapCodec<ForServer> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> binFields(builder)
                .apply(builder,ForServer::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,ForServer> STREAM_CODEC = binStreamFields(ForServer.class).assemble(ForServer::new);
        public static final NotificationType<ForServer> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        protected ForServer(Component accountName,boolean isDeposit,MoneyValue amount) { super(accountName, isDeposit, amount); }
        @Override
        protected Component getName() { return TEXT_INTERACTION_SERVER.get(); }
        @Override
        public NotificationType<?> getType() { return TYPE; }
        @Override
        protected boolean equals(Notification other) { return other instanceof ForServer o && this.parentMatches(o); }
        @Override
        protected int hash() { return this.hashParent(); }
    }

}