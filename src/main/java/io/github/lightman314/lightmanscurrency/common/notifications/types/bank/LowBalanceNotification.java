package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class LowBalanceNotification extends SingleLineNotification {

	public static final NotificationType<LowBalanceNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("bank_low_balance"),LowBalanceNotification::new);
	
	private MutableComponent accountName;
	private MoneyValue value = MoneyValue.empty();

	private LowBalanceNotification() { }

	protected LowBalanceNotification(MutableComponent accountName, MoneyValue value) {
		this.accountName = accountName;
		this.value = value;
	}

	public static Supplier<Notification> create(@Nonnull MutableComponent accountName, @Nonnull MoneyValue value) { return () -> new LowBalanceNotification(accountName,value); }

	@Nonnull
    @Override
	protected NotificationType<LowBalanceNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Nonnull
	@Override
	public MutableComponent getMessage() { return LCText.NOTIFICATION_BANK_LOW_BALANCE.get(this.value.getText()); }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.putString("Name", Component.Serializer.toJson(this.accountName,lookup));
		compound.put("Amount", this.value.save());
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
		this.value = MoneyValue.safeLoad(compound, "Amount");
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
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
