package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShelfBlock extends TraderBlockRotatable implements IItemTraderBlock, IVariantBlock {


	protected final int tradeCount;
	
	private static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,16d,5d);
	private static final VoxelShape SHAPE_SOUTH = box(0d,0d,11d,16d,16d,16d);
	private static final VoxelShape SHAPE_EAST = box(11d,0d,0d,16d,16d,16d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,0d,5d,16d,16d);
	
	public ShelfBlock(Properties properties) { this(properties, 1); }
	public ShelfBlock(Properties properties, int tradeCount) { super(properties, LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST)); this.tradeCount = tradeCount; }

	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, this.tradeCount); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER.asTooltip(this.tradeCount); }
	
}
