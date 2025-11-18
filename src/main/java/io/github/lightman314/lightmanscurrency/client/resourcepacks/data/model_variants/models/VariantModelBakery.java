package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.client.resources.model.ModelBakery.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantModelBakery {


    private static final Map<String,String> BUILTIN_MODELS = Map.of("missing", MISSING_MODEL_MESH);
    private final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private final Map<ResourceLocation, BlockModel> modelResources;
    private final Set<ResourceLocation> loadingStack = new HashSet<>();
    private final Map<ResourceLocation, UnbakedModel> unbakedCache = new HashMap<>();
    private final Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> blockStates;
    private final Map<ResourceLocation,List<ResourceLocation>> defaultModels = new HashMap<>();
    private final Map<ModelCacheKey,UnbakedModel> topLevelModels = new HashMap<>();
    private final Map<ModelCacheKey,BakedModel> bakedTopLevelModels = new HashMap<>();
    private final Multimap<ModelCacheKey,VariantModelLocation> keyToVariantLocation = HashMultimap.create();
    private final UnbakedModel missingModel;

    public int getBakedModelCount() { return this.bakedTopLevelModels.size(); }

    public final Map<VariantModelLocation,BakedModel> getBakedTopLevelModels() {
        Map<VariantModelLocation,BakedModel> result = new HashMap<>();
        Map<ModelCacheKey,Collection<VariantModelLocation>> keyCollectionMap = this.keyToVariantLocation.asMap();
        this.bakedTopLevelModels.forEach((key,model) -> {
            for(VariantModelLocation modelLocation : keyCollectionMap.getOrDefault(key,new ArrayList<>()))
                result.put(modelLocation,model);
        });
        return ImmutableMap.copyOf(result);
    }

    public VariantModelBakery(BlockColors blockColors,
                              ProfilerFiller profilerFiller,
                              Map<ResourceLocation,BlockModel> modelResources,
                              Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> blockStates,
                              Map<ResourceLocation,ModelVariant> variants)
    {
        this.modelResources = new HashMap<>(modelResources);
        this.blockStates = blockStates;

        profilerFiller.push("missing_model");
        try {
            this.missingModel = this.loadBlockModel(MISSING_MODEL_LOCATION);
        } catch (Exception e) { throw new RuntimeException("Error loading missing model!",e); }

        profilerFiller.popPush("variant models");
        variants.forEach((id,variant) -> {
            //Target-based generation
            for(ResourceLocation target : variant.getTargets())
            {
                ModelProperties properties = ModelProperties.collectProperties(variant,target);
                if(properties != null)
                {
                    int max = properties.requiredModels();
                    int rotatable = properties.modelsFromBlockState();
                    boolean isRotatable = properties.isRotatable();
                    //Load the item model
                    if(variant.getItemModel() != null)
                        this.loadModel(variant,id,variant.getItemModel(),-1,properties.target(),false);
                    else
                        this.loadModel(variant,id,properties.target().withPrefix("item/"),-1,properties.target(),false);
                    if(variant.hasBlockModels())
                    {
                        //Load each model
                        for(int i = 0; i < max; ++i)
                            this.loadModel(variant,id,variant.getBlockModels().get(i),i,properties.target(),isRotatable && i < rotatable);
                    }
                    else if(properties.requiredModels > 0)
                    {
                        List<ResourceLocation> models = this.getDefaultModels(properties);
                        max = Math.min(max,models.size());
                        //Load each model
                        for(int i = 0; i < max; ++i)
                            this.loadModel(variant,id,models.get(i),i,properties.target(),isRotatable && i < rotatable);
                    }
                }
                else
                {
                    if(variant.isItemVariant())
                        LightmansCurrency.LogWarning("Unable to set up variant '" + id + "' for target " + target + " as it is not a valid variant item.");
                    else
                        LightmansCurrency.LogWarning("Unable to set up variant '" + id + "' for target " + target + " as it is not a valid variant block.");
                }
            }
        });
        profilerFiller.pop();

        profilerFiller.popPush("resolve_parents");
        this.topLevelModels.values().forEach(model -> model.resolveParents(this::getModel));
        profilerFiller.pop();

        LightmansCurrency.LogDebug("Loaded " + this.topLevelModels.size() + " models for variant use");

    }

    public void bakeModels(BiFunction<String,Material,TextureAtlasSprite> textureGetter)
    {
        this.topLevelModels.forEach((id,unbakedModel) -> {
            BakedModel bakedmodel = null;
            try {
                bakedmodel = new VariantModelBaker(id,textureGetter).bakeUncached(unbakedModel,BlockModelRotation.X0_Y0);
            } catch (Exception e) {
                LightmansCurrency.LogWarning("Unable to bake model: '" + id + "'", e);
            }
            if (bakedmodel != null) {
                this.bakedTopLevelModels.put(id, Objects.requireNonNull(bakedmodel));
                //LightmansCurrency.LogDebug("Successfully baked Variant Model " + id);
            }
            else
                LightmansCurrency.LogWarning("Variant Model " + id + " returned null during the baking process");
        });
    }

    private List<ResourceLocation> getDefaultModels(ModelProperties properties)
    {
        ResourceLocation target = properties.target();
        if(this.defaultModels.containsKey(target))
            return this.defaultModels.get(target);
        List<ResourceLocation> defaultModels = VariantModelHelper.getDefaultModels(properties,this.blockStates,BlockStateModelLoader::predicate);
        this.defaultModels.put(target,defaultModels);
        return defaultModels;
    }

    private UnbakedModel getModel(ResourceLocation model)
    {
        if(this.unbakedCache.containsKey(model))
            return this.unbakedCache.get(model);
        else if(this.loadingStack.contains(model))
            throw new IllegalStateException("Circular reference while loading " + model);
        else
        {
            this.loadingStack.add(model);

            while(!this.loadingStack.isEmpty())
            {
                ResourceLocation resourcelocation = this.loadingStack.iterator().next();

                try {
                    if (!this.unbakedCache.containsKey(resourcelocation)) {
                        UnbakedModel unbakedmodel = this.loadBlockModel(resourcelocation);
                        this.unbakedCache.put(resourcelocation, unbakedmodel);
                        this.loadingStack.addAll(unbakedmodel.getDependencies());
                    }
                } catch (Exception e) {
                    this.unbakedCache.put(resourcelocation,this.missingModel);
                } finally {
                    this.loadingStack.remove(resourcelocation);
                }
            }
            return this.unbakedCache.getOrDefault(model,this.missingModel);
        }
    }

    private BlockModel loadBlockModel(ResourceLocation location) throws IOException {
        String s = location.getPath();
        if ("builtin/generated".equals(s)) {
            return GENERATION_MARKER;
        } else if ("builtin/entity".equals(s)) {
            return BLOCK_ENTITY_MARKER;
        } else if (s.startsWith("builtin/")) {
            String s1 = s.substring("builtin/".length());
            String s2 = BUILTIN_MODELS.get(s1);
            if (s2 == null) {
                throw new FileNotFoundException(location.toString());
            } else {
                Reader reader = new StringReader(s2);
                BlockModel blockmodel1 = BlockModel.fromStream(reader);
                blockmodel1.name = location.toString();
                return blockmodel1;
            }
        } else {
            ResourceLocation resourcelocation = MODEL_LISTER.idToFile(location);
            BlockModel blockmodel = this.modelResources.get(resourcelocation);
            if (blockmodel == null) {
                throw new FileNotFoundException(resourcelocation.toString());
            } else {
                blockmodel.name = location.toString();
                return blockmodel;
            }
        }
    }

    private void loadModel(ModelVariant variant, ResourceLocation variantID, ResourceLocation model, int modelIndex, ResourceLocation target, boolean rotatable)
    {
        ResourceLocation cacheVariant = null;
        if(variant.hasTextureOverrides())
        {
            //Cache the variant as it's relevant to the model key
            cacheVariant = variantID;
            ResourceLocation newModelID = VersionUtil.modResource(variantID.getNamespace(), "lc_model_variants/" + variantID.getPath() + "/" + modelIndex);
            //Create new BlockModel for the given target
            VariantModelHelper.createCustomBlockModel(model,this.modelResources,variant.getTextureOverrides(),newModelID);
            model = newModelID;
        }

        if(rotatable)
        {
            for(int yRot = 0; yRot < 360; yRot += 90)
            {
                ModelCacheKey key = new ModelCacheKey(model,cacheVariant,yRot);
                VariantModelLocation modelLocation = VariantModelLocation.rotatable(variantID,target,modelIndex,yRot);
                UnbakedModel temp = this.topLevelModels.get(key);
                if(temp == null)
                {
                    temp = new MultiVariant(Lists.newArrayList(new Variant(model,BlockModelRotation.by(0,yRot).getRotation(),false,1)));
                    this.topLevelModels.put(key,temp);
                }
                this.keyToVariantLocation.put(key,modelLocation);
            }
        }
        else
        {
            ModelCacheKey key = new ModelCacheKey(model,cacheVariant,0);
            VariantModelLocation modelLocation = VariantModelLocation.basic(variantID,target,modelIndex);
            UnbakedModel temp = this.topLevelModels.get(key);
            if(temp == null)
            {
                ResourceLocation fileID = MODEL_LISTER.idToFile(model);
                temp = this.getModel(model);
                this.topLevelModels.put(key,temp);
            }
            this.keyToVariantLocation.put(key,modelLocation);
        }
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    private class VariantModelBaker implements ModelBaker
    {

        private final Function<Material,TextureAtlasSprite> textureGetter;
        VariantModelBaker(ModelCacheKey modelLocation, BiFunction<String,Material,TextureAtlasSprite> textureGetter) {
            this.textureGetter = material -> textureGetter.apply(modelLocation.toString(),material);
        }

        @Override
        public UnbakedModel getModel(ResourceLocation location) { return VariantModelBakery.this.getModel(location); }

        @Nullable
        @Override
        public BakedModel bake(ResourceLocation location, ModelState transform) { return this.bake(location,transform,this.textureGetter); }

        @Override
        @Nullable
        public UnbakedModel getTopLevelModel(ModelResourceLocation location) { return null; }

        @Override
        @Nullable
        public BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            UnbakedModel model = this.getModel(location);
            if(model != null)
                return this.bakeUncached(model,state,sprites);
            return null;
        }

        @Nullable
        public BakedModel bakeUncached(UnbakedModel model, ModelState state) {
            return this.bakeUncached(model,state,this.textureGetter);
        }
        @Override
        @Nullable
        public BakedModel bakeUncached(UnbakedModel model, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            if (model instanceof BlockModel blockmodel && blockmodel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                return ITEM_MODEL_GENERATOR
                        .generateBlockModel(sprites, blockmodel)
                        .bake(this, blockmodel, sprites, state, false);
            }

            return model.bake(this, sprites, state);
        }
        @Override
        public Function<Material, TextureAtlasSprite> getModelTextureGetter() { return this.textureGetter; }

    }

    private record ModelCacheKey(ResourceLocation model, @Nullable ResourceLocation variant, int yRot) {}

    public record ModelProperties(int requiredModels, int modelsFromBlockState, boolean isRotatable, ResourceLocation target, Function<Integer,ResourceLocation> defaultModelSource, Function<BlockState,Integer> stateIndexLookup)
    {
        @Nullable
        private static ModelProperties collectProperties(ModelVariant variant, ResourceLocation target)
        {
            if(variant.isItemVariant())
            {
                IVariantItem item = VariantProvider.getVariantItem(BuiltInRegistries.ITEM.get(target));
                if(item != null)
                    return new ModelProperties(item.requiredModels(),0,false,item.getItemID(),item::getDefaultModel, s -> -1);
            }
            else
            {
                IVariantBlock block = VariantProvider.getVariantBlock(BuiltInRegistries.BLOCK.get(target));
                if(block != null)
                    return new ModelProperties(block.requiredModels(),block.modelsFromBlockState(),block.isRotatable(),block.getBlockID(),block::getCustomDefaultModel,block::getModelIndex);
            }
            return null;
        }
    }

}
