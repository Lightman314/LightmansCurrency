package io.github.lightman314.lightmanscurrency.features.cash_register;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.world.block.RotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CashRegisterBlock extends RotatableBlock implements EntityBlock {

    public static final VoxelShape SHAPE = box(1d,0d,1d,15d,10d,15d);

    public static final MultiLineTextEntry TOOLTIP = MultiLineTextEntry.tooltip(LCApi.MODID,"cash_register");

    public CashRegisterBlock(Properties properties) { super(properties,SHAPE); }
    public CashRegisterBlock(Properties properties,VoxelShape shape) { super(properties,shape); }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new CashRegisterBlockEntity(worldPosition,blockState); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof CashRegisterBlockEntity be)
            be.tryOpenMenu(player);
        return InteractionResult.SUCCESS;
    }

}
