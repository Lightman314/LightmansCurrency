package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CapabilityInterfaceBlockEntity extends BlockEntity implements IVariantDataStorage {
	
	public CapabilityInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get(), pos, state);
	}

	public static <T,C> void easyRegisterCapProvider(RegisterCapabilitiesEvent event, BlockCapability<T,C> capability)
	{
		event.registerBlockEntity(capability,ModBlockEntities.CAPABILITY_INTERFACE.get(),(be,context) -> {
			BlockState state = be.getBlockState();
            BlockPos pos = be.getBlockPos();
            if(state.getBlock() instanceof ICapabilityBlock capabilityBlock)
			{
                Level level = be.getLevel();
				BlockPos newPos = capabilityBlock.getCapabilityBlockPos(state,pos);
				if(newPos.equals(pos) || level.getBlockEntity(newPos) instanceof CapabilityInterfaceBlockEntity)
					return null;
				return level.getCapability(capability,newPos,context);
			}
			return null;
		});
	}

	@Nullable
	@Override
	public ResourceLocation getCurrentVariant() {
		AtomicReference<ResourceLocation> result = new AtomicReference<>(null);
		this.tryRunOnCoreBlockEntity(be -> {
            IVariantDataStorage storage = getVariantData(be);
            if(storage != null)
                result.set(storage.getCurrentVariant());
		});
		return result.get();
	}

	@Override
	public void setVariant(@Nullable ResourceLocation variant) {
		this.tryRunOnCoreBlockEntity(be -> {
            IVariantDataStorage storage = getVariantData(be);
            if(storage != null)
                storage.setVariant(variant);
		});
	}

	@Override
	public void setVariant(@Nullable ResourceLocation variant, boolean locked) {
		this.tryRunOnCoreBlockEntity(be -> {
            IVariantDataStorage storage = getVariantData(be);
            if(storage != null)
                storage.setVariant(variant,locked);
		});
	}

	@Override
	public boolean isVariantLocked() {
		AtomicBoolean result = new AtomicBoolean(false);
		this.tryRunOnCoreBlockEntity(be -> {
            IVariantDataStorage storage = getVariantData(be);
            if(storage != null)
                result.set(storage.isVariantLocked());
		});
		return result.get();
	}

    @Nullable
    private static IVariantDataStorage getVariantData(BlockEntity be)
    {
        if(be instanceof IVariantDataStorage storage)
            return storage;
        return IVariantDataStorage.get(be.getLevel(),be.getBlockPos());
    }

	@Nullable
	public final BlockEntity tryGetCoreBlockEntity()
	{
		BlockState state = this.getBlockState();
		if(state.getBlock() instanceof ICapabilityBlock block)
		{
			BlockPos newPos = block.getCapabilityBlockPos(state,this.worldPosition);
			if(newPos.equals(this.worldPosition))
				return null;
			BlockEntity be = this.level.getBlockEntity(newPos);
			if(be instanceof CapabilityInterfaceBlockEntity)
				return null;
			return be;
		}
		return null;
	}

	public final void tryRunOnCoreBlockEntity(Consumer<BlockEntity> consumer) {
		BlockEntity be = this.tryGetCoreBlockEntity();
		if(be != null)
			consumer.accept(be);
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
