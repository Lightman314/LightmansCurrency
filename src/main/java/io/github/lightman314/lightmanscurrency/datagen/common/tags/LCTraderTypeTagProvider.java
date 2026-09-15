package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

import java.util.concurrent.CompletableFuture;

public class LCTraderTypeTagProvider extends IntrinsicHolderTagsProvider<TraderType> {

    public LCTraderTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) { this(output,lookupProvider,LCApi.MODID); }
    public LCTraderTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid) {
        super(output,LCRegistries.Trader.TRADER_TYPE_KEY,lookupProvider,TraderType::getResourceKey,modid);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        this.tag(LCTags.TraderTypes.ITEM_TRADER)
                .add(LCTraderTypes.ITEM_TRADER.get())
                .add(LCTraderTypes.ARMOR_DISPLAY.get());

    }

}
