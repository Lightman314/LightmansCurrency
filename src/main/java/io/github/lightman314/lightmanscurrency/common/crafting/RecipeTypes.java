package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class RecipeTypes {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		COIN_MINT = register("coin_mint");
		TICKET = register("ticket");
		
	}

	private static <T extends Recipe<?>> Supplier<RecipeType<T>> register(@Nonnull String id)
	{
		return ModRegistries.RECIPE_TYPES.register(id, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,id)));
	}
	
	public static final Supplier<RecipeType<CoinMintRecipe>> COIN_MINT;

	public static final Supplier<RecipeType<TicketStationRecipe>> TICKET;

}
