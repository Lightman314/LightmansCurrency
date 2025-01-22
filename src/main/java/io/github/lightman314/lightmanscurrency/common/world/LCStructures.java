package io.github.lightman314.lightmanscurrency.common.world;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.mixin.TemplatePoolAccess;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Code mostly copied from Immersive Engineering's `common.world.Villages` class
 * as it was the best example I could find of adding new villager houses to the pool
 */
@EventBusSubscriber
public class LCStructures {

    private static List<String> villageBiomes() { return ImmutableList.of("plains","snowy","savanna","desert","taiga"); }
    private static List<String> houseTypes() { return ImmutableList.of("banker","shop"); }

    private static final Map<ResourceLocation,Function<RegistryAccess,Holder<StructureProcessorList>>> structureProcessors;

    public static final ResourceKey<StructureProcessorList> PROCESSOR_DESERT_BANKER_ARCHAEOLOGY = processorList("desert_banker_archaeology");
    public static final ResourceKey<StructureProcessorList> PROCESSOR_PLAINS_SHOP_ARCHAEOLOGY = processorList("plains_shop_archaeology");
    public static final ResourceKey<StructureProcessorList> PROCESSOR_TAIGA_SHOP_ARCHAEOLOGY = processorList("taiga_shop_archaeology");
    public static final ResourceKey<StructureProcessorList> PROCESSOR_DESERT_SHOP_ARCHAEOLOGY = processorList("desert_shop_archaeology");
    public static final ResourceKey<StructureProcessorList> PROCESSOR_IDAS_TAIGA_LARGE_BANK = processorList("idas_taiga_large_bank");

    public static final ResourceKey<StructureProcessorList> PROCESSOR_ANCIENT_RUINS = processorList("ancient_ruins");

    public static final ResourceKey<StructureSet> STRUCTURE_NETHER_RUINS = structureSet("nether_ruins");


