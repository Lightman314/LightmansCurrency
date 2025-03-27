package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

@MethodsReturnNonnullByDefault
public abstract class SingleLineTaxableNotification extends TaxableNotification {

    protected SingleLineTaxableNotification(MoneyValue taxesPaid) { super(taxesPaid); }
    protected SingleLineTaxableNotification() { }

    @Override
    protected final List<MutableComponent> getNormalMessageLines() { return Lists.newArrayList(this.getNormalMessage()); }
    protected abstract MutableComponent getNormalMessage();

}