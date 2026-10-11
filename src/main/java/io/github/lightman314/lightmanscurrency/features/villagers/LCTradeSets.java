package io.github.lightman314.lightmanscurrency.features.villagers;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;

public final class LCTradeSets {

    private LCTradeSets() {}

    public static final ResourceKey<TradeSet> BANKER_LEVEL_1 = resourceKey("banker/level_1");
    public static final ResourceKey<TradeSet> BANKER_LEVEL_2 = resourceKey("banker/level_2");
    public static final ResourceKey<TradeSet> BANKER_LEVEL_3 = resourceKey("banker/level_3");
    public static final ResourceKey<TradeSet> BANKER_LEVEL_4 = resourceKey("banker/level_4");
    public static final ResourceKey<TradeSet> BANKER_LEVEL_5 = resourceKey("banker/level_5");

    public static final Int2ObjectMap<ResourceKey<TradeSet>> BANKER_SET = Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1,BANKER_LEVEL_1),
            Int2ObjectMap.entry(2,BANKER_LEVEL_2),
            Int2ObjectMap.entry(3,BANKER_LEVEL_3),
            Int2ObjectMap.entry(4,BANKER_LEVEL_4),
            Int2ObjectMap.entry(5,BANKER_LEVEL_5)
    );

    public static final ResourceKey<TradeSet> CASHIER_LEVEL_1 = resourceKey("cashier/level_1");
    public static final ResourceKey<TradeSet> CASHIER_LEVEL_2 = resourceKey("cashier/level_2");
    public static final ResourceKey<TradeSet> CASHIER_LEVEL_3 = resourceKey("cashier/level_3");
    public static final ResourceKey<TradeSet> CASHIER_LEVEL_4 = resourceKey("cashier/level_4");
    public static final ResourceKey<TradeSet> CASHIER_LEVEL_5 = resourceKey("cashier/level_5");

    public static final Int2ObjectMap<ResourceKey<TradeSet>> CASHIER_SET = Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1,CASHIER_LEVEL_1),
            Int2ObjectMap.entry(2,CASHIER_LEVEL_2),
            Int2ObjectMap.entry(3,CASHIER_LEVEL_3),
            Int2ObjectMap.entry(4,CASHIER_LEVEL_4),
            Int2ObjectMap.entry(5,CASHIER_LEVEL_5)
    );

    private static ResourceKey<TradeSet> resourceKey(String name) { return ResourceKey.create(Registries.TRADE_SET,LCApi.id(name));}

}
