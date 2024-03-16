package io.github.lightman314.lightmanscurrency.datagen.common.advancements;

import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.concurrent.CompletableFuture;

public class LCAdvancementProvider extends ForgeAdvancementProvider {

    public LCAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, Lists.newArrayList(new LCCurrencyAdvancements()));
    }
}
