package io.github.lightman314.lightmanscurrency.api.money;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;

import java.util.*;

/**
 * A helper class used to combine multiple Money Values into a single instance for easy viewing.<br>
 * Implements {@link MoneyResourceHandler} so that {@link MoneyDisplayHelper} utilities can be utilized,
 * however this should not be treated as such in most cases.
 */
public class MoneyView extends MoneyResourceHandler.ViewOnly {

    private static final MoneyView EMPTY = new MoneyView();

    private final ImmutableMap<MoneyKey,MoneyValue> values;

    private MoneyView() { this.values = ImmutableMap.of(); }
    private MoneyView(Builder builder) {
        ImmutableMap.Builder<MoneyKey,MoneyValue> temp = ImmutableMap.builderWithExpectedSize(builder.values.size());
        builder.values.forEach((key,list) -> {
            MoneyValue result = MoneyValueHelper.quickSumValues(list);
            if(!result.isEmpty())
                temp.put(result.getKey(),result);
        });
        this.values = temp.build();
    }

    @Override
    public List<MoneyValue> getAllResources() { return new ArrayList<>(this.values.values()); }
    @Override
    public MoneyValue getResource(MoneyKey key) { return this.values.getOrDefault(key,MoneyValue.empty()); }

    public static MoneyView wrap(Iterable<? extends MoneyResourceHandler> handlers)
    {
        Builder builder = builder();
        for(MoneyResourceHandler handler : handlers)
            builder.merge(handler);
        return builder.build();
    }

    public static MoneyView simple(MoneyValue value) { return builder().add(value).build(); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder
    {
        private final Map<MoneyKey,List<MoneyValue>> values = new HashMap<>();
        private Builder() {}

        public Builder merge(MoneyResourceHandler handler) { return this.add(handler.getAllResources()); }
        public Builder merge(Builder builder) { builder.values.forEach((key,list) -> this.add(list)); return this; }

        public Builder add(MoneyValue value)
        {
            if(value.isEmpty() || value.isFree() || value.isInvalid())
                return this;
            MoneyKey key = value.getKey();
            List<MoneyValue> list = this.values.getOrDefault(key,new ArrayList<>());
            list.add(value);
            this.values.put(key,list);
            return this;
        }
        public Builder add(Collection<MoneyValue> values)
        {
            for(MoneyValue value : values)
                this.add(value);
            return this;
        }

        public MoneyView build() { return new MoneyView(this); }

    }

}
