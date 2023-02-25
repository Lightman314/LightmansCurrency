package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypes {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		COIN_MINT = ModRegistries.RECIPE_TYPES.register("coin_mint", () -> new RecipeType<CoinMintRecipe>() {
			@Override
			public String toString() { return "lightmanscurrency:coin_mint"; }
		});
		
	}
	
	public static final RegistryObject<RecipeType<CoinMintRecipe>> COIN_MINT;
	
}
