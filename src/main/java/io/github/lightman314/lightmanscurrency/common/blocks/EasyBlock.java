package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }
}
