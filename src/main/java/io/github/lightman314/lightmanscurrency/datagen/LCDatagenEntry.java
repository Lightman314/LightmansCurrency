package io.github.lightman314.lightmanscurrency.datagen;

import io.github.lightman314.lightmanscurrency.datagen.client.LCCloserItemPositionProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.LCEnglishProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.LCItemPositionProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.LCModelProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.LCEnchantmentProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.LCVillagerTradeProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.curios.LCCuriosDataProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LCLootTableProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.tags.*;
import io.github.lightman314.lightmanscurrency.datagen.common.LCRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public final class LCDatagenEntry {

    private LCDatagenEntry() {}

    @SubscribeEvent
    private static void onDatagen(GatherDataEvent.Client event)
    {

        //Datapack Registries before getting the registry future
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.ENCHANTMENT,LCEnchantmentProvider::bootstrap)
                .add(Registries.VILLAGER_TRADE,LCVillagerTradeProvider::bootstrap)
        );

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        PackOutput closerItemsOutput = generator.getPackOutput("closer_items");
        CompletableFuture<HolderLookup.Provider> registryFuture = event.getLookupProvider();

        //Models
        event.addProvider(new LCModelProvider(output));
        //Translations
        event.addProvider(new LCEnglishProvider(output));
        //Item Position Data
        event.addProvider(new LCItemPositionProvider(output));
        event.addProvider(new LCCloserItemPositionProvider(closerItemsOutput));

        //Recipes
        event.addProvider(LCRecipeProvider.create(output,registryFuture));
        //Loot
        event.addProvider(LCLootTableProvider.create(output,registryFuture));
        //Tags
        event.addProvider(new LCItemTagProvider(output,registryFuture));
        event.addProvider(new LCBlockTagProvider(output,registryFuture));
        event.addProvider(new LCTraderTypeTagProvider(output,registryFuture));
        event.addProvider(new LCTraderNodeTypeTagProvider(output,registryFuture));
        event.addProvider(new LCEnchantmentTagProvider(output,registryFuture));
        event.addProvider(new LCVillagerTradeTagProvider(output,registryFuture));

        //Modded Data
        if(ModList.get().isLoaded("curios"))
            event.addProvider(new LCCuriosDataProvider(output,registryFuture));

    }

}
