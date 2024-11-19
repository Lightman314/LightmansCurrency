package io.github.lightman314.lightmanscurrency.datagen.integration;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
                .addCosmetic(true)
                .icon(VersionUtil.lcResource("item/empty_wallet_slot"));

        this.createSlot("charm")
                .operation(AttributeModifier.Operation.ADD_VALUE)
                .size(1);

        this.createEntities("lightmanscurrency_default_slots")
                .addEntities(EntityType.PLAYER)
                .addSlots("wallet","charm");

    }

}
