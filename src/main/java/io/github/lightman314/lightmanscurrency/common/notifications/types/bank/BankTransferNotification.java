package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BankTransferNotification extends Notification {

	public static final NotificationType<BankTransferNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "bank_transfer"),BankTransferNotification::new);
	
	PlayerReference player;
	MoneyValue amount = MoneyValue.empty();
	MutableComponent accountName;
	MutableComponent otherAccount;
	boolean wasReceived;
	
	private BankTransferNotification() { }
	public BankTransferNotification(PlayerReference player, MoneyValue amount, MutableComponent accountName, MutableComponent otherAccount, boolean wasReceived) {
		this.player = player;
		this.amount = amount;
		this.accountName = accountName;
		this.otherAccount = otherAccount;
		this.wasReceived = wasReceived;
	}
	
	@Nonnull
    @Override
	protected NotificationType<BankTransferNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		return LCText.NOTIFICATION_BANK_TRANSFER.get(this.player.getName(true), this.amount.getText(), this.wasReceived ? LCText.GUI_FROM.get() : LCText.GUI_TO.get(), this.otherAccount);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.put("Amount", this.amount.save());
		compound.putString("Account", Component.Serializer.toJson(this.accountName));
		compound.putString("Other", Component.Serializer.toJson(this.otherAccount));
		compound.putBoolean("Received", this.wasReceived);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.amount = MoneyValue.safeLoad(compound, "Amount");
		this.accountName = Component.Serializer.fromJson(compound.getString("Account"));
		this.otherAccount = Component.Serializer.fromJson(compound.getString("Other"));
		this.wasReceived = compound.getBoolean("Received");
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof BankTransferNotification n)
		{
			return n.player.is(this.player) && n.amount.equals(this.amount) && n.accountName.equals(this.accountName) && n.otherAccount.equals(this.otherAccount) && n.wasReceived == this.wasReceived;
		}
		return false;
	}

}
