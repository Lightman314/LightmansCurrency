package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class SingleLineTaxableNotification extends TaxableNotification {

    protected SingleLineTaxableNotification(MoneyValue taxesPaid) { super(taxesPaid); }
    protected SingleLineTaxableNotification() { }
    protected SingleLineTaxableNotification(MoneyValue taxesPaid, CommonData data) { super(taxesPaid,data); }

    @Override
    protected final List<Component> getNormalMessageLines() { return Lists.newArrayList(this.getNormalMessage()); }
    protected abstract Component getNormalMessage();

}
