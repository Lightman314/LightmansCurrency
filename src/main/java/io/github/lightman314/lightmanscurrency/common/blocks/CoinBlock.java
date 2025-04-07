package io.github.lightman314.lightmanscurrency.common.blocks;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoinBlock extends FallingBlock {
	
	public CoinBlock(Properties properties) { super(properties); }

	protected boolean isFullBlock() { return true; }

	protected CoinBlock build(Properties p) { return new CoinBlock(p); }

	@Nonnull
	@Override
	protected MapCodec<? extends FallingBlock> codec() { return simpleCodec(this::build); }

	@Nonnull
	@Override
	public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
		if(this.isFullBlock())
			return super.getOcclusionShape(state, level, pos);
		return Shapes.empty();
	}

	protected SoundEvent getBreakingSound() { return ModSounds.COINS_CLINKING.get(); }
	
	@Override
	public void onLand(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState fallingState, @Nonnull BlockState hitState, @Nonnull FallingBlockEntity fallingBlock) {
		
		if(level instanceof ServerLevel sl)
		{
			//Set the block as air
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			//Spawn the items
			LootTable lootTable = this.getLootTable(level, fallingState);
			if(lootTable != null)
			{
				LootParams params = new LootParams.Builder(sl).create(LootContextParamSets.EMPTY);
				for(ItemStack item : lootTable.getRandomItems(params))
					Block.popResource(level, pos, item);
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
			return server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,this.getLootTableLocation(state)));
		return null;
	}

	@Nonnull
	protected ResourceLocation getLootTableLocation(@Nonnull BlockState state)
	{
		ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return blockID.withPrefix("blocks/falling/");
	}
	
}
