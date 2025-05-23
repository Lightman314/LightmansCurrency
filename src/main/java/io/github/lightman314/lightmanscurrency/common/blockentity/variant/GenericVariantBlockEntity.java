package io.github.lightman314.lightmanscurrency.common.blockentity.variant;

import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class GenericVariantBlockEntity extends EasyBlockEntity {

    public GenericVariantBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.GENERIC_VARIANT.get(), pos, state); }

}
