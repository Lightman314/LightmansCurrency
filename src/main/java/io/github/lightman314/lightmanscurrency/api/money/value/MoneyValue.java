package io.github.lightman314.lightmanscurrency.api.money.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.NullValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Objects;

/**
 * Class made to store and reference values of Money.
 * To use my built-in implementation of this, see {@link CoinValue}
 */
public abstract class MoneyValue {

    public static final Codec<MoneyValue> CODEC = Codec.withAlternative(
            //Intended Codec
            CurrencyType.CODEC.dispatch(MoneyValue::getType,CurrencyType::moneyValueCodec)
            //Fallback Codec for old data
            , CodecHelper.oldValueLoader(MoneyValue::loadOldData,"Money Value"));
    public static final Codec<MoneyValue> NON_EMPTY_CODEC = CODEC.validate(value -> {
        if(value.isEmpty() && !value.isFree())
            return DataResult.error(() -> "Money Value cannot be empty!");
        return DataResult.success(value);
    });
    public static final Codec<MoneyValue> NON_EMPTY_OR_FREE_CODEC = CODEC.validate(value -> {
        if(value.isEmpty())
            return DataResult.error(() -> "Money Value cannot be empty or free!");
        return DataResult.success(value);
    });



    private static Codec<MoneyValue> argumentCodec(boolean allowEmpty) {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success(MoneyValueParser.parse(new StringReader(string),allowEmpty));
            } catch (CommandSyntaxException e) { return DataResult.error(() -> "Error parsing Money Value: " + e.getMessage()); }
        },MoneyValueParser::writeParsable);
    }

    public static final Codec<MoneyValue> LENIENT_CODEC = Codec.withAlternative(CODEC,argumentCodec(true));
    public static final Codec<MoneyValue> LENIENT_NON_EMPTY_CODEC = Codec.withAlternative(NON_EMPTY_CODEC,argumentCodec(false));
    public static final Codec<MoneyValue> LENIENT_NON_EMPTY_OR_FREE_CODEC = Codec.withAlternative(NON_EMPTY_OR_FREE_CODEC,argumentCodec(false));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyValue> STREAM_CODEC = CurrencyType.STREAM_CODEC.dispatch(MoneyValue::getType,CurrencyType::moneyValueStreamCodec);
    
    public static MoneyValue free() { return NullValue.FREE; }
    public static MoneyValue empty() { return NullValue.EMPTY; }

    private String uniqueName;

    /**
     * The {@link CurrencyType} id corresponding to this value type.
     */
    public abstract CurrencyType<?> getType();

    protected String generateUniqueName() { return LCRegistries.CURRENCY_TYPE.getKey(this.getType()).toString(); }

    /**
     * Returns a unique name for this storage type.<br>
     * Used by {@link MoneyStorage} and {@link MoneyView} to seperate {@link MoneyValue}'s into their different value entries.<br>
     * For {@link CoinValue} data, this returns a combination of it's <code>chain</code> and {@link #getType()},
     * but values without varying types may simply return a String version of {@link #getType()}
     */
    
    public final String getUniqueName() {
        if(this.uniqueName == null)
            this.uniqueName = this.generateUniqueName();
        return this.uniqueName;
    }

    
    protected final String generateCustomUniqueName(String addon) { return generateCustomUniqueName(this.getType(),addon); }

    public static String generateCustomUniqueName(CurrencyType<?> type, String addon)
    {
        ResourceLocation id = LCRegistries.CURRENCY_TYPE.getKey(type);
        if(addon.isEmpty())
            return id.toString();
        else //For custom unique names, place '!' character between them, as it's not a legal ResourceLocation character
            return id + "!" + addon;
    }

    /**
     * Whether this value is considered "Free".
     * Should never be true for values used to denote an amount of stored money.
     * Should only be true if this is {@link #free()}
     */
    public boolean isFree() { return false; }

    /**
     * Whether this value has nothing stored in it.
     * Used to cull un-used value data to saveItem space, but can also be used to simplify math
     * as there's no need to add two values if one is already empty (and thus a value of 0)
     * By default returns <code>true</code> if {@link MoneyValue#getCoreValue} returns exactly 0.
     * @see #empty()
     */
    public abstract boolean isEmpty();

    /**
     * Whether this value is a valid price amount.
     * By default, this is returns <code>true</code> if {@link #isFree()} is <code>true</code> or {@link #isEmpty()} is <code>false</code>
     */
    public boolean isValidPrice() { return this.isFree() || !this.isEmpty(); }

    /**
     * Whether this value is invalid, and should not be used for calculations or handling.
     * Typically only flagged this way to maintain old data if a config file loaded improperly,
     * and we don't want to just throw it away.
     */
    public boolean isInvalid() { return false; }

    /**
     * Whether these values are the same type, and thus can be added or subtracted from each other.
     * By default, confirms that {@link #getUniqueName()} is equal,
     * but allows compatiblity if one or both are the {@link #free()} or {@link #empty()} constant.
     */
    public boolean sameType(MoneyValue otherValue) { return otherValue.getUniqueName().equals(this.getUniqueName()) || this instanceof NullValue || otherValue instanceof NullValue; }

    @Range(from = 0,to = Long.MAX_VALUE)
    public abstract long getCoreValue();

    /**
     * Returns a string display of this value.
     */
    
    public final String getString() { return this.getString(""); }

    /**
     * Returns a string display of this value.
     * By default, returns a string version of {@link #getText(Component)}
     */
    
    public String getString(String emptyText) { return this.getText(EasyText.literal(emptyText)).getString(); }

    /**
     * Returns a text display of this value.
     */
    
    public final Component getText() { return this.getText(EasyText.empty()); }

    /**
     * Returns a text display of this value.
     * @param emptyText Text to display if this value is empty (such as "NULL" or "0" or even blank text). May be ignored if your value has its own formatting for being empty (such as $0, etc.)
     */
    
    public final Component getText(String emptyText) { return this.getText(EasyText.literal(emptyText)); }
    /**
     * Returns a text display of this value.
     * @param emptyText Text to display if this value is empty (such as "NULL" or "0" or even blank text). May be ignored if your value has its own formatting for being empty (such as $0, etc.)
     */
    public abstract Component getText(Component emptyText);

    /**
     * Does math to add the given value to this value.
     * Should confirm that the two values are compatible with {@link #sameType(MoneyValue)} before executing
     * @param addedValue The {@link MoneyValue} to add to this value.
     * @return <code>null</code> if the added value is incompatible,
     * otherwise it should return a new MoneyValue instance with a total value equal to
     * <code>this#</code>{@link #getCoreValue()} + <code>addedValue#</code>{@link #getCoreValue()}
     * @see #sameType(MoneyValue)
     * @see #containsValue(MoneyValue)
     * @see #subtractValue(MoneyValue)
     * @see #getCoreValue()
     */
    @Nullable
    public abstract MoneyValue addValue(MoneyValue addedValue);

    /**
     * Whether this value contains enough money to safely subtract the given {@link MoneyValue}.
     * @return false if <code>this#</code>{@link #getCoreValue()} <code>queryValue#</code>{@link #getCoreValue()},
     * or if the queried value is not compatible.
     * @see #addValue(MoneyValue)
     * @see #subtractValue(MoneyValue)
     * @see #getCoreValue()
     */
    public abstract boolean containsValue(MoneyValue queryValue);

    /**
     * Does math to remove the given value from this value.
     * Should check {@link #containsValue(MoneyValue)} to confirm that it is capable
     * of removing this much money from the stored value before executing.
     * @param removedValue The {@link MoneyValue} to subtract from this value.
     * @return <code>null</code> if the subtracted value is incompatible,
     * otherwise it should return a new MoneyValue instance with a total value equal to
     * <code>this#</code>{@link #getCoreValue()} - <code>removedValue#</code>{@link #getCoreValue()}
     * @see #addValue(MoneyValue)
     * @see #containsValue(MoneyValue)
     * @see #getCoreValue()
     */
    @Nullable
    public abstract MoneyValue subtractValue(MoneyValue removedValue);

    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link TradeRule}'s that give
     * percentage-based discounts.
     * Will round down by default.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @return {@link #free()} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise, a value equal to <code>{@link #getCoreValue()} * percentage / 100 </code>
     * @see #percentageOfValue(int, boolean)
     */
    public final MoneyValue percentageOfValue(int percentage) { return this.percentageOfValue(percentage, false); }

    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link TradeRule}'s that give
     * percentage-based discounts.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @param roundUp Whether we should round a value up to nearest valid value if the exact percentage results in a partial value. If <code>false</code> round down.
     * @return {@link #free()} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise, a value equal to <code>{@link #getCoreValue()} * percentage / 100 </code>
     */
    public abstract MoneyValue percentageOfValue(int percentage, boolean roundUp);

    /**
     * Does math to multiply this value by the given amount.<br>
     * Result can be rounded as desired, but should be of the same type as the original value unless the result is {@link #empty() empty}.
     * @param multiplier The amount to multiply this value by.
     * @return The mathematical result of multiplying this value by the given number.
     */
    public abstract MoneyValue multiplyValue(double multiplier);

    public boolean allowInterest() { return !(this instanceof NullValue); }

    /**
     * Function called when a block that contains money is broken through either legitimate means, or by illegal means.
     * @param owner Data about the blocks' owner, so that any non-item based money can instead be given to the player directly.
     * @return List of items to drop/eject. Leave empty if money is given to the owner manually.
     */
    public abstract List<ItemStack> onBlockBroken(OwnerData owner);

    /**
     * Returns the smallest non-zero value of this money value type.
     */
    public abstract MoneyValue getSmallestValue();

    /**
     * Returns a Money Value with the same {@link #getUniqueName()} but with the given core value<br>
     * Used for calculated math
     */
    
    public abstract MoneyValue fromCoreValue(long value);


    /**
     * Saves this {@link MoneyValue} data into an NBT tag.
     * @see #CODEC
     * @see #safeLoad(CompoundTag,String)
     */
    public final CompoundTag save() { return (CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow(); }

    /**
     * Encodes this value into the given buffer for use in custom packets.
     */
    public final void encode(RegistryFriendlyByteBuf buffer) { STREAM_CODEC.encode(buffer,this); }

    /**
     * Saves this {@link MoneyValue} data into a Json Object
     */
    public final JsonObject toJson() { return (JsonObject)CODEC.encodeStart(JsonOps.INSTANCE,this).getOrThrow(); }

    /**
     * Decoded this value from the given buffer.
     * If it fails to load the data, an empty value will be given instead.
     */
    public static MoneyValue decode(RegistryFriendlyByteBuf buffer) { return STREAM_CODEC.decode(buffer); }

    public static MoneyValue load(CompoundTag tag) {
        try { return CODEC.decode(NbtOps.INSTANCE,tag).getOrThrow().getFirst();
        } catch (IllegalStateException ignored) { return empty(); }
    }

    /**
     * Loads a {@link MoneyValue} from the given tag.
     * Tag given should match the tag created by {@link #save()}
     * Requires that the requisite {@link CurrencyType} be registered in for it to load custom Money Values
     */
    private static MoneyValue loadOldData(CompoundTag tag)
    {
        //LightmansCurrency.LogDebug("Attempting to load tag as MoneyValue:\n" + tag.getAsString());
        if(tag.contains("type", Tag.TAG_STRING))
        {
            ResourceLocation valueType;
            try { valueType = ResourceLocation.parse(tag.getString("type"));
            } catch (ResourceLocationException e) {
                //LightmansCurrency.LogError("Error loading CoinValue type " + tag.getString("type"));
                return empty();
            }
            CurrencyType<?> currencyType = LCRegistries.CURRENCY_TYPE.get(valueType);
            if(currencyType != null)
            {
                //LightmansCurrency.LogDebug("Loaded Money Value from tag. Result: " + result.getString("Empty") + "\nTag: " + tag.getAsString());
                return currencyType.loadOldMoneyValue(tag);
            }
            else
            {
                LightmansCurrency.LogError("No CurrencyType " + valueType + " could be found. Could not load the stored value!");
                return empty();
            }
        }
        else {
            //LightmansCurrency.LogDebug("Loaded deprecated CoinValue from tag. Result: " + result.getString("Empty") + "\nTag: " + tag.getAsString());
            return CoinValue.loadDeprecated(tag);
        }
    }

    
    public static MoneyValue safeLoad(CompoundTag parentTag, String tagName)
    {
        if(parentTag.contains(tagName,Tag.TAG_COMPOUND))
        {
            MoneyValue result = loadOldData(parentTag.getCompound(tagName));
            return result == null ? empty() : result;
        }
        else
        {
            MoneyValue result = CoinValue.loadDeprecated(parentTag, tagName);
            return result == null ? empty() : result;
        }
    }

    public static MoneyValue loadFromJson(JsonElement json) throws JsonSyntaxException, ResourceLocationException {
        if(json.isJsonArray() || json.isJsonPrimitive())
            return CoinValue.loadDeprecated(json);
        return loadFromJson(GsonHelper.convertToJsonObject(json, "Price"));
    }

    public static MoneyValue loadFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        return LENIENT_CODEC.decode(JsonOps.INSTANCE,json).getOrThrow(JsonSyntaxException::new).getFirst();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MoneyValue otherVal)
            return this.getUniqueName().equals(otherVal.getUniqueName()) && this.getCoreValue() == otherVal.getCoreValue() && this.isFree() == otherVal.isFree();
        return super.equals(obj);
    }

    @Override
    public final int hashCode() { return Objects.hash(this.isFree(),this.getUniqueName(),this.getCoreValue()); }

}
