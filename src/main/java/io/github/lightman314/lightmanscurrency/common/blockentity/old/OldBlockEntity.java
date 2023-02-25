package io.github.lightman314.lightmanscurrency.common.blockentity.old;

import io.github.lightman314.lightmanscurrency.common.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.tickable.IServerTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@Deprecated
public abstract class OldBlockEntity extends EasyBlockEntity implements IServerTicker {

	private CompoundTag oldTag = null;
	
	protected OldBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type,pos,state); }
	
	@Override
	public void load(@NotNull CompoundTag compound) { this.oldTag = compound; }
	
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
