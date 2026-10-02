package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import com.mojang.datafixers.util.Function3;
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

public abstract class TallWideRotatableTraderBlock extends TallRotatableTraderBlock implements IWideBlock {

    private final Function3<Direction,Boolean,Boolean,VoxelShape> shape;
    public TallWideRotatableTraderBlock(Properties properties) { this(properties,ShapeHelper.TALL_WIDE_BOX); }
    public TallWideRotatableTraderBlock(Properties properties,Function3<Direction,Boolean,Boolean,VoxelShape> shape) { super(properties); this.shape = shape; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ISLEFT);
    }

    @Override
    protected BlockState initializeDefaultState(BlockState state) {
        return super.initializeDefaultState(state).setValue(ISLEFT,true);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape.apply(this.getFacing(state),this.isBottomBlock(state),this.isLeftBlock(state));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(ISLEFT,true);
    }

    @Override
    protected void removeOtherParts(LevelAccessor level, BlockPos pos, BlockState state) {
        boolean otherHeightState = !state.getValue(ISBOTTOM);
        boolean otherSideState = !state.getValue(ISLEFT);
        BlockPos otherHeight = this.getOtherHeight(pos,state);
        this.deleteBlock(level,otherHeight,state.setValue(ISBOTTOM,otherHeightState));
        BlockPos otherSide = this.getOtherSide(pos,state);
        this.deleteBlock(level,otherSide,state.setValue(ISLEFT,otherSideState));
        BlockPos otherHeightAndSide = this.getOtherHeight(otherSide,state);
        this.deleteBlock(level,otherHeightAndSide,state.setValue(ISBOTTOM,otherHeightState).setValue(ISLEFT,otherSideState));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        BlockPos topLeftPos = pos.above();
        BlockPos rightPos = this.getRightBlock(pos,state);
        BlockPos topRightPos = rightPos.above();
        if(this.isReplaceable(level,topLeftPos) && this.isReplaceable(level,rightPos) && this.isReplaceable(level,topRightPos)) {
            Direction facing = this.getFacing(state);
            level.setBlockAndUpdate(topLeftPos,state.setValue(FACING,facing).setValue(ISBOTTOM,false).setValue(ISLEFT,true));
            level.setBlockAndUpdate(rightPos,state.setValue(FACING,facing).setValue(ISBOTTOM,true).setValue(ISLEFT,false));
            level.setBlockAndUpdate(topRightPos,state.setValue(FACING,facing).setValue(ISBOTTOM,false).setValue(ISLEFT,false));
        }
        else {
            //Failed placing the top block. Abort placement
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            if(by instanceof Player p)
                p.getInventory().placeItemBackInInventory(stack.copyWithCount(1));
            return;
        }
        this.baseSetPlacedBy(level,pos,state,by,stack);
    }
}