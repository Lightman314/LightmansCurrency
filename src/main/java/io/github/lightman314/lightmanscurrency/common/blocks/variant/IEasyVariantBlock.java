package io.github.lightman314.lightmanscurrency.common.blocks.variant;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
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
public interface IEasyVariantBlock extends EntityBlock, IVariantBlock, ICapabilityBlock {

    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(this instanceof IWideBlock wb && wb.getIsRight(state))
            return new CapabilityInterfaceBlockEntity(pos,state);
        if(this instanceof IDeepBlock db && db.getIsBack(state))
            return new CapabilityInterfaceBlockEntity(pos,state);
        if(this instanceof ITallBlock tb && tb.getIsTop(state))
            return new CapabilityInterfaceBlockEntity(pos,state);
        return new GenericVariantBlockEntity(pos,state);
    }

}