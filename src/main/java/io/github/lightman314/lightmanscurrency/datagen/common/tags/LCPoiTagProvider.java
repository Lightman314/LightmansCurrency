package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.LCPoiTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.concurrent.CompletableFuture;

public class LCPoiTagProvider extends KeyTagProvider<PoiType> {

    public LCPoiTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,Registries.POINT_OF_INTEREST_TYPE,lookupProvider,LCApi.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .add(LCPoiTypes.CASHIER.getKey())
                .add(LCPoiTypes.BANKER.getKey());
    }
}
