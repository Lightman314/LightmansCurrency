package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TicketTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TicketKioskBlock extends TraderBlockTallRotatable implements IItemTraderBlock, IVariantBlock {
	
	public static final int TRADECOUNT = 4;
	
	private static final VoxelShape HORIZ_SHAPE = box(3d,0d,1d,13d,32d,15d);
	private static final VoxelShape VERT_SHAPE = box(1d,0d,3d,15d,32d,13d);
	
	public TicketKioskBlock(Properties properties)
	{
		super(properties, LazyShapes.lazyTallDirectionalShape(VERT_SHAPE, HORIZ_SHAPE, VERT_SHAPE, HORIZ_SHAPE));
	}

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new TicketTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.TICKET_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER_TICKET.asTooltip(TRADECOUNT); }
	
}
