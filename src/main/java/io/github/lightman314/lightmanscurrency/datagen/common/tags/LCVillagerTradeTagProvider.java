package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.features.villagers.LCVillagerTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VillagerTradesTagsProvider;
import net.minecraft.tags.VillagerTradeTags;

import java.util.concurrent.CompletableFuture;

public class LCVillagerTradeTagProvider extends VillagerTradesTagsProvider {

    public LCVillagerTradeTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        this.tag(VillagerTradeTags.WANDERING_TRADER_COMMON)
                .add(LCVillagerTrades.WANDERING_TRADER_ATM);

        this.tag(VillagerTradeTags.WANDERING_TRADER_UNCOMMON)
                .add(LCVillagerTrades.WANDERING_TRADER_DISPLAY_CASE);

    }

}
