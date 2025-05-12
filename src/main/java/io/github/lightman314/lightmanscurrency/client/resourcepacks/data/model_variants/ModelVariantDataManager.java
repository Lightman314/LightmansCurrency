package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariantDataManager implements PreparableReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final ModelVariantDataManager INSTANCE = new ModelVariantDataManager();

    public static final String DIRECTORY = "lightmanscurrency/model_variants";

    private ModelVariantDataManager() { }

    private final Map<ResourceLocation,ModelVariant> variants = new HashMap<>();
    private final Map<ResourceLocation,List<ResourceLocation>> variantsByTarget = new HashMap<>();

    @Nullable
    public static ModelVariant getVariant(@Nullable ResourceLocation variant) { return variant == null ? null : INSTANCE.variants.get(variant); }
    @Nonnull
    public static List<ResourceLocation> getPotentialVariants(ResourceLocation target) { return INSTANCE.variantsByTarget.getOrDefault(target,ImmutableList.of()); }

    public static void forEach(Consumer<ModelVariant> consumer) { INSTANCE.variants.forEach((id,variant) -> consumer.accept(variant)); }
    public static void forEachWithID(BiConsumer<ResourceLocation,ModelVariant> biConsumer) { INSTANCE.variants.forEach(biConsumer); }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        Map<ResourceLocation,JsonElement> map = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager,DIRECTORY,GSON,map);
        LightmansCurrency.LogDebug("Loading Model Variant Data");
        this.variants.clear();
        Map<ResourceLocation,ModelVariant> temp = new HashMap<>();
        this.variantsByTarget.clear();
        final Map<ResourceLocation,ImmutableList.Builder<ResourceLocation>> builders = new HashMap<>();
        map.forEach((id,json) -> {
            try {
                ModelVariant variant = ModelVariant.parse(GsonHelper.convertToJsonObject(json,"top element"));
                temp.put(id,variant);
            }catch (JsonSyntaxException | IllegalArgumentException | ResourceLocationException e) {
                LightmansCurrency.LogError("Parsing error loading model variant data " + id,e);
            }
        });
        //Run validation check for all
        temp.forEach((id,variant) -> variant.validate(temp,id));
        //Actually store the variants to the local caches
        temp.forEach((id,variant) -> {
            if(!variant.isInvalid())
            {
                this.variants.put(id,variant);
                //Add to list of potential variants only if the ModelVariant is fully valid
                for(ResourceLocation target : variant.getTargets())
                {
                    var builder = builders.getOrDefault(target,ImmutableList.builder());
                    builder.add(id);
                    builders.put(target,builder);
                }
            }
        });
        builders.forEach((id,builder) -> this.variantsByTarget.put(id,builder.build()));
        //Register the texture relevant models to the ModelTextureCache
        return CompletableFuture.completedFuture(null);
    }

    private static void addToList(List<ModelResourceLocation> list, ModelResourceLocation newValue)
    {
        if(list.contains(newValue))
            return;
        list.add(newValue);
    }

}