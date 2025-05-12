package io.github.lightman314.lightmanscurrency.mixin.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.model.util.VariantModelHelper;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadTopLevel(ModelResourceLocation modelID);
    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation modelID);
    @Shadow
    protected abstract void loadModel(ResourceLocation modelID) throws Exception;

    @Inject(at = @At("RETURN"),method = "<init>")
    private void init(BlockColors blockColors, ProfilerFiller profiler, Map<ResourceLocation, BlockModel> blockModels, Map<ResourceLocation,List<ModelBakery.LoadedJson>> ignoreMe, CallbackInfo ci)
    {
        profiler.push("lightmans_curency_model_variants");
        Map<ResourceLocation,List<ModelBakery.LoadedJson>> editableMap = new HashMap<>(this.getBlockStateResources());
        Map<ResourceLocation,BlockModel> modelData = new HashMap<>(this.getModelResources());
        Map<ResourceLocation, StateDefinition<Block, BlockState>> staticDefinitionsBackup = getStaticDefinitions();
        Map<ResourceLocation,StateDefinition<Block,BlockState>> editableStaticDefinitions = new HashMap<>(staticDefinitionsBackup);
        ModelVariantDataManager.forEachWithID((id,variant) -> {
            if(variant.hasTextureOverrides())
            {
                //Create custom models
                if(variant.hasModels() && variant.getItem() != null)
                {
                    Function<String,ResourceLocation> idGenerator = VariantModelHelper.createIDGenerator(id,null);
                    List<ResourceLocation> originalModels = variant.getModels();
                    List<ResourceLocation> newModels = VariantModelHelper.createCustomBlockModel(
                            originalModels,
                            modelData,
                            variant.getTextureOverrides(),
                            idGenerator);
                    variant.overrideModels(newModels);
                    //Item Model
                    ResourceLocation itemModel = variant.getItem();
                    ResourceLocation newModel = idGenerator.apply("item");
                    VariantModelHelper.createCustomBlockModel(itemModel,modelData,variant.getTextureOverrides(),newModel);
                    variant.overrideItemModel(newModel);
                }
                else
                {
                    final ResourceLocation singleTargetID = VersionUtil.lcResource("null");
                    Map<ResourceLocation,List<ResourceLocation>> targetedBlockModels = new HashMap<>();
                    Map<ResourceLocation,ResourceLocation> targetedItemModels = new HashMap<>();
                    boolean singleTarget = variant.getTargets().size() <= 1;
                    //Look up default models for each target
                    for(ResourceLocation target : variant.getTargets())
                    {
                        ResourceLocation mapID = singleTarget ? singleTargetID : target;
                        Block b = ForgeRegistries.BLOCKS.getValue(target);
                        if(b instanceof IVariantBlock block)
                        {
                            Function<String,ResourceLocation> idGenerator = VariantModelHelper.createIDGenerator(id, singleTarget ? null : target);
                            List<ResourceLocation> originalModels = VariantModelHelper.getDefaultModels(b,block,editableMap,ModelBakeryMixin::runPredicate);
                            List<ResourceLocation> newModels = VariantModelHelper.createCustomBlockModel(
                                    originalModels,
                                    modelData,
                                    variant.getTextureOverrides(),
                                    idGenerator
                            );
                            for(int i = block.modelsRequiringRotation(); i < block.requiredModels(); ++i)
                            {
                                //Create texture model for bonus models
                                ResourceLocation defaultModel = block.getCustomDefaultModel(i);
                                ResourceLocation newModelID = idGenerator.apply(String.valueOf(i));
                                VariantModelHelper.createCustomBlockModel(defaultModel,modelData,variant.getTextureOverrides(),newModelID);
                                newModels.add(newModelID);
                            }
                            targetedBlockModels.put(mapID,newModels);
                            //Item Model
                            ResourceLocation itemID = block.getItemID().withPrefix("item/");
                            ResourceLocation newModelID = idGenerator.apply("item");
                            VariantModelHelper.createCustomBlockModel(itemID,modelData,variant.getTextureOverrides(),newModelID);
                            targetedItemModels.put(mapID,newModelID);
                        }
                    }
                    if(singleTarget)
                    {
                        variant.overrideModels(targetedBlockModels.get(singleTargetID));
                        variant.overrideItemModel(targetedItemModels.get(singleTargetID));
                    }
                    else
                    {
                        variant.defineTargetBasedModels(targetedBlockModels);
                        variant.defineTargetBasedItemModel(targetedItemModels);
                        LightmansCurrency.LogDebug("Targeted Texture Override Models for " + id + ":\nItem Models: " + DebugUtil.debugMap(targetedItemModels) + "\nBlock Models: " + DebugUtil.debugMap(targetedBlockModels,Object::toString,DebugUtil::debugList));
                    }
                }
            }
        });
        //Create custom block state json files for our custom models
        List<Pair<ResourceLocation,Boolean>> generatedStates = new ArrayList<>();
        List<ResourceLocation> loadedItemModels = new ArrayList<>();
        ModelVariantDataManager.forEach(variant -> {
            for(ResourceLocation target : variant.getTargets())
            {
                Block b = ForgeRegistries.BLOCKS.getValue(target);
                if(b instanceof IVariantBlock block)
                {
                    int index = 0;
                    for(ResourceLocation model : variant.getModels(block))
                    {
                        ResourceLocation fileID = ModelBakery.BLOCKSTATE_LISTER.idToFile(model);
                        if(!editableMap.containsKey(fileID))
                        {
                            if(index >= block.modelsRequiringRotation())
                                return;
                            boolean rotatable = block instanceof IRotatableBlock rb;
                            JsonElement json = VariantModelHelper.generateBlockStateFile(model,rotatable);
                            editableMap.put(fileID, ImmutableList.of(new ModelBakery.LoadedJson("generated/lightmans_currency_variants",json)));
                            generatedStates.add(Pair.of(model,rotatable));
                            //1.20.1 only
                            //Define state in the static definition
                            StateDefinition<Block,BlockState> definition = rotatable ? VariantModelHelper.ROTATABLE_FAKE_DEFINITION : VariantModelHelper.NORMAL_FAKE_DEFINITION;
                            editableStaticDefinitions.put(model,definition);
                            LightmansCurrency.LogDebug("Generated fake block state for " + model);
                        }
                        index++;
                    }
                }
            }
        });
        //Define updated values in the parent
        this.setModelResources(ImmutableMap.copyOf(modelData));
        this.setBlockStateResources(ImmutableMap.copyOf(editableMap));
        setStaticDefinitions(editableStaticDefinitions);
        List<UnbakedModel> resolveParents = new ArrayList<>();
        for(Pair<ResourceLocation,Boolean> pair : generatedStates)
        {
            StateDefinition<Block,BlockState> definition = pair.getSecond() ? VariantModelHelper.ROTATABLE_FAKE_DEFINITION : VariantModelHelper.NORMAL_FAKE_DEFINITION;
            for(BlockState state : definition.getPossibleStates())
            {
                //Put state definition in the STATIC_DEFINITIONS map
                ModelResourceLocation modelID = BlockModelShaper.stateToModelLocation(pair.getFirst(),state);
                this.loadTopLevel(modelID);
                UnbakedModel model = this.getModel(modelID);
                if(!resolveParents.contains(model))
                    resolveParents.add(model);
            }
        }
        //1.20.1
        //Manual hook instead of the `ModelEvent.RegisterAdditional` event as this mixin is applied after that event is called
        //Model Variant Models
        List<ResourceLocation> addedModels = new ArrayList<>();
        ModelVariantDataManager.forEachWithID((id,variant) -> {
            for(ResourceLocation target : variant.getTargets())
            {
                Block b = ForgeRegistries.BLOCKS.getValue(target);
                if(b instanceof IVariantBlock block)
                {
                    ResourceLocation itemID = variant.getItem(block);
                    if(itemID != null)
                    {
                        if(!addedModels.contains(itemID))
                            addedModels.add(itemID);
                    }
                    else
                        LightmansCurrency.LogWarning("Variant " + id + " does not have an item model defined!");
                    if(block.requiredModels() > block.modelsRequiringRotation())
                    {
                        List<ResourceLocation> models = variant.getModels(block);
                        for(int i = block.requiredModels(); i < models.size(); ++i)
                        {
                            ResourceLocation modelID = models.get(i);
                            if(!addedModels.contains(modelID))
                                addedModels.add(modelID);
                        }
                    }
                }
            }
        });
        for(ResourceLocation m : addedModels)
        {
            UnbakedModel unbakedmodel = this.getModel(m);
            this.getUnbakedCache().put(m,unbakedmodel);
            this.getTopLevelModels().put(m,unbakedmodel);
            if(!resolveParents.contains(unbakedmodel))
                resolveParents.add(unbakedmodel);
        }
        LightmansCurrency.LogDebug("Registered " + addedModels.size() + " Model Variant models\n" + DebugUtil.debugList(addedModels));
        for(UnbakedModel model : resolveParents)
            model.resolveParents(this::getModel);
        //Reset static definitions back to their original value
        setStaticDefinitions(staticDefinitionsBackup);
        profiler.pop();
    }

    @Accessor("modelResources")
    protected abstract Map<ResourceLocation,BlockModel> getModelResources();

    @Mutable
    @Accessor("modelResources")
    protected abstract void setModelResources(Map<ResourceLocation,BlockModel> modelResources);

    @Accessor("blockStateResources")
    protected abstract Map<ResourceLocation,List<ModelBakery.LoadedJson>> getBlockStateResources();

    @Mutable
    @Accessor("blockStateResources")
    protected abstract void setBlockStateResources(Map<ResourceLocation,List<ModelBakery.LoadedJson>> value);

    @Invoker("predicate")
    private static Predicate<BlockState> runPredicate(StateDefinition<Block, BlockState> stateDefinition, String properties) { throw new AssertionError(); }

    @Accessor("STATIC_DEFINITIONS")
    private static Map<ResourceLocation, StateDefinition<Block, BlockState>> getStaticDefinitions() { throw new AssertionError(); }

    @Mutable
    @Accessor("STATIC_DEFINITIONS")
    private static void setStaticDefinitions(Map<ResourceLocation, StateDefinition<Block, BlockState>> value) { throw new AssertionError(); }

    @Accessor("unbakedCache")
    protected abstract Map<ResourceLocation, UnbakedModel> getUnbakedCache();

    @Accessor("topLevelModels")
    protected abstract Map<ResourceLocation,UnbakedModel> getTopLevelModels();

}
