package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PaygateBlock extends TraderBlockRotatable implements IVariantBlock {
	
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
	public int modelsRequiringRotation() { return 2; }

	@Override
	public int getModelIndex(BlockState state) {
		if(state.getValue(POWERED))
			return 1;
		return 0;
	}
	
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Get the item in the players hand
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof PaygateBlockEntity paygate)
			{
				int tradeIndex = paygate.getValidTicketTrade(player, player.getItemInHand(hand));
				if(tradeIndex >= 0)
				{
					PaygateTraderData trader = paygate.getTraderData();
					if(trader != null)
					{
						trader.TryExecuteTrade(TradeContext.create(trader, player).build(), tradeIndex);
						return InteractionResult.SUCCESS;
					}
				}
			}
		}
		return super.use(state, level, pos, player, hand, result);
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean isSignalSource(@Nonnull BlockState state) { return true; }
	
	@Override
	@SuppressWarnings("deprecation")
	public int getSignal(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Direction dir) {
		if(state.getValue(POWERED) && level.getBlockEntity(pos) instanceof PaygateBlockEntity be)
		{
			//Use opposite side as the direction input is relative to the requestor
			Direction relativeSide = IRotatableBlock.getRelativeSide(this.getFacing(state),dir.getOpposite());
			if(be.allowOutputSide(relativeSide))
				return be.getPowerLevel(relativeSide);
		}
		return 0;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_PAYGATE.asTooltip());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new PaygateBlockEntity(pos, state); }

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.PAYGATE.get(); }
	
}
