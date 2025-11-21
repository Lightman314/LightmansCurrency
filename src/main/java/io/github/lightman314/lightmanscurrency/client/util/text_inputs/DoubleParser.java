package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class DoubleParser implements Function<String,Double>, Predicate<String> {

    public static final DoubleParser DEFAULT = builder().build();

    private final Supplier<Double> minValue;
    private final Supplier<Double> maxValue;
    private final Supplier<Double> emptyValue;
    private DoubleParser(Builder builder)
    {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Double apply(String text) {
        Double val = NumberUtil.GetDoubleValue(text,this.emptyValue.get());
        return val == null ? null : MathUtil.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    @Override
    public boolean test(String s) {
        if(NumberUtil.IsDouble(s))
        {
            double value = this.apply(s);
            return value >= this.minValue.get() && value <= this.maxValue.get();
        }
        return s.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private Supplier<Double> minValue = () -> Double.MAX_VALUE * -1d;
        private Supplier<Double> maxValue = () -> Double.MAX_VALUE;
        private Supplier<Double> emptyValue = () -> null;
        private Builder() {}

        public Builder min(double minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Double> minValue) { this.minValue = Objects.requireNonNull(minValue); return this; }
        public Builder max(double maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Double> maxValue) { this.maxValue = Objects.requireNonNull(maxValue); return this; }
        public Builder empty(double emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Double> emptyValue) { this.emptyValue = Objects.requireNonNull(emptyValue); return this; }

        private DoubleParser build() { return new DoubleParser(this); }
        public Consumer<TextInputUtil.Builder<Double>> consumer() {
            return b -> {
                DoubleParser result = this.build();
                b.parser(result).filter(result);
            };
        }
    }

}
