package io.github.lightman314.lightmanscurrency.api.world.block;

import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class TallRotatableBlock extends RotatableBlock implements ITallBlock {

    private final BiFunction<Direction,Boolean,VoxelShape> shape;

    public TallRotatableBlock(Properties properties) { this(properties,EasyShapes.TALL_BOX); }
    public TallRotatableBlock(Properties properties, VoxelShape shape) { this(properties,EasyShapes.tallSingleShape(shape)); }
    public TallRotatableBlock(Properties properties, BiFunction<Direction,Boolean,VoxelShape> shape) {
        super(properties.pushReaction(PushReaction.BLOCK));
        this.shape = shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ISBOTTOM);
    }
    @Override
    protected BlockState initializeDefaultState(BlockState state) { return super.initializeDefaultState(state).setValue(ISBOTTOM,true); }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return this.shape.apply(this.getFacing(state),this.isBottomBlock(state)); }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) { return super.getStateForPlacement(context).setValue(ISBOTTOM,true); }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        if(this.isReplaceable(level,pos.above()))
            level.setBlockAndUpdate(pos.above(),this.defaultBlockState().setValue(ISBOTTOM,false).setValue(FACING,state.getValue(FACING)));
        else
        {
            //Failed placing the top block. Abort placement
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            if(by instanceof Player p)
                p.getInventory().placeItemBackInInventory(itemStack.copyWithCount(1));
            return;
        }
        super.setPlacedBy(level,pos,state,by,itemStack);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if(directionToNeighbour == Direction.UP && this.isBottomBlock(state) || directionToNeighbour == Direction.DOWN && this.isTopBlock(state))
        {
            if(neighbourState.is(this))
                return state;
            else //Other block is no longer this block, so the tall block has been broken
                return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state,level,ticks,pos,directionToNeighbour,neighbourPos,neighbourState,random);
    }
}
