package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.gacha.nodes.GachaStorageNode;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

import java.util.concurrent.CompletableFuture;

public class LCTraderNodeTypeTagProvider extends IntrinsicHolderTagsProvider<TraderNodeType<?>> {

    public LCTraderNodeTypeTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider) { this(output,lookupProvider, LCApi.MODID); }
    public LCTraderNodeTypeTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider, String modid) {
        super(output,LCRegistries.Trader.TRADER_NODE_TYPE_KEY, lookupProvider,TraderNodeType::getResourceKey,modid);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(LCTags.TraderNodes.DENY_ITEM_BARTERING)
                .add(GachaStorageNode.TYPE);
    }

}
