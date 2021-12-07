package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DummyBlockEntity extends BlockEntity{

	public DummyBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.DUMMY, pos, state);
	}
	
}
