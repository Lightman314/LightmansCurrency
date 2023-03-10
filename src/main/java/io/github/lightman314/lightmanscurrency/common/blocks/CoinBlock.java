package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CoinBlock extends FallingBlock {
	
	private final Supplier<IItemProvider> coinItem;
	
	public CoinBlock(Properties properties, Supplier<IItemProvider> coinItem)
	{
		super(properties);
		this.coinItem = coinItem;
	}
	
	protected int getCoinCount()
	{
		return 36;
	}
	
	protected SoundEvent getBreakingSound() { return ModSounds.COINS_CLINKING; }
	
	@Override
	public void onLand(World level, @Nonnull BlockPos pos, @Nonnull BlockState fallingState, @Nonnull BlockState hitState, @Nonnull FallingBlockEntity fallingBlock) {
		
		if(!level.isClientSide)
		{
			//Set the block as air
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			//Spawn the coins
			for(int i = 0; i < getCoinCount(); i++)
			{
				Block.popResource(level, pos, new ItemStack(this.coinItem.get()));
			}
			//Play the breaking sound
			level.playSound(null, pos, this.getBreakingSound(), SoundCategory.BLOCKS, 1f, 1f);
		}
		
	}
	
}
