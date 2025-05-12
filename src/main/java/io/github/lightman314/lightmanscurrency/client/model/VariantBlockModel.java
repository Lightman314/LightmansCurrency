package io.github.lightman314.lightmanscurrency.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.model.util.VariantModelHelper;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.mixin.client.RenderStateShardAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantBlockModel implements IDynamicBakedModel {

    public static final ModelProperty<ResourceLocation> VARIANT = new ModelProperty<>();
    public static final ModelProperty<BlockState> STATE = new ModelProperty<>();

    //Data Cache so that complex calculations only need to be done once
    //No need to make this static as each model should only cover a singular block state
    private final Map<QuadCacheKey,List<BakedQuad>> quadCache = new HashMap<>();
    private final Map<RenderCacheKey,BakedModel> blockModelCache = new HashMap<>();

    private final IVariantBlock block;
    private final BakedModel defaultModel;
    public VariantBlockModel(IVariantBlock block,BakedModel defaultModel)
    {
        this.block = block;
        this.defaultModel = defaultModel;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ResourceLocation variantID = null;
        if(level.getBlockEntity(pos) instanceof IVariantSupportingBlockEntity be)
            variantID = be.getCurrentVariant();
        //Add my own properties to the existing model data
        return modelData.derive()
                .with(VARIANT,variantID)
                .with(STATE,state).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return this.getQuadData(extraData,state,side,renderType);
    }

    @Override
    public boolean useAmbientOcclusion() { return this.defaultModel.useAmbientOcclusion(); }

    @Override
    public boolean isGui3d() { return this.defaultModel.isGui3d(); }

    @Override
    public boolean usesBlockLight() { return this.defaultModel.usesBlockLight(); }

    @Override
    public boolean isCustomRenderer() { return this.defaultModel.isCustomRenderer(); }

    @Override
    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleIcon() { return this.defaultModel.getParticleIcon(); }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) { return this.getModel(data).getParticleIcon(data); }

    @Override
    public ItemOverrides getOverrides() { return this.defaultModel.getOverrides(); }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) { return this.getModel(data).getRenderTypes(state,rand,data); }

    @Override
    public boolean useAmbientOcclusion(BlockState state) { return this.defaultModel.useAmbientOcclusion(state); }
    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) { return this.defaultModel.useAmbientOcclusion(state,renderType); }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) { return this.defaultModel.applyTransform(transformType,poseStack,applyLeftHandTransform); }

    private List<BakedQuad> getQuadData(ModelData data, @Nullable BlockState state, @Nullable Direction side, @Nullable RenderType renderType)
    {
        QuadCacheKey key = new QuadCacheKey(data.get(VARIANT),state,side,renderType);
        if(this.quadCache.containsKey(key))
            return this.quadCache.get(key);
        //Build quads for key
        ResourceLocation variantID = key.variantID;
        RandomSource rand = RandomSource.create();
        if(variantID == null)
        {
            List<BakedQuad> quads = this.defaultModel.getQuads(state,side,rand,data,renderType);
            this.quadCache.put(key,quads);
            return quads;
        }
        else
        {
            ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
            //Confirm that variant supports our target block
            if(variant == null || !variant.getTargets().contains(this.block.getBlockID()))
            {
                //Use the default quads
                List<BakedQuad> quads = this.defaultModel.getQuads(state,side,rand,data,renderType);
                this.quadCache.put(key,quads);
                return quads;
            }
            else
            {
                List<BakedQuad> quads;
                ResourceLocation modelID = VariantModelHelper.getModelID(variant,this.block,state);
                if(modelID == null)
                    quads = this.defaultModel.getQuads(state,side,rand,data,renderType);
                else
                {
                    BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelID);
                    quads = model.getQuads(state,side,rand,data,renderType);
                }
                this.quadCache.put(key,quads);
                return quads;
            }
        }
    }

    private BakedModel getModel(ModelData data)
    {
        RenderCacheKey key = new RenderCacheKey(data.get(VARIANT),data.get(STATE));
        if(this.blockModelCache.containsKey(key))
            return this.blockModelCache.get(key);
        //Build render data for key
        ResourceLocation variantID = key.variantID;
        if(variantID == null)
        {
            this.blockModelCache.put(key,this.defaultModel);
            return this.defaultModel;
        }
        else
        {
            ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
            //Confirm the variant supports our target block
            if(variant == null || !variant.getTargets().contains(this.block.getBlockID()))
            {
                this.blockModelCache.put(key,this.defaultModel);
                return this.defaultModel;
            }
            else
            {
                BakedModel model;
                ResourceLocation modelID = VariantModelHelper.getModelID(variant,this.block,data.get(STATE));
                if(modelID == null)
                    model = this.defaultModel;
                else
                    model = Minecraft.getInstance().getModelManager().getModel(modelID);
                this.blockModelCache.put(key,model);
                return model;
            }
        }
    }

    private record QuadCacheKey(@Nullable ResourceLocation variantID, @Nullable BlockState state, @Nullable Direction side, @Nullable RenderType renderType) {
        //Overriding the record hashCode method as the BlockState class doesn't have a hashcode implementation, so I don't trust it.
        @Override
        public int hashCode() {
            int stateHash = 0;
            if(this.state != null)
                stateHash = BlockModelShaper.stateToModelLocation(this.state).hashCode();
            return Objects.hash(this.variantID,stateHash,this.side,this.renderType instanceof RenderStateShardAccessor a ? a.getName() : 0);
        }

        @Override
        public String toString() {
            return "QuadCacheKey[" + (this.variantID == null ? "null" : this.variantID) + "," +
                    (this.state == null ? "null" : BlockModelShaper.stateToModelLocation(this.state)) + "," +
                    this.side + "," +
                    (this.renderType instanceof RenderStateShardAccessor a ? a.getName() : "null") + "]";
        }
    }

    private record RenderCacheKey(@Nullable ResourceLocation variantID, @Nullable BlockState state) {}

}