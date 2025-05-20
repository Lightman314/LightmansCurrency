package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Predicate;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryAccessor {

    @Invoker("predicate")
    public static Predicate<BlockState> runPredicate(StateDefinition<Block, BlockState> stateDefinition, String properties) { throw new AssertionError(); }

}
