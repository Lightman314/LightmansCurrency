package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
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

import javax.annotation.Nonnull;

public class CoinJarBlock extends RotatableBlock{

	public CoinJarBlock(Properties properties)
	{
		super(properties);
	}
	
	public CoinJarBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader level)
	{
		return new CoinJarBlockEntity();
	}
	
	@Override
	public void setPlacedBy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		TileEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinJarBlockEntity)
		{
			CoinJarBlockEntity jar = (CoinJarBlockEntity)blockEntity;
			jar.readItemTag(stack);
		}
	}
	
	@Nonnull
	@Override
	public ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
		{
			ItemStack coinStack = player.getItemInHand(hand);
			if(!MoneyUtil.isCoin(coinStack, false))
				return ActionResultType.SUCCESS;
			//Add coins to the bank
			TileEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CoinJarBlockEntity)
			{
				CoinJarBlockEntity jar = (CoinJarBlockEntity)blockEntity;
				if(jar.addCoin(coinStack))
					coinStack.shrink(1);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void playerWillDestroy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TileEntity tileEntity = level.getBlockEntity(pos);
		if(tileEntity instanceof CoinJarBlockEntity)
		{
			CoinJarBlockEntity jarEntity = (CoinJarBlockEntity)tileEntity;
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player) > 0)
			{
				//Drop the item for this block, with the JarData in it.
				ItemStack dropStack = new ItemStack(Item.byBlock(this), 1);
				if(jarEntity.getStorage().size() > 0)
					jarEntity.writeItemTag(dropStack);
				Block.popResource(level, pos, dropStack);
			}
			else
			{
				//Only drop the coins within the jar
				jarEntity.getStorage().forEach(coin -> Block.popResource(level, pos, coin));
			}
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
}
