package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

public class AncientMoneyValue extends MoneyValue implements IItemBasedValue {

    public final AncientCoinType type;
    public final long count;

    private AncientMoneyValue(@Nonnull AncientCoinType type, long count) {
        this.type = type;
        this.count = count;
    }

    @Nonnull
    public static MoneyValue of(AncientCoinType type, long count) {
        if(type == null || count <= 0)
            return empty();
        return new AncientMoneyValue(type,count);
    }

    @Nonnull
    @Override
    protected ResourceLocation getType() { return AncientMoneyType.TYPE; }

    @Nonnull
    @Override
    protected String generateUniqueName() { return this.generateCustomUniqueName(this.type.resourceSafeName()); }

    @Override
    public boolean isEmpty() { return this.count <= 0; }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCoreValue() { return Math.max(0,this.count); }

    @Override
    public MutableComponent getText(@Nonnull MutableComponent emptyText) {
        if(this.isEmpty())
            return emptyText;
        return LCText.ANCIENT_COIN_VALUE_DISPLAY.get(this.getCoreValue(),this.type.initial(),this.type.icon());
    }

    @Override
    public MoneyValue addValue(@Nonnull MoneyValue addedValue) {
        if(addedValue instanceof AncientMoneyValue other && other.type == this.type)
            return of(this.type,this.count + other.count);
        //Return this if the other value is empty
        if(addedValue.isEmpty())
            return this;
        return null;
    }

    @Override
    public boolean containsValue(@Nonnull MoneyValue queryValue) {
        if(queryValue instanceof AncientMoneyValue value && value.type == this.type)
            return value.count <= this.count;
        return queryValue.isEmpty();
    }

    @Override
    public MoneyValue subtractValue(@Nonnull MoneyValue removedValue) {
        if(removedValue instanceof AncientMoneyValue other && other.type == this.type)
            return of(this.type, this.count - other.count);
        //Return this if the other value is empty
        if(removedValue.isEmpty())
            return this;
        return null;
    }

    @Override
    public MoneyValue percentageOfValue(int percentage, boolean roundUp) {
        if(percentage == 100)
            return this;
        if(percentage == 0)
            return MoneyValue.free();
        if(this.count <= 0)
            return MoneyValue.free();
        long value = this.getCoreValue();
        //Calculate the new value
        long newValue = value * MathUtil.clamp(percentage, 0, 1000) / 100L;
        //Calculate the new value in double format for rounding checks
        if(roundUp)
        {
            long partial = value * MathUtil.clamp(percentage, 0, 1000) % 100L;
            if(partial > 0)
                newValue += 1;
        }
        if(newValue <= 0)
            return MoneyValue.free();
        return this.fromCoreValue(newValue);
    }

    @Nonnull
    @Override
    public MoneyValue multiplyValue(double multiplier) {
        BigDecimal value = BigDecimal.valueOf(this.getCoreValue());
        BigDecimal result = value.multiply(BigDecimal.valueOf(multiplier));
        //If less than 1, return empty
        if(result.compareTo(BigDecimal.valueOf(0.5d)) < 0)
            return MoneyValue.empty();
        if(result.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0)
        {
            //If larger than max long value, return max long value
            return of(this.type,Long.MAX_VALUE);
        }
        long rounding = 0;
        if(result.remainder(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.5d)) >= 0)
            rounding = 1;
        return of(this.type, result.longValue() + rounding);
    }

    @Nonnull
    @Override
    public List<ItemStack> getAsItemList() {
        return Lists.newArrayList(this.type.asItem(this.count));
    }

    @Nonnull
    @Override
    public List<ItemStack> onBlockBroken(@Nonnull OwnerData owner) { return this.getAsSeperatedItemList(); }

    @Nonnull
    @Override
    public MoneyValue getSmallestValue() { return of(this.type,1); }

    @Override
    public boolean allowInterest() { return false; }

    @Nonnull
    @Override
    public MoneyValue fromCoreValue(long value) { return of(this.type,value); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.putString("Coin",this.type.toString());
        tag.putLong("Count",this.count);
    }

    public static MoneyValue load(@Nonnull CompoundTag tag)
    {
        AncientCoinType type = EnumUtil.enumFromString(tag.getString("Coin"), AncientCoinType.values(),null);
        long count = tag.getLong("Count");
        return of(type,count);
    }

    @Override
    protected void writeAdditionalToJson(@Nonnull JsonObject json) {
        json.addProperty("Coin",this.type.toString());
        json.addProperty("Count",this.count);
    }

    public static MoneyValue loadFromJson(@Nonnull JsonObject json) throws JsonSyntaxException
    {
        String typeString = GsonHelper.getAsString(json,"Coin");
        AncientCoinType type = EnumUtil.enumFromString(typeString, AncientCoinType.values(),null);
        if(type == null)
            throw new JsonSyntaxException(typeString + " is not a valid CoinType");
        long count = GsonHelper.getAsLong(json,"Count");
        if(count <= 0)
            throw new JsonSyntaxException("Count cannot be less than 1");
        return of(type,count);
    }

}
