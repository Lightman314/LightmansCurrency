package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantModelHelper {

    private VariantModelHelper() {}

    /**
     * State should not be null. Only to be used for getting the block model for the given variant/state
     */
    @Nullable
    public static VariantModelLocation getModelID(ModelVariant variant, ResourceLocation variantID, IVariantBlock block, BlockState state)
    {
        if(state == null)
            return VariantModelLocation.item(variantID,block.getBlockID());
        int index = block.getModelIndex(state);
        if(index < block.modelsRequiringRotation() && block instanceof IRotatableBlock rb)
            return VariantModelLocation.rotatable(variantID,block.getBlockID(),index,rb.getRotationY(state));
        else
            return VariantModelLocation.basic(variantID,block.getBlockID(),index);
    }

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
        ResourceLocation fileID = ModelBakery.MODEL_LISTER.idToFile(newModelID);
        if(modelData.containsKey(fileID))
            return;
        Map<String, Either<Material,String>> textureMap = new HashMap<>();
        textureOverrides.forEach((key,texture) ->
                textureMap.put(key,Either.left(new Material(InventoryMenu.BLOCK_ATLAS,texture)))
        );
        BlockModel newModel = new BlockModel(model,new ArrayList<>(),textureMap,null,null, ItemTransforms.NO_TRANSFORMS,new ArrayList<>());
        modelData.put(fileID,newModel);
    }

    public static List<ResourceLocation> getDefaultModels(Block b, IVariantBlock block, Map<ResourceLocation,List<ModelBakery.LoadedJson>> blockStateData, BiFunction<StateDefinition<Block, BlockState>,String,Predicate<BlockState>> tester)
    {
        //Get LoadedJson list from the map
        ResourceLocation fileID = ModelBakery.BLOCKSTATE_LISTER.idToFile(block.getBlockID());
        List<ModelBakery.LoadedJson> blockStates = blockStateData.getOrDefault(fileID,new ArrayList<>());
        //Intiailize list that should be built
        List<ResourceLocation> models = NonNullList.withSize(block.requiredModels(),VersionUtil.vanillaResource("null"));
        List<Integer> completedIndexes = new ArrayList<>();
        //Set up loading context
        BlockModelDefinition.Context context = new BlockModelDefinition.Context();
        context.setDefinition(b.getStateDefinition());
        if(blockStates.isEmpty())
            LightmansCurrency.LogWarning("No Block States json file found for " + block.getBlockID());
        for(ModelBakery.LoadedJson json : blockStates)
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
        for(int i = block.modelsRequiringRotation(); i < block.requiredModels(); ++i)
            models.set(i,block.getCustomDefaultModel(i));
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