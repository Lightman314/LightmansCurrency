package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public abstract class TickableBlockEntity extends BlockEntity{

	private static final List<TickableBlockEntity> TICKING_ENTITIES = new ArrayList<>();
	
	protected TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		TICKING_ENTITIES.add(this);
	}
	
	@Override
	public void setRemoved()
	{
		super.setRemoved();
		TICKING_ENTITIES.remove(this);
	}
	
	public void clearRemoved() {
		super.clearRemoved();
		TICKING_ENTITIES.add(this);
	}
	
	public void clientTick() {}
	public void tick() {}
	public void serverTick() {}
	
	public static void tickHandler(Level level, BlockPos pos, BlockState state, TickableBlockEntity blockEntity)
	{
		if(level.isClientSide)
			blockEntity.clientTick();
		else
			blockEntity.serverTick();
		blockEntity.tick();
	}
	
}
