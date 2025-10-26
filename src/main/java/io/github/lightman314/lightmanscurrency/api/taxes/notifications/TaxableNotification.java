package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TaxableNotification extends Notification {

    private MoneyValue taxesPaid = MoneyValue.empty();

    protected TaxableNotification(MoneyValue taxesPaid) { this.taxesPaid = taxesPaid; }

    protected TaxableNotification() {}
    
    @Override
    public final List<Component> getMessageLines() {
        List<Component> lines = new ArrayList<>(this.getNormalMessageLines());
        if(!this.taxesPaid.isEmpty())
            lines.add(LCText.NOTIFICATION_TAXES_PAID.get(this.taxesPaid.getText("ERROR")));
        return lines;
    }

    protected abstract List<Component> getNormalMessageLines();

    @Override
    protected final void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.saveNormal(compound, lookup);
        compound.put("TaxesPaid", this.taxesPaid.save());
    }

    protected abstract void saveNormal(CompoundTag compound, HolderLookup.Provider lookup);

    @Override
    protected final void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.taxesPaid = MoneyValue.safeLoad(compound, "TaxesPaid");
        this.loadNormal(compound, lookup);
    }

    protected abstract void loadNormal(CompoundTag compound, HolderLookup.Provider lookup);

    protected boolean TaxesMatch(TaxableNotification other) { return other.taxesPaid.equals(this.taxesPaid); }


}
