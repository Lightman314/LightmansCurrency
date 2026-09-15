package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class TaxableNotification extends Notification {

    private final MoneyValue taxesPaid;
    public final MoneyValue getTaxesPaid() { return this.taxesPaid; }

    public TaxableNotification(MoneyValue taxesPaid) { this.taxesPaid = taxesPaid; }

    @Override
    public final List<Component> getMessageLines() {
        List<Component> list = new ArrayList<>(this.getNormalMessageLines());
        if(!this.taxesPaid.isEmpty())
            list.add(TaxesPaidNotification.TEXT.get(this.taxesPaid.getText()));
        return list;
    }

    protected abstract List<Component> getNormalMessageLines();

    protected boolean parentDataMatches(TaxableNotification other) { return this.taxesPaid.equals(other.taxesPaid); }
    protected int hashParentData() { return this.taxesPaid.hashCode(); }

    public static abstract class SingleLine extends TaxableNotification {

        public SingleLine(MoneyValue taxesPaid) { super(taxesPaid); }

        @Override
        protected final List<Component> getNormalMessageLines() { return List.of(this.getMessage()); }

        protected abstract Component getMessage();

    }

}