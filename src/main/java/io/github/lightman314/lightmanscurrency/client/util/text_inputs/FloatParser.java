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
public final class FloatParser implements Function<String,Float>, Predicate<String> {

    public static final FloatParser DEFAULT = builder().build();

    private final Supplier<Float> minValue;
    private final Supplier<Float> maxValue;
    private final Supplier<Float> emptyValue;
    private FloatParser(Builder builder)
    {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.emptyValue = builder.emptyValue;
    }

    @Override
    public Float apply(String text) { return MathUtil.clamp(NumberUtil.GetFloatValue(text,this.emptyValue.get()),this.minValue.get(),this.maxValue.get()); }

    @Override
    public boolean test(String s) {
        if(NumberUtil.IsFloatOrEmpty(s))
        {
            float value = this.apply(s);
            return value >= this.minValue.get() && value <= this.maxValue.get();
        }
        return false;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private Supplier<Float> minValue = () -> Float.MAX_VALUE * -1f;
        private Supplier<Float> maxValue = () -> Float.MAX_VALUE;
        private Supplier<Float> emptyValue = () -> 0f;
        private Builder() {}

        public Builder min(float minValue) { this.minValue = () -> minValue; return this; }
        public Builder min(Supplier<Float> minValue) { this.minValue = Objects.requireNonNull(minValue); return this; }
        public Builder max(float maxValue) { this.maxValue = () -> maxValue; return this; }
        public Builder max(Supplier<Float> maxValue) { this.maxValue = Objects.requireNonNull(maxValue); return this; }
        public Builder empty(float emptyValue) { this.emptyValue = () -> emptyValue; return this; }
        public Builder empty(Supplier<Float> emptyValue) { this.emptyValue = Objects.requireNonNull(emptyValue); return this; }

        private FloatParser build() { return new FloatParser(this); }
        public Consumer<TextInputUtil.Builder<Float>> consumer() {
            return b -> {
                FloatParser result = this.build();
                b.parser(result).filter(result);
            };
        }
    }

}