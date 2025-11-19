package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.TargetSelectorHelper;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.UnbakedVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models.VariantModelBakery;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models.VariantModelLocation;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariantDataManager implements PreparableReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final ModelVariantDataManager INSTANCE = new ModelVariantDataManager();

    public static final String DIRECTORY = "lightmanscurrency/model_variants";

    private Map<ResourceLocation,ModelVariant> variants = new HashMap<>();
    private Map<ResourceLocation,List<ResourceLocation>> variantsByTarget = new HashMap<>();
    private Map<VariantModelLocation,BakedModel> variantModels = new HashMap<>();

    private ModelVariantDataManager() { }

    public static Map<ResourceLocation,CompletableFuture<AtlasSet.StitchResult>> atlasPreparation = null;

    @Nullable
    public static ModelVariant getVariant(@Nullable ResourceLocation variant) {
        return variant == null ? null : INSTANCE.variants.get(variant);
    }
    public static List<ResourceLocation> getPotentialVariants(ResourceLocation target) { return INSTANCE.variantsByTarget.getOrDefault(target,ImmutableList.of()); }

    public static BakedModel getModel(VariantModelLocation modelID)
    {
        BakedModel result = INSTANCE.variantModels.get(modelID);
        if(result == null)
        {
            LightmansCurrency.LogWarning("Unable to find variant model of type " + modelID + "#" + Integer.toHexString(modelID.hashCode()));
            LightmansCurrency.LogInfo(INSTANCE.variantModels.size() + " variant models are present at this time.");
            return Minecraft.getInstance().getModelManager().getMissingModel();
        }
        return result;
    }

    public static void forEach(Consumer<ModelVariant> consumer) { INSTANCE.variants.forEach((id,variant) -> consumer.accept(variant)); }
    public static void forEachWithID(BiConsumer<ResourceLocation,ModelVariant> biConsumer) { INSTANCE.variants.forEach(biConsumer); }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        preparationsProfiler.startTick();
        //Clear the data cache for the model so that it'll obey the new data
        CompletableFuture<VariantMaps> variantFuture = loadModelVariants(resourceManager,backgroundExecutor);
        CompletableFuture<Map<ResourceLocation,BlockModel>> blockModelFuture = loadBlockModels(resourceManager,backgroundExecutor);
        CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> blockStateFuture = loadBlockStates(resourceManager,backgroundExecutor);
        //This one should be included
        CompletableFuture<Void> uploadVariants = variantFuture.thenApplyAsync(this::uploadVariants,backgroundExecutor);
        CompletableFuture<VariantModelBakery> modelBakeryFuture = variantFuture.thenCombineAsync(blockModelFuture.thenCombineAsync(blockStateFuture, Pair::of,backgroundExecutor),(variantMaps, modelsAndStates) ->
                new VariantModelBakery(Minecraft.getInstance().getBlockColors(),reloadProfiler,modelsAndStates.getFirst(),modelsAndStates.getSecond(),variantMaps.variantMap)
        );
        //Try to wait for the texture atlases to finish loading
        Map<ResourceLocation,CompletableFuture<AtlasSet.StitchResult>> atlasLoading = Objects.requireNonNull(atlasPreparation,"Vanilla Atlases are not currently loading!");

        //Register the texture relevant models to the ModelTextureCache
        return CompletableFuture.allOf(Stream.concat(Stream.concat(atlasLoading.values().stream(),Stream.of(uploadVariants)),Stream.of(modelBakeryFuture)).toArray(CompletableFuture[]::new))
                .thenApplyAsync(v -> loadModels(reloadProfiler,
                        atlasLoading.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().join())),
                        modelBakeryFuture.join()
                ))
                .thenCompose(state -> state.readyForUpload.thenApply(v -> state))
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(state -> this.apply(state,reloadProfiler),gameExecutor);
    }

    private static CompletableFuture<VariantMaps> loadModelVariants(ResourceManager resourceManager, Executor executor)
    {
        return CompletableFuture.supplyAsync(() -> {
            HashMap<ResourceLocation,JsonElement> result = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager,DIRECTORY,GSON,result);
            return result;
        },executor).thenApply((variantData) ->{
            Map<ResourceLocation, UnbakedVariant> temp = new HashMap<>();
            final Map<ResourceLocation,ImmutableList.Builder<ResourceLocation>> builders = new HashMap<>();
            variantData.forEach((id,json) -> {
                try {
                    UnbakedVariant variant = UnbakedVariant.parse(GsonHelper.convertToJsonObject(json,"top element"));
                    temp.put(id,variant);
                }catch (JsonSyntaxException | IllegalArgumentException | ResourceLocationException e) {
                    LightmansCurrency.LogError("Parsing error loading model variant data " + id,e);
                }
            });
            //Run validation check for all
            temp.forEach((id,variant) -> variant.validate(temp,id));
            //Actually store the variants to the local caches
            Map<ResourceLocation,ModelVariant> results = new HashMap<>();
            Map<ResourceLocation,List<ResourceLocation>> results2 = new HashMap<>();
            TargetSelectorHelper targetSelectorHelper = new TargetSelectorHelper();
            temp.forEach((id,variant) -> {
                if(!variant.isInvalid())
                {
                    results.put(id,variant.bake(id,targetSelectorHelper));
                    //Add to list of potential variants only if the ModelVariant is fully valid
                    for(ResourceLocation target : variant.getTargets())
                    {
                        var builder = builders.getOrDefault(target,ImmutableList.builder());
                        builder.add(id);
                        builders.put(target,builder);
                    }
                }
            });
            builders.forEach((id,builder) -> results2.put(id,builder.build()));
            return new VariantMaps(results,results2);
        });
    }

    //Copied from ModelBakery
    private static CompletableFuture<Map<ResourceLocation,BlockModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> ModelBakery.MODEL_LISTER.listMatchingResources(resourceManager), executor).thenCompose((modelMap) -> {
            List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> list = new ArrayList<>(modelMap.size());
            for(var entry : modelMap.entrySet())
            {
                list.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        Reader reader = entry.getValue().openAsReader();

                        Pair<ResourceLocation,BlockModel> pair;
                        try {
                            pair = Pair.of(entry.getKey(), BlockModel.fromStream(reader));
                        } catch (Throwable var6) {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var5) {
                                    var6.addSuppressed(var5);
                                }
                            }
                            throw var6;
                        }

                        if (reader != null) {
                            reader.close();
                        }

                        return pair;
                    } catch (Exception var7) {
                        return null;
                    }
                }, executor));
            }
            return Util.sequence(list).thenApply((results) -> results.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> loadBlockStates(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> ModelBakery.BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor).thenCompose((resourceMap) -> {
            List<CompletableFuture<Pair<ResourceLocation, List<ModelBakery.LoadedJson>>>> list = new ArrayList<>(resourceMap.size());
            for(var entry : resourceMap.entrySet()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    List<Resource> list1 = entry.getValue();
                    List<ModelBakery.LoadedJson> list2 = new ArrayList<>(list1.size());
                    for(Resource resource : list1)
                    {
                        try {
                            Reader reader = resource.openAsReader();

                            try {
                                JsonObject jsonobject = GsonHelper.parse(reader);
                                list2.add(new ModelBakery.LoadedJson(resource.sourcePackId(), jsonobject));
                            } catch (Throwable var9) {
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (Throwable var8) {
                                        var9.addSuppressed(var8);
                                    }
                                }

                                throw var9;
                            }

                            if (reader != null) {
                                reader.close();
                            }
                        } catch (Exception ignored) { }
                    }

                    return Pair.of(entry.getKey(), list2);
                }, executor));
            }

            return Util.sequence(list).thenApply((p_248966_) -> p_248966_.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private Void uploadVariants(VariantMaps variantMaps)
    {

        this.variants = ImmutableMap.copyOf(variantMaps.variantMap);
        this.variantsByTarget = ImmutableMap.copyOf(variantMaps.variantsByTarget);
        return null;
    }

    private void apply(ReloadState state, ProfilerFiller profiler)
    {
        profiler.startTick();
        profiler.push("upload");
        //Perhaps don't upload the atlas so that we don't conflict with vanilla?
        //state.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
        VariantModelBakery modelBakery = state.modelBakery;
        this.variantModels = modelBakery.getBakedTopLevelModels();
        LightmansCurrency.LogInfo("Loaded " + modelBakery.getBakedModelCount() + " variant models for " + this.variantModels.size() + " possible variant states!");
        profiler.pop();
        profiler.endTick();
    }

    private void debugModelResults()
    {
        StringBuilder modelList = new StringBuilder();
        this.variantModels.keySet().forEach((modelID -> modelList.append('\n').append(modelID)));
        LightmansCurrency.LogDebug("Model List:" + modelList);
    }

    private static ReloadState loadModels(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations, VariantModelBakery modelBakery)
    {
        profilerFiller.push("load");
        profilerFiller.popPush("baking");
        Multimap<String,Material> multimap = HashMultimap.create();
        modelBakery.bakeModels((id,material) -> {
            AtlasSet.StitchResult result = atlasPreparations.get(material.atlasLocation());
            TextureAtlasSprite sprite = result.getSprite(material.texture());
            if(sprite != null)
                return sprite;
            else
            {
                multimap.put(id,material);
                return result.missing();
            }
        });
        multimap.asMap().forEach((id,materials) -> LightmansCurrency.LogWarning(String.format("Missing textures in model %s:\n%s",id, materials.stream().sorted(Material.COMPARATOR).map(m -> "    " + m.atlasLocation() + ":" + m.texture()).collect(Collectors.joining("\n")))));
        profilerFiller.popPush("dispatch");
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(atlasPreparations.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(CompletableFuture[]::new));
        profilerFiller.pop();
        profilerFiller.endTick();
        return new ReloadState(modelBakery,atlasPreparations,completableFuture);
    }

    private record VariantMaps(Map<ResourceLocation,ModelVariant> variantMap, Map<ResourceLocation,List<ResourceLocation>> variantsByTarget) {}

    record ReloadState(VariantModelBakery modelBakery,Map<ResourceLocation,AtlasSet.StitchResult> atlasPreparations, CompletableFuture<Void> readyForUpload) {}

}