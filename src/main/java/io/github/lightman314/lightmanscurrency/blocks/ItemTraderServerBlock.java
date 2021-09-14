package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemTraderServerBlock extends RotatableBlock implements EntityBlock{

	final int tradeCount;
	
	public ItemTraderServerBlock(Properties properties, int tradeCount)
	{
		super(properties, LazyShapes.BOX_T);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalItemTraderBlockEntity(pos, state, this.tradeCount);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			UniversalTraderBlockEntity blockEntity = (UniversalTraderBlockEntity)level.getBlockEntity(pos);
			if(blockEntity != null)
			{
				if(stack.hasCustomHoverName())
					blockEntity.init(player, stack.getDisplayName().getString());
				else
					blockEntity.init(player);
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		UniversalTraderBlockEntity traderEntity = (UniversalTraderBlockEntity)level.getBlockEntity(pos);
		if(traderEntity != null)
		{
			//LightmansCurrency.LOGGER.info("Testing if the player can break the server-block (block.onBlockHarvested," + (worldIn.isRemote ? "client" : "server") +").");
			if(!traderEntity.canBreak(player))
				return;
			//LightmansCurrency.LOGGER.info("Block can be broken. Running onDestroyed code (block.onBlockHarvested," + (worldIn.isRemote ? "client" : "server") +").");
			traderEntity.onDestroyed();
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open the screen
			UniversalItemTraderBlockEntity tileEntity = (UniversalItemTraderBlockEntity)level.getBlockEntity(pos);
			if(tileEntity != null)
			{
				//Update the owner
				if(tileEntity.isOwner(player))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					tileEntity.updateOwner(player);
					tileEntity.openStorageMenu(player);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}
	
}
