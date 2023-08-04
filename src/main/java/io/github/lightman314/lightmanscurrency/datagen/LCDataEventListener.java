package io.github.lightman314.lightmanscurrency.datagen;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.datagen.client.LCBlockStateProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.LCRecipeProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.tags.LCBlockTagProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.tags.LCItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class LCDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if(event.includeServer())
        {
            //Recipes
            generator.addProvider(new LCRecipeProvider(generator));

            //Tags
            LCBlockTagProvider blockTagProvider = new LCBlockTagProvider(generator, existingFileHelper);
            generator.addProvider(blockTagProvider);
            generator.addProvider(new LCItemTagProvider(generator, blockTagProvider, existingFileHelper));
            //1.18 does not have POI Type Tags for Profession Purposes.
        }
        if(event.includeClient())
        {
            //Block States
            generator.addProvider(new LCBlockStateProvider(generator, existingFileHelper));
        }
    }

}