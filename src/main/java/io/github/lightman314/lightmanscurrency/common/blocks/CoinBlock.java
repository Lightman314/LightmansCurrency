package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoinBlock extends FallingBlock {
	
	public CoinBlock(Properties properties) { super(properties); }

	protected boolean isFullBlock() { return true; }

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
		
		if(level instanceof ServerLevel sl)
		{
			//Set the block as air
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			//Spawn the coins
			LootTable lootTable = this.getLootTable(level,fallingState);
			if(lootTable != null)
			{
				LootParams params = new LootParams.Builder(sl).create(LootContextParamSets.EMPTY);
				for(ItemStack item : lootTable.getRandomItems(params))
					Block.popResource(level,pos,item);
			}
			//Play the breaking sound
			level.playSound(null, pos, this.getBreakingSound(), SoundSource.BLOCKS, 1f, 1f);
		}
		
	}

	@Nullable
	protected LootTable getLootTable(@Nonnull Level level, @Nonnull BlockState state)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.getLootData().getLootTable(this.getLootTableLocation(state));
		return null;
	}
	@Nonnull
	protected ResourceLocation getLootTableLocation(@Nonnull BlockState state)
	{
		ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		return blockID.withPrefix("blocks/falling/");
	}

}
