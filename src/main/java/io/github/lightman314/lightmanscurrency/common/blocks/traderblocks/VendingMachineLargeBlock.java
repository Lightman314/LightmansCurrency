package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IDeprecatedBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.TallRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallWideRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;

public class VendingMachineLargeBlock extends TraderBlockTallWideRotatable implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 12;
	
	public VendingMachineLargeBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	public List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ITEM_TRADER.get()); }
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }

	public static class ReplaceMe extends VendingMachineLargeBlock implements IDeprecatedBlock
	{
		private final Color color;
		public ReplaceMe(Properties properties, Color color) { super(properties); this.color = color; }

		@Override
		public Block replacementBlock() { return ModBlocks.VENDING_MACHINE_LARGE.get(this.color); }

		@Override
		public void replaceBlock(Level level, BlockPos pos, BlockState oldState) {
			Block newBlock = this.replacementBlock();
			if(newBlock != null)
			{
				BlockState newState = newBlock.defaultBlockState().setValue(RotatableBlock.FACING, oldState.getValue(RotatableBlock.FACING)).setValue(TallRotatableBlock.ISBOTTOM, oldState.getValue(TallRotatableBlock.ISBOTTOM)).setValue(TraderBlockTallWideRotatable.ISLEFT, oldState.getValue(TraderBlockTallWideRotatable.ISLEFT));
				replaceTraderBlock(level, pos, newState);
			}
		}

	}

}
