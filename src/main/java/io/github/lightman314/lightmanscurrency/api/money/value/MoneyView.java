package io.github.lightman314.lightmanscurrency.api.money.value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.*;

public final class MoneyView {

    private static final MoneyView EMPTY = new MoneyView();

    private final Map<String,MoneyValue> values;

    private MoneyView() { this.values = ImmutableMap.of(); }
    private MoneyView(Builder builder) {
        Map<String,MoneyValue> results = new HashMap<>();
        builder.values.forEach((name,list) -> {
            if(!list.isEmpty())
            {
                MoneyValue firstVal = list.get(0);
                MoneyValue sum = firstVal.getCurrency().sumValues(list);
                if(sum != null && !sum.isEmpty() && !sum.isFree() && !sum.isInvalid())
                    results.put(name, sum);
            }
        });
        this.values = ImmutableMap.copyOf(results);
    }

    public static MoneyView.Builder builder() { return new MoneyView.Builder(); }
    public static MoneyView empty() { return EMPTY; }

    @Nonnull
    public List<MoneyValue> allValues() { return ImmutableList.copyOf(this.values.values()); }

    /**
     * Returns the stored value with the name requested.
     */
    @Nonnull
    public MoneyValue valueOf(@Nonnull String uniqueName) { return this.values.getOrDefault(uniqueName, MoneyValue.empty()); }

    /**
     * Whether the given amount of money (or more) is currently stored in the value holder.
     */
    public boolean containsValue(@Nonnull MoneyValue value) { return this.valueOf(value.getUniqueName()).containsValue(value); }

    /**
     * Returns the maximum amount of money that can be taken
     */
    @Nonnull
    public MoneyValue capValue(@Nonnull MoneyValue value) { return this.containsValue(value) ? value : this.valueOf(value.getUniqueName()); }

    public boolean isEmpty() { return this.values.isEmpty() || this.values.values().stream().allMatch(MoneyValue::isEmpty); }

    public String getString()
    {
        StringBuilder builder = new StringBuilder();
        for(MoneyValue value : this.allValues())
        {
            if(!value.isEmpty())
            {
                if(!builder.isEmpty())
                    builder.append('\n');
                builder.append(value.getString());
            }
        }
        return builder.toString();
    }

    @Nonnull
    public MoneyValue getRandomValue() {
        if(this.values.isEmpty())
            return MoneyValue.empty();
        List<MoneyValue> values = this.values.values().stream().toList();
        int displayIndex = (int)(TimeUtil.getCurrentTime() / 2000 % values.size());
        return values.get(displayIndex);
    }

    @Nonnull
    public MutableComponent getRandomValueText() { return this.getRandomValueText(EasyText.translatable("gui.lightmanscurrency.bank.balance.empty")); }
    @Nonnull
    public MutableComponent getRandomValueText(@Nonnull String emptyText) { return this.getRandomValueText(EasyText.literal(emptyText)); }
    @Nonnull
    public MutableComponent getRandomValueText(@Nonnull MutableComponent emptyText)
    {
        if(this.values.isEmpty())
            return emptyText;
        return this.getRandomValue().getText();
    }

    public static final class Builder
    {

        private final Map<String,List<MoneyValue>> values = new HashMap<>();

        private Builder() {}

        @Nonnull
        public Builder merge(@Nonnull IMoneyViewer storage) { this.add(storage.getStoredMoney().allValues()); return this; }
        @Nonnull
        public Builder merge(@Nonnull Builder values) { values.values.forEach((name,list) -> this.add(list)); return this; }
        @Nonnull
        public Builder merge(@Nonnull MoneyView values) { this.add(values.allValues()); return this; }

        /**
         * Adds the given {@link MoneyValue}.
         */
        @Nonnull
        public Builder add(@Nonnull MoneyValue value)
        {
            if(value.isEmpty() || value.isFree() || value.isInvalid())
                return this;
            String name = value.getUniqueName();
            List<MoneyValue> list = this.values.getOrDefault(name, new ArrayList<>());
            list.add(value);
            if(!this.values.containsKey(name))
                this.values.put(name, list);
            return this;
        }

        /**
         * Adds all the given {@link MoneyValue MoneyValues}.
         */
        @Nonnull
        public Builder add(@Nonnull Collection<MoneyValue> values)
        {
            for(MoneyValue val : values)
            {
                if(val != null)
                    this.add(val);
            }
            return this;
        }

        @Nonnull
        public MoneyView build() { return new MoneyView(this); }

    }

}