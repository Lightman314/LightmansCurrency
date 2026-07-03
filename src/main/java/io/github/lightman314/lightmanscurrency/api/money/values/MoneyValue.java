package io.github.lightman314.lightmanscurrency.api.money.values;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.values.impl.EmptyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Immutable
public abstract class MoneyValue {

    public static final Codec<MoneyValue> CODEC = LCRegistries.Money.VALUE_TYPE.byNameCodec().dispatch(MoneyValue::getType,MoneyValueType::codec);
    public static final Codec<MoneyValue> NON_EMPTY_CODEC = CODEC.validate(value -> {
        if(value.isEmpty() && !value.isFree())
            return DataResult.error(() -> "Money Value cannot be empty!");
        return DataResult.success(value);
    });
    public static final Codec<MoneyValue> NON_EMPTY_OR_FREE_CODEC = CODEC.validate(value -> {
        if(value.isEmpty())
        {
            if(value.isFree())
                return DataResult.error(() -> "Money Value cannot be free!");
            else
                return DataResult.error(() -> "Money Value cannot be empty!");
        }
        return DataResult.success(value);
    });
    public static final Codec<Map<MoneyKey,MoneyValue>> SET_CODEC = NON_EMPTY_OR_FREE_CODEC.listOf().xmap(list -> {
        Map<MoneyKey,MoneyValue> map = new HashMap<>();
        for(MoneyValue v : list)
            map.put(v.getKey(),v);
        return map;
    },map -> new ArrayList<>(map.values()));

