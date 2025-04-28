package io.github.lightman314.lightmanscurrency.mixin.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.model.util.VariantModelHelper;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import net.minecraft.client.color.block.BlockColors;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(BlockStateModelLoader.class)
public abstract class BlockStateModelLoaderMixin {

    @Inject(at = @At("RETURN"),method = "<init>")
    private void init(Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources,
                      ProfilerFiller profiler,
                      UnbakedModel missingModel,
                      BlockColors blockColors,
                      BiConsumer<ModelResourceLocation, UnbakedModel> discoveredModelOutput,CallbackInfo ci)
    {
        //Create custom block state json files for our custom models
        Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> editableMap = new HashMap<>(blockStateResources);
        List<Pair<ResourceLocation,Boolean>> generatedStates = new ArrayList<>();
        ModelVariantDataManager.forEach(variant -> {
            if(variant.getModels().isEmpty() || variant.getTargets().isEmpty())
                return;
            int index = 0;
            for(ResourceLocation model : variant.getModels())
            {
                ResourceLocation fileID = BlockStateModelLoader.BLOCKSTATE_LISTER.idToFile(model);
                if(!editableMap.containsKey(fileID))
                {
                    //We're assuming all the targets are either rotatable or non-rotatable
                    ResourceLocation target = variant.getTargets().getFirst();
                    if(BuiltInRegistries.BLOCK.get(target) instanceof IVariantBlock vb)
                    {
                        if(index >= vb.modelsRequiringRotation())
                            return;
                        boolean rotatable = vb instanceof IRotatableBlock rb;
                        JsonElement json = VariantModelHelper.generateBlockStateFile(model,rotatable);
                        editableMap.put(fileID, ImmutableList.of(new BlockStateModelLoader.LoadedJson("generated/lightmans_currency_variants",json)));
                        generatedStates.add(Pair.of(model,rotatable));
                    }
                }
                index++;
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

    @Mutable
    @Accessor("blockStateResources")
    protected abstract void setBlockStateResources(Map<ResourceLocation,List<BlockStateModelLoader.LoadedJson>> map);

}
