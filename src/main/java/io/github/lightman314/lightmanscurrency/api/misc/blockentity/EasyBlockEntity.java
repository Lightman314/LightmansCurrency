package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class EasyBlockEntity extends BlockEntity implements IClientTracker {

    @Nonnull
    public final RegistryAccess registryAccess() { return this.level.registryAccess(); }

    public EasyBlockEntity(@Nonnull BlockEntityType<?> type, @Nonnull BlockPos pos, @Nonnull BlockState state) { super(type, pos, state); }

    @Override
    public boolean isClient() { return this.level == null || this.level.isClientSide; }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider lookup) { return this.saveWithoutMetadata(lookup); }

}
