package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class SimpleCraftingCondition implements ICondition {

	private final ResourceLocation id;
	private final Supplier<Boolean> test;
	
	protected SimpleCraftingCondition(ResourceLocation id, Supplier<Boolean> test) {
		this.id = id;
		this.test = test;
	}
	
	@Override
	public ResourceLocation getID() { return this.id; }
	
	@Override
	public boolean test() { return this.test(null); }
	
	@Override
	public boolean test(IContext context) {
		try { return test.get(); }
		catch(Throwable t) { LightmansCurrency.LogError("SimpleCraftingCondition '" + this.id + "' encountered an error during the test.", t); return false; }
	}
	
}