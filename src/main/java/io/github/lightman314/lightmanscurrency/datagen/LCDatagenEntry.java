package io.github.lightman314.lightmanscurrency.datagen;

import io.github.lightman314.lightmanscurrency.datagen.client.LCLanguageProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.LCModelProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.LCRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public final class LCDatagenEntry {

    private LCDatagenEntry() {}


    @SubscribeEvent
    private static void onDatagen(GatherDataEvent.Client event)
    {

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registryFuture = event.getLookupProvider();

        //Models
        event.addProvider(new LCModelProvider(output));
        //Translations
        event.addProvider(new LCLanguageProvider(output));

        //Recipes
        event.addProvider(LCRecipeProvider.create(output,registryFuture));

    }

}