    private static Codec<MoneyValue> argumentCodec(boolean allowEmpty,boolean allowFree)
    {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success(MoneyValueParser.parse(new StringReader(string),allowEmpty,allowFree));
            } catch (CommandSyntaxException e) { return DataResult.error(() -> "Error parsing Money Value: " + e.getMessage()); }
        }, MoneyValueParser::writeParsable);
    }

    public static final Codec<MoneyValue> LENIENT_CODEC = Codec.withAlternative(CODEC,argumentCodec(true,true));
    public static final Codec<MoneyValue> LENIENT_NON_EMPTY_CODEC = Codec.withAlternative(NON_EMPTY_CODEC,argumentCodec(false,true));
    public static final Codec<MoneyValue> LENIENT_NON_EMPTY_OR_FREE_CODEC = Codec.withAlternative(NON_EMPTY_OR_FREE_CODEC,argumentCodec(false,false));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyValue> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Money.VALUE_TYPE_KEY).dispatch(MoneyValue::getType,MoneyValueType::streamCodec);

    public static final StreamCodec<RegistryFriendlyByteBuf,Map<MoneyKey,MoneyValue>> SET_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list()).map(list -> {
        Map<MoneyKey,MoneyValue> map = new HashMap<>();
        for(MoneyValue v : list)
        {
            if(!v.isFree() && !v.isEmpty())
                map.put(v.getKey(),v);
        }
        return map;
        },map -> new ArrayList<>(map.values()));

    public static MoneyValue empty() { return EmptyValue.EMPTY_INSTANCE; }
    public static MoneyValue free() { return EmptyValue.FREE_INSTANCE; }

    /**
     * The {@link MoneyValueType} corresponding to this value type.
     */
    public abstract MoneyValueType<?> getType();

    /**
     * Obtains a {@link MoneyValueHelper} instance that can be used to optimize bulk operations and improve display handling.
     */
    public MoneyValueHelper getTypedHelper() { return MoneyValueHelper.DEFAULT; }

    /**
     * Used internally to obtain and cache this money value's {@link MoneyKey}
     * @see #getKey()
     */
    protected abstract MoneyKey generateKey();

    private MoneyKey key = null;
    /**
     * Returns a unique {@link MoneyKey} for this value type.<br>
     * Multiple Money Values of the same {@link #getType() type}, may have differing Keys if they use the same system, but are considered seperate value types<br>
     * For example, multiple {@link CoinValue CoinValue} values will use the same underlying system,
     * but money from different "coin chains" need to be handled independently and cannot be combined.
     */
    public final MoneyKey getKey() {
        if(this.key == null)
            this.key = Objects.requireNonNull(this.generateKey(),"Failed to generate a Money Key!");
        return this.key;
    }

    /**
     * Whether this value is considered "Free".<br>
     * Is only true if this is the {@link #free()} constant
     */
    public final boolean isFree() { return this == free(); }

    /**
     * Whether this value has nothing stored in it.
     * Used to cull un-used value data to save space, but can also be used to simplify math
     * as there's no need to add two values if one is already empty (and thus a value of 0)
     * By default returns <code>true</code> if {@link MoneyValue#getInternalValue()} returns exactly 0.
     * @see #empty()
     */
    public boolean isEmpty() { return this.getInternalValue() == 0; }

    /**
     * Whether this value is a valid price amount.
     * By default, this returns <code>true</code> if {@link #isFree()} is <code>true</code> or {@link #isEmpty()} is <code>false</code>
     */
    public boolean isValidPrice() { return this.isFree() || !this.isEmpty(); }
    /**
     * Whether this value is invalid, and should not be used for calculations or handling.<br>
     * Typically only flagged this way to maintain old data if a config file loaded improperly,
     * and we don't want to just throw it away.
     */
    public boolean isInvalid() { return false; }

    /**
     * Whether this value is capable of being added to or subtracted from the other value.<br>
     * By default, confirms that {@link #getKey()} is equal <b>OR</b> if either of the values are {@link #isEmpty() empty}.
     */
    public boolean compatibleTypes(MoneyValue otherValue) { return otherValue.getKey().equals(this.getKey()) || this.isEmpty() || otherValue.isEmpty(); }

    /**
     * An internal numerical representation of this value.<br>
     * Primarily intended to be used for comparing two values together to see if one is greater than the other,
     * but is also used for math internally in various locations.
     */
    @Range(from = 0,to = Long.MAX_VALUE)
    public abstract long getInternalValue();

    /**
     * Returns a string display of this value.
     * @see #getString(String)
     * @see #getText()
     * @see #getText(Component) 
     */
    public final String getString() { return this.getString(""); }
    /**
     * Returns a string display of this value.<br>
     * @param emptyText The string to return if this value is empty.
     * @see #getString()
     * @see #getText()
     * @see #getText(Component) 
     */
    public String getString(String emptyText) { return this.getText(Component.literal(emptyText)).getString(); }
    /**
     * Returns a text display of this value.
     * @see #getString()
     * @see #getString(String)
     * @see #getText(Component)
     */
    public final Component getText() { return this.getText(Component.empty()); }
    /**
     * Returns a text display of this value.
     * @see #getString()
     * @see #getString(String)
     * @see #getText()
     */
    public abstract Component getText(Component emptyText);

    /**
     * Does math to add the given value to this value.
     * Should confirm that the two values are compatible with {@link #compatibleTypes(MoneyValue)} before executing
     * @param addedValue The {@link MoneyValue} to add to this value.
     * @return <code>null</code> if the added value is incompatible,
     * otherwise it should return a new MoneyValue instance with a total value equal to
     * <code>this#</code>{@link #getInternalValue()} + <code>addedValue#</code>{@link #getInternalValue()}
     * @see #compatibleTypes(MoneyValue)
     * @see #containsValue(MoneyValue)
     * @see #subtractValue(MoneyValue)
     * @see #getInternalValue()
     */
    @Nullable
    public MoneyValue addValue(MoneyValue addedValue) {
        if(this.compatibleTypes(addedValue))
            return this.fromInternalValue(this.getInternalValue() + addedValue.getInternalValue());
        return null;
    }

    /**
     * Does math to remove the given value from this value.
     * Should check {@link #containsValue(MoneyValue)} to confirm that it is capable
     * of removing this much money from the stored value before executing.
     * @param removedValue The {@link MoneyValue} to subtract from this value.
     * @return <code>null</code> if the subtracted value is incompatible
     * <b>OR</b> it's less than or equal to this value,
     * otherwise it should return a new MoneyValue instance with a total value equal to
     * <code>this#</code>{@link #getInternalValue()} - <code>removedValue#</code>{@link #getInternalValue()}
     * @see #addValue(MoneyValue)
     * @see #containsValue(MoneyValue)
     * @see #getInternalValue()
     */
    @Nullable
    public MoneyValue subtractValue(MoneyValue removedValue) {
        if(this.compatibleTypes(removedValue) && this.containsValue(removedValue))
            return this.fromInternalValue(this.getInternalValue() - removedValue.getInternalValue());
        return null;
    }

    /**
     * Whether this value contains enough money to safely subtract the given {@link MoneyValue}.
     * @return false if <code>this#</code>{@link #getInternalValue()} < <code>queryValue#</code>{@link #getInternalValue()},
     * or if the queried value is not compatible.
     * @see #addValue(MoneyValue)
     * @see #subtractValue(MoneyValue)
     * @see #getInternalValue()
     */
    public boolean containsValue(MoneyValue queryValue)
    {
        if(this.compatibleTypes(queryValue))
            return this.getInternalValue() >= queryValue.getInternalValue();
        return false;
    }

    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link TradeRule}'s that give
     * percentage-based discounts.
     * Will round down by default.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @return {@link #free()} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise, a value equal to <code>{@link #getInternalValue()} * percentage / 100 </code>
     * @see #percentageOfValue(int, boolean)
     */
    public final MoneyValue percentageOfValue(int percentage) { return this.percentageOfValue(percentage,false); }
    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link TradeRule}'s that give
     * percentage-based discounts.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @param roundUp Whether we should round a value up to nearest valid value if the exact percentage results in a partial value. If <code>false</code> round down.
     * @return {@link #free()} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise, a value equal to <code>{@link #getInternalValue()} * percentage / 100 </code>
     */
    public MoneyValue percentageOfValue(int percentage,boolean roundUp) {
        if(percentage == 100)
            return this;
        if(percentage == 0)
            return free();
        long value = this.getInternalValue();
        //Calculate the new value
        long newValue = value * percentage / 100;
        //Calculate if we should round up
        if(roundUp)
        {
            long partial = (value * percentage) % 100;
            if(partial > 0)
                newValue++;
        }
        if(newValue == 0)
            return free();
        return this.fromInternalValue(newValue);
    }

    /**
     * Does math to multiply this value by the given amount.<br>
     * Result can be rounded as desired, but should be of the same type as the original value unless the result is {@link #empty() empty}.
     * @param multiplier The amount to multiply this value by.
     * @return The mathematical result of multiplying this value by the given number.
     */
    public MoneyValue multiplyValue(double multiplier)
    {
        BigDecimal value = BigDecimal.valueOf(this.getInternalValue());
        BigDecimal result = value.multiply(BigDecimal.valueOf(multiplier));
        // If less than 1, return empty
        if(result.compareTo(BigDecimal.valueOf(0.5d)) < 0)
            return empty();
        //If larger than the max long value, return max long value
        if(result.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0)
            return this.fromInternalValue(Long.MAX_VALUE);
        if(result.remainder(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.5d)) >= 0)
            this.fromInternalValue(result.longValue() + 1);
        return this.fromInternalValue(result.longValue());
    }

    /**
     * Whether this money value supports bank account interest
     */
    public boolean allowInterest() { return !this.isEmpty(); }

    /**
     * Function called when a block that contains money is broken through a means that would result in items spawning in the world.<br>
     * Depending on the config values, this may happen through legitimate means or through griefing.
     * @param itemSpawner The method used to handle spawning items into the world as though it were any other part of the blocks drops.
     * @param owner Data about the blocks owner, so that any non-item based money can instead be given to the owner directly.
     */
    public abstract void spawnInWorld(Consumer<ItemStack> itemSpawner,OwnerHolder owner);

    /**
     * @return The smallest non-zero value of this money value type.
     * @see #fromInternalValue(long)
     */
    public MoneyValue getSmallestValue() { return this.fromInternalValue(1); }

    /**
     * Returns a Money Value with the same {@link #getKey()} key/type, but with the given internal value.<br>
     * Used often for default addition/subtraction calculations.
     */
    public final MoneyValue fromInternalValue(long value){
        if(value <= 0)
            return empty();
        return this.copyWithInternalValue(value);
    }

    /**
     * Internal implementation of {@link #fromInternalValue(long)} but without the forced empty test.
     */
    protected abstract MoneyValue copyWithInternalValue(long value);

    @Override
    public String toString() { return "MoneyValue[" + this.getKey() + ";" + this.getInternalValue() + "]"; }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MoneyValue otherVal)
            return this.getKey().equals(otherVal.getKey()) && this.getInternalValue() == otherVal.getInternalValue();
        return super.equals(obj);
    }

    @Override
    public int hashCode() { return Objects.hash(this.getKey(),this.getInternalValue()); }

}
