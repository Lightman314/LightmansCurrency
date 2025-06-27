package io.github.lightman314.lightmanscurrency.datagen;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.datagen.client.*;
import io.github.lightman314.lightmanscurrency.datagen.client.language.EnglishProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.resourcepacks.LCCloserItemPositionProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.LCDatapackProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.advancements.LCAdvancementProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.*;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LCLootModifierProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LCLootTableProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.tags.*;
import io.github.lightman314.lightmanscurrency.datagen.integration.*;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class LCDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupHolder = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        //Recipes
        generator.addProvider(event.includeServer(), new LCRecipeProvider(output));
        //Loot Tables
        generator.addProvider(event.includeServer(), LCLootTableProvider.create(output));
        //Global Loot Modifiers
        generator.addProvider(event.includeServer(), new LCLootModifierProvider(output));
        //Advancements
        generator.addProvider(event.includeServer(), new LCAdvancementProvider(output, lookupHolder, existingFileHelper));

        //Datapack
        generator.addProvider(event.includeServer(),new LCDatapackProvider(output,lookupHolder));

        //Tags
        LCBlockTagProvider blockTagProvider = new LCBlockTagProvider(output, lookupHolder, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new LCItemTagProvider(output, lookupHolder, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new LCPoiTagProvider(output, lookupHolder, existingFileHelper));

        //Block States
        generator.addProvider(event.includeClient(), new LCBlockStateProvider(output, existingFileHelper));

        //Item Positions for Item Traders
        generator.addProvider(event.includeClient(), new LCItemPositionProvider(output));
        generator.addProvider(event.includeClient(), new LCCloserItemPositionProvider(output));

        //Model Variants
        generator.addProvider(event.includeClient(), new LCModelVariantProvider(output));

        //Language
        generator.addProvider(event.includeClient(), new EnglishProvider(output));

        //Integration
        if(LCCurios.isLoaded())
            generator.addProvider(event.includeServer(), new LCCuriosProvider(output,existingFileHelper,lookupHolder));
        if(ModList.get().isLoaded("computercraft"))
            generator.addProvider(event.includeServer(), new LCComputerCraftProvider(output));

    }

}
