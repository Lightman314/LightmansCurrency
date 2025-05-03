package io.github.lightman314.lightmanscurrency.client.model.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.common.blocks.properties.YRotationProperty;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class VariantModelHelper {

    private VariantModelHelper() {}

    public static final YRotationProperty ROTATION_PROPERTY = YRotationProperty.of("rotation");
    public static final StateDefinition<Block,BlockState> ROTATABLE_FAKE_DEFINITION = new StateDefinition.Builder<Block,BlockState>(Blocks.AIR)
            .add(ROTATION_PROPERTY)
            .create(Block::defaultBlockState, BlockState::new);
    public static final StateDefinition<Block,BlockState> NORMAL_FAKE_DEFINITION = new StateDefinition.Builder<Block,BlockState>(Blocks.AIR)
            .create(Block::defaultBlockState,BlockState::new);

    private static final ResourceLocation NULL_MODEL_ID = VersionUtil.vanillaResource("null");

    private static Map<ResourceLocation,BlockModel> modelDataCache = null;
    public static void setModelDataCache(Map<ResourceLocation, BlockModel> map) { modelDataCache = map; }
    public static Map<ResourceLocation,BlockModel> getModelDataCache() { return modelDataCache; }

    /**
     * State should not be null. Only to be used for getting the block model for the given variant/state
     */
    @Nullable
    public static ModelResourceLocation getModelID(ModelVariant variant, IVariantBlock block, BlockState state)
    {
        if(state == null)
            return null;
        int index = block.getModelIndex(state);
        ResourceLocation model = variant.getModel(block,index);
        if(model == null)
        {
            LightmansCurrency.LogWarning("Missing targeted model for " + block.getBlockID() + " at index " + index);
            return null;
        }
        if(index >= block.modelsRequiringRotation())
            return ModelResourceLocation.standalone(model);
        Map<Property<?>,Comparable<?>> map = new HashMap<>();
        if(block instanceof IRotatableBlock rb)
            map.put(ROTATION_PROPERTY,rb.getRotationY(state));
        return new ModelResourceLocation(model,BlockModelShaper.statePropertiesToString(map));
    }

    private static final List<Pair<ResourceLocation,Boolean>> generatedStates = new ArrayList<>();
    public static void defineGeneratedStates(List<Pair<ResourceLocation,Boolean>> list) { generatedStates.addAll(list); }
    public static List<Pair<ResourceLocation,Boolean>> getStatesToGenerate() { return generatedStates; }

    public static JsonElement generateBlockStateFile(ResourceLocation modelID, boolean rotatable)
    {
        JsonObject json = new JsonObject();
        JsonObject variants = new JsonObject();
        if(rotatable)
        {
            for(int yRot : YRotationProperty.POSSIBLE_VALUES)
            {
                JsonObject entry = new JsonObject();
                entry.addProperty("model",modelID.toString());
                entry.addProperty("y",yRot);
                variants.add(BlockModelShaper.statePropertiesToString(ImmutableMap.of(ROTATION_PROPERTY,yRot)),entry);
            }
        }
        else {
            JsonObject entry = new JsonObject();
            entry.addProperty("model",modelID.toString());
            variants.add("",entry);
        }
        json.add("variants",variants);
        return json;
    }

    @SubscribeEvent
    public static void onModelsLoaded(ModelEvent.BakingCompleted event) { modelDataCache = null; }

    public static List<ResourceLocation> createCustomBlockModel(List<ResourceLocation> originalModels, Map<ResourceLocation,BlockModel> modelData, Map<String,ResourceLocation> textureOverrides, Function<String,ResourceLocation> idGenerator)
    {
        List<ResourceLocation> newModels = new ArrayList<>();
        for(int i = 0; i < originalModels.size(); ++i)
        {
            ResourceLocation model = originalModels.get(i);
            ResourceLocation newModelID = idGenerator.apply(String.valueOf(i));
            createCustomBlockModel(model,modelData,textureOverrides,newModelID);
            newModels.add(newModelID);
        }
        return newModels;
    }
    public static void createCustomBlockModel(ResourceLocation model, Map<ResourceLocation,BlockModel> modelData, Map<String,ResourceLocation> textureOverrides, ResourceLocation newModelID)
    {
        Map<String, Either<Material,String>> textureMap = new HashMap<>();
        textureOverrides.forEach((key,texture) ->
                textureMap.put(key,Either.left(new Material(InventoryMenu.BLOCK_ATLAS,texture)))
        );
        BlockModel newModel = new BlockModel(model,new ArrayList<>(),textureMap,null,null, ItemTransforms.NO_TRANSFORMS,new ArrayList<>());
        ResourceLocation fileID = ModelBakery.MODEL_LISTER.idToFile(newModelID);
        modelData.put(fileID,newModel);
    }

    public static List<ResourceLocation> getDefaultModels(Block b, IVariantBlock block, Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> blockStateData, BiFunction<StateDefinition<Block, BlockState>,String,Predicate<BlockState>> tester)
    {
        //Get LoadedJson list from the map
        ResourceLocation fileID = BlockStateModelLoader.BLOCKSTATE_LISTER.idToFile(block.getBlockID());
        List<BlockStateModelLoader.LoadedJson> blockStates = blockStateData.getOrDefault(fileID,new ArrayList<>());
        //Intiailize list that should be built
        List<ResourceLocation> models = NonNullList.withSize(block.modelsRequiringRotation(),VersionUtil.vanillaResource("null"));
        List<Integer> completedIndexes = new ArrayList<>();
        //Set up loading context
        BlockModelDefinition.Context context = new BlockModelDefinition.Context();
        context.setDefinition(b.getStateDefinition());
        if(blockStates.isEmpty())
            LightmansCurrency.LogWarning("No Block States json file found for " + block.getBlockID());
        for(BlockStateModelLoader.LoadedJson json : blockStates)
        {
            try {
                BlockModelDefinition definition = BlockModelDefinition.fromJsonElement(context,json.data());
                List<BlockState> allStates = new ArrayList<>(b.getStateDefinition().getPossibleStates());
                definition.getVariants().forEach((key,mv) -> {
                    List<BlockState> applicableStates = allStates.stream().filter(tester.apply(context.getDefinition(),key)).toList();
                    for(BlockState state : applicableStates)
                    {
                        int index = block.getModelIndex(state);
                        if(completedIndexes.contains(index))
                            continue;
                        for(Variant v : mv.getVariants())
                        {
                            if(v.getModelLocation() != null)
                            {
                                models.set(index,v.getModelLocation());
                                completedIndexes.add(index);
                                break;
                            }
                        }
                    }
                });
            }catch (Exception e) { LightmansCurrency.LogWarning("Error parsing Block Model Definition for " + block.getBlockID(),e); }
        }
        return models;
    }

    public static Function<String,ResourceLocation> createIDGenerator(ResourceLocation variantID, @Nullable ResourceLocation target)
    {
        if(target == null)
            return s -> VersionUtil.modResource(variantID.getNamespace(),"lc_model_variants/" + variantID.getPath() + "/" + s);
        else
            return s -> VersionUtil.modResource(variantID.getNamespace(),"lc_model_variants/" + variantID.getPath() + "/" + target.getNamespace() + "/" + target.getPath() + "/" + s);
    }

}
