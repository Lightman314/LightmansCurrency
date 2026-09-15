package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.features.loot.functions.RandomizeItemFunction;
import io.github.lightman314.lightmanscurrency.features.villagers.LCVillagerTrades;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.List;
import java.util.Optional;

public final class LCVillagerTradeProvider {

    private LCVillagerTradeProvider() {}

    public static void bootstrap(BootstrapContext<VillagerTrade> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(LCVillagerTrades.WANDERING_TRADER_ATM,new VillagerTrade(
                new TradeCost(LCItems.COIN_GOLD,1),
                new ItemStackTemplate(LCBlocks.ATM.asItem()),
                1,1,0.05f, Optional.empty(), List.of()
        ));
        HolderSet<Item> displayCases = items.getOrThrow(LCTags.Items.GROUP_DISPLAY_CASE);
        context.register(LCVillagerTrades.WANDERING_TRADER_DISPLAY_CASE,new VillagerTrade(
                new TradeCost(LCItems.COIN_GOLD,2),
                new ItemStackTemplate(LCBlocks.DISPLAY_CASE.get(VanillaColor.WHITE).asItem()),
                12,1,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(displayCases))
        ));
    }

}
