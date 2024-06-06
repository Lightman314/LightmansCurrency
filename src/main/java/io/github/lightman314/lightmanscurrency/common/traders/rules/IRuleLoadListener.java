package io.github.lightman314.lightmanscurrency.common.traders.rules;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IRuleLoadListener {

    default void beforeLoading(@Nullable ITradeRuleHost host, @Nonnull List<CompoundTag> allData, @Nonnull List<TradeRule> rules) {}
    default void afterLoading(@Nullable ITradeRuleHost host, @Nonnull List<CompoundTag> allData, @Nonnull List<TradeRule> rules) {}

}
