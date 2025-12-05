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
public final class LongParser implements Function<String,Long>, Predicate<String> {

    public static final LongParser DEFAULT = builder().build();

    private final Supplier<Long> minValue;
    private final Supplier<Long> maxValue;
    private final Supplier<Long> emptyValue;
    private LongParser(Builder builder)
    {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Long apply(String text) {
        Long val = NumberUtil.GetLongValue(text,this.emptyValue.get());
        return val == null ? null : MathUtil.clamp(val,this.minValue.get(),this.maxValue.get());
    }

    @Override
    public boolean test(String s) {
        if(NumberUtil.IsLong(s))
        {
            long value = this.apply(s);
            return value >= this.minValue.get() && value <= this.maxValue.get();
        }
        return s.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private Supplier<Long> minValue = () -> Long.MIN_VALUE;
        private Supplier<Long> maxValue = () -> Long.MAX_VALUE;
        private Supplier<Long> emptyValue = () -> null;
        private Builder() {}

        public Builder min(long minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Long> minValue) { this.minValue = Objects.requireNonNull(minValue); return this; }
        public Builder max(long maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Long> maxValue) { this.maxValue = Objects.requireNonNull(maxValue); return this; }
        public Builder empty(long emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Long> emptyValue) { this.emptyValue = Objects.requireNonNull(emptyValue); return this; }

        private LongParser build() { return new LongParser(this); }
        public Consumer<TextInputUtil.Builder<Long>> consumer() {
            return b -> {
                LongParser result = this.build();
                b.parser(result).filter(result);
            };
        }
    }

}
