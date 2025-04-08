package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class RecipeTypes {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		COIN_MINT = register("coin_mint");
		TICKET = register("ticket");
		
	}

	private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> register(@Nonnull String id)
	{
		return ModRegistries.RECIPE_TYPES.register(id, () -> RecipeType.simple(VersionUtil.lcResource(id)));
	}
	
	public static final RegistryObject<RecipeType<CoinMintRecipe>> COIN_MINT;

	public static final RegistryObject<RecipeType<TicketStationRecipe>> TICKET;

}
