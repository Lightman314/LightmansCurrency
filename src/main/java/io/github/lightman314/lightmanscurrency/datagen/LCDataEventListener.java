package io.github.lightman314.lightmanscurrency.datagen;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.datagen.client.*;
import io.github.lightman314.lightmanscurrency.datagen.client.language.EnglishProvider;
import io.github.lightman314.lightmanscurrency.datagen.client.resourcepacks.LCCloserItemPositionProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.advancements.LCAdvancementProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.*;
import io.github.lightman314.lightmanscurrency.datagen.common.enchantments.LCEnchantmentProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LCLootTableProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.tags.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class LCDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupHolder = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        //Recipes
        generator.addProvider(event.includeServer(), new LCRecipeProvider(output,lookupHolder));
        //Loot Tables
        generator.addProvider(event.includeServer(), LCLootTableProvider.create(output,lookupHolder));
        //Advancements
        generator.addProvider(event.includeServer(), new LCAdvancementProvider(output, lookupHolder, existingFileHelper));

        //Enchantments
        generator.addProvider(event.includeServer(), new LCEnchantmentProvider(output,lookupHolder));

        //Tags
        LCBlockTagProvider blockTagProvider = new LCBlockTagProvider(output, lookupHolder, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new LCItemTagProvider(output, lookupHolder, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new LCPoiTagProvider(output, lookupHolder, existingFileHelper));
        generator.addProvider(event.includeClient(), new LCEnchantmentTagProvider(output, lookupHolder, existingFileHelper));

        //Block States
        generator.addProvider(event.includeClient(), new LCBlockStateProvider(output, existingFileHelper));

        //Item Positions for Item Traders
        generator.addProvider(event.includeClient(), new LCItemPositionProvider(output));
        generator.addProvider(event.includeClient(), new LCCloserItemPositionProvider(output));

        //Language
        generator.addProvider(event.includeClient(), new EnglishProvider(output));

    }

}
