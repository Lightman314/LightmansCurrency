package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TaxEntryCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class TaxesCollectedNotification extends SingleLineNotification {

    public static final NotificationType<TaxesCollectedNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "taxes_collected"),TaxesCollectedNotification::new);

    private MutableComponent taxedName = EasyText.literal("NULL");
    private MoneyValue amount = MoneyValue.empty();
    private TaxEntryCategory category;

    private TaxesCollectedNotification() {}
    private TaxesCollectedNotification(MutableComponent taxedName, MoneyValue amount, TaxEntryCategory category) { this.taxedName = taxedName; this.amount = amount; this.category = category; }

    public static NonNullSupplier<Notification> create(MutableComponent taxedName, MoneyValue amount, TaxEntryCategory category) { return () -> new TaxesCollectedNotification(taxedName, amount, category); }

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
    protected void saveAdditional(@Nonnull CompoundTag compound) {
        compound.putString("TaxedName", Component.Serializer.toJson(this.taxedName));
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save());
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound) {
        this.taxedName = Component.Serializer.fromJson(compound.getString("TaxedName"));
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = new TaxEntryCategory(compound.getCompound("Category"));
    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) {
        if(other instanceof TaxesCollectedNotification tcn)
            return tcn.taxedName.getString().equals(this.taxedName.getString()) && tcn.amount.equals(this.amount) && tcn.category.matches(this.category);
        return false;
    }

}
