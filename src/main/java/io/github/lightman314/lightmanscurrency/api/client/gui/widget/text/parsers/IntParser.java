package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;

import java.util.function.Function;
import java.util.function.Supplier;

public class IntParser implements Function<String,Integer>  {

    public static final IntParser DEFAULT = builder().build();
    public static final IntParser ONE_TO_ONE_HUNDRED = builder().min(1).max(100).build();

    private final Supplier<Integer> minValue;
    private final Supplier<Integer> maxValue;
    private final Supplier<Integer> emptyValue;
    private IntParser(Builder builder) {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Integer apply(String text) {
        Integer val = NumberHelper.getInteger(text,this.emptyValue.get());
        return val == null ? null : Math.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Supplier<Integer> minValue = () -> Integer.MIN_VALUE;
        private Supplier<Integer> maxValue = () -> Integer.MAX_VALUE;
        private Supplier<Integer> emptyValue = () -> null;

        public Builder min(int minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Integer> minValue) { this.minValue = minValue; return this; }
        public Builder max(int maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Integer> maxValue) { this.maxValue = maxValue; return this; }
        public Builder empty(int emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Integer> emptyValue) { this.emptyValue = emptyValue; return this; }

        public IntParser build() { return new IntParser(this); }

    }

}
