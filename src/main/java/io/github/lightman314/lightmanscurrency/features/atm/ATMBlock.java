package io.github.lightman314.lightmanscurrency.features.atm;

import io.github.lightman314.lightmanscurrency.api.world.block.TallRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ATMBlock extends TallRotatableBlock {

    public ATMBlock(Properties properties) { super(properties); }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            //Open the ATM Menu
            player.openMenu(ATMMenu.createProvider(this,pos));
        }
        return InteractionResult.SUCCESS;
    }

}
