package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaMachineBlockEntity extends TraderBlockEntity<GachaTrader> {

    private int color = -1;

    public GachaMachineBlockEntity(BlockPos pos, BlockState state) { this(pos,state,0xFFFFFF); }
    public GachaMachineBlockEntity(BlockPos pos, BlockState state, int color) { super(ModBlockEntities.GACHA_MACHINE.get(), pos, state); }

    @Nonnull
    @Override
    protected GachaTrader buildNewTrader() { return new GachaTrader(this.level,this.worldPosition,this.color); }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        super.saveAdditional(compound, lookup);
        compound.putInt("Color",this.color);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        super.loadAdditional(compound, lookup);
        this.color = compound.getInt("Color");
    }

    @Nullable
    @Override
    protected GachaTrader castOrNullify(TraderData trader) {
        if(trader instanceof GachaTrader gt)
            return gt;
        return null;
    }

}
