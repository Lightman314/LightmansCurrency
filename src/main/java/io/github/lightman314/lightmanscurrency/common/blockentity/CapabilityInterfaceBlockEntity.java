package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;

public class CapabilityInterfaceBlockEntity extends BlockEntity {
	
	public CapabilityInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get(), pos, state);
	}

	public static <T,C> void easyRegisterCapProvider(@Nonnull RegisterCapabilitiesEvent event, @Nonnull BlockCapability<T,C> capability)
	{
		event.registerBlock(capability, (level,pos,state,be,context) -> {
			if(state.getBlock() instanceof ICapabilityBlock handlerBlock)
			{
				BlockPos newPos = handlerBlock.getCapabilityBlockPos(state, level, pos);
				if(newPos.equals(pos) || level.getBlockEntity(newPos) instanceof CapabilityInterfaceBlockEntity)
					return null;
				return level.getCapability(capability,newPos,context);
			}
			return null;
		}, BlockEntityBlockHelper.getBlocksForBlockEntities(BlockEntityBlockHelper.CAPABILITY_INTERFACE_TYPE));
	}

}
