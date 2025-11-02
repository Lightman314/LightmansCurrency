package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LowBalanceNotification extends SingleLineNotification {

	public static final NotificationType<LowBalanceNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("bank_low_balance"),LowBalanceNotification::new);
	
	private Component accountName;
	private MoneyValue value = MoneyValue.empty();

	private LowBalanceNotification() { }

	protected LowBalanceNotification(Component accountName, MoneyValue value) {
		this.accountName = accountName;
		this.value = value;
	}

	public static Supplier<Notification> create(Component accountName, MoneyValue value) { return () -> new LowBalanceNotification(accountName,value); }

    @Override
	protected NotificationType<LowBalanceNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public Component getMessage() { return LCText.NOTIFICATION_BANK_LOW_BALANCE.get(this.value.getString()); }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", Component.Serializer.toJson(this.accountName));
		compound.put("Amount", this.value.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"));
		this.value = MoneyValue.safeLoad(compound, "Amount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof LowBalanceNotification lbn)
		{
			if(!lbn.accountName.getString().equals(this.accountName.getString()))
				return false;
			if(!lbn.value.equals(this.value))
				return false;
			//Passed all the checks.
			return true;
		}
		return false;
	}
	
	
	
}
