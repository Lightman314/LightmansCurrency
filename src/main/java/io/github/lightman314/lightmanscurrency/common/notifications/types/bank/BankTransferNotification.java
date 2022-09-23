package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class BankTransferNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_transfer");
	
	PlayerReference player;
	CoinValue amount = new CoinValue();
	MutableComponent accountName;
	MutableComponent otherAccount;
	boolean wasReceived;
	
	public BankTransferNotification(CompoundTag compound) { this.load(compound); }
	public BankTransferNotification(PlayerReference player, CoinValue amount, MutableComponent accountName, MutableComponent otherAccount, boolean wasReceived) {
		this.player = player;
		this.amount = amount;
		this.accountName = accountName;
		this.otherAccount = otherAccount;
		this.wasReceived = wasReceived;
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public MutableComponent getMessage() {
		return new TranslatableComponent("log.bank.transfer", this.player.getName(true), this.amount.getComponent(), new TranslatableComponent(this.wasReceived ? "log.bank.transfer.from" : "log.bank.transfer.to"), this.otherAccount);
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		this.amount.save(compound, "Amount");
		compound.putString("Account", Component.Serializer.toJson(this.accountName));
		compound.putString("Other", Component.Serializer.toJson(this.otherAccount));
		compound.putBoolean("Received", this.wasReceived);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.amount.load(compound, "Amount");
		this.accountName = Component.Serializer.fromJson(compound.getString("Account"));
		this.otherAccount = Component.Serializer.fromJson(compound.getString("Other"));
		this.wasReceived = compound.getBoolean("Received");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof BankTransferNotification)
		{
			BankTransferNotification n = (BankTransferNotification)other;
			return n.player.is(this.player) && n.amount.equals(this.amount) && n.accountName.equals(this.accountName) && n.otherAccount.equals(this.otherAccount) && n.wasReceived == this.wasReceived;
		}
		return false;
	}

}