package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.crafting.WalletUpgradeRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {

	private static final List<IRecipeSerializer<?>> RECIPES = new ArrayList<>();
	
	public static final SpecialRecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = register("crafting_wallet_upgrade", new SpecialRecipeSerializer<>(WalletUpgradeRecipe::new));
	
	private static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(String name, S recipeSerializer)
	{
		recipeSerializer.setRegistryName(new ResourceLocation(LightmansCurrency.MODID, name));
		RECIPES.add(recipeSerializer);
		return recipeSerializer;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		RECIPES.forEach(recipe -> event.getRegistry().register(recipe));
		RECIPES.clear();
	}
	
}
