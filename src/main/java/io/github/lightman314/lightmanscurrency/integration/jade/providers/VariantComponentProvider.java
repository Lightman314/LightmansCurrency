package io.github.lightman314.lightmanscurrency.integration.jade.providers;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.TooltipInfo;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;

public class VariantComponentProvider implements IBlockComponentProvider{

    public final ResourceLocation ID = VersionUtil.lcResource("model_variant");
    public static final VariantComponentProvider INSTANCE = new VariantComponentProvider();

    private VariantComponentProvider() { }

    @Override
    @Nullable
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if(accessor.getBlock() instanceof IVariantBlock && accessor.getBlockEntity() instanceof IVariantSupportingBlockEntity be)
        {
            ResourceLocation variant = be.getCurrentVariant();
            if(variant != null)
            {
                ItemStack item = new ItemStack(accessor.getBlock());
                item.set(ModDataComponents.MODEL_VARIANT,variant);
                return ItemStackElement.of(item);
            }
        }
        return IBlockComponentProvider.super.getIcon(accessor, config, currentIcon);
    }
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig iPluginConfig) {
        if(accessor.getBlock() instanceof IVariantBlock && accessor.getBlockEntity() instanceof IVariantSupportingBlockEntity be)
        {
            ResourceLocation variantID = be.getCurrentVariant();
            if(variantID != null)
            {
                ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
                if(variant != null)
                {
                    tooltip.add(LCText.TOOLTIP_MODEL_VARIANT_NAME.get(variant.getName().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
                    if(variant.has(VariantProperties.TOOLTIP_INFO))
                    {
                        TooltipInfo extraTooltip = variant.get(VariantProperties.TOOLTIP_INFO);
                        if(extraTooltip.drawOnJade)
                            tooltip.addAll(extraTooltip.getTooltip());
                    }
                }
            }
            if(be.isVariantLocked())
                tooltip.add(LCText.TOOLTIP_MODEL_VARIANT_LOCKED.getWithStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
        }
    }
    @Override
    public ResourceLocation getUid() { return ID; }

}
