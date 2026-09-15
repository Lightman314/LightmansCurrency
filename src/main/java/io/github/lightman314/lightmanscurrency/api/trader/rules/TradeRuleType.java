package io.github.lightman314.lightmanscurrency.api.trader.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public final class TradeRuleType<T extends TradeRule> extends AbstractType.WithCodec<T,TradeRuleType<?>> {

    private final Codec<T> fullCodec;
    public Codec<T> fullCodec() { return this.fullCodec; }

    private final Supplier<T> factory;
    public T createNew() { return this.factory.get(); }

    public TradeRuleType(MapCodec<T> codec,Supplier<T> factory) { super(codec); this.fullCodec = codec.codec(); this.factory = factory; }

    @Override
    protected Registry<TradeRuleType<?>> getRegistry() { return LCRegistries.Trader.TRADE_RULE_TYPE; }
    @Override
    protected String getName() { return "TradeRuleType"; }

}
