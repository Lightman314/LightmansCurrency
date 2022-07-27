package io.github.lightman314.lightmanscurrency.blocks.networktraders;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.networktraders.templates.NetworkTraderBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemTraderServerBlock extends NetworkTraderBlock {

	public static final int SMALL_SERVER_COUNT = 3;
	public static final int MEDIUM_SERVER_COUNT = 6;
	public static final int LARGE_SERVER_COUNT = 12;
	public static final int EXTRA_LARGE_SERVER_COUNT = 16;
	
	final int tradeCount;
	
	public ItemTraderServerBlock(Properties properties, int tradeCount)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalItemTraderBlockEntity(pos, state, this.tradeCount);
	}

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, UniversalTraderData trader) { }
	
}
