package io.github.lightman314.lightmanscurrency.common.blocks.util;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Deprecated //Replaced by TickableBlockEntity#createTicker, and implemented by IEasyEntityBlock
public class TickerUtil {

	@SuppressWarnings("unchecked")
	@Nullable
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<?> type1, BlockEntityType<?> type2, BlockEntityTicker<? super E> ticker)
	{
		return type1 == type2 ? (BlockEntityTicker<A>)ticker : null;
	}
	
}
