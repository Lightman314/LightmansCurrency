package io.github.lightman314.lightmanscurrency.common.blocks.variant;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.GenericVariantBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utility interface that allows blocks without block entities to easily support variant rendering
 */
@ParametersAreNonnullByDefault
public interface IEasyVariantBlock extends EntityBlock, IVariantBlock {

    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (this) {
            case IWideBlock wb when !wb.getIsLeft(state) -> new CapabilityInterfaceBlockEntity(pos,state);
            case IDeepBlock db when !db.getIsFront(state) -> new CapabilityInterfaceBlockEntity(pos,state);
            case ITallBlock tb when !tb.getIsBottom(state) -> new CapabilityInterfaceBlockEntity(pos,state);
            default -> new GenericVariantBlockEntity(pos, state);
        };
    }

}
