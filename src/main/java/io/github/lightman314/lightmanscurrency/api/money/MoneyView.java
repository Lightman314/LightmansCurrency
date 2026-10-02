package io.github.lightman314.lightmanscurrency.api.money;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * A helper class used to combine multiple Money Values into a single instance for easy viewing.<br>
 * Implements {@link MoneyResourceHandler} so that {@link MoneyDisplayHelper} utilities can be utilized,
 * however no insert/extract methods will function.
 */
public final class MoneyView extends MoneyResourceHandler.ViewOnly {

    public static final Codec<MoneyView> CODEC = MoneyValue.SET_CODEC.xmap(MoneyView::new,v -> v.values);
    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyView> STREAM_CODEC = MoneyValue.SET_STREAM_CODEC.map(MoneyView::new,v -> v.values);

    public static final MoneyView EMPTY = new MoneyView();

    private final ImmutableMap<DualKey,MoneyValue> values;

    private MoneyView() { this.values = ImmutableMap.of(); }
    private MoneyView(Map<DualKey,MoneyValue> values) { this.values = ImmutableMap.copyOf(values); }
    private MoneyView(Builder builder) {
        ImmutableMap.Builder<DualKey,MoneyValue> temp = ImmutableMap.builderWithExpectedSize(builder.values.size());
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
    public MoneyValue getResource(DualKey key) { return this.values.getOrDefault(key,MoneyValue.empty()); }

    public Builder makeMutable() { return builder().merge(this); }

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
        private final Map<DualKey,List<MoneyValue>> values = new HashMap<>();
        private Builder() {}

        public Builder merge(MoneyResourceHandler handler) { return this.add(handler.getAllResources()); }
        public Builder merge(Builder builder) { builder.values.forEach((key,list) -> this.add(list)); return this; }

        public Builder add(MoneyValue value)
        {
            if(value.isEmpty() || value.isFree() || value.isInvalid())
                return this;
            DualKey key = value.getKey();
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
