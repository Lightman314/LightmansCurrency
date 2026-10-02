package io.github.lightman314.lightmanscurrency.datagen.common.curios;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class LCCuriosDataProvider extends CuriosDataProvider {

    public LCCuriosDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(LCApi.MODID,output,registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries) {

        this.createSlot(LCCuriosHelper.WALLET_SLOT)
                .addCosmetic(true)
                .order(-1)
                .icon(LCApi.id("container/slot/wallet"))
                .addEntity(EntityType.PLAYER);

    }

}
