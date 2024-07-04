package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nonnull;

public class SimpleCraftingCondition implements ICondition {

	private final Supplier<Boolean> test;
	private final Supplier<MapCodec<? extends ICondition>> codec;
	
	protected SimpleCraftingCondition(@Nonnull Supplier<MapCodec<? extends ICondition>> codec, @Nonnull Supplier<Boolean> test) {
		this.test = test;
		this.codec = codec;
	}

	@Override
	public boolean test(@Nonnull IContext context) {
		try { return test.get(); }
		catch(Throwable t) { LightmansCurrency.LogError("SimpleCraftingCondition '" + this.getClass().getSimpleName() + "' encountered an error during the test.", t); return false; }
	}

	@Nonnull
	@Override
	public MapCodec<? extends ICondition> codec() { return this.codec.get(); }

}
