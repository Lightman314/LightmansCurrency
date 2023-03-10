package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TicketTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

public class TicketKioskBlock extends TraderBlockTallRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 4;
	
	private static final VoxelShape HORIZ_SHAPE = box(3d,0d,1d,13d,32d,15d);
	private static final VoxelShape VERT_SHAPE = box(1d,0d,3d,15d,32d,13d);
	
	public TicketKioskBlock(Properties properties)
	{
		super(properties, LazyShapes.lazyTallDirectionalShape(VERT_SHAPE, HORIZ_SHAPE, VERT_SHAPE, HORIZ_SHAPE));
	}
	
	@Override
	public TileEntity makeTrader() { return new TicketTraderBlockEntity(TRADECOUNT); }

	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleBlock) { return new ArrayList<>(); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state) { return new ArrayList<>(); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex() { return -1; }
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER_TICKET; }
	
}