package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PaygateBlock extends TraderBlockRotatable {

	public static final IntegerProperty POWER_LEVEL = BlockStateProperties.LEVEL;
	
	public PaygateBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(POWER_LEVEL, 0)
		);
	}

	@Nonnull
	@Override
	protected ItemInteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
		if(!level.isClientSide)
		{
			if(level.getBlockEntity(pos) instanceof PaygateBlockEntity paygate)
			{
				int tradeIndex = paygate.getValidTicketTrade(player,stack);
				if(tradeIndex >= 0)
				{
					PaygateTraderData trader = paygate.getTraderData();
					if(trader != null && trader.TryExecuteTrade(TradeContext.create(trader,player).build(),tradeIndex).isSuccess())
						return ItemInteractionResult.SUCCESS;
				}
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWER_LEVEL);
    }
	
	@Override
	public boolean isSignalSource(@Nonnull BlockState state) { return true; }
	
	@Override
	public int getSignal(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Direction dir) {
		if(level.getBlockEntity(pos) instanceof PaygateBlockEntity be)
		{
			//Use opposite side as the direction input is relative to the requestor
			Direction relativeSide = IRotatableBlock.getRelativeSide(this.getFacing(state),dir.getOpposite());
			if(be.allowOutputSide(relativeSide))
				return state.getValue(POWER_LEVEL);
		}
		return 0;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_PAYGATE.asTooltip());
		super.appendHoverText(stack, context, tooltip, flagIn);
	}

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new PaygateBlockEntity(pos, state); }

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.PAYGATE.get(); }
	
}
