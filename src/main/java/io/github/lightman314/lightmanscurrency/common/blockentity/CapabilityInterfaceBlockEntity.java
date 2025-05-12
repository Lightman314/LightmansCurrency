package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityInterfaceBlockEntity extends BlockEntity implements IVariantSupportingBlockEntity {
	
	public CapabilityInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get(), pos, state);
	}

	@Nullable
	@Override
	public ResourceLocation getCurrentVariant()
	{
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
	public void setVariant(@Nullable ResourceLocation variant)
	{
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
	public final <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side)
	{
		BlockState state = this.getBlockState();
		if(state.getBlock() instanceof ICapabilityBlock handlerBlock)
		{
			BlockPos newPos = handlerBlock.getCapabilityBlockPos(state,this.level,this.worldPosition);
			if(newPos.equals(this.worldPosition))
				return super.getCapability(cap,side);
			BlockEntity be = this.level.getBlockEntity(newPos);
			if(be instanceof CapabilityInterfaceBlockEntity)
				return super.getCapability(cap,side);
			return be.getCapability(cap,side);
		}
		return super.getCapability(cap, side);
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
