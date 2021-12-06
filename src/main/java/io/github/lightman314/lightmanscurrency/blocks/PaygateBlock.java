package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class PaygateBlock extends RotatableBlock implements EntityBlock, IOwnableBlock{
	
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
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new PaygateBlockEntity(pos, state);
	}
	
	@Override
	public boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof PaygateBlockEntity)
		{
			PaygateBlockEntity paygate = (PaygateBlockEntity)blockEntity;
			return paygate.canBreak(player);
		}
		return true;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			
			//Get the item in the players hand
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof PaygateBlockEntity)
			{
				PaygateBlockEntity paygate = (PaygateBlockEntity)tileEntity;
				//Update the owner
				if(paygate.isOwner(player))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					paygate.setOwner(player);
				}
				if(!paygate.isActive() && paygate.validTicket(player.getItemInHand(hand)))
				{
					paygate.activate();
					player.getItemInHand(hand).shrink(1);
					//Attempt to give the player a ticket stub
					ItemStack ticketStub = new ItemStack(ModItems.TICKET_STUB);
					if(!player.addItem(ticketStub))
					{
						InventoryUtil.dumpContents(level, player.blockPosition(), ImmutableList.of(ticketStub));
					}
					return InteractionResult.SUCCESS;
				}
				TileEntityUtil.sendUpdatePacket(tileEntity);
				NetworkHooks.openGui((ServerPlayer)player, paygate, pos);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof PaygateBlockEntity)
			{
				PaygateBlockEntity paygate = (PaygateBlockEntity)blockEntity;
				paygate.setOwner(player);
				if(stack.hasCustomHoverName())
					paygate.setCustomName(stack.getDisplayName());
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof PaygateBlockEntity)
		{
			PaygateBlockEntity paygate = (PaygateBlockEntity)blockEntity;
			if(!paygate.canBreak(player))
				return;
			List<ItemStack> coins = MoneyUtil.getCoinsOfValue(paygate.getStoredMoney());
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
	
	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}
	
	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
		
		if(state.getValue(POWERED))
			return 15;
		return 0;
		
	}
	
}
