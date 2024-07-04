package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class RepairWithMoneyData
{

    public static final Codec<RepairWithMoneyData> CODEC =
            RecordCodecBuilder.create(builder ->
                    builder.group(Codec.STRING.optionalFieldOf("baseCost","").forGetter(d -> d.baseCostInput),
                                    Codec.STRING.optionalFieldOf("infintyExtraCost","").forGetter(d -> d.infinityExtraCostInput))
                            .apply(builder,RepairWithMoneyData::new));

    long lastCacheTime = 0;
    Cache cache = null;

    private final String baseCostInput;
    public MoneyValue getBaseCost() { this.checkCache(); return this.cache.baseCost(); }
    private final String infinityExtraCostInput;
    public MoneyValue getInfinityExtraCost() { this.checkCache(); return this.cache.infinityExtraCost; }

    public RepairWithMoneyData(@Nonnull String baseCost, @Nonnull String infinityExtraCost) { this.baseCostInput = baseCost; this.infinityExtraCostInput = infinityExtraCost; }

    private void checkCache()
    {
        //Re-parse values every 2 minutes
        if(this.cache == null || System.currentTimeMillis() - this.lastCacheTime >= 1000 * 120)
        {
            MoneyValue baseCost = this.safeParse(this.baseCostInput, LCConfig.SERVER.moneyMendingRepairCost, null);
            MoneyValue infinityExtraCost = this.safeParse(this.infinityExtraCostInput, LCConfig.SERVER.moneyMendingInfinityCost, baseCost);
            this.cache = new Cache(baseCost,infinityExtraCost);
        }
    }

    @Nonnull
    private MoneyValue safeParse(@Nonnull String input, @Nonnull Supplier<MoneyValue> fallback, @Nullable MoneyValue mustMatch) {
        try {
            if(input.isEmpty())
                return fallback.get();
            MoneyValue result = MoneyValueParser.parse(new StringReader(input),mustMatch != null);
            if(mustMatch != null && !result.sameType(mustMatch))
                return MoneyValue.empty();
            return result;
        } catch (CommandSyntaxException exception) {
            MoneyValue result = fallback.get();
            if(mustMatch != null && !result.sameType(mustMatch))
                return MoneyValue.empty();
            return result;
        }
    }

    @Override
    public int hashCode() { return Objects.hash(this.baseCostInput,this.infinityExtraCostInput); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RepairWithMoneyData other)
            return this.baseCostInput.equals(other.baseCostInput) && this.infinityExtraCostInput.equals(other.infinityExtraCostInput);
        return false;
    }

    private record Cache(@Nonnull MoneyValue baseCost, @Nonnull MoneyValue infinityExtraCost) {}

}
