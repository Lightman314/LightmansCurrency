package io.github.lightman314.lightmanscurrency.api.money.values;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Range;

/**
 * A {@link MoneyValue} wrapper that allows for negative MoneyValue results to various math<br>
 * Should properly perform calculations for any {@link MoneyValue} type that properly implements {@link MoneyValue#fromInternalValue(long)} and {@link MoneyValue#getInternalValue()}
 */
public final class FlexibleMoneyValue {

    public static final Codec<FlexibleMoneyValue> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.fieldOf("negative").forGetter(v -> v.negative),
            MoneyValue.CODEC.fieldOf("value").forGetter(v -> v.value)
    ).apply(builder,FlexibleMoneyValue::of));
    public static final StreamCodec<RegistryFriendlyByteBuf,FlexibleMoneyValue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,v -> v.negative,
            MoneyValue.STREAM_CODEC,v -> v.value,
            FlexibleMoneyValue::of);

    public static final FlexibleMoneyValue EMPTY = new FlexibleMoneyValue(false,MoneyValue.empty());

    public final boolean negative;
    public final MoneyValue value;
    private FlexibleMoneyValue(boolean negative,MoneyValue value)
    {
        this.negative = negative;
        this.value = value;
    }

    /**
     * Constructor for a negative Money Value
     */
    public static FlexibleMoneyValue negative(MoneyValue val) { return of(true,val); }

    /**
     * Constructor for a positive/normal Money Value
     */
    public static FlexibleMoneyValue positive(MoneyValue val) { return of(false,val); }

    /**
     * Constructor for a Money Value with variable positivity<br>
     * @param negative Whether the value is considered negative or not
     * @param val A normal {@link MoneyValue} representation of the value. If {@link MoneyValue#isEmpty()}, {@link #EMPTY} will be returned
     * @return A FlexibleMoneyValue instance with the given parameters
     */
    public static FlexibleMoneyValue of(boolean negative,MoneyValue val)
    {
        if(val.isEmpty())
            return EMPTY;
        return new FlexibleMoneyValue(negative,val);
    }

    public boolean isEmpty() { return this.value.isEmpty(); }

    public MoneyKey getKey() { return this.value.getKey(); }

    public boolean compatibleType(MoneyValue value) { return this.value.compatibleTypes(value); }
    public boolean compatibleType(FlexibleMoneyValue value) { return this.compatibleType(value.value); }

    @Range(from = Long.MIN_VALUE,to = Long.MAX_VALUE)
    public long getInternalValue() { return this.isEmpty() ? 0 : this.negative ? this.value.getInternalValue() * -1 : this.value.getInternalValue(); }

    public FlexibleMoneyValue addValue(MoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return positive(value);
        if(this.value.compatibleTypes(value))
            return assembleResult(this.getInternalValue() + value.getInternalValue(),value);
        return EMPTY;
    }

    public FlexibleMoneyValue addValue(FlexibleMoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return value;
        if(this.value.compatibleTypes(value.value))
            return assembleResult(this.getInternalValue() + value.getInternalValue(),value.value);
        return EMPTY;
    }

    public FlexibleMoneyValue subtractValue(MoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return of(true,value);
        if(this.value.compatibleTypes(value))
            return assembleResult(this.getInternalValue() - value.getInternalValue(),value);
        return EMPTY;
    }

    public FlexibleMoneyValue subtractValue(FlexibleMoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return of(!value.negative,value.value);
        if(this.value.compatibleTypes(value.value))
            return assembleResult(this.getInternalValue() - value.getInternalValue(),value.value);
        return EMPTY;
    }

    public FlexibleMoneyValue percentageOfValue(int percentage, boolean roundUp)
    {
        boolean neg = this.negative;
        if(percentage < 0)
        {
            neg = !this.negative;
            percentage *= -1;
        }
        if(percentage == 0)
            return EMPTY;
        return of(neg,this.value.percentageOfValue(percentage,roundUp));
    }

    public FlexibleMoneyValue multiplyValue(double multiplier)
    {
        boolean neg = this.negative;
        if(multiplier < 0d)
        {
            neg = !this.negative;
            multiplier *= -1d;
        }
        if(multiplier == 0d)
            return EMPTY;
        return of(neg,this.value.multiplyValue(multiplier));
    }

    private static FlexibleMoneyValue assembleResult(long value, MoneyValue reference)
    {
        if(value < 0)
            return negative(reference.fromInternalValue(value * -1));
        else if(value == 0)
            return EMPTY;
        return positive(reference.fromInternalValue(value));
    }

    public Component getText() { return this.value.getText(Component.literal("0")); }
    public Component getText(int color,int negativeColor) { return this.value.getText(Component.literal("0")).copy().withColor(this.negative ? negativeColor : color); }

}