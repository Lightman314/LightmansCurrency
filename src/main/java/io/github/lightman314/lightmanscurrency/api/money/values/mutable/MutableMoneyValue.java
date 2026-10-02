package io.github.lightman314.lightmanscurrency.api.money.values.mutable;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

/**
 * A Mutable holder for a MoneyValue instance
 */
public class MutableMoneyValue {

    public static final Codec<MutableMoneyValue> CODEC = MoneyValue.CODEC.xmap(MutableMoneyValue::new, MutableMoneyValue::getValue);
    public static final StreamCodec<RegistryFriendlyByteBuf, MutableMoneyValue> STREAM_CODEC = MoneyValue.STREAM_CODEC.map(MutableMoneyValue::new, MutableMoneyValue::getValue);

    private MoneyValue value;
    public MoneyValue getValue() { return this.value; }
    public MutableMoneyValue() { this(MoneyValue.empty()); }
    public MutableMoneyValue(MoneyValue value) { this.value = value; }

    public final boolean isEmpty() { return this.value.isEmpty(); }
    public final boolean isFree() { return this.value.isFree(); }
    public final boolean isValidPrice() { return this.value.isValidPrice(); }
    public final boolean compatibleTypes(MoneyValue other) { return this.value.compatibleTypes(other); }

    public final DualKey getKey() { return this.value.getKey(); }
    public final MoneyValueType<?> getType() { return this.value.getType(); }
    public final long getInternalValue() { return this.value.getInternalValue(); }

    public final String getString() { return this.value.getString(); }
    public final String getString(String emptyText) { return this.value.getString(emptyText); }
    public final Component getText() { return this.value.getText(); }
    public final Component getText(Component emptyText) { return this.value.getText(emptyText); }

    public final boolean containsValue(MoneyValue value) { return this.value.containsValue(value); }
    public final boolean allowInterest() { return this.value.allowInterest(); }

    public final MoneyValue getSmallestValue() { return this.value.getSmallestValue(); }
    public final MoneyValue fromInternalValue(long value) { return this.value.fromInternalValue(value); }

    public final boolean addValue(MoneyValue addedValue) {
        MoneyValue result = this.value.addValue(addedValue);
        if(result != null) {
            this.value = result;
            return true;
        }
        return false;
    }

    public final boolean subtractValue(MoneyValue removedValue) {
        MoneyValue result = this.value.subtractValue(removedValue);
        if(result != null) {
            this.value = result;
            return true;
        }
        return false;
    }

    public final void percentageOfValue(int percentage) { this.value = this.value.percentageOfValue(percentage); }
    public final void percentageOfValue(int percentage,boolean roundUp) { this.value = this.value.percentageOfValue(percentage,roundUp); }

    public final void multiplyValue(double multiplier) { this.value = this.value.multiplyValue(multiplier); }

    @Override
    public String toString() { return "MoneyValueHolder[" + this.getKey() + ";" + this.getInternalValue() + "]"; }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MutableMoneyValue h)
            return this.value.equals(h.value);
        return false;
    }
    @Override
    public int hashCode() { return this.value.hashCode(); }

}
