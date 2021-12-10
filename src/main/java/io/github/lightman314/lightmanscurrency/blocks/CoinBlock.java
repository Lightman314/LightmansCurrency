package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

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
	public void onLand(Level level, BlockPos pos, BlockState fallingState, BlockState hitState, FallingBlockEntity fallingBlock) {
		
		if(!level.isClientSide)
		{
			//Set the block as air
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			//Spawn the coins
			for(int i = 0; i < getCoinCount(); i++)
			{
				Block.popResource(level, pos, new ItemStack(coinItem));
			}
			//Play the breaking sound
			level.playSound(null, pos, this.getBreakingSound(), SoundSource.BLOCKS, 1f, 1f);
		}
		
	}
	
}
