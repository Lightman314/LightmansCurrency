package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.datagen.common.structures.LCProcessorListProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LCDatapackProvider extends DatapackBuiltinEntriesProvider {

    private static final List<Consumer<RegistrySetBuilder>> addons = new ArrayList<>();
    public static void registerAddon(Consumer<RegistrySetBuilder> addon) { addons.add(addon);}

    public LCDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, applyAddons(new RegistrySetBuilder())
                .add(Registries.PROCESSOR_LIST, LCProcessorListProvider::bootstrap),
                Set.of(LightmansCurrency.MODID));
    }

    private static RegistrySetBuilder applyAddons(RegistrySetBuilder builder) {
        for(Consumer<RegistrySetBuilder> addon : addons)
            addon.accept(builder);
        return builder;
    }



}
