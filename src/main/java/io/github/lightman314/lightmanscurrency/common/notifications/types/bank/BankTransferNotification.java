package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
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

public class BankTransferNotification extends SingleLineNotification {

	public static final NotificationType<BankTransferNotification> TYPE = new Type();
	
	PlayerReference player = PlayerReference.NULL;
	MoneyValue amount = MoneyValue.empty();
    Component accountName = EasyText.empty();
    Component otherAccount = EasyText.empty();
	boolean wasReceived = false;
	
	private BankTransferNotification() { }
	private BankTransferNotification(PlayerReference player, MoneyValue amount, Component account, Component otherAccount, boolean wasReceived, CommonData data) {
        super(data);
        this.player = player;
        this.amount = amount;
        this.accountName = account;
        this.otherAccount = otherAccount;
        this.wasReceived = wasReceived;
    }
	public BankTransferNotification(PlayerReference player, MoneyValue amount, Component accountName, Component otherAccount, boolean wasReceived) {
		this.player = player;
		this.amount = amount;
		this.accountName = accountName;
		this.otherAccount = otherAccount;
		this.wasReceived = wasReceived;
	}

    @Override
	public NotificationType<BankTransferNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public Component getMessage() {
		return LCText.NOTIFICATION_BANK_TRANSFER.get(this.player.getName(true), this.amount.getText(), this.wasReceived ? LCText.GUI_FROM.get() : LCText.GUI_TO.get(), this.otherAccount);
	}

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.amount = MoneyValue.safeLoad(compound, "Amount");
		this.accountName = Component.Serializer.fromJson(compound.getString("Account"),lookup);
		this.otherAccount = Component.Serializer.fromJson(compound.getString("Other"),lookup);
		this.wasReceived = compound.getBoolean("Received");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof BankTransferNotification n)
		{
			return n.player.is(this.player) && n.amount.equals(this.amount) && n.accountName.equals(this.accountName) && n.otherAccount.equals(this.otherAccount) && n.wasReceived == this.wasReceived;
		}
		return false;
	}

    private static class Type extends NotificationType<BankTransferNotification>
    {
        private static final MapCodec<BankTransferNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                ComponentSerialization.CODEC.fieldOf("otherAccount").forGetter(n -> n.otherAccount),
                Codec.BOOL.fieldOf("received").forGetter(n -> n.wasReceived),
                baseFields()
        ).apply(builder,BankTransferNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,BankTransferNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                MoneyValue.STREAM_CODEC,n -> n.amount,
                ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                ComponentSerialization.STREAM_CODEC,n -> n.otherAccount,
                ByteBufCodecs.BOOL, n -> n.wasReceived,
                BankTransferNotification::new);

        @Override
        protected BankTransferNotification createNew() { return new BankTransferNotification(); }
        @Override
        public MapCodec<BankTransferNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,BankTransferNotification> streamCodec() { return null; }
    }

}
