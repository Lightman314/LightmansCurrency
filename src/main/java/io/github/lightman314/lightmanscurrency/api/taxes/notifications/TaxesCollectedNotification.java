package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TaxEntryCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TaxesCollectedNotification extends SingleLineNotification {

    public static final NotificationType<TaxesCollectedNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("taxes_collected"),TaxesCollectedNotification::new);

    private MutableComponent taxedName = EasyText.literal("NULL");
    private MoneyValue amount = MoneyValue.empty();
    private TaxEntryCategory category;

    private TaxesCollectedNotification() {}
    private TaxesCollectedNotification(MutableComponent taxedName, MoneyValue amount, TaxEntryCategory category) { this.taxedName = taxedName; this.amount = amount; this.category = category; }

    public static Supplier<Notification> create(MutableComponent taxedName, MoneyValue amount, TaxEntryCategory category) { return () -> new TaxesCollectedNotification(taxedName, amount, category); }

    @Nonnull
    @Override
    protected NotificationType<TaxesCollectedNotification> getType() { return TYPE; }

    @Nonnull
    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Nonnull
    @Override
    public MutableComponent getMessage() { return LCText.NOTIFICATION_TAXES_COLLECTED.get(this.amount.getText("NULL"), this.taxedName); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putString("TaxedName", Component.Serializer.toJson(this.taxedName, lookup));
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save(lookup));
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        this.taxedName = Component.Serializer.fromJson(compound.getString("TaxedName"), lookup);
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = new TaxEntryCategory(compound.getCompound("Category"), lookup);
    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) {
        if(other instanceof TaxesCollectedNotification tcn)
            return tcn.taxedName.getString().equals(this.taxedName.getString()) && tcn.amount.equals(this.amount) && tcn.category.matches(this.category);
        return false;
    }

}
