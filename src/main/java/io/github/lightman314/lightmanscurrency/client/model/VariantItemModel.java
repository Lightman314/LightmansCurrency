package io.github.lightman314.lightmanscurrency.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantItemModel implements IDynamicBakedModel {

    private final Map<QuadKey,List<BakedQuad>> quadCache = new HashMap<>();

    public final BakedModel defaultModel;
    private final ModelResourceLocation defaultModelID;
    private final ModelVariant variant;
    public VariantItemModel(BakedModel defaultModel, ModelResourceLocation defaultModelID, ModelVariant variant) {
        this.defaultModel = defaultModel;
        this.defaultModelID = defaultModelID;
        this.variant = variant;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return this.getQuads(side,renderType);
    }

    private List<BakedQuad> getQuads(@Nullable Direction side, @Nullable RenderType renderType)
    {
        QuadKey key = new QuadKey(side,renderType);
        if(this.quadCache.containsKey(key))
            return this.quadCache.get(key);
        List<BakedQuad> quads = this.defaultModel.getQuads(null,side,RandomSource.create(),ModelData.EMPTY,renderType);
        quads = VariantBlockModel.applyTextureOverrides(this.variant,quads,this.defaultModelID.id());
        this.quadCache.put(key,quads);
        return quads;
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
    public ItemOverrides getOverrides() { return this.defaultModel.getOverrides(); }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) { return this.defaultModel.getRenderTypes(itemStack,fabulous); }
    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) { return this.defaultModel.getRenderPasses(itemStack, fabulous); }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) { return this.defaultModel.applyTransform(transformType,poseStack,applyLeftHandTransform); }

    private record QuadKey(@Nullable Direction side, @Nullable RenderType renderType) { }

}
