package io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public class ItemTraderInterfaceBlock extends TraderInterfaceBlock {

	public ItemTraderInterfaceBlock(Properties properties) { super(properties); }

	@Override
	protected TileEntity createBlockEntity(BlockState state) {
		return new ItemTraderInterfaceBlockEntity();
	}
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER_INTERFACE; }
	
	@Override
	protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderInterfaceBlockEntity trader) { }
	
}
