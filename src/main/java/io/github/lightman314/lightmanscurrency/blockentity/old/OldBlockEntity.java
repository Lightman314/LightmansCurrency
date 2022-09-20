package io.github.lightman314.lightmanscurrency.blockentity.old;

import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OldBlockEntity extends TickableBlockEntity {

	private CompoundTag oldTag = null;
	
	protected OldBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type,pos,state); }
	
	@Override
	public void load(CompoundTag compound) { this.oldTag = compound; }
	
	@Override
	public void serverTick() {
		if(this.level != null && this.oldTag != null)
		{
			BlockEntity newBE = this.createReplacement(this.oldTag);
			this.level.setBlockEntity(newBE);
		}
	}
	
	protected abstract BlockEntity createReplacement(CompoundTag compound);
	
}