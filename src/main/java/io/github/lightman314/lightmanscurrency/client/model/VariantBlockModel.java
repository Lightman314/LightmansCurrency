package io.github.lightman314.lightmanscurrency.client.model;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.model.util.VariantModelHelper;
import io.github.lightman314.lightmanscurrency.client.model.util.TextureMap;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantBlockModel implements IDynamicBakedModel {

    private static final ModelProperty<ResourceLocation> VARIANT = new ModelProperty<>();
    private static final ModelProperty<BlockState> STATE = new ModelProperty<>();

    private final IVariantBlock block;
    //private final IRotatableBlock rotatable;
    private final BakedModel defaultModel;
    private final TargetResults defaultResults;
    public VariantBlockModel(IVariantBlock block,BakedModel defaultModel,ModelResourceLocation defaultModelID)
    {
        this.block = block;
        //this.rotatable = block instanceof IRotatableBlock rb ? rb : null;
        this.defaultModel = defaultModel;
        this.defaultResults = new TargetResults(this.defaultModel,defaultModelID.id());
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ResourceLocation variantID = null;
        if(level.getBlockEntity(pos) instanceof IVariantSupportingBlockEntity be)
            variantID = be.getCurrentVariant();
        //Have target model also add their own properties
        ModelData other = this.getTargetData(variantID,state).model.getModelData(level,pos,state,modelData);
        //Add my own properties to the existing model data
        return other.derive()
                .with(VARIANT,variantID)
                .with(STATE,state).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        TargetResults target = this.getTargetData(extraData);
        List<BakedQuad> results = target.model.getQuads(state,side,rand,extraData,renderType);
        //Apply texture overrides if present
        results = applyTextureOverrides(target.variant,results,target.modelID);
        return results;
    }

    private static List<BakedQuad> applyTextureOverrides(@Nullable ModelVariant variant,List<BakedQuad> quads,ResourceLocation modelID)
    {
        Map<String,ResourceLocation> textureOverrides = variant == null ? ImmutableMap.of() : variant.getTextureOverrides();
        if(variant == null || textureOverrides.isEmpty())
            return quads;
        TextureMap originalTextures = VariantModelHelper.getTexturesFor(modelID);
        List<BakedQuad> result = new ArrayList<>();
        for(BakedQuad quad : quads)
        {
            String key = originalTextures.getKey(quad.getSprite());
            if(key == null)
            {
                result.add(quad);
                LightmansCurrency.LogDebug("Could not find texture key for a sprite on " + modelID);
            }
            else
            {
                ResourceLocation tex = textureOverrides.get(key);
                TextureAtlasSprite sprite = getSprite(tex);
                if(sprite == null)
                {
                    result.add(quad);
                }
                else //Replace texture with the new sprite
                {
                    result.add(new BakedQuad(quad.getVertices(),quad.getTintIndex(),quad.getDirection(),sprite,quad.isShade(),quad.hasAmbientOcclusion()));
                    LightmansCurrency.LogDebug("Replaced quad texture with " + tex);
                }
            }
        }
        return result;
    }

    @Nullable
    private static TextureAtlasSprite getSprite(@Nullable ResourceLocation texture)
    {
        if(texture == null)
            return null;
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
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
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        TargetResults target = this.getTargetData(data);
        if(target.variant != null && target.variant.getTextureOverrides().containsKey("particle"))
        {
            //Replace particle
            return getSprite(target.variant.getTextureOverrides().get("particle"));
        }
        return this.getTargetModel(data).getParticleIcon(data);
    }

    @Override
    public ItemOverrides getOverrides() { return this.defaultModel.getOverrides(); }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) { return this.getTargetModel(data).getRenderTypes(state,rand,data); }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) { return this.getTargetModel(data).useAmbientOcclusion(state,data,renderType); }

    private BakedModel getTargetModel(ModelData data) { return this.getTargetData(data).model; }
    private TargetResults getTargetData(ModelData data) {
        if(data.has(VARIANT))
            return this.getTargetData(data.get(VARIANT),data.get(STATE));
        LightmansCurrency.LogWarning("Attempted to get the target model without the required ModelData properties!",new Throwable());
        return this.defaultResults;
    }
    private TargetResults getTargetData(ResourceLocation variantID, @Nullable BlockState state) {
        ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
        if(variant == null)
            return this.defaultResults;
        else
        {
            if(!variant.getTargets().contains(this.block.getBlockID()))
            {
                LightmansCurrency.LogWarning("Variant " + variantID + " is not supposed to target " + this.block.getBlockID());
                return this.defaultResults;
            }
            //Don't return a custom model
            if(variant.getModels().isEmpty())
                return this.defaultResults.ofVariant(variant);

            int entryIndex = state == null ? 0 : this.block.getModelIndex(state);
            ModelResourceLocation modelID = VariantModelHelper.getModelID(variant,this.block,state);
            BakedModel bm = Minecraft.getInstance().getModelManager().getModel(modelID);
            return new TargetResults(bm,variant,modelID.id());
        }
    }

    private static class TargetResults {
        private final BakedModel model;
        @Nullable
        private final ModelVariant variant;
        private final ResourceLocation modelID;
        private TargetResults(BakedModel model, ResourceLocation modelID) { this(model,null,modelID); }
        private TargetResults(BakedModel model, @Nullable ModelVariant variant, ResourceLocation modelID)
        {
            this.model = model;
            this.variant = variant;
            this.modelID = modelID;
        }
        private TargetResults ofVariant(@Nullable ModelVariant variant) { return new TargetResults(this.model,variant,this.modelID); }
    }

}
