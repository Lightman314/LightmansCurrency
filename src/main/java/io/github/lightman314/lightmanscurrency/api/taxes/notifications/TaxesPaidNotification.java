package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TaxesPaidNotification extends SingleLineNotification {

    public static final NotificationType<TaxesPaidNotification> TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "taxes_paid"),TaxesPaidNotification::new);

    private MoneyValue amount = MoneyValue.empty();
    private NotificationCategory category = NotificationCategory.GENERAL;

    private TaxesPaidNotification() {}
    private TaxesPaidNotification(MoneyValue amount, NotificationCategory category) { this.amount = amount; this.category = category;  }

    public static Supplier<Notification> create(MoneyValue amount, NotificationCategory category) { return () -> new TaxesPaidNotification(amount, category); }

    @Nonnull
    @Override
    protected NotificationType<TaxesPaidNotification> getType() { return TYPE; }

    @Nonnull
    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Nonnull
    @Override
    public MutableComponent getMessage() {
        if(this.amount.isEmpty())
            return LCText.NOTIFICATION_TAXES_PAID_NULL.get();
        else
            return LCText.NOTIFICATION_TAXES_PAID.get(this.amount.getText());
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
    {
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save(lookup));
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
    {
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = NotificationAPI.API.LoadCategory(compound.getCompound("Category"),lookup);
    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) {
        if(other instanceof TaxesPaidNotification tpn)
            return tpn.amount.equals(this.amount) && tpn.category.matches(this.category);
        return false;
    }
}
