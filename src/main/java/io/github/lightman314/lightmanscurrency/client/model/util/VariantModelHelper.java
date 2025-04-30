package io.github.lightman314.lightmanscurrency.client.model.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blocks.properties.YRotationProperty;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.mixin.client.ModelBakeryAccessor;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

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

    private static final Map<ResourceLocation,TextureMap> textureData = new HashMap<>();

    public static TextureMap getTexturesFor(ResourceLocation model) { return textureData.getOrDefault(model,TextureMap.EMPTY); }

    public static ModelResourceLocation getModelID(ModelVariant variant, IVariantBlock block, @Nullable BlockState state)
    {
        if(state == null)
            return variant.getItem();
        int index = block.getModelIndex(state);
        ResourceLocation model = variant.getModel(index);
        if(index >= block.modelsRequiringRotation())
            return ModelResourceLocation.standalone(model);
        Map<Property<?>,Comparable<?>> map = new HashMap<>();
        if(block instanceof IRotatableBlock rb)
            map.put(ROTATION_PROPERTY,rb.getRotationY(state));
        return new ModelResourceLocation(model,BlockModelShaper.statePropertiesToString(map));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelsBaked(ModelEvent.BakingCompleted event)
    {
        textureData.clear();
        //Abort if models are disabled in the config
        if(!LCConfig.CLIENT.variantBlockModels.get())
            return;
        //variantModels.clear();
        if(event.getModelBakery() instanceof ModelBakeryAccessor mba)
        {
            List<ResourceLocation> completedTargets = new ArrayList<>();
            ModelVariantDataManager.forEachWithID((id,variant) -> {
                if(!variant.getTextureOverrides().isEmpty())
                {
                    LightmansCurrency.LogDebug("Attempting to collect Texture Maps for " + id);
                    if(variant.getModels().isEmpty())
                    {
                        for(ResourceLocation target : variant.getTargets())
                        {
                            if(completedTargets.contains(target))
                                continue;
                            completedTargets.add(target);
                            Block b = BuiltInRegistries.BLOCK.get(target);
                            for(BlockState state : b.getStateDefinition().getPossibleStates())
                                getTextureMap(mba,BlockModelShaper.stateToModelLocation(state));
                        }
                    }
                    else
                    {
                        for(ResourceLocation model : variant.getModels())
                            getTextureMap(mba,model);
                    }
                }
            });
        }
    }

    private static void getTextureMap(ModelBakeryAccessor mba, ModelResourceLocation modelID)
    {
        if(textureData.containsKey(modelID.id()))
            return;
        UnbakedModel unbakedModel = mba.getTopLevelModels().get(modelID);
        if(unbakedModel instanceof BlockModel blockModel)
            getTextureMap(modelID.id(),blockModel);
        else if(unbakedModel instanceof MultiVariant mv)
        {
            int success = 0;
            for(Variant v : mv.getVariants())
            {
                UnbakedModel model2 = mba.getUnbakedCache().get(v.getModelLocation());
                if(model2 instanceof BlockModel blockModel)
                {
                    getTextureMap(modelID.id(),blockModel);
                    success++;
                }
            }
            if(success == 0)
                LightmansCurrency.LogWarning("Model " + modelID + " has no valid BlockModel variants!");
        }
        else
            LightmansCurrency.LogWarning("Model " + modelID + " is not a valid BlockModel or MultiVariant so I cannot get it's texture data. Model is actually a " + unbakedModel.getClass().getName());
    }
    private static void getTextureMap(ModelBakeryAccessor mba, ResourceLocation modelID)
    {
        if(textureData.containsKey(modelID))
            return;
        UnbakedModel unbakedModel = mba.getUnbakedCache().get(modelID);
        if(unbakedModel instanceof BlockModel blockModel)
            getTextureMap(modelID,blockModel);
        else
            LightmansCurrency.LogWarning("Model " + modelID + " is not a valid BlockModel so I cannot get it's texture data. Model is actually a " + unbakedModel.getClass().getName());

    }
    private static void getTextureMap(ResourceLocation modelID, BlockModel blockModel)
    {
        Map<String,ResourceLocation> textureMap = new HashMap<>();
        for(String key : blockModel.textureMap.keySet())
            textureMap.put(key,blockModel.getMaterial(key).texture());
        textureData.put(modelID,TextureMap.create(textureMap));
        LightmansCurrency.LogDebug("Collected texture data from " + modelID + ": " + textureMap);
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

}
