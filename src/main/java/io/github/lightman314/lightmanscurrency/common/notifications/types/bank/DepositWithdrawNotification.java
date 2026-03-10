package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart4;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public abstract class DepositWithdrawNotification extends SingleLineNotification {

	public static final NotificationType<Player> PLAYER_TYPE = new Player.Type();
	public static final NotificationType<Custom> CUSTOM_TYPE = new Custom.Type();
	public static final NotificationType<Server> SERVER_TYPE = new Server.Type();

	protected Component accountName = EasyText.empty();
	protected boolean isDeposit = false;
	protected MoneyValue amount = MoneyValue.empty();

	protected DepositWithdrawNotification(Component accountName, boolean isDeposit, MoneyValue amount) { this.accountName = accountName; this.isDeposit = isDeposit; this.amount = amount; }
	protected DepositWithdrawNotification(Component accountName, boolean isDeposit, MoneyValue amount,CommonData data) {
        super(data);
        this.accountName = accountName;
        this.isDeposit = isDeposit;
        this.amount = amount;
    }
	protected DepositWithdrawNotification() {}

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
		this.isDeposit = compound.getBoolean("Deposit");
		this.amount = MoneyValue.safeLoad(compound, "Amount");
	}
	
	protected abstract Component getName();

	@Override
	public Component getMessage() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW.get(this.getName(), this.isDeposit ? LCText.NOTIFICATION_BANK_DEPOSIT.get() : LCText.NOTIFICATION_BANK_WITHDRAW.get(), this.amount.getText()); }

    protected static <T extends DepositWithdrawNotification> Products.P4<RecordCodecBuilder.Mu<T>,Component,Boolean,MoneyValue,CommonData> dwFields(RecordCodecBuilder.Instance<T> builder)
    {
        return builder.group(
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                Codec.BOOL.fieldOf("deposit").forGetter(n -> n.isDeposit),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
                baseFields()
        );
    }

    protected static <T extends DepositWithdrawNotification> SPart4<RegistryFriendlyByteBuf,T,Component,Boolean,MoneyValue,CommonData> dwStreamFields(Class<T> clazz) { return dwStreamFields(); }
    protected static <T extends DepositWithdrawNotification> SPart4<RegistryFriendlyByteBuf,T,Component,Boolean,MoneyValue,CommonData> dwStreamFields()
    {
        return SPart4.of(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                ByteBufCodecs.BOOL,n -> n.isDeposit,
                MoneyValue.STREAM_CODEC,n -> n.amount);
    }

	public static class Player extends DepositWithdrawNotification {

		PlayerReference player = PlayerReference.NULL;

		private Player() {}
		private Player(PlayerReference player, Component accountName, boolean isDeposit, MoneyValue amount, CommonData data) {
            super(accountName, isDeposit, amount, data);
            this.player = player;
        }
		public Player(PlayerReference player, Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.player = player; }

		@Override
		protected Component getName() { return this.player.getNameComponent(this.isClient()); }

        @Override
		public NotificationType<Player> getType() { return PLAYER_TYPE; }
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.player = PlayerReference.load(compound.getCompound("Player"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Player n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.player.is(this.player);
			return false;
		}

        private static class Type extends NotificationType<Player>
        {
            private static final MapCodec<Player> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player)
            ).and(DepositWithdrawNotification.dwFields(builder))
                    .apply(builder,Player::new));

            private static final StreamCodec<RegistryFriendlyByteBuf,Player> STREAM_CODEC = StreamHelper.combine(dwStreamFields(),
                    PlayerReference.STREAM_CODEC,n -> n.player,
                    Player::new);

            @Override
            protected Player createNew() { return new Player(); }
            @Override
            public MapCodec<Player> codec() { return CODEC; }
            @Override
            public StreamCodec<RegistryFriendlyByteBuf, Player> streamCodec() { return STREAM_CODEC; }
        }
		
	}
	
	public static class Custom extends DepositWithdrawNotification {
		Component objectName = EasyText.empty();

		private Custom() {}
        private Custom(Component objectName,Component accountName,boolean isDeposit,MoneyValue amount,CommonData data) {
            super(accountName,isDeposit,amount,data);
            this.objectName = objectName;
        }
		public Custom(String objectName, Component accountName, boolean isDeposit, MoneyValue amount) { this(EasyText.literal(objectName),accountName,isDeposit,amount); }
		public Custom(Component objectName, Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.objectName = objectName; }

		@Override
		protected Component getName() { return this.objectName; }

        @Override
		public NotificationType<Custom> getType() { return CUSTOM_TYPE; }
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.objectName = Component.Serializer.fromJson(compound.getString("Trader"),lookup);
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Custom n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.objectName.equals(this.objectName);
			return false;
		}

        private static class Type extends NotificationType<Custom>
        {
            private static final MapCodec<Custom> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    ComponentSerialization.CODEC.fieldOf("machine").forGetter(n -> n.objectName)
            ).and(dwFields(builder))
                    .apply(builder,Custom::new));

            private static final StreamCodec<RegistryFriendlyByteBuf,Custom> STREAM_CODEC = StreamHelper.combine(dwStreamFields(),
                    ComponentSerialization.STREAM_CODEC,n -> n.objectName,
                    Custom::new);

            @Override
            protected Custom createNew() { return new Custom(); }
            @Override
            public MapCodec<Custom> codec() { return CODEC; }
            @Override
            public StreamCodec<RegistryFriendlyByteBuf,Custom> streamCodec() { return null; }
        }
		
	}

	public static class Server extends DepositWithdrawNotification {

		private Server() {}
		private Server(Component accountName, boolean isDeposit, MoneyValue amount,CommonData data) { super(accountName, isDeposit, amount, data); }
		private Server(Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); }

		public static Supplier<Notification> create(Component accountName, boolean isDeposit, MoneyValue amount) { return () -> new Server(accountName,isDeposit,amount); }

		@Override
		protected Component getName() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW_SERVER.get(); }

        @Override
		public NotificationType<Server> getType() { return SERVER_TYPE; }

		@Override
		protected boolean canMerge(Notification other) { return false; }

        private static class Type extends NotificationType<Server>
        {
            private static final MapCodec<Server> CODEC = RecordCodecBuilder.mapCodec(builder ->
                    dwFields(builder)
                    .apply(builder,Server::new));

            private static final StreamCodec<RegistryFriendlyByteBuf,Server> STREAM_CODEC = dwStreamFields(Server.class).assemble(Server::new);

            @Override
            protected Server createNew() { return new Server(); }
            @Override
            public MapCodec<Server> codec() { return CODEC; }
            @Override
            public StreamCodec<RegistryFriendlyByteBuf, Server> streamCodec() { return STREAM_CODEC; }
        }

	}
	
}
