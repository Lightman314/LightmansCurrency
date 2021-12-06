package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TickableBlockEntity extends BlockEntity{

	private static final List<TickableBlockEntity> TICKING_ENTITIES = Lists.newArrayList();
	
	protected TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type,pos, state);
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		TICKING_ENTITIES.remove(this);
	}
	
	@Override
	public void clearRemoved() {
		super.clearRemoved();
		TICKING_ENTITIES.add(this);
	}
	
	public void clientTick() { }
	public void tick() { }
	public void serverTick() { }
	
	public static void tickHandler(Level level, BlockPos pos, BlockState state, TickableBlockEntity blockEntity) {
		if(level.isClientSide)
			blockEntity.clientTick();
		else
			blockEntity.serverTick();
		blockEntity.tick();
	}
	
}
