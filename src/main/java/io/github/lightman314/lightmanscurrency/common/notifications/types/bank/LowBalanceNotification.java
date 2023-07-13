package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class LowBalanceNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_low_balance");
	
	private MutableComponent accountName;
	private CoinValue value = CoinValue.EMPTY;
	
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
		return EasyText.translatable("notifications.message.bank_low_balance", this.value.getString());
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", EasyText.Serializer.toJson(this.accountName));
		compound.put("Amount", this.value.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.accountName = EasyText.Serializer.fromJson(compound.getString("Name"));
		this.value = CoinValue.safeLoad(compound, "Amount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof LowBalanceNotification lbn)
		{
			if(!lbn.accountName.getString().equals(this.accountName.getString()))
				return false;
			if(lbn.value.getValueNumber() != this.value.getValueNumber())
				return false;
			//Passed all the checks.
			return true;
		}
		return false;
	}
	
	
	
}
