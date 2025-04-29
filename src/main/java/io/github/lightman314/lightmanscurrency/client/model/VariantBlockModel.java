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
import net.minecraft.client.renderer.block.BlockModelShaper;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantBlockModel implements IDynamicBakedModel {

    private static final ModelProperty<ResourceLocation> VARIANT = new ModelProperty<>();
    private static final ModelProperty<BlockState> STATE = new ModelProperty<>();

    //Data Cache so that complex calculations only need to be done once
    //No need to make this static as each model should only cover a singular block state
    private final Map<QuadCacheKey,List<BakedQuad>> quadCache = new HashMap<>();
    private final Map<RenderCacheKey,RenderData> renderCache = new HashMap<>();

    private final IVariantBlock block;
    //private final IRotatableBlock rotatable;
    private final BakedModel defaultModel;
    private final ModelResourceLocation defaultModelID;
    public VariantBlockModel(IVariantBlock block,BakedModel defaultModel,ModelResourceLocation defaultModelID)
    {
        this.block = block;
        //this.rotatable = block instanceof IRotatableBlock rb ? rb : null;
        this.defaultModel = defaultModel;
        this.defaultModelID = defaultModelID;
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
    public TextureAtlasSprite getParticleIcon(ModelData data) { return this.getRenderData(data).particleTextures(); }

    @Override
    public ItemOverrides getOverrides() { return this.defaultModel.getOverrides(); }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) { return this.getRenderData(data).model().getRenderTypes(state,rand,data); }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) { return this.getRenderData(data).model().useAmbientOcclusion(state,data,renderType); }

    private List<BakedQuad> getQuadData(ModelData data,@Nullable BlockState state, @Nullable Direction side, @Nullable RenderType renderType)
    {
        QuadCacheKey key = new QuadCacheKey(data.get(VARIANT),state,side,renderType);
        if(quadCache.containsKey(key))
            return quadCache.get(key);
        //Build quads for key
        ResourceLocation variantID = data.get(VARIANT);
        RandomSource rand = RandomSource.create();
        if(variantID == null)
        {
            List<BakedQuad> quads = this.defaultModel.getQuads(state,side,RandomSource.create(),data,renderType);
            quadCache.put(key,quads);
            return quads;
        }
        else
        {
            ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
            if(variant == null)
            {
                //Use the default quads
                List<BakedQuad> quads = this.defaultModel.getQuads(state,side,rand,data,renderType);
                quadCache.put(key,quads);
                return quads;
            }
            else
            {
                List<BakedQuad> quads;
                ModelResourceLocation modelID = this.defaultModelID;
                if(variant.getModels().isEmpty())
                    quads = this.defaultModel.getQuads(state,side,rand,data,renderType);
                else
                {
                    modelID = VariantModelHelper.getModelID(variant,this.block,state);
                    BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelID);
                    quads = model.getQuads(state,side,rand,data,renderType);
                }
                quads = applyTextureOverrides(variant,quads,modelID.id());
                quadCache.put(key,quads);
                return quads;
            }
        }
    }

    private RenderData getRenderData(ModelData data)
    {
        RenderCacheKey key = new RenderCacheKey(data.get(VARIANT),data.get(STATE));
        if(renderCache.containsKey(key))
            return renderCache.get(key);
        //Build render data for key
        ResourceLocation variantID = data.get(VARIANT);
        if(variantID == null)
        {
            RenderData result = new RenderData(this.defaultModel.getParticleIcon(data),this.defaultModel);
            renderCache.put(key,result);
            return result;
        }
        else
        {
            ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
            if(variant == null)
            {
                RenderData result = new RenderData(this.defaultModel.getParticleIcon(data),this.defaultModel);
                renderCache.put(key,result);
                return result;
            }
            else
            {
                BakedModel model;
                if(variant.getModels().isEmpty())
                    model = this.defaultModel;
                else
                {
                    ModelResourceLocation modelID = VariantModelHelper.getModelID(variant,this.block,data.get(STATE));
                    model = Minecraft.getInstance().getModelManager().getModel(modelID);
                }
                TextureAtlasSprite sprite = model.getParticleIcon(data);
                if(variant.getTextureOverrides().containsKey("particle"))
                {
                    ResourceLocation spriteID = variant.getTextureOverrides().get("particle");
                    TextureAtlasSprite newSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteID);
                    if(newSprite != null)
                        sprite = newSprite;
                }
                RenderData result = new RenderData(sprite,model);
                renderCache.put(key,result);
                return result;
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
            return Objects.hash(this.variantID,stateHash,this.side,this.renderType == null ? 0 : this.renderType.name.hashCode());
        }
    }

    private record RenderCacheKey(@Nullable ResourceLocation variantID, @Nullable BlockState state) {}

    private record RenderData(TextureAtlasSprite particleTextures,BakedModel model) { }

}
