package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CapabilityInterfaceBlockEntity extends BlockEntity implements IVariantSupportingBlockEntity {
	
	public CapabilityInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get(), pos, state);
	}

	@Nullable
	@Override
	public ResourceLocation getCurrentVariant()
	{
		AtomicReference<ResourceLocation> result = new AtomicReference<>(null);
		this.tryRunOnCoreBlockEntity(be -> {
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				result.set(vsbe.getCurrentVariant());
		});
		return result.get();
	}

	@Override
	public void setVariant(@Nullable ResourceLocation variant)
	{
		this.tryRunOnCoreBlockEntity(be -> {
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				vsbe.setVariant(variant);
		});
	}

	@Override
	public void setVariant(@Nullable ResourceLocation variant, boolean locked)
	{
		this.tryRunOnCoreBlockEntity(be -> {
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				vsbe.setVariant(variant,locked);
		});
	}

	@Override
	public boolean isVariantLocked() {
		AtomicBoolean result = new AtomicBoolean(false);
		this.tryRunOnCoreBlockEntity(be -> {
			if(be instanceof IVariantSupportingBlockEntity vsbe)
				result.set(vsbe.isVariantLocked());
		});
		return result.get();
	}

	@Nullable
	public final BlockEntity tryGetCoreBlockEntity()
	{
		BlockState state = this.getBlockState();
		if(state.getBlock() instanceof ICapabilityBlock block)
		{
			BlockPos newPos = block.getCapabilityBlockPos(state,this.level,this.worldPosition);
			if(newPos.equals(this.worldPosition))
				return null;
			BlockEntity be = this.level.getBlockEntity(newPos);
			if(be instanceof CapabilityInterfaceBlockEntity)
				return null;
			return be;
		}
		return null;
	}

	public final void tryRunOnCoreBlockEntity(Consumer<BlockEntity> consumer)
	{
		BlockEntity be = this.tryGetCoreBlockEntity();
		if(be != null)
			consumer.accept(be);
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
			if(be == null || be instanceof CapabilityInterfaceBlockEntity)
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
			if(VariantProvider.getVariantBlock(state.getBlock()) != null && !state.getValue(IVariantBlock.VARIANT))
				this.level.setBlockAndUpdate(this.worldPosition,state.setValue(IVariantBlock.VARIANT,true));
		}
		super.onLoad();
	}
}
