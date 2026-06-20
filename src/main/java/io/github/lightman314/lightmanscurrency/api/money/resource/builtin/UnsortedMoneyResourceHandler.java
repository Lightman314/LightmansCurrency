package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;

import java.util.List;

public class UnsortedMoneyResourceHandler extends IterableMoneyResourceHandler {

    private final List<MoneyResourceHandler> children;
    public UnsortedMoneyResourceHandler(List<? extends MoneyResourceHandler> children) { this.children = ImmutableList.copyOf(children); }

    @Override
    public List<MoneyValue> getAllResources() { return MoneyView.wrap(this.children).getAllResources(); }

    @Override
    public MoneyValue getResource(MoneyKey key) { return MoneyView.wrap(this.children).getResource(key); }

    @Override
    protected Iterable<? extends MoneyResourceHandler> insertIterable() { return this.children; }
    @Override
    protected Iterable<? extends MoneyResourceHandler> extractIterable() { return this.children; }

}
