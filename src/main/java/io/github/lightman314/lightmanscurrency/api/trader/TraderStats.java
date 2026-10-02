package io.github.lightman314.lightmanscurrency.api.trader;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;

public final class TraderStats {
    private TraderStats() {}

    public static final StatKey<Integer,Integer> INTERACTION_COUNT = StatKey.createInt(LCApi.id("trader_interactions"),-100);
    public static final StatKey<Long,Long> LAST_INTERACTION = StatKey.createTimestamp(LCApi.id("last_interaction"),Integer.MIN_VALUE);


}