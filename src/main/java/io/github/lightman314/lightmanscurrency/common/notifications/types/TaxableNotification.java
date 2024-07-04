package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public abstract class TaxableNotification extends Notification {

    private MoneyValue taxesPaid = MoneyValue.empty();

    protected TaxableNotification(@Nonnull MoneyValue taxesPaid) { this.taxesPaid = taxesPaid; }

    protected TaxableNotification() {}

    @Nonnull
    @Override
    public final MutableComponent getMessage() {
        if(this.taxesPaid.isEmpty())
            return this.getNormalMessage();
        return this.getNormalMessage().append("\n").append(LCText.NOTIFICATION_TAXES_PAID.get(this.taxesPaid.getText("ERROR")));
    }

    @Nonnull
    protected abstract MutableComponent getNormalMessage();

    @Override
    protected final void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        this.saveNormal(compound, lookup);
        compound.put("TaxesPaid", this.taxesPaid.save());
    }

    protected abstract void saveNormal(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup);

    @Override
    protected final void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        this.taxesPaid = MoneyValue.safeLoad(compound, "TaxesPaid");
        this.loadNormal(compound, lookup);
    }

    protected abstract void loadNormal(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup);

    protected boolean TaxesMatch(TaxableNotification other) { return other.taxesPaid.equals(this.taxesPaid); }

}
