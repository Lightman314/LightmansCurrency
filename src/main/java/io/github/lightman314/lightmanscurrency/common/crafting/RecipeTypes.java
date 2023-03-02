package io.github.lightman314.lightmanscurrency.common.crafting;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypes {

	public static final RecipeType<CoinMintRecipe> COIN_MINT = register("lightmanscurrency:coin_mint");
	
	@SuppressWarnings("deprecation")
	private static <T extends Recipe<?>> RecipeType<T> register(final String key)
	{
		//Unfreeze and the registry before registration.
		MappedRegistry<?> registry = null;
		if(Registry.RECIPE_TYPE instanceof MappedRegistry<?>)
		{
			registry = (MappedRegistry<?>)Registry.RECIPE_TYPE;
			registry.unfreeze();
		}
		RecipeType<T> value = Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(key), new RecipeType<T>() {
			@Override
			public String toString() { return key; }
		});
		//Re-freeze the registry after registration.
		if(registry != null)
			registry.freeze();
		return value;
	}
	
}
