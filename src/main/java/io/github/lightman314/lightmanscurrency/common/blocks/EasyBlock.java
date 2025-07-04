package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.TooltipInfo;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EasyBlock extends Block {

    public EasyBlock(Properties properties) {
        super(properties);
        if(this instanceof IVariantBlock vb)
            this.registerDefaultState(this.defaultBlockState().setValue(IVariantBlock.VARIANT,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        if(this instanceof IVariantBlock vb)
            builder.add(IVariantBlock.VARIANT);
    }

    protected boolean isBlockOpaque(BlockState state) { return this.isBlockOpaque(); }
    protected boolean isBlockOpaque() { return true; }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if(this instanceof IVariantBlock && state.getValue(IVariantBlock.VARIANT))
            return Shapes.empty();
        if(this.isBlockOpaque(state))
            return super.getOcclusionShape(state, level, pos);
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }

    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, @Nonnull HitResult target, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nonnull Player player) {
        ItemStack result = super.getCloneItemStack(state,target,level,pos,player);
        if(player.isCrouching() && this instanceof IVariantBlock vb && level.getBlockEntity(pos) instanceof IVariantSupportingBlockEntity be)
            IVariantSupportingBlockEntity.copyDataToItem(be,result);
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if(this instanceof IVariantBlock block && stack.has(ModDataComponents.MODEL_VARIANT))
        {
            ResourceLocation variantID = stack.get(ModDataComponents.MODEL_VARIANT);
            ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
            if(variant != null)
            {
                tooltip.add(LCText.TOOLTIP_MODEL_VARIANT_NAME.get(variant.getName().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
                if(variant.has(VariantProperties.TOOLTIP_INFO))
                {
                    TooltipInfo extraTooltip = variant.get(VariantProperties.TOOLTIP_INFO);
                    if(extraTooltip.drawOnItem)
                        tooltip.addAll(extraTooltip.getTooltip());
                }
            }
            if(tooltipFlag.isAdvanced())
                tooltip.add(LCText.TOOLTIP_MODEL_VARIANT_ID.get(variantID.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if(stack.has(ModDataComponents.VARIANT_LOCK))
            tooltip.add(LCText.TOOLTIP_MODEL_VARIANT_LOCKED.getWithStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack,context,tooltip,tooltipFlag);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        this.tryCopyVariant(level,pos,stack);
    }

    protected final void tryCopyVariant(Level level, BlockPos pos, ItemStack stack)
    {
        if(level.isClientSide)
            return;
        if(this instanceof IVariantBlock && level.getBlockEntity(pos) instanceof IVariantSupportingBlockEntity be)
            be.setVariant(stack.getOrDefault(ModDataComponents.MODEL_VARIANT,null),stack.has(ModDataComponents.VARIANT_LOCK));
    }

}
