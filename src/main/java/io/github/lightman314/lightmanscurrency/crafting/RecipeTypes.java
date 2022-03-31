package io.github.lightman314.lightmanscurrency.crafting;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypes {

	public static final RecipeType<CoinMintRecipe> COIN_MINT = register("lightmanscurrency:coin_mint");
	
	private static <T extends Recipe<?>> RecipeType<T> register(final String key)
	{
		//Unfreeze and the registry before registration.
		return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(key), new RecipeType<T>() {
			@Override
			public String toString() { return key; }
		});
	}
	
}
