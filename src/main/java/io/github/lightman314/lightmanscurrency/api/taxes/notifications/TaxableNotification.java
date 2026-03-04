package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart2;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class TaxableNotification extends Notification {

    private MoneyValue taxesPaid = MoneyValue.empty();
    public MoneyValue getTaxesPaid() { return this.taxesPaid; }

    protected TaxableNotification(MoneyValue taxesPaid) { this.taxesPaid = taxesPaid; }
    protected TaxableNotification() {}
    protected TaxableNotification(MoneyValue taxesPaid, CommonData data) { super(data); this.taxesPaid = taxesPaid; }
    
    @Override
    public final List<Component> getMessageLines() {
        List<Component> lines = new ArrayList<>(this.getNormalMessageLines());
        if(!this.taxesPaid.isEmpty())
            lines.add(LCText.NOTIFICATION_TAXES_PAID.get(this.taxesPaid.getText("ERROR")));
        return lines;
    }

    protected abstract List<Component> getNormalMessageLines();

    @Override
    protected final void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.taxesPaid = MoneyValue.safeLoad(compound, "TaxesPaid");
        this.loadNormal(compound, lookup);
    }

    protected abstract void loadNormal(CompoundTag compound, HolderLookup.Provider lookup);

    protected boolean TaxesMatch(TaxableNotification other) { return other.taxesPaid.equals(this.taxesPaid); }

    protected static <T extends TaxableNotification> Products.P2<RecordCodecBuilder.Mu<T>,MoneyValue,CommonData> taxableFields(RecordCodecBuilder.Instance<T> builder)
    {
        return builder.group(
                MoneyValue.CODEC.fieldOf("taxesPaid").forGetter(TaxableNotification::getTaxesPaid),
                baseFields());
    }

    protected static <T extends TaxableNotification> SPart2<RegistryFriendlyByteBuf,T,MoneyValue, CommonData> taxableStreamFields()
    {
        return SPart2.of(baseStreamFields(),MoneyValue.STREAM_CODEC,TaxableNotification::getTaxesPaid);
    }



}
