package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;
import java.util.List;

public class AncientMoneyValue extends MoneyValue implements IItemBasedValue {

    public final AncientCoinType type;
    public final long count;

    public static final MapCodec<AncientMoneyValue> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            EnumUtil.buildCodec(AncientCoinType.class,"Ancient Coin Type").fieldOf("coin").forGetter(v -> v.type),
            Codec.LONG.validate(l -> l > 0 ? DataResult.success(l) : DataResult.error(() -> "Count cannot be less than 1")).fieldOf("count").forGetter(v -> v.count)
    ).apply(builder,AncientMoneyValue::new));
    public static final StreamCodec<ByteBuf,AncientMoneyValue> STREAM_CODEC = StreamCodec.composite(
            EnumUtil.streamCodec(AncientCoinType.class,"Ancient Coin Type"),v -> v.type,
            ByteBufCodecs.VAR_LONG,v -> v.count,
            AncientMoneyValue::parseOrThrow);


    private AncientMoneyValue(AncientCoinType type, long count) {
        this.type = type;
        this.count = count;
    }

    private static AncientMoneyValue parseOrThrow(AncientCoinType type,long count) {
        if(type == null)
            throw new DecoderException("Cannot parse a null Ancient Coin Type!");
        if(count <= 0)
            throw new DecoderException("Count cannot be less than 1");
        return new AncientMoneyValue(type,count);
    }

    public static MoneyValue of(AncientCoinType type, long count) {
        if(type == null || count <= 0)
            return empty();
        return new AncientMoneyValue(type,count);
    }

    @Override
    public CurrencyType<?> getType() { return AncientMoneyType.INSTANCE; }
    
    @Override
    protected String generateUniqueName() { return this.generateCustomUniqueName(this.type.resourceSafeName()); }

    @Override
    public boolean isEmpty() { return this.count <= 0; }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCoreValue() { return Math.max(0,this.count); }

    @Override
    public Component getText(Component emptyText) {
        if(this.isEmpty())
            return emptyText;
        return LCText.ANCIENT_COIN_VALUE_DISPLAY.get(this.getCoreValue(),this.type.initial(),this.type.icon());
    }

    @Override
    public MoneyValue addValue(MoneyValue addedValue) {
        if(addedValue instanceof AncientMoneyValue other && other.type == this.type)
            return of(this.type,this.count + other.count);
        //Return this if the other value is empty
        if(addedValue.isEmpty())
            return this;
        return null;
    }

    @Override
    public boolean containsValue(MoneyValue queryValue) {
        if(queryValue instanceof AncientMoneyValue value && value.type == this.type)
            return value.count <= this.count;
        return queryValue.isEmpty();
    }

    @Override
    public MoneyValue subtractValue(MoneyValue removedValue) {
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

    
    @Override
    public List<ItemStack> getAsItemList() {
        return Lists.newArrayList(this.type.asItem(this.count));
    }

    
    @Override
    public List<ItemStack> onBlockBroken(OwnerData owner) { return this.getAsSeperatedItemList(); }

    
    @Override
    public MoneyValue getSmallestValue() { return of(this.type,1); }

    @Override
    public boolean allowInterest() { return false; }

    @Override
    public MoneyValue fromCoreValue(long value) { return of(this.type,value); }

    public static MoneyValue load(CompoundTag tag)
    {
        AncientCoinType type = EnumUtil.enumFromString(tag.getString("Coin"), AncientCoinType.values(),null);
        long count = tag.getLong("Count");
        return of(type,count);
    }

    public static MoneyValue loadFromJson(JsonObject json) throws JsonSyntaxException
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
