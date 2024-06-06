package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class CoinBlock extends FallingBlock {
	
	private final Supplier<Item> coinItem;
	
	public CoinBlock(Properties properties, Supplier<Item> coinItem)
	{
		super(properties);
		this.coinItem = coinItem;
	}

	protected boolean isFullBlock() { return true; }
	
	protected int getCoinCount() { return 36; }

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
		if(this.isFullBlock())
			return super.getOcclusionShape(state, level, pos);
		return Shapes.empty();
	}

	protected SoundEvent getBreakingSound() { return ModSounds.COINS_CLINKING.get(); }
	
	@Override
	public void onLand(Level level, @Nonnull BlockPos pos, @Nonnull BlockState fallingState, @Nonnull BlockState hitState, @Nonnull FallingBlockEntity fallingBlock) {
		
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
			level.playSound(null, pos, this.getBreakingSound(), SoundSource.BLOCKS, 1f, 1f);
		}
		
	}
	
}
