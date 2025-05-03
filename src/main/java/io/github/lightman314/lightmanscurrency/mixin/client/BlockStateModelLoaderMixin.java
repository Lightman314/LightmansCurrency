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
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(BlockStateModelLoader.class)
public abstract class BlockStateModelLoaderMixin {

    @Inject(at = @At("RETURN"),method = "<init>")
    private void init(Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> ignoreMe,
                      ProfilerFiller profiler,
                      UnbakedModel missingModel,
                      BlockColors blockColors,
                      BiConsumer<ModelResourceLocation, UnbakedModel> discoveredModelOutput,CallbackInfo ci)
    {
        Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> editableMap = new HashMap<>(this.getBlockStateResources());
        Map<ResourceLocation,BlockModel> modelData = VariantModelHelper.getModelDataCache();
        //Generate custom models for every variant that has texture overrides
        if(modelData != null)
        {
            ModelVariantDataManager.forEachWithID((id,variant) -> {
                if(variant.hasTextureOverrides())
                {
                    //Create custom models
                    if((variant.hasModels() && variant.getItem() != null))
                    {
                        Function<String,ResourceLocation> idGenerator = VariantModelHelper.createIDGenerator(id,null);
                        List<ResourceLocation> originalModels = variant.getModels();
                        List<ResourceLocation> newModels = VariantModelHelper.createCustomBlockModel(
                                originalModels,
                                modelData,
                                variant.getTextureOverrides(),
                                idGenerator
                        );
                        variant.overrideModels(newModels);
                        //Item Model
                        ResourceLocation itemModel = variant.getItem().id();
                        ResourceLocation newModel = idGenerator.apply("item");
                        VariantModelHelper.createCustomBlockModel(itemModel,modelData,variant.getTextureOverrides(),newModel);
                        variant.overrideItemModel(ModelResourceLocation.standalone(newModel));
                    }
                    else
                    {
                        final ResourceLocation singleTargetID = VersionUtil.lcResource("null");
                        Map<ResourceLocation,List<ResourceLocation>> targetedBlockModels = new HashMap<>();
                        Map<ResourceLocation,ModelResourceLocation> targetedItemModels = new HashMap<>();
                        boolean singleTarget = variant.getTargets().size() <= 1;
                        //Look up default models for each target
                        for(ResourceLocation target : variant.getTargets())
                        {
                            ResourceLocation mapID = singleTarget ? singleTargetID : target;
                            Block b = BuiltInRegistries.BLOCK.get(target);
                            if(b instanceof IVariantBlock block)
                            {
                                Function<String,ResourceLocation> idGenerator = VariantModelHelper.createIDGenerator(id,singleTarget ? null : target);
                                List<ResourceLocation> originalModels = VariantModelHelper.getDefaultModels(b,block,editableMap,BlockStateModelLoaderMixin::runPredicate);
                                List<ResourceLocation> newModels =  VariantModelHelper.createCustomBlockModel(
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
                                targetedItemModels.put(mapID,ModelResourceLocation.standalone(newModelID));
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
                            LightmansCurrency.LogDebug("Targeted Texture Override Models for " + id + ":\nItem Models: " + DebugUtil.debugMap(targetedItemModels) + "\nBlock Models: " + DebugUtil.debugMap(targetedBlockModels,Objects::toString,DebugUtil::debugList));
                        }
                    }
                }
            });
        }
        else
            LightmansCurrency.LogError("Could not access the BlockModels for applying texture variants to");
        //Create custom block state json files for our custom models
        List<Pair<ResourceLocation,Boolean>> generatedStates = new ArrayList<>();
        ModelVariantDataManager.forEach(variant -> {
            for(ResourceLocation target : variant.getTargets())
            {
                Block b = BuiltInRegistries.BLOCK.get(target);
                if(b instanceof IVariantBlock block)
                {
                    int index = 0;
                    for(ResourceLocation model : variant.getModels(block))
                    {
                        ResourceLocation fileID = BlockStateModelLoader.BLOCKSTATE_LISTER.idToFile(model);
                        if(!editableMap.containsKey(fileID))
                        {
                            if(index >= block.modelsRequiringRotation())
                                return;
                            boolean rotatable = block instanceof IRotatableBlock rb;
                            JsonElement json = VariantModelHelper.generateBlockStateFile(model,rotatable);
                            editableMap.put(fileID, ImmutableList.of(new BlockStateModelLoader.LoadedJson("generated/lightmans_currency_variants",json)));
                            generatedStates.add(Pair.of(model,rotatable));
                            LightmansCurrency.LogDebug("Generated fake block state for " + model);
                        }
                        index++;
                    }
                }
            }
        });
        this.setBlockStateResources(ImmutableMap.copyOf(editableMap));
        VariantModelHelper.defineGeneratedStates(generatedStates);
    }

    @Inject(at = @At("RETURN"),method = "loadAllBlockStates")
    private void loadAllBlockStates(CallbackInfo ci)
    {
        for(Pair<ResourceLocation,Boolean> state : VariantModelHelper.getStatesToGenerate())
        {
            StateDefinition<Block,BlockState> definition = state.getSecond() ? VariantModelHelper.ROTATABLE_FAKE_DEFINITION : VariantModelHelper.NORMAL_FAKE_DEFINITION;
            this.runLoadBlockStateDefinitions(state.getFirst(),definition);
        }
    }

    @Invoker("loadBlockStateDefinitions")
    protected abstract void runLoadBlockStateDefinitions(ResourceLocation blockStateId, StateDefinition<Block, BlockState> stateDefenition);

    @Accessor("blockStateResources")
    protected abstract Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> getBlockStateResources();

    @Mutable
    @Accessor("blockStateResources")
    protected abstract void setBlockStateResources(Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> map);

    @Invoker("predicate")
    private static Predicate<BlockState> runPredicate(StateDefinition<Block, BlockState> stateDefentition, String properties) { throw new AssertionError(); }

}
