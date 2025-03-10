package io.github.lightman314.lightmanscurrency.datagen.integration;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class LCCuriosProvider extends CuriosDataProvider {

    public LCCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(LightmansCurrency.MODID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {

        this.createSlot("wallet")
                .order(0) //Give the wallet a priority of 0 so that it appears above most default slots
                .addCosmetic(true)
                .icon(VersionUtil.lcResource("item/empty_wallet_slot"));

        this.createSlot("charm")
                .operation("ADD")
                .size(1);

        this.createEntities("lightmanscurrency_default_slots")
                .addPlayer()
                .addSlots("wallet","charm");

    }

}
