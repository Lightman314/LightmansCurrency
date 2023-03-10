package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.crafting.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;

public class ModRecipes {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		WALLET_UPGRADE = ModRegistries.RECIPE_SERIALIZERS.register("crafting_wallet_upgrade", WalletUpgradeRecipe.Serializer::new);
		
		COIN_MINT = ModRegistries.RECIPE_SERIALIZERS.register("coin_mint", CoinMintRecipeSerializer::new);
		
	}
	
	public static final RegistryObject<IRecipeSerializer<WalletUpgradeRecipe>> WALLET_UPGRADE;
	public static final RegistryObject<IRecipeSerializer<CoinMintRecipe>> COIN_MINT;
	
}
