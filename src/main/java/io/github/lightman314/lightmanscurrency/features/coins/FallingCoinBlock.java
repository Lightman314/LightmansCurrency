package io.github.lightman314.lightmanscurrency.features.coins;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.core.LCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;

public class FallingCoinBlock extends FallingBlock {

    public FallingCoinBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends FallingBlock> codec() { return simpleCodec(FallingCoinBlock::new); }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter level, BlockPos pos) { return 0; }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        //We don't need to animate dust particles for these
    }

    protected SoundEvent getBreakingSound() { return LCSounds.COINS_CLINKING.get(); }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replacedBlock, FallingBlockEntity entity) {
        if(level instanceof ServerLevel sl)
        {
            //Reset the block state at that position
            level.setBlockAndUpdate(pos,replacedBlock);
            //Spawn the items
            LootTable lootTable = this.getLootTable(sl,state);
            if(lootTable != null)
            {
                LootParams params = new LootParams.Builder(sl).create(LootContextParamSets.EMPTY);
                for(ItemStack item : lootTable.getRandomItems(params))
                    popResource(level,pos,item);
            }
            //Play the breaking sound
            level.playSound(null,pos,this.getBreakingSound(),SoundSource.BLOCKS,1f,1f);
        }
    }

    @Nullable
    protected LootTable getLootTable(ServerLevel level, BlockState state) { return level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,this.getLootTableLocation(state))); }

    protected Identifier getLootTableLocation(BlockState state) { return BuiltInRegistries.BLOCK.getKey(state.getBlock()).withPrefix("blocks/falling/"); }


}
