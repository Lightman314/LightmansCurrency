package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.world.World;


public class RecipeValidator {
	
	public static Results getValidRecipes(World level)
	{
		Results results = new Results();
		RecipeManager recipeManager = level.getRecipeManager();
		for(IRecipe<?> recipe : getRecipes(recipeManager, RecipeTypes.COIN_MINT))
		{
			if(recipe instanceof CoinMintRecipe)
			{
				CoinMintRecipe mintRecipe = (CoinMintRecipe)recipe;
				if(mintRecipe.isValid())
				{
					results.coinMintRecipes.add(mintRecipe);
				}
			}
		}
		return results;
	}
	
	private static Collection<IRecipe<?>> getRecipes(RecipeManager recipeManager, IRecipeType<?> recipeType)
	{
		return recipeManager.getRecipes().stream().filter(recipe -> recipe.getType() == recipeType).collect(Collectors.toSet());
	}
	
	public static class Results
	{
		private final List<CoinMintRecipe> coinMintRecipes = Lists.newArrayList();
		
		
		public List<CoinMintRecipe> getCoinMintRecipes() { return this.coinMintRecipes; }
	}
	
}
