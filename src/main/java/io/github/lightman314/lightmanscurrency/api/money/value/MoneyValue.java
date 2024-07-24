package io.github.lightman314.lightmanscurrency.api.money.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.EmptyPriceEntry;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class made to store and reference values of Money.
 * To use my built-in implementation of this, see {@link CoinValue}
 */
public abstract class MoneyValue {

    private static MoneyValue FREE = null;
    @Nonnull
    public static MoneyValue free() {
        if(FREE == null)
            FREE = new NullValue(true);
        return FREE;
    }
    private static MoneyValue EMPTY = null;
    @Nonnull
    public static MoneyValue empty() {
        if (EMPTY == null)
            EMPTY = new NullValue(false);
        return EMPTY;
    }

    private String uniqueName;

    /**
     * The {@link CurrencyType} id corresponding to this value type.
     */
    @Nonnull
    protected abstract ResourceLocation getType();
    /**
     * The corresponding {@link CurrencyType} that should be used to handle this MoneyValue
     * May return null if this currency type hasn't been properly registered
     */
    public CurrencyType getCurrency() { return MoneyAPI.API.GetRegisteredCurrencyType(this.getType()); }

    @Nonnull
    protected String generateUniqueName() { return this.getType().toString(); }

    /**
     * Returns a unique name for this storage type.
     * Used by {@link MoneyStorage} & {@link MoneyView} to seperate {@link MoneyValue}'s into their different value entries.
     * For {@link CoinValue} data, this returns a combination of it's <code>chain</code> and {@link #getType()},
     * but values without varying types may simply return a String version of {@link #getType()}
     */
    @Nonnull
    public final String getUniqueName() {
        if(this.uniqueName == null)
            this.uniqueName = this.generateUniqueName();
        return this.uniqueName;
    }

    @Nonnull
    protected final String generateCustomUniqueName(@Nonnull String addon) { return generateCustomUniqueName(this.getType(), addon); }

    @Nonnull
    public static String generateCustomUniqueName(@Nonnull ResourceLocation type, @Nonnull String addon)
    {
        if(addon.isEmpty())
            return type.toString();
        else //For custom unique names, place '!' character between them, as it's not a legal ResourceLocation character
            return type + "!" + addon;
    }

    /**
     * Whether this value is considered "Free".
     * Should never be true for values used to denote an amount of stored money.
     * Should only be true if this is {@link #FREE}
     */
    public boolean isFree() { return false; }

    /**
     * Whether this value has nothing stored in it.
     * Used to cull un-used value data to saveItem space, but can also be used to simplify math
     * as there's no need to add two values if one is already empty (and thus a value of 0)
     * By default returns <code>true</code> if {@link MoneyValue#getCoreValue} returns exactly 0.
     * @see #EMPTY
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
     * but allows compatiblity if one or both are the {@link #FREE} or {@link #EMPTY} constant.
     */
    public boolean sameType(@Nonnull MoneyValue otherValue) { return otherValue.getUniqueName().equals(this.getUniqueName()) || this instanceof NullValue || otherValue instanceof NullValue; }


    public abstract long getCoreValue();

    /**
     * Returns a string display of this value.
     */
    @Nonnull
    public final String getString() { return this.getString(""); }

    /**
     * Returns a string display of this value.
     * By default, returns a string version of {@link #getText(MutableComponent)}
     */
    @Nonnull
    public String getString(@Nonnull String emptyText) { return this.getText(EasyText.literal(emptyText)).getString(); }

    /**
     * Returns a text display of this value.
     */
    @Nonnull
    public final MutableComponent getText() { return this.getText(EasyText.empty()); }

    /**
     * Returns a text display of this value.
     * @param emptyText Text to display if this value is empty (such as "NULL" or "0" or even blank text). May be ignored if your value has its own formatting for being empty (such as $0, etc.)
     */
    @Nonnull
    public final MutableComponent getText(@Nonnull String emptyText) { return this.getText(EasyText.literal(emptyText)); }
    /**
     * Returns a text display of this value.
     * @param emptyText Text to display if this value is empty (such as "NULL" or "0" or even blank text). May be ignored if your value has its own formatting for being empty (such as $0, etc.)
     */
    public abstract MutableComponent getText(@Nonnull MutableComponent emptyText);

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
    public abstract MoneyValue addValue(@Nonnull MoneyValue addedValue);

