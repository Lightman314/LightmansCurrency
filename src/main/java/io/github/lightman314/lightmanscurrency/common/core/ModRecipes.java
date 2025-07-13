package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

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
		COUPON = ModRegistries.RECIPE_SERIALIZERS.register("coupon", CouponRecipe.Serializer::new);

	}
	
	public static final Supplier<RecipeSerializer<WalletUpgradeRecipe>> WALLET_UPGRADE;
	public static final Supplier<RecipeSerializer<CoinMintRecipe>> COIN_MINT;

	public static final Supplier<RecipeSerializer<TicketRecipe>> TICKET;
	public static final Supplier<RecipeSerializer<MasterTicketRecipe>> TICKET_MASTER;
	public static final Supplier<RecipeSerializer<CouponRecipe>> COUPON;

}
