package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data;

import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.util.WildcardTargetSelector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Function;

public final class TargetSelectorHelper {

    private final Map<SelectorKey,List<ResourceLocation>> ITEM_RESULTS = new HashMap<>();
    private final Map<SelectorKey,List<ResourceLocation>> BLOCK_RESULTS = new HashMap<>();
    public TargetSelectorHelper() { }

    public void lookupItems(List<String> targetSelectors, List<ResourceLocation> targets, int models)
    {
        lookup(targetSelectors,targets,models,ITEM_RESULTS, BuiltInRegistries.ITEM, i -> {
            IVariantItem item = VariantProvider.getVariantItem(i);
            return item == null ? -1 : item.requiredModels();
        });
    }

    public void lookupBlocks(List<String> targetSelectors, List<ResourceLocation> targets, int models)
    {
        lookup(targetSelectors,targets,models,BLOCK_RESULTS,BuiltInRegistries.BLOCK,b -> {
            IVariantBlock block = VariantProvider.getVariantBlock(b);
            return block == null ? -1 : block.requiredModels();
        });
    }

    private static <T> void lookup(List<String> targetSelectors, List<ResourceLocation> targets, int models, Map<SelectorKey,List<ResourceLocation>> resultCache, Registry<T> registry, Function<T,Integer> modelRequirementLookup)
    {
        if(targetSelectors.isEmpty())
            return;
        List<SelectorKey> selectors = new ArrayList<>(targetSelectors.stream().map(WildcardTargetSelector::parse).map(s -> new SelectorKey(s,models)).toList());
        for(SelectorKey s : new ArrayList<>(selectors))
        {
            if(resultCache.containsKey(s))
            {
                addTargets(targets,resultCache.get(s));
                selectors.remove(s);
            }
        }
        //Check if all the selectors were pre-cached
        if(selectors.isEmpty())
            return;
        //Otherwise lookup entries and cache results
        Map<SelectorKey,List<ResourceLocation>> tempCache = new HashMap<>();
        for(SelectorKey s : selectors)
            tempCache.put(s,new ArrayList<>());
        for(T entry : registry)
        {
            int requiredModels = modelRequirementLookup.apply(entry);
            if(requiredModels >= 0 && requiredModels == models)
            {
                ResourceLocation itemID = registry.getKey(entry);
                if(testSelectors(selectors,itemID,tempCache))
                    addTarget(targets,itemID);
            }
        }
        //Now update the cache with the new info
        resultCache.putAll(tempCache);
    }

    private static boolean testSelectors(List<SelectorKey> targetSelectors, ResourceLocation id, Map<SelectorKey,List<ResourceLocation>> tempCache)
    {
        String idString = id.toString();
        boolean success = false;
        for(SelectorKey s : targetSelectors)
        {
            if(s.selector.matches(idString))
            {
                //Cache the results
                tempCache.get(s).add(id);
                //Acknowledge the pass, but keep checking the other selectors so that the cache is accurate
                success = true;
            }
        }
        return success;
    }

    private static void addTargets(List<ResourceLocation> targets, List<ResourceLocation> newTargets)
    {
        for(ResourceLocation newTarget : newTargets)
            addTarget(targets,newTarget);
    }

    private static void addTarget(List<ResourceLocation> targets, ResourceLocation target)
    {
        if(targets.contains(target))
            return;
        targets.add(target);
    }

    private record SelectorKey(WildcardTargetSelector selector,int blockModels) {}

}