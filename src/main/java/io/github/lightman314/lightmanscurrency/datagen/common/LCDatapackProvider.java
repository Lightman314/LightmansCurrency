package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.datagen.common.enchantments.LCEnchantmentProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.structures.LCProcessorListProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCDatapackProvider extends DatapackBuiltinEntriesProvider {

    public LCDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, new RegistrySetBuilder()
                .add(Registries.ENCHANTMENT, LCEnchantmentProvider::bootstrap)
                .add(Registries.PROCESSOR_LIST, LCProcessorListProvider::bootstrap)
                ,Set.of(LightmansCurrency.MODID));
    }

}
