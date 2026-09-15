package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DoubleParser implements Function<String,Double>, Predicate<String>  {

    public static final DoubleParser DEFAULT = builder().build();

    public static final Function<Double,String> DEFAULT_WRITER = d -> {
        if (d % 1d == 0d)
            return String.valueOf(d.longValue());
        return String.valueOf(d);
    };

    private final Supplier<Double> minValue;
    private final Supplier<Double> maxValue;
    private final Supplier<Double> emptyValue;
    private DoubleParser(Builder builder) {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Double apply(String text) {
        Double val = NumberHelper.getDouble(text,this.emptyValue.get());
        return val == null ? null : Math.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    @Override
    public boolean test(String text) {
        //Test both with and missing the final decimal point just to be safe
        return testInternal(text) || (text.endsWith(".")  && testInternal(text.substring(0, text.length() - 1)));
    }

    private boolean testInternal(String text) {
        if(NumberHelper.isDouble(text))
        {
            Double val = NumberHelper.getDouble(text,null);
            return val != null && val >= this.minValue.get() && val <= this.maxValue.get();
        }
        return text.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Supplier<Double> minValue = () -> Double.MIN_VALUE;
        private Supplier<Double> maxValue = () -> Double.MAX_VALUE;
        private Supplier<Double> emptyValue = () -> null;

        public Builder min(double minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Double> minValue) { this.minValue = minValue; return this; }
        public Builder max(double maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Double> maxValue) { this.maxValue = maxValue; return this; }
        public Builder empty(double emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Double> emptyValue) { this.emptyValue = emptyValue; return this; }

        public DoubleParser build() { return new DoubleParser(this); }

    }

}
