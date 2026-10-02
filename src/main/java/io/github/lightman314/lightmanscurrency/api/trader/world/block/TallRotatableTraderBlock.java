package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.api.world.block.ShapeHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
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

public abstract class TallRotatableTraderBlock extends RotatableTraderBlock implements ITallBlock {

    private final BiFunction<Direction,Boolean,VoxelShape> shape;
    public TallRotatableTraderBlock(Properties properties) { this(properties, ShapeHelper.TALL_BOX); }
    public TallRotatableTraderBlock(Properties properties,VoxelShape shape) { this(properties, ShapeHelper.tallSingleShape(shape)); }
    public TallRotatableTraderBlock(Properties properties,BiFunction<Direction,Boolean,VoxelShape> shape) {
        super(properties);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if(this.isReplaceable(level,pos.above()))
            level.setBlockAndUpdate(pos.above(),state.setValue(ISBOTTOM,false).setValue(FACING,this.getFacing(state)));
        else {
            //Failed placing the top block. Abort placement
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            if(by instanceof Player p)
                p.getInventory().placeItemBackInInventory(stack.copyWithCount(1));
            return;
        }
        super.setPlacedBy(level, pos, state, by, stack);
    }

    protected final void baseSetPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity by,ItemStack stack) {
        super.setPlacedBy(level,pos,state,by,stack);
    }

    @Override
    protected void removeOtherParts(LevelAccessor level, BlockPos pos, BlockState state) {
        boolean otherHeightState = !state.getValue(ISBOTTOM);
        this.deleteBlock(level,this.getOtherHeight(pos,state),state.setValue(ISBOTTOM,otherHeightState));
    }

}
