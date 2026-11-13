package io.github.lightman314.lightmanscurrency.features.trader.paygate.blocks;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.RotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

public class PaygateBlock extends RotatableTraderBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PaygateBlock(Properties properties) { super(properties); }

    @Override
    protected BlockState initializeDefaultState(BlockState state) {
        return super.initializeDefaultState(state).setValue(POWERED,false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public TraderBlockEntity createBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new PaygateBlockEntity(worldPosition,blockState);
    }

    @Override
    protected boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(state.getValue(POWERED) && level.getBlockEntity(pos) instanceof PaygateBlockEntity be) {
            //Use opposite side as the direction input is relative to the requestor
            Direction relativeSide = IRotatableBlock.getRelativeSide(this.getFacing(state),direction.getOpposite());
            return be.getPowerLevel(relativeSide);
        }
        return 0;
    }

}
