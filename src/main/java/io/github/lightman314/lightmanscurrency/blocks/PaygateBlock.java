package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class PaygateBlock extends RotatableBlock implements EntityBlock{
	
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	
	public PaygateBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(POWERED, false)
		);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.PAYGATE, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new PaygateBlockEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof PaygateBlockEntity)
			{
				PaygateBlockEntity trader = (PaygateBlockEntity)blockEntity;
				//Update the owner
				if(trader.isOwner(player))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(player);
				}
				TileEntityUtil.sendUpdatePacket(blockEntity);
				NetworkHooks.openGui((ServerPlayer)player, new SimpleMenuProvider((windowId, inventory, playerEntity) -> new PaygateContainer(windowId, inventory, trader), new TranslatableComponent("")), pos);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			PaygateBlockEntity blockEntity = (PaygateBlockEntity)level.getBlockEntity(pos);
			if(blockEntity != null)
			{
				blockEntity.setOwner(player);
				if(stack.hasCustomHoverName())
					blockEntity.setCustomName(stack.getDisplayName());
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof PaygateBlockEntity)
		{
			PaygateBlockEntity tileEntity = (PaygateBlockEntity)blockEntity;
			List<ItemStack> coins = MoneyUtil.getCoinsOfValue(tileEntity.getStoredMoney());
			InventoryUtil.dumpContents(level, pos, coins);
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
	
	public boolean isSignalSource(BlockState state) {
		return true;
	}
	
	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
		
		if(state.getValue(POWERED))
			return Redstone.SIGNAL_MAX;
		return Redstone.SIGNAL_NONE;
		
	}
	
	
	
}
