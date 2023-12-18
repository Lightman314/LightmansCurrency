package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		WALLET_UPGRADE = ModRegistries.RECIPE_SERIALIZERS.register("crafting_wallet_upgrade", WalletUpgradeRecipe.Serializer::new);
		
		COIN_MINT = ModRegistries.RECIPE_SERIALIZERS.register("coin_mint", CoinMintRecipe.Serializer::new);

		TICKET = ModRegistries.RECIPE_SERIALIZERS.register("ticket", TicketRecipe.Serializer::new);
		TICKET_MASTER = ModRegistries.RECIPE_SERIALIZERS.register("ticket_master", MasterTicketRecipe.Serializer::new);

	}
	
	public static final RegistryObject<RecipeSerializer<WalletUpgradeRecipe>> WALLET_UPGRADE;
	public static final RegistryObject<RecipeSerializer<CoinMintRecipe>> COIN_MINT;

	public static final RegistryObject<RecipeSerializer<TicketRecipe>> TICKET;
	public static final RegistryObject<RecipeSerializer<MasterTicketRecipe>> TICKET_MASTER;

}
