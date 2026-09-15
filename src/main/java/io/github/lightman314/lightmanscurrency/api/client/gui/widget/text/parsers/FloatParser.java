package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FloatParser implements Function<String,Float>, Predicate<String>  {

    public static final FloatParser DEFAULT = builder().build();

    public static final Function<Float,String> FLOAT_WRITER = f -> {
        if(f % 1f == 0f)
            return String.valueOf(f.intValue());
        return String.valueOf(f);
    };

    private final Supplier<Float> minValue;
    private final Supplier<Float> maxValue;
    private final Supplier<Float> emptyValue;
    private FloatParser(Builder builder) {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Float apply(String text) {
        Float val = NumberHelper.getFloat(text,this.emptyValue.get());
        return val == null ? null : Math.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    @Override
    public boolean test(String text) {
        //Test both with and missing the final decimal point just to be safe
        return testInternal(text) || (text.endsWith(".")  && testInternal(text.substring(0, text.length() - 1)));
    }

    private boolean testInternal(String text) {
        if(NumberHelper.isFloat(text))
        {
            Float val = NumberHelper.getFloat(text,null);
            return val != null && val >= this.minValue.get() && val <= this.maxValue.get();
        }
        return text.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Supplier<Float> minValue = () -> Float.MIN_VALUE;
        private Supplier<Float> maxValue = () -> Float.MAX_VALUE;
        private Supplier<Float> emptyValue = () -> null;

        public Builder min(float minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Float> minValue) { this.minValue = minValue; return this; }
        public Builder max(float maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Float> maxValue) { this.maxValue = maxValue; return this; }
        public Builder empty(float emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Float> emptyValue) { this.emptyValue = emptyValue; return this; }

        public FloatParser build() { return new FloatParser(this); }

    }

}
