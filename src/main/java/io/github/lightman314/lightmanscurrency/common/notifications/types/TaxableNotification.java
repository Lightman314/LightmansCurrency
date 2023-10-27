package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public abstract class TaxableNotification extends Notification {

    private CoinValue taxesPaid = CoinValue.EMPTY;

    protected TaxableNotification(@Nonnull CoinValue taxesPaid) { this.taxesPaid = taxesPaid; }

    /**
     * Blank init for loading.
     * Not loading in this init, as it can result in values being loaded and
     * then reset to a default value after they were already defined in the load call.
     */
    protected TaxableNotification() { }

    @Override
    public final MutableComponent getMessage() {
        if(this.taxesPaid.hasAny())
            return this.getNormalMessage().append("\n").append(EasyText.translatable("notifications.message.taxes.paid", this.taxesPaid.getComponent("ERROR")));
        return this.getNormalMessage();
    }

    @Nonnull
    protected abstract MutableComponent getNormalMessage();

    @Override
    protected final void saveAdditional(CompoundTag compound) {
        this.saveNormal(compound);
        compound.put("TaxesPaid", this.taxesPaid.save());
    }

    protected abstract void saveNormal(CompoundTag compound);

    @Override
    protected final void loadAdditional(CompoundTag compound) {
        this.taxesPaid = CoinValue.safeLoad(compound, "TaxesPaid");
        this.loadNormal(compound);
    }

    protected abstract void loadNormal(CompoundTag compound);

    protected boolean TaxesMatch(TaxableNotification other) { return other.taxesPaid.equals(this.taxesPaid); }

}