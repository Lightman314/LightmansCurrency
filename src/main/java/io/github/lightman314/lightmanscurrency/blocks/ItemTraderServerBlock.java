package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.tileentity.UniversalItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ItemTraderServerBlock extends RotatableBlock implements ITraderBlock{

	final int tradeCount;
	
	public ItemTraderServerBlock(Properties properties, int tradeCount)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new UniversalItemTraderTileEntity(this.tradeCount);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!worldIn.isRemote())
		{
			UniversalTraderTileEntity tileEntity = (UniversalTraderTileEntity)worldIn.getTileEntity(pos);
			if(tileEntity != null)
			{
				if(stack.hasDisplayName())
					tileEntity.init(player, stack.getDisplayName().getString());
				else
					tileEntity.init(player);
			}
		}
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		UniversalTraderTileEntity tileEntity = (UniversalTraderTileEntity)worldIn.getTileEntity(pos);
		if(tileEntity != null)
		{
			//LightmansCurrency.LOGGER.info("Testing if the player can break the server-block (block.onBlockHarvested," + (worldIn.isRemote ? "client" : "server") +").");
			if(!tileEntity.canBreak(player))
				return;
			//LightmansCurrency.LOGGER.info("Block can be broken. Running onDestroyed code (block.onBlockHarvested," + (worldIn.isRemote ? "client" : "server") +").");
			tileEntity.onDestroyed();
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote)
		{
			//Open the screen
			UniversalItemTraderTileEntity tileEntity = (UniversalItemTraderTileEntity)world.getTileEntity(pos);
			if(tileEntity != null)
			{
				//Update the owner
				if(tileEntity.isOwner(playerEntity))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					tileEntity.updateOwner(playerEntity);
					tileEntity.openStorageMenu(playerEntity);
				}
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
}
