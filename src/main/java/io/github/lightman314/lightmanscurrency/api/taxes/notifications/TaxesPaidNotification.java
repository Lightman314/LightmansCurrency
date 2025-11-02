package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class TaxesPaidNotification extends SingleLineNotification {

    public static final NotificationType<TaxesPaidNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("taxes_paid"),TaxesPaidNotification::new);

    private MoneyValue amount = MoneyValue.empty();
    private NotificationCategory category = NotificationCategory.GENERAL;

    private TaxesPaidNotification() {}
    private TaxesPaidNotification(MoneyValue amount, NotificationCategory category) { this.amount = amount; this.category = category;  }

    public static NonNullSupplier<Notification> create(MoneyValue amount, NotificationCategory category) { return () -> new TaxesPaidNotification(amount, category); }

    @Nonnull
    @Override
    protected NotificationType<TaxesPaidNotification> getType() { return TYPE; }

    @Nonnull
    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Nonnull
    @Override
    public Component getMessage() {
        if(this.amount.isEmpty())
            return LCText.NOTIFICATION_TAXES_PAID_NULL.get();
        else
            return LCText.NOTIFICATION_TAXES_PAID.get(this.amount.getText());
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound)
    {
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save());
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound)
    {
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = NotificationAPI.getApi().LoadCategory(compound.getCompound("Category"));
    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) {
        if(other instanceof TaxesPaidNotification tpn)
            return tpn.amount.equals(this.amount) && tpn.category.matches(this.category);
        return false;
    }
}
