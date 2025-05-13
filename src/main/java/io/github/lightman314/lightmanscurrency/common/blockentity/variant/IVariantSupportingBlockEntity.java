package io.github.lightman314.lightmanscurrency.common.blockentity.variant;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVariantSupportingBlockEntity {

    @Nonnull
    BlockState getBlockState();

    @Nullable
    Level getLevel();

    @Nonnull
    BlockPos getBlockPos();

    @Nullable
    ResourceLocation getCurrentVariant();

    void setVariant(@Nullable ResourceLocation variant);

}
