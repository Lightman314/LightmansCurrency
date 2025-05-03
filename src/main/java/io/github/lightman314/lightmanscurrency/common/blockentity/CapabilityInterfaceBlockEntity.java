package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityInterfaceBlockEntity extends BlockEntity implements IVariantSupportingBlockEntity {
	
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

	@Nullable
	@Override
	public ResourceLocation getCurrentVariant() {
		BlockState state = this.getBlockState();
		if(state.getBlock() instanceof ICapabilityBlock block)
		{
			BlockPos newPos = block.getCapabilityBlockPos(state,this.level,this.worldPosition);
			BlockEntity be = this.level.getBlockEntity(newPos);
			if(be instanceof CapabilityInterfaceBlockEntity)
				return null;
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				return vsbe.getCurrentVariant();
		}
		return null;
	}

	@Override
	public void setVariant(@Nullable ResourceLocation variant) {
		BlockState state = this.getBlockState();
		if(state.getBlock() instanceof ICapabilityBlock block)
		{
			BlockPos newPos = block.getCapabilityBlockPos(state,this.level,this.worldPosition);
			BlockEntity be = this.level.getBlockEntity(newPos);
			if(be instanceof CapabilityInterfaceBlockEntity)
				return;
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				vsbe.setVariant(variant);
		}
	}

	@Override
	public void onLoad() {
		if(this.getCurrentVariant() != null)
		{
			BlockState state = this.getBlockState();
			if(state.getBlock() instanceof IVariantBlock vb && !state.getValue(IVariantBlock.VARIANT))
				this.level.setBlockAndUpdate(this.worldPosition,state.setValue(IVariantBlock.VARIANT,true));
		}
		super.onLoad();
	}
}
