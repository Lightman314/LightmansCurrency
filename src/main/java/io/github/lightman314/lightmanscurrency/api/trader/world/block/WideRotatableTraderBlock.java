package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.api.world.block.ShapeHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IWideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public abstract class WideRotatableTraderBlock extends RotatableTraderBlock implements IWideBlock {

    private final BiFunction<Direction,Boolean, VoxelShape> shape;
    public WideRotatableTraderBlock(Properties properties) { this(properties, ShapeHelper.WIDE_BOX); }
    public WideRotatableTraderBlock(Properties properties,BiFunction<Direction,Boolean,VoxelShape> shape) { super(properties); this.shape = shape; }

    @Override
    protected BlockState initializeDefaultState(BlockState state) {
        return super.initializeDefaultState(state).setValue(ISLEFT,true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ISLEFT);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(ISLEFT,true);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape.apply(this.getFacing(state),this.isLeftBlock(state));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        BlockPos rightPos = this.getRightBlock(pos,state);
        if(this.isReplaceable(level,rightPos))
            level.setBlockAndUpdate(rightPos,state.setValue(ISLEFT,false).setValue(FACING,this.getFacing(state)));
        else {
            //Failed placing the top block. Abort placement
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            if(by instanceof Player p)
                p.getInventory().placeItemBackInInventory(stack.copyWithCount(1));
            return;
        }
        super.setPlacedBy(level,pos,state,by,stack);
    }

    protected final void baseSetPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity by,ItemStack stack) {
        super.setPlacedBy(level,pos,state,by,stack);
    }

    @Override
    protected void removeOtherParts(LevelAccessor level, BlockPos pos, BlockState state) {
        boolean otherSideState = !state.getValue(ISLEFT);
        this.deleteBlock(level,this.getOtherSide(pos,state),state.setValue(ISLEFT,otherSideState));
    }

}
