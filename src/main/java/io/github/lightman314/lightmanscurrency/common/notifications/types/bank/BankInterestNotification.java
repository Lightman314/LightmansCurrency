package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BankInterestNotification extends SingleLineNotification {

    public static final NotificationType<BankInterestNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("bank_interest"), BankInterestNotification::new);

    protected MutableComponent accountName;
    protected MoneyValue amount;

    protected BankInterestNotification() {}
    protected BankInterestNotification(@Nonnull MutableComponent accountName, @Nonnull MoneyValue amount)
    {
        this.accountName = accountName;
        this.amount = amount;
    }

    public static Supplier<Notification> create(@Nonnull MutableComponent accountName, @Nonnull MoneyValue amount) { return () -> new BankInterestNotification(accountName,amount); }

    @Nonnull
    @Override
    protected NotificationType<BankInterestNotification> getType() { return TYPE; }

    @Nonnull
    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Nonnull
    @Override
    public MutableComponent getMessage() { return LCText.NOTIFICATION_BANK_INTEREST.get(this.amount.getText()); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putString("Name", Component.Serializer.toJson(this.accountName,lookup));
        compound.put("Amount", this.amount.save());
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
        this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
        this.amount = MoneyValue.safeLoad(compound, "Amount");
    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) { return false; }

}
