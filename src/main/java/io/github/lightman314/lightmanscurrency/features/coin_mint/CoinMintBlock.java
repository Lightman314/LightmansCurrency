package io.github.lightman314.lightmanscurrency.features.coin_mint;

import io.github.lightman314.lightmanscurrency.api.world.block.RotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

import javax.annotation.Nullable;

public class CoinMintBlock extends RotatableBlock implements EntityBlock {

    public CoinMintBlock(Properties properties) { super(properties); }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CoinMintBlockEntity(worldPosition,blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            if(level.getBlockEntity(pos) instanceof CoinMintBlockEntity be)
                player.openMenu(be);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof CoinMintBlockEntity be)
            return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(be.getStorage());
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state,level,pos);
    }

}
