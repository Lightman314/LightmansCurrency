package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LongParser implements Function<String,Long>, Predicate<String>  {

    public static final LongParser DEFAULT = builder().build();

    private final Supplier<Long> minValue;
    private final Supplier<Long> maxValue;
    private final Supplier<Long> emptyValue;
    private LongParser(Builder builder) {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Long apply(String text) {
        Long val = NumberHelper.getLong(text,this.emptyValue.get());
        return val == null ? null : Math.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    @Override
    public boolean test(String text) {
        if(NumberHelper.isLong(text))
        {
            Long val = NumberHelper.getLong(text,null);
            return val != null && val >= this.minValue.get() && val <= this.maxValue.get();
        }
        return text.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Supplier<Long> minValue = () -> Long.MIN_VALUE;
        private Supplier<Long> maxValue = () -> Long.MAX_VALUE;
        private Supplier<Long> emptyValue = () -> null;

        public Builder min(long minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Long> minValue) { this.minValue = minValue; return this; }
        public Builder max(long maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Long> maxValue) { this.maxValue = maxValue; return this; }
        public Builder empty(long emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Long> emptyValue) { this.emptyValue = emptyValue; return this; }

        public LongParser build() { return new LongParser(this); }

    }

}
