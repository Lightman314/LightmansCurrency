package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PaygateBlock extends TraderBlockRotatable {
	
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	
	public PaygateBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(POWERED, false)
		);
	}
	
	@Override
	public @Nonnull ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
		{
			//Get the item in the players hand
			TileEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof PaygateBlockEntity)
			{
				PaygateBlockEntity paygate = (PaygateBlockEntity)tileEntity;
				int tradeIndex = paygate.getValidTicketTrade(player, player.getItemInHand(hand));
				if(tradeIndex >= 0)
				{
					PaygateTraderData trader = paygate.getTraderData();
					if(trader != null)
					{
						trader.ExecuteTrade(TradeContext.create(trader, player).build(), tradeIndex);
						return ActionResultType.SUCCESS;
					}
				}
			}
		}
		return super.use(state, level, pos, player, hand, result);
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
	
	@Override
	public boolean isSignalSource(@Nonnull BlockState state)
	{
		return true;
	}
	
	@Override
	public int getSignal(BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull Direction dir) {
		
		if(state.getValue(POWERED))
			return 15;
		return 0;
		
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.PAYGATE);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	@Override
	protected TileEntity makeTrader() { return new PaygateBlockEntity(); }
	
}