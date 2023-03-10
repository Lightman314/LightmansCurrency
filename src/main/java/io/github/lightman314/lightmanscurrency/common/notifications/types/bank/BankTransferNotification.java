package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class BankTransferNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_transfer");
	
	PlayerReference player;
	CoinValue amount = new CoinValue();
	IFormattableTextComponent accountName;
	IFormattableTextComponent otherAccount;
	boolean wasReceived;
	
	public BankTransferNotification(CompoundNBT compound) { this.load(compound); }
	public BankTransferNotification(PlayerReference player, CoinValue amount, IFormattableTextComponent accountName, IFormattableTextComponent otherAccount, boolean wasReceived) {
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
	public IFormattableTextComponent getMessage() {
		return EasyText.translatable("log.bank.transfer", this.player.getName(true), this.amount.getComponent(), EasyText.translatable(this.wasReceived ? "log.bank.transfer.from" : "log.bank.transfer.to"), this.otherAccount);
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.put("Player", this.player.save());
		this.amount.save(compound, "Amount");
		compound.putString("Account", ITextComponent.Serializer.toJson(this.accountName));
		compound.putString("Other", ITextComponent.Serializer.toJson(this.otherAccount));
		compound.putBoolean("Received", this.wasReceived);
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.amount.load(compound, "Amount");
		this.accountName = ITextComponent.Serializer.fromJson(compound.getString("Account"));
		this.otherAccount = ITextComponent.Serializer.fromJson(compound.getString("Other"));
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