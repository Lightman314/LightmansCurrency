package io.github.lightman314.lightmanscurrency.api.money.values.mutable;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

/**
 * A Mutable holder for a MoneyValue instance
 */
public class MutableFlexibleMoneyValue {

    public static final Codec<MutableFlexibleMoneyValue> CODEC = FlexibleMoneyValue.CODEC.xmap(MutableFlexibleMoneyValue::new, MutableFlexibleMoneyValue::getValue);
    public static final StreamCodec<RegistryFriendlyByteBuf, MutableFlexibleMoneyValue> STREAM_CODEC = FlexibleMoneyValue.STREAM_CODEC.map(MutableFlexibleMoneyValue::new, MutableFlexibleMoneyValue::getValue);

    private FlexibleMoneyValue value;
    public FlexibleMoneyValue getValue() { return this.value; }
    public MutableFlexibleMoneyValue() { this(FlexibleMoneyValue.EMPTY); }
    public MutableFlexibleMoneyValue(FlexibleMoneyValue value) { this.value = value; }

    public final boolean isEmpty() { return this.value.isEmpty(); }

    public final DualKey getKey() { return this.value.getKey(); }
    public final long getInternalValue() { return this.value.getInternalValue(); }

    public Component getText() { return this.value.getText(); }
    public Component getText(int color,int negativeColor) { return this.value.getText(color,negativeColor); }

    public final boolean addValue(MoneyValue addedValue) {
        return this.addValue(FlexibleMoneyValue.of(false,addedValue));
    }

    public final boolean addValue(FlexibleMoneyValue addedValue) {
        if(this.value.compatibleType(addedValue)) {
            this.value = this.value.addValue(addedValue);
            return true;
        }
        return false;
    }

    public final boolean subtractValue(MoneyValue removedValue) {
        return this.subtractValue(FlexibleMoneyValue.of(false,removedValue));
    }

    public final boolean subtractValue(FlexibleMoneyValue removedValue) {
        if(this.value.compatibleType(removedValue)) {
            this.value = this.value.subtractValue(removedValue);
            return true;
        }
        return false;
    }

    public final void percentageOfValue(int percentage) { this.value = this.value.percentageOfValue(percentage,true); }
    public final void percentageOfValue(int percentage,boolean roundUp) { this.value = this.value.percentageOfValue(percentage,roundUp); }

    public final void multiplyValue(double multiplier) { this.value = this.value.multiplyValue(multiplier); }

    @Override
    public String toString() { return "FlexibleMoneyValueHolder[" + this.getKey() + ";" + this.getInternalValue() + "]"; }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MutableFlexibleMoneyValue h)
            return this.value.equals(h.value);
        return false;
    }
    @Override
    public int hashCode() { return this.value.hashCode(); }

}
