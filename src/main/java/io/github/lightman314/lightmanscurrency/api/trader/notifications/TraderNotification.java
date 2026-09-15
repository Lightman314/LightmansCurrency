package io.github.lightman314.lightmanscurrency.api.trader.notifications;

import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TraderNotification extends Notification {

    protected final TraderCategory category;
    public TraderNotification(TraderCategory category) { this.category = category; }

    @Override
    public final TraderCategory getCategory() { return this.category; }

    protected boolean parentDataMatches(TraderNotification other) { return this.category.equals(other.category); }
    protected int hashParentData() { return this.category.hashCode(); }

    public static abstract class SingleLine extends TraderNotification {

        public SingleLine(TraderCategory category) { super(category); }

        @Override
        public final List<Component> getMessageLines() { return List.of(this.getMessage()); }
        protected abstract Component getMessage();

    }

    public static abstract class Taxable extends TraderNotification {

        private final MoneyValue taxesPaid;
        public final MoneyValue getTaxesPaid() { return this.taxesPaid; }

        public Taxable(TraderCategory category,MoneyValue taxesPaid) { super(category); this.taxesPaid = taxesPaid; }

        @Override
        public final List<Component> getMessageLines() {
            List<Component> list = new ArrayList<>(this.getNormalMessageLines());
            if(!this.taxesPaid.isEmpty())
                list.add(TaxesPaidNotification.TEXT.get(this.taxesPaid.getText()));
            return list;
        }

        protected abstract List<Component> getNormalMessageLines();

        protected boolean parentDataMatches(Taxable other) { return super.parentDataMatches(other) && this.taxesPaid.equals(other.taxesPaid); }

        protected int hashParentData() { return Objects.hash(super.hashParentData(),this.taxesPaid); }

    }

    public static abstract class TaxableSingleLine extends Taxable {

        public TaxableSingleLine(TraderCategory category, MoneyValue taxesPaid) { super(category, taxesPaid); }

        @Override
        protected final List<Component> getNormalMessageLines() { return List.of(this.getMessage()); }

        protected abstract Component getMessage();

    }

}