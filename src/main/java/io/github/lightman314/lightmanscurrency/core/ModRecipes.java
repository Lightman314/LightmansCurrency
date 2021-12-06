package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.crafting.WalletUpgradeRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {

	private static final List<RecipeSerializer<?>> RECIPES = new ArrayList<>();
	
	public static final SimpleRecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = register("crafting_wallet_upgrade", new SimpleRecipeSerializer<>(WalletUpgradeRecipe::new));
	
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
