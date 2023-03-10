package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class LowBalanceNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_low_balance");
	
	private IFormattableTextComponent accountName;
	private CoinValue value = new CoinValue();
	
	public LowBalanceNotification(IFormattableTextComponent accountName, CoinValue value) {
		this.accountName = accountName;
		this.value = value;
	}
	
	public LowBalanceNotification(CompoundNBT compound) { this.load(compound); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public IFormattableTextComponent getMessage() {
		return EasyText.translatable("notifications.message.bank_low_balance", this.value.getString());
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.putString("Name", ITextComponent.Serializer.toJson(this.accountName));
		this.value.save(compound, "Amount");
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.accountName = ITextComponent.Serializer.fromJson(compound.getString("Name"));
		this.value.load(compound, "Amount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof LowBalanceNotification)
		{
			LowBalanceNotification lbn = (LowBalanceNotification)other;
			if(!lbn.accountName.getString().equals(this.accountName.getString()))
				return false;
			if(lbn.value.getRawValue() != this.value.getRawValue())
				return false;
			//Passed all the checks.
			return true;
		}
		return false;
	}
	
}