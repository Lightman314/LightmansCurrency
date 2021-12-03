package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoinBlock extends FallingBlock{
	
	private final Item coinItem;
	
	public CoinBlock(Properties properties, Item coinItem)
	{
		super(properties);
		this.coinItem = coinItem;
	}
	
	protected int getCoinCount()
	{
		return 36;
	}
	
	protected SoundEvent getBreakingSound()
	{
		return CurrencySoundEvents.COINS_CLINKING;
	}
	
	@Override
	public void onEndFalling(World worldIn, BlockPos pos, BlockState fallingState, BlockState hitState, FallingBlockEntity fallingBlock) {
		
		if(!worldIn.isRemote)
		{
			//CurrencyMod.LOGGER.info("CoinBlock.onEndFalling Server-side");
			//Set the block as air
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			//Spawn the coins
			for(int i = 0; i < getCoinCount(); i++)
			{
				spawnAsEntity(worldIn, pos, new ItemStack(coinItem, 1));
			}
			//Play the breaking sound
			worldIn.playSound(null, pos, this.getBreakingSound(), SoundCategory.BLOCKS, 1f, 1f);
			
		}
			
	}
	
}
