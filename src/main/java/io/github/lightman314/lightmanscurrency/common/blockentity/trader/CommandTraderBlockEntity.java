package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandTraderBlockEntity extends TraderBlockEntity<CommandTrader> {

    public CommandTraderBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COMMAND_TRADER.get(),pos,state); }
    protected CommandTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Nonnull
    @Override
    protected CommandTrader buildNewTrader() { return new CommandTrader(this.level,this.worldPosition); }

    @Nullable
    @Override
    protected CommandTrader castOrNullify(@Nonnull TraderData trader) {
        if(trader instanceof CommandTrader ct)
            return ct;
        return null;
    }

}