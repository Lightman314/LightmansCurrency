package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.CoinJarTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CoinJarBlock extends RotatableBlock {

	public CoinJarBlock(Properties properties)
	{
		super(properties);
	}
	
	public CoinJarBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new CoinJarTileEntity();
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if(tileEntity instanceof CoinJarTileEntity)
		{
			CoinJarTileEntity jarEntity = (CoinJarTileEntity)tileEntity;
			jarEntity.readItemTag(stack);
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote())
		{
			ItemStack coinStack = getPlayersHeldCoin(playerEntity);
			if(coinStack.isEmpty())
				return ActionResultType.SUCCESS;
			//Add coins to the bank
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof CoinJarTileEntity)
			{
				CoinJarTileEntity jarEntity = (CoinJarTileEntity)tileEntity;
				if(jarEntity.addCoin(coinStack))
					coinStack.shrink(1);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	private static ItemStack getPlayersHeldCoin(PlayerEntity player)
	{
		//Get hotbar item
		if(MoneyUtil.isCoin(player.inventory.getCurrentItem(), false))
			return player.inventory.getCurrentItem();
		else if(MoneyUtil.isCoin(player.inventory.offHandInventory.get(0), false))
			return player.inventory.offHandInventory.get(0);
		return ItemStack.EMPTY;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if(tileEntity instanceof CoinJarTileEntity)
		{
			CoinJarTileEntity jarEntity = (CoinJarTileEntity)tileEntity;
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.inventory.getCurrentItem()) > 0)
			{
				//Drop the item for this block, with the JarData in it.
				@SuppressWarnings("deprecation")
				ItemStack dropStack = new ItemStack(Item.getItemFromBlock(this), 1);
				if(jarEntity.getStorage().size() > 0)
					jarEntity.writeItemTag(dropStack);
				Block.spawnAsEntity(worldIn, pos, dropStack);
			}
			else
			{
				//Only drop the coins within the jar
				jarEntity.getStorage().forEach(coin -> Block.spawnAsEntity(worldIn, pos, coin));
			}
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}
	
}
