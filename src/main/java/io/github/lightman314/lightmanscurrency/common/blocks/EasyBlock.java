package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack result = super.getCloneItemStack(state, target, level, pos, player);

        if (player.isCrouching() && this instanceof IVariantBlock vb)
            IVariantBlock.copyDataToItem(IVariantDataStorage.get(level,pos),result);
        return result;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        this.tryCopyVariant(level,pos,stack);
    }

    protected final void tryCopyVariant(Level level, BlockPos pos, ItemStack stack)
    {
        if(level.isClientSide)
            return;
        if(this instanceof IVariantBlock)
            IVariantBlock.copyDataFromItem(IVariantDataStorage.get(level,pos),stack);
    }

}
