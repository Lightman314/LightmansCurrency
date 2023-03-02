package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class LowBalanceNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_low_balance");
	
	private MutableComponent accountName;
	private CoinValue value = new CoinValue();
	
	public LowBalanceNotification(MutableComponent accountName, CoinValue value) {
		this.accountName = accountName;
		this.value = value;
	}
	
	public LowBalanceNotification(CompoundTag compound) { this.load(compound); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public MutableComponent getMessage() {
		return new TranslatableComponent("notifications.message.bank_low_balance", this.value.getString());
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", Component.Serializer.toJson(this.accountName));
		this.value.save(compound, "Amount");
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"));
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
			//Passed all of the checks.
			return true;
		}
		return false;
	}
	
}