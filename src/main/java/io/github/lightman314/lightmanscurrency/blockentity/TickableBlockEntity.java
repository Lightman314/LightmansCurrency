package io.github.lightman314.lightmanscurrency.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TickableBlockEntity extends BlockEntity{
	
	protected TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type,pos, state);
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
