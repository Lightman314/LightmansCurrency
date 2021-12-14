package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {

	private static final List<RecipeSerializer<?>> RECIPES = new ArrayList<>();
	
	public static final RecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = register("crafting_wallet_upgrade", new WalletUpgradeRecipe.Serializer());
	public static final RecipeSerializer<CoinMintRecipe> COIN_MINT = register("coin_mint", new CoinMintRecipeSerializer());
	
	private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S recipeSerializer)
	{
		recipeSerializer.setRegistryName(new ResourceLocation(LightmansCurrency.MODID, name));
		RECIPES.add(recipeSerializer);
		return recipeSerializer;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<RecipeSerializer<?>> event)
	{
		RECIPES.forEach(recipe -> event.getRegistry().register(recipe));
		RECIPES.clear();
	}
	
}
