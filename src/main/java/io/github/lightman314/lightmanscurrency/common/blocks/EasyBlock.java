package io.github.lightman314.lightmanscurrency.common.blocks;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EasyBlock extends Block {

    public EasyBlock(Properties properties) { super(properties); }

    protected boolean isBlockOpaque(BlockState state) { return this.isBlockOpaque(); }

    protected boolean isBlockOpaque() { return true; }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if(this.isBlockOpaque(state))
            return super.getOcclusionShape(state, level, pos);
        return Shapes.empty();
    }

}
