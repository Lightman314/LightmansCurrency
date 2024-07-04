package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotMachineTraderBlockEntity extends TraderBlockEntity<SlotMachineTraderData> {

    public SlotMachineTraderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLOT_MACHINE_TRADER.get(), pos, state);
    }

    @Nullable
    @Override
    protected SlotMachineTraderData castOrNullify(@Nonnull TraderData trader) {
        if(trader instanceof SlotMachineTraderData sm)
            return sm;
        return null;
    }

    @Nonnull
    @Override
    protected SlotMachineTraderData buildNewTrader() { return new SlotMachineTraderData(this.level, this.worldPosition); }

}
