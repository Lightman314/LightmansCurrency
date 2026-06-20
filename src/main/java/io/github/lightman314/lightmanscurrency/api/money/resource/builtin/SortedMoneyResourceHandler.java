package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SortedMoneyResourceHandler extends IterableMoneyResourceHandler {

    private final List<SortableMoneyResourceHandler> holdersExtractFirst;
    private final List<SortableMoneyResourceHandler> holdersInsertFirst;

    public SortedMoneyResourceHandler(Collection<SortableMoneyResourceHandler> holders)
    {
        this.holdersExtractFirst = new ArrayList<>(holders);
        SortableMoneyResourceHandler.sortExtractFirst(this.holdersExtractFirst);
        this.holdersInsertFirst = new ArrayList<>(holders);
        SortableMoneyResourceHandler.sortInsertFirst(this.holdersInsertFirst);
    }

    @Override
    public List<MoneyValue> getAllResources() { return MoneyView.wrap(this.holdersExtractFirst).getAllResources(); }
    @Override
    public MoneyValue getResource(MoneyKey key) { return MoneyView.wrap(this.holdersExtractFirst).getResource(key); }

    public List<Component> getTooltips() {
        List<Component> result = new ArrayList<>();
        for(SortableMoneyResourceHandler child : this.holdersExtractFirst)
            child.formatTooltip(result::add);
        return result;
    }

    @Override
    protected Iterable<? extends MoneyResourceHandler> insertIterable() { return this.holdersInsertFirst; }
    @Override
    protected Iterable<? extends MoneyResourceHandler> extractIterable() { return this.holdersExtractFirst; }

}
