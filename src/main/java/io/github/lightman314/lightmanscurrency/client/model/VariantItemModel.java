package io.github.lightman314.lightmanscurrency.client.model;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models.VariantModelLocation;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantItemModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<ResourceLocation> VARIANT = new ModelProperty<>();
    private static final ModelProperty<BlockState> STATE = new ModelProperty<>();

    //Data Cache so that complex calculations only need to be done once
    //No need to make this static as each model should only cover a singular block
    private final Map<ResourceLocation,BakedModel> itemModelCache = new HashMap<>();

    private final IVariantItem item;
    private final BakedModel defaultModel;
    private final ItemOverrides overrides;
    public VariantItemModel(IVariantItem item, BakedModel defaultModel)
    {
        super(defaultModel);
        this.item = item;
        this.defaultModel = defaultModel;
        this.overrides = new Overrides();
    }
    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) { return this.getModel(itemStack).getRenderTypes(itemStack,fabulous); }
    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) { return List.of(this.getModel(itemStack)); }

    @Override
    public ItemOverrides getOverrides() { return this.overrides; }

    private BakedModel getModel(ItemStack item)
    {
        ResourceLocation variantID = item.getOrDefault(ModDataComponents.MODEL_VARIANT,null);
        if(variantID == null)
            return this.defaultModel;
        if(this.itemModelCache.containsKey(variantID))
            return this.itemModelCache.get(variantID);
        ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
        //Confirm the variant supports our target block
        if(variant == null || !variant.isValidTarget(this.item))
        {
            this.itemModelCache.put(variantID,this.defaultModel);
            return this.defaultModel;
        }
        else
        {
            BakedModel model;
            VariantModelLocation modelID = VariantModelLocation.item(variantID,this.item.getItemID());
            if(modelID == null)
                model = this.defaultModel;
            else
                model = ModelVariantDataManager.getModel(modelID);
            this.itemModelCache.put(variantID,model);
            return model;
        }
    }

    private class Overrides extends ItemOverrides
    {
        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            model = VariantItemModel.this.getModel(stack);
            return model.getOverrides().resolve(model,stack,level,entity,seed);
        }
    }

}