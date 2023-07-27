package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class EasyBlockEntity extends BlockEntity implements IClientTracker {

    public EasyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public boolean isClient() { return this.level == null || this.level.isClientSide; }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

}
