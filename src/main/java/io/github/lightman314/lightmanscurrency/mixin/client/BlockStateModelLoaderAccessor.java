package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Predicate;

@Mixin(BlockStateModelLoader.class)
public abstract class BlockStateModelLoaderAccessor {

    @Invoker("predicate")
    public static Predicate<BlockState> runPredicate(StateDefinition<Block, BlockState> stateDefentition, String properties) { throw new AssertionError(); }

}
