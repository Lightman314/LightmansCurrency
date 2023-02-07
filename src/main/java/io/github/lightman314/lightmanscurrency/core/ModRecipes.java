package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		WALLET_UPGRADE = ModRegistries.RECIPE_SERIALIZERS.register("crafting_wallet_upgrade", WalletUpgradeRecipe.Serializer::new);
		
		COIN_MINT = ModRegistries.RECIPE_SERIALIZERS.register("coin_mint", CoinMintRecipeSerializer::new);
		
	}
	
	public static final RegistryObject<RecipeSerializer<WalletUpgradeRecipe>> WALLET_UPGRADE;
	public static final RegistryObject<RecipeSerializer<CoinMintRecipe>> COIN_MINT;
	
}
