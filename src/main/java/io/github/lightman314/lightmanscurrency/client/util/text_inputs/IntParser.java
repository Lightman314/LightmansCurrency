package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class IntParser implements Function<String,Integer>, Predicate<String> {

    public static final IntParser DEFAULT = builder().build();
    public static final IntParser ONE_TO_ONE_HUNDRED = builder().min(1).max(100).build();

    private final Supplier<Integer> minValue;
    private final Supplier<Integer> maxValue;
    private final Supplier<Integer> emptyValue;
    private IntParser(Builder builder)
    {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Integer apply(String text) {
        Integer val = NumberUtil.GetIntegerValue(text,this.emptyValue.get());
        return val == null ? null : MathUtil.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public boolean test(String s) {
        if(NumberUtil.IsInteger(s))
        {
            int value = this.apply(s);
            return value >= this.minValue.get() && value <= this.maxValue.get();
        }
        return s.isEmpty();
    }

    public static class Builder
    {
        private Supplier<Integer> minValue = () -> Integer.MIN_VALUE;
        private Supplier<Integer> maxValue = () -> Integer.MAX_VALUE;
        private Supplier<Integer> emptyValue = () -> null;
        private Builder() {}

        public Builder min(int minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Integer> minValue) { this.minValue = Objects.requireNonNull(minValue); return this; }
        public Builder max(int maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Integer> maxValue) { this.maxValue = Objects.requireNonNull(maxValue); return this; }
        public Builder empty(int emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Integer> emptyValue) { this.emptyValue = Objects.requireNonNull(emptyValue); return this; }

        private IntParser build() { return new IntParser(this); }
        public Consumer<TextInputUtil.Builder<Integer>> consumer() {
            return b -> {
                IntParser result = this.build();
                b.parser(result).filter(result);
            };
        }
    }

}