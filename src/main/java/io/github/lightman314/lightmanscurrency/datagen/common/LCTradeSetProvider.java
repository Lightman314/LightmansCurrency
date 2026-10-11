package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.features.villagers.LCTradeSets;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;

public final class LCTradeSetProvider {

    private LCTradeSetProvider() {}

    public static void bootstrap(BootstrapContext<TradeSet> context) {
        //Banker
        TradeSets.register(context,LCTradeSets.BANKER_LEVEL_1,LCTags.VillagerTrades.BANKER_LEVEL_1);
        TradeSets.register(context,LCTradeSets.BANKER_LEVEL_2,LCTags.VillagerTrades.BANKER_LEVEL_2);
        TradeSets.register(context,LCTradeSets.BANKER_LEVEL_3,LCTags.VillagerTrades.BANKER_LEVEL_3);
        TradeSets.register(context,LCTradeSets.BANKER_LEVEL_4,LCTags.VillagerTrades.BANKER_LEVEL_4);
        TradeSets.register(context,LCTradeSets.BANKER_LEVEL_5,LCTags.VillagerTrades.BANKER_LEVEL_5);
        //Cashier
        TradeSets.register(context,LCTradeSets.CASHIER_LEVEL_1,LCTags.VillagerTrades.CASHIER_LEVEL_1);
        TradeSets.register(context,LCTradeSets.CASHIER_LEVEL_2,LCTags.VillagerTrades.CASHIER_LEVEL_2);
        TradeSets.register(context,LCTradeSets.CASHIER_LEVEL_3,LCTags.VillagerTrades.CASHIER_LEVEL_3);
        TradeSets.register(context,LCTradeSets.CASHIER_LEVEL_4,LCTags.VillagerTrades.CASHIER_LEVEL_4);
        TradeSets.register(context,LCTradeSets.CASHIER_LEVEL_5,LCTags.VillagerTrades.CASHIER_LEVEL_5);
    }

}
