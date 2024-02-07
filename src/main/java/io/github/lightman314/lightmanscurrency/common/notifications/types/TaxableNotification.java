package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
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
        return this.getNormalMessage().append("\n").append(EasyText.translatable("notifications.message.taxes.paid", this.taxesPaid.getText("ERROR")));
    }

    @Nonnull
    protected abstract MutableComponent getNormalMessage();

    @Override
    protected final void saveAdditional(@Nonnull CompoundTag compound) {
        this.saveNormal(compound);
        compound.put("TaxesPaid", this.taxesPaid.save());
    }

    protected abstract void saveNormal(CompoundTag compound);

    @Override
    protected final void loadAdditional(@Nonnull CompoundTag compound) {
        this.taxesPaid = MoneyValue.safeLoad(compound, "TaxesPaid");
        this.loadNormal(compound);
    }

    protected abstract void loadNormal(CompoundTag compound);

    protected boolean TaxesMatch(TaxableNotification other) { return other.taxesPaid.equals(this.taxesPaid); }

}
