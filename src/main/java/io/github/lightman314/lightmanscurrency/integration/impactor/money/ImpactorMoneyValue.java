package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;


import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImpactorMoneyValue extends MoneyValue {

    public static final MapCodec<ImpactorMoneyValue> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            LCImpactorCompat.KEY_CODEC.fieldOf("currency").forGetter(ImpactorMoneyValue::getCurrencyKey),
            CodecHelper.BIG_DECIMAL.fieldOf("value").forGetter(ImpactorMoneyValue::getValue)
    ).apply(builder,ImpactorMoneyValue::parse));

    public static final StreamCodec<FriendlyByteBuf,ImpactorMoneyValue> STREAM_CODEC = StreamCodec.composite(
            LCImpactorCompat.KEY_STREAM_CODEC,ImpactorMoneyValue::getCurrencyKey,
            StreamHelper.BIG_DECIMAL,ImpactorMoneyValue::getValue,
            ImpactorMoneyValue::parse);

    private final Currency currency;
    public Currency getImpactorCurrency() { return this.currency; }
    private Key getCurrencyKey() { return this.currency.key(); }
    private final BigDecimal value;
    public BigDecimal getValue() { return this.value; }

    private ImpactorMoneyValue(Currency currency, BigDecimal value) {
        this.currency = Objects.requireNonNull(currency,"Currency Type must not be null!");
        this.value = value;
    }

    @Override
    protected String generateUniqueName() { return this.generateCustomUniqueName(this.getCurrencyKey().toString()); }

    @Override
    public CurrencyType<?> getType() { return ImpactorCurrencyType.INSTANCE; }

    @Override
    public boolean isEmpty() { return this.getCoreValue() <= 0; }

    private static BigDecimal getDecimalNullifier(Currency currency) {
        int decimals = currency.decimals();
        int result = 1;
        while(decimals-- > 0)
            result *= 10;
        return BigDecimal.valueOf(result);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCoreValue() { return Math.max(0,this.value.multiply(getDecimalNullifier(this.currency)).longValue()); }

    @Override
    public Component getText(Component emptyText) {
        if(this.isEmpty())
            return emptyText;
        return LCImpactorCompat.convertComponent(this.currency.format(this.value));
    }

    @Override
    public MoneyValue addValue(MoneyValue addedValue) {
        if(addedValue instanceof ImpactorMoneyValue other && other.getImpactorCurrency().key().equals(this.currency.key()))
            return of(this.currency,this.value.add(other.value));
        return null;
    }

    @Override
    public boolean containsValue(MoneyValue queryValue) {
        if(queryValue instanceof ImpactorMoneyValue other)
            return this.value.doubleValue() >= other.value.doubleValue();
        return false;
    }

    @Override
    public MoneyValue subtractValue(MoneyValue removedValue) {
        if(removedValue instanceof ImpactorMoneyValue other && other.getImpactorCurrency().key().equals(this.currency.key()))
            return of(this.currency,this.value.subtract(other.value));
        return null;
    }

    @Override
    public MoneyValue percentageOfValue(int percentage, boolean roundUp) {
        if(percentage == 100)
            return this;
        if(percentage == 0)
            return MoneyValue.free();
        BigDecimal mult = BigDecimal.valueOf(percentage).divide(BigDecimal.valueOf(100),MathContext.UNLIMITED);
        BigDecimal newValue = this.value.multiply(mult);
        return of(this.currency,newValue);
    }

    @Override
    public MoneyValue multiplyValue(double multiplier) {
        BigDecimal mult = BigDecimal.valueOf(multiplier);
        BigDecimal newValue = this.value.multiply(mult);
        return of(this.currency,newValue);
    }

    @Override
    public List<ItemStack> onBlockBroken(OwnerData owner) {
        PlayerReference player = owner.getPlayerForContext();
        Account account = LCImpactorCompat.getPlayerAccount(player.id,this.currency);
        if(account != null)
            account.deposit(this.value);
        return new ArrayList<>();
    }

    
    @Override
    public MoneyValue getSmallestValue() { return fromCoreValue(1); }

    
    @Override
    public MoneyValue fromCoreValue(long value) {
        BigDecimal result = BigDecimal.ONE.divide(getDecimalNullifier(this.currency),MathContext.UNLIMITED);
        return of(this.currency,result);
    }

    public static MoneyValue loadOldValue(CompoundTag tag) {
        Key currency = Key.key(tag.getString("Currency"),':');
        BigDecimal value = new BigDecimal(tag.getString("Value"));
        return parse(currency,value);
    }

    private static ImpactorMoneyValue parse(Key currencyKey,BigDecimal value) {
        MoneyValue val = of(currencyKey,value);
        if(val instanceof ImpactorMoneyValue iv)
            return iv;
        return null;
    }

    public static MoneyValue of(Key currencyKey,BigDecimal value) { return of(EconomyService.instance().currencies().currency(currencyKey).orElse(null),value); }
    public static MoneyValue of(Currency currency, BigDecimal value) {
        if(currency == null)
            return MoneyValue.empty();
        if(value.doubleValue() <= 0)
            return empty();
        return new ImpactorMoneyValue(currency,value);
    }

}