    /**
     * Whether this value contains enough money to safely subtract the given {@link MoneyValue}.
     * @return false if <code>this#</code>{@link #getCoreValue()} < <code>queryValue#</code>{@link #getCoreValue()},
     * or if the queried value is not compatible.
     * @see #addValue(MoneyValue)
     * @see #subtractValue(MoneyValue)
     * @see #getCoreValue()
     */
    public abstract boolean containsValue(@Nonnull MoneyValue queryValue);

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
    public abstract MoneyValue subtractValue(@Nonnull MoneyValue removedValue);

    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule}'s that give
     * percentage-based discounts.
     * Will round down by default.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @return {@link #free()} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise a value equal to <code>{@link #getCoreValue()} * percentage / 100 </code>
     * @see #percentageOfValue(int, boolean)
     */
    @Nonnull
    public final MoneyValue percentageOfValue(int percentage) { return this.percentageOfValue(percentage, false); }

    /**
     * Does math to obtain the given percentage of the value.
     * Used to calculate Tax Collection and certain {@link io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule}'s that give
     * percentage-based discounts.
     * @param percentage The percentage value between 0 and 1000 (limited to 1000% to avoid values exceeding number limitations)
     * @param roundUp Whether we should round a value up to nearest valid value if the exact percentage results in a partial value. If <code>false</code> round down.
     * @return {@link #FREE} if percentage = 0, <code>this</code> if percentage = 100.
     * Otherwise a value equal to <code>{@link #getCoreValue()} * percentage / 100 </code>
     */
    public abstract MoneyValue percentageOfValue(int percentage, boolean roundUp);

    /**
     * Does math to multiply this value by the given amount.<br>
     * Result can be rounded as desired, but should be of the same type as the original value unless the result is {@link #empty() empty}.
     * @param multiplier The amount to multiply this value by.
     * @return The mathematical result of multiplying this value by the given number.
     */
    @Nonnull
    public abstract MoneyValue multiplyValue(double multiplier);

    /**
     * Function called when a block that contains money is broken through either legitimate means, or by illegal means.
     * @param level The level that the block was in.
     * @param owner Data about the blocks' owner, so that any non-item based money can instead be given to the player directly.
     * @return List of items to drop/eject. Leave empty if money is given to the owner manually.
     */
    @Nonnull
    public abstract List<ItemStack> onBlockBroken(@Nonnull Level level, @Nonnull OwnerData owner);

    /**
     * Returns the smallest non-zero value of this money value type.
     */
    @Nonnull
    public abstract MoneyValue getSmallestValue();

    /**
     * Returns a Money Value with the same {@link #getUniqueName()} but with the given core value<br>
     * Used for calculated math
     */
    @Nonnull
    public abstract MoneyValue fromCoreValue(long value);


    /**
     * Saves this {@link MoneyValue} data into an NBT tag.
     * @see #load(CompoundTag) 
     * @see #safeLoad(CompoundTag, String)
     */
    @Nonnull
    public final CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("type", this.getType().toString());
        return tag;
    }

    /**
     * Type-dependent method to actually save the value to the NBT tag.
     * Data saved here should be loadable via its corresponding {@link CurrencyType#loadMoneyValue(CompoundTag)}
     */
    protected abstract void saveAdditional(@Nonnull CompoundTag tag);

    /**
     * Encodes this value into the given buffer for use in custom packets.
     */
    public final void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.save()); }

    /**
     * Saves this {@link MoneyValue} data into a Json Object
     */
    public final JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        this.writeAdditionalToJson(json);
        json.addProperty("type", this.getType().toString());
        return json;
    }

    /**
     * Type-dependent method to actually save the value to the NBT tag.
     * Data saved here should be loadable via its corresponding {@link CurrencyType#loadMoneyValue(CompoundTag)}
     */
    protected abstract void writeAdditionalToJson(@Nonnull JsonObject json);

    /**
     * Decoded this value from the given buffer.
     * If it fails to load the data, an empty value will be given instead.
     */
    @Nonnull
    public static MoneyValue decode(@Nonnull FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readAnySizeNbt();
        MoneyValue loadedValue = load(tag);
        return Objects.requireNonNullElse(loadedValue, EMPTY);
    }

    /**
     * Loads a {@link MoneyValue} from the given tag.
     * Tag given should match the tag created by {@link #save()}
     * Requires that the requisite {@link CurrencyType} be registered in for it to load custom Money Values
     */
    @Nullable
    public static MoneyValue load(@Nonnull CompoundTag tag)
    {
        //LightmansCurrency.LogDebug("Attempting to load tag as MoneyValue:\n" + tag.getAsString());
        if(tag.contains("type", Tag.TAG_STRING))
        {
            ResourceLocation valueType;
            try { valueType = new ResourceLocation(tag.getString("type"));
            } catch (ResourceLocationException e) {
                //LightmansCurrency.LogError("Error loading CoinValue type " + tag.getString("type"));
                return null;
            }
            CurrencyType currencyType = MoneyAPI.API.GetRegisteredCurrencyType(valueType);
            if(currencyType != null)
            {
                //LightmansCurrency.LogDebug("Loaded Money Value from tag. Result: " + result.getString("Empty") + "\nTag: " + tag.getAsString());
                return currencyType.loadMoneyValue(tag);
            }
            else
            {
                LightmansCurrency.LogError("No CurrencyType " + valueType + " could be found. Could not load the stored value!");
                return null;
            }
        }
        else {
            //LightmansCurrency.LogDebug("Loaded deprecated CoinValue from tag. Result: " + result.getString("Empty") + "\nTag: " + tag.getAsString());
            return CoinValue.loadDeprecated(tag);
        }
    }

    @Nonnull
    public static MoneyValue safeLoad(@Nonnull CompoundTag parentTag, @Nonnull String tagName)
    {
        if(parentTag.contains(tagName, Tag.TAG_COMPOUND))
        {
            MoneyValue result = load(parentTag.getCompound(tagName));
            return result == null ? empty() : result;
        }
        else
        {
            MoneyValue result = CoinValue.loadDeprecated(parentTag, tagName);
            return result == null ? empty() : result;
        }

    }

    public static MoneyValue loadFromJson(@Nonnull JsonElement json) throws JsonSyntaxException, ResourceLocationException {
        if(json.isJsonArray() || json.isJsonPrimitive())
            return CoinValue.loadDeprecated(json);
        return loadFromJson(GsonHelper.convertToJsonObject(json, "Price"));
    }

    public static MoneyValue loadFromJson(@Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        if(json.has("type"))
        {
            ResourceLocation valueType = new ResourceLocation(GsonHelper.getAsString(json, "type"));
            CurrencyType currencyType = MoneyAPI.API.GetRegisteredCurrencyType(valueType);
            if(currencyType != null)
                return currencyType.loadMoneyValueJson(json);
            else
                throw new JsonSyntaxException("No CurrencyType " + valueType + " could be found. Could not load the stored json value!");
        }
        else
            return CoinValue.loadDeprecated(json);
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public abstract DisplayEntry getDisplayEntry(@Nullable List<Component> additionalTooltips, boolean tooltipOverride);

    private static final class NullValue extends MoneyValue
    {
        private final boolean free;
        private NullValue(boolean free) { this.free = free; }
        @Nonnull
        @Override
        protected ResourceLocation getType() { return NullCurrencyType.TYPE;}
        @Nonnull
        @Override
        protected String generateUniqueName() { return this.free ? "null!free" : "null!empty"; }
        @Override
        public boolean isFree() { return this.free; }
        @Override
        public boolean isValidPrice() { return this.free; }
        @Override
        public boolean isEmpty() { return true; }
        @Override
        public long getCoreValue() { return 0; }
        @Nonnull
        @Override
        public MutableComponent getText(@Nonnull MutableComponent emptyText) { return this.free ? LCText.GUI_MONEY_VALUE_FREE.get() : emptyText; }
        @Override
        public MoneyValue addValue(@Nonnull MoneyValue addedValue) { return addedValue; }
        @Nonnull
        @Override
        public MoneyValue multiplyValue(double multiplier) { return this; }
        @Override
        public boolean containsValue(@Nonnull MoneyValue queryValue) { return queryValue.isFree() || queryValue.isEmpty(); }
        @Override
        public MoneyValue subtractValue(@Nonnull MoneyValue removedValue) { return removedValue.isFree() || removedValue.isEmpty() ? this : null; }
        @Override
        public MoneyValue percentageOfValue(int percentage, boolean roundUp) { return FREE; }
        @Nonnull
        @Override
        public List<ItemStack> onBlockBroken(@Nonnull Level level, @Nonnull OwnerData owner) { return new ArrayList<>(); }
        @Override
        protected void saveAdditional(@Nonnull CompoundTag tag) { tag.putBoolean("Free", this.isFree()); }
        @Override
        protected void writeAdditionalToJson(@Nonnull JsonObject json) { if(this.isFree()) json.addProperty("Free", true); }
        @Nonnull
        @Override
        public MoneyValue getSmallestValue() { return this; }
        @Nonnull
        @Override
        public MoneyValue fromCoreValue(long value) { return this; }
        @Nonnull
        @Override
        public DisplayEntry getDisplayEntry(@Nullable List<Component> tooltips, boolean tooltipOverride) { return new EmptyPriceEntry(this, tooltips); }
        @Override
        public String toString() { return "NullMoneyValue:"+ (this.free ? "Free" : "Empty"); }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MoneyValue otherVal)
            return this.getUniqueName().equals(otherVal.getUniqueName()) && this.getCoreValue() == otherVal.getCoreValue() && this.isFree() == otherVal.isFree();
        return super.equals(obj);
    }
}
