package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.blockentity.interfaces.tickable.IClientTicker;
import io.github.lightman314.lightmanscurrency.blockentity.interfaces.tickable.IServerTicker;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public abstract class TickableBlockEntity extends EasyBlockEntity implements IClientTicker, IServerTicker, IEasyTickable {

	//Should inherit IClientTicker, IServerTicker, and/or IEasyTickable instead of extending this class
	@Deprecated
	protected TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type,pos, state);
	}


	public static <A extends BlockEntity> BlockEntityTicker<A> createTicker(@NotNull Level level, @NotNull BlockState state, BlockEntityType<A> type)
	{
		A be = type.create(BlockPos.ZERO, state);
		if(be instanceof IEasyTickable)
		{
			if(level.isClientSide && be instanceof IClientTicker)
				return TickableBlockEntity::clientTicker2;
			if(!level.isClientSide && be instanceof IServerTicker)
				return TickableBlockEntity::serverTicker2;
			return TickableBlockEntity::commonTicker;
		}
		else
		{
			if(level.isClientSide && be instanceof IClientTicker)
				return TickableBlockEntity::clientTicker1;
			if(!level.isClientSide && be instanceof IServerTicker)
				return TickableBlockEntity::serverTicker1;
		}
		return null;
	}

	private static <T extends BlockEntity> void commonTicker(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		((IEasyTickable)blockEntity).tick();
	}

	private static <T extends BlockEntity> void clientTicker1(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		((IClientTicker)blockEntity).clientTick();
	}

	private static <T extends BlockEntity> void clientTicker2(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		((IClientTicker)blockEntity).clientTick();
		((IEasyTickable)blockEntity).tick();
	}

	private static <T extends BlockEntity> void serverTicker1(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		((IServerTicker)blockEntity).serverTick();
	}

	private static <T extends BlockEntity> void serverTicker2(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		((IServerTicker)blockEntity).serverTick();
		((IEasyTickable)blockEntity).tick();
	}

	@Deprecated
	public static <T extends BlockEntity> void tickHandler(Level level, BlockPos ignored1, BlockState ignored2, T blockEntity) {
		if(level.isClientSide && blockEntity instanceof IClientTicker ct)
			ct.clientTick();
		else if(!level.isClientSide && blockEntity instanceof IServerTicker st)
			st.serverTick();
		if(blockEntity instanceof IEasyTickable et)
			et.tick();
	}

	@Override
	public void clientTick() {}
	@Override
	public void serverTick() {}
	@Override
	public void tick() {}
	
}
