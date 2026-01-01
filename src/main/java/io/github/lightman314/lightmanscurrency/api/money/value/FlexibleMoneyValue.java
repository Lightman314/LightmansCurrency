package io.github.lightman314.lightmanscurrency.api.money.value;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Range;

/**
 * A {@link MoneyValue} wrapper that allows for negative MoneyValue results to various math<br>
 * Should properly perform calculations for any {@link MoneyValue} type that properly implements {@link MoneyValue#fromCoreValue(long)} and {@link MoneyValue#getCoreValue()}}
 */
public final class FlexibleMoneyValue {

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

    public String getUniqueName() { return this.value.getUniqueName(); }

    public boolean sameType(MoneyValue value) { return this.value.sameType(value); }
    public boolean sameType(FlexibleMoneyValue value) { return this.sameType(value.value); }

    @Range(from = Long.MIN_VALUE,to = Long.MAX_VALUE)
    public long getCoreValue() { return this.isEmpty() ? 0 : this.negative ? this.value.getCoreValue() * -1 : this.value.getCoreValue(); }

    public FlexibleMoneyValue addValue(MoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return positive(value);
        if(this.value.sameType(value))
            return assembleResult(this.getCoreValue() + value.getCoreValue(),value);
        return EMPTY;
    }

    public FlexibleMoneyValue addValue(FlexibleMoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return value;
        if(this.value.sameType(value.value))
            return assembleResult(this.getCoreValue() + value.getCoreValue(),value.value);
        return EMPTY;
    }

    public FlexibleMoneyValue subtractValue(MoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return of(true,value);
        if(this.value.sameType(value))
            return assembleResult(this.getCoreValue() - value.getCoreValue(),value);
        return EMPTY;
    }

    public FlexibleMoneyValue subtractValue(FlexibleMoneyValue value)
    {
        if(value.isEmpty())
            return this;
        if(this.isEmpty())
            return of(!value.negative,value.value);
        if(this.value.sameType(value.value))
            return assembleResult(this.getCoreValue() - value.getCoreValue(),value.value);
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
            return negative(reference.fromCoreValue(value * -1));
        else if(value == 0)
            return EMPTY;
        return positive(reference.fromCoreValue(value));
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("negative",this.negative);
        tag.put("value",this.value.save());
        return tag;
    }

    public static FlexibleMoneyValue load(CompoundTag tag)
    {
        if(tag.contains("negative") && tag.contains("value"))
            return of(tag.getBoolean("negative"),MoneyValue.load(tag.getCompound("value")));
        if(tag.contains("type"))
            return positive(MoneyValue.load(tag));
        return EMPTY;
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.negative);
        this.value.encode(buffer);
    }

    public static FlexibleMoneyValue decode(FriendlyByteBuf buffer) { return new FlexibleMoneyValue(buffer.readBoolean(),MoneyValue.decode(buffer)); }

    public Component getText() { return this.value.getText("0"); }
    public Component getText(int color, int negativeColor) { return this.value.getText("0").withStyle(s -> s.withColor(this.negative ? negativeColor : color)); }

}