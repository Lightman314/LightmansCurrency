package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class SimpleCraftingCondition implements ICondition {

	private final ResourceLocation id;
	private final Supplier<Boolean> test;
	private final Codec<? extends ICondition> codec;
	
	public SimpleCraftingCondition(ResourceLocation id, Supplier<Boolean> test) {
		this.id = id;
		this.test = test;
		this.codec = this.buildCodec();
	}

	protected Codec<? extends ICondition> buildCodec() { return Codec.unit(this).stable(); }

	@Override
	public Codec<? extends ICondition> codec() { return this.codec; }

	@Override
	public boolean test(IContext context) {
		try { return test.get(); }
		catch(Throwable t) { LightmansCurrency.LogError("SimpleCraftingCondition '" + this.id + "' encountered an error during the test.", t); return false; }
	}

}
