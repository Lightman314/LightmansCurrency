package io.github.lightman314.lightmanscurrency.api.traders.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public abstract class TradeRuleType<T extends TradeRule> {

    public static final Codec<TradeRuleType<?>> CODEC = LCRegistries.TRADE_RULE.byNameCodec();

    public abstract T create();

    public abstract MapCodec<T> mapCodec();

    @Deprecated
    public T loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        T rule = this.create();
        rule.load(tag,lookup);
        return rule;
    }

    @Override
    public final int hashCode() { return LCRegistries.TRADE_RULE.getKey(this).hashCode(); }
    @Override
    public final String toString() { return "TradeRuleType[" + LCRegistries.TRADE_RULE.getKey(this) + "]"; }

}