    static {
        structureProcessors = new HashMap<>();
        //Villages
        structureProcessors.put(VersionUtil.lcResource("village/houses/desert_banker"),easyGetter(PROCESSOR_DESERT_BANKER_ARCHAEOLOGY));
        structureProcessors.put(VersionUtil.lcResource("village/houses/plains_shop"),easyGetter(PROCESSOR_PLAINS_SHOP_ARCHAEOLOGY));
        structureProcessors.put(VersionUtil.lcResource("village/houses/taiga_shop"),easyGetter(PROCESSOR_TAIGA_SHOP_ARCHAEOLOGY));
        structureProcessors.put(VersionUtil.lcResource("village/houses/desert_shop"),easyGetter(PROCESSOR_DESERT_SHOP_ARCHAEOLOGY));
        structureProcessors.put(VersionUtil.lcResource("village/houses/idas_plains_large_bank"),easyGetter(PROCESSOR_DESERT_SHOP_ARCHAEOLOGY));
        //Ancient City
        structureProcessors.put(VersionUtil.lcResource("ancient_city/ancient_ruins"),easyGetter(PROCESSOR_ANCIENT_RUINS));
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event)
    {
        if(event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            return;
        RegistryAccess registryAccess = event.getRegistryAccess();
        boolean spawnIDAS = false;//ModList.get().isLoaded("idas") && LCConfig.COMMON.structureIDAS.get();
        if(LCConfig.COMMON.structureVillageHouses.get())
        {
            for(String biome : villageBiomes())
            {
                ResourceLocation housePool = VersionUtil.vanillaResource("village/" + biome + "/houses");
                for(String type : houseTypes())
                {
                    addToPool(
                            housePool,
                            VersionUtil.lcResource("village/houses/" + biome + "_" + type),
                            registryAccess,
                            3
                    );
                }
                //debugPool(housePool,registryAccess);
            }
            //Add Integrated Dungeons and Structures village structures
            if(spawnIDAS)
            {
                addToPool(VersionUtil.vanillaResource("village/taiga/houses"),
                        VersionUtil.lcResource("village/houses/idas_taiga_large_bank"),
                        registryAccess,
                        1
                );
                addToPool(VersionUtil.vanillaResource("village/plains/houses"),
                        VersionUtil.lcResource("village/houses/idas_plains_gundam"),
                        registryAccess,
                        1
                );
            }
            LightmansCurrency.LogInfo("Added custom Lightman's Currency village structures to their respective pools");
        }
        if(LCConfig.COMMON.structureAncientCity.get())
        {
            addToPool(VersionUtil.vanillaResource("ancient_city/structures"),
                    VersionUtil.lcResource("ancient_city/ancient_ruins"),
                    registryAccess,
                    2);
        }


    }

    private static void addToPool(@Nonnull ResourceLocation poolID, @Nonnull ResourceLocation toAdd, RegistryAccess registryAccess, int weight)
    {
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        StructureTemplatePool pool = registry.get(poolID);
        if(pool instanceof TemplatePoolAccess access)
        {
            if(!(access.getRawTemplates() instanceof ArrayList))
                access.setRawTemplates(new ArrayList<>(access.getRawTemplates()));
            Holder<StructureProcessorList> processor = lookupProcessorForStructure(toAdd,registryAccess);
            SinglePoolElement newElement = (processor == null ? SinglePoolElement.single(toAdd.toString()) : SinglePoolElement.single(toAdd.toString(),processor)).apply(Projection.RIGID);
            access.getRawTemplates().add(Pair.of(newElement,weight));
            int oldCount = access.getTemplates().size();
            for(int i = 0; i < weight; ++i)
                access.getTemplates().add(newElement);
            LightmansCurrency.LogDebug("Added " + toAdd + " to the " + poolID + " structure pool with a weight of " + weight + " (previously had " + oldCount + " total weight)");
        }
        else if(pool == null)
            LightmansCurrency.LogWarning("Could not find StructureTemplatePool " + poolID);
        else
            LightmansCurrency.LogWarning("StructureTemplatePool Accessor Mixin was not set up correctly!\nWill be unable to add custom villager houses to the village pools.");
    }

    @Nullable
    private static Holder<StructureProcessorList> lookupProcessorForStructure(@Nonnull ResourceLocation structure, RegistryAccess registryAccess)
    {
        Function<RegistryAccess,Holder<StructureProcessorList>> getter = structureProcessors.getOrDefault(structure,null);
        return getter == null ? null : getter.apply(registryAccess);
    }

    private static Function<RegistryAccess,Holder<StructureProcessorList>> easyGetter(@Nonnull String processorID) { return easyGetter(VersionUtil.lcResource(processorID)); }
    private static Function<RegistryAccess,Holder<StructureProcessorList>> easyGetter(@Nonnull ResourceLocation processorID) { return easyGetter(ResourceKey.create(Registries.PROCESSOR_LIST,processorID)); }
    private static Function<RegistryAccess,Holder<StructureProcessorList>> easyGetter(@Nonnull ResourceKey<StructureProcessorList> key)
    {
        return registryAccess -> {
            HolderLookup.RegistryLookup<StructureProcessorList> registry = registryAccess.lookupOrThrow(Registries.PROCESSOR_LIST);
            return registry.get(key).orElse(null);
        };
    }

    private static ResourceKey<StructureProcessorList> processorList(String id) { return ResourceKey.create(Registries.PROCESSOR_LIST,VersionUtil.lcResource(id)); }
    private static ResourceKey<StructureSet> structureSet(String id) { return ResourceKey.create(Registries.STRUCTURE_SET,VersionUtil.lcResource(id)); }

    private static void debugPool(@Nonnull ResourceLocation poolID, @Nonnull RegistryAccess registryAccess)
    {
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        StructureTemplatePool pool = registry.get(poolID);
        if(pool instanceof TemplatePoolAccess access)
        {
            LightmansCurrency.LogDebug("Raw Entries for Pool '" + poolID + "':\n" + DebugUtil.debugList(access.getRawTemplates(),p -> p.getFirst().toString() + ";Weight: " + p.getSecond().toString()));
            LightmansCurrency.LogDebug("Entries for Pool '" + poolID + "':\n" + DebugUtil.debugList(access.getTemplates()));
        }
    }

}
