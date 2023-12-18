package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;


public class RecipeValidator {
	
	public static Results<CoinMintRecipe> getValidMintRecipes(Level level)
	{
		Results<CoinMintRecipe> results = new Results<>();
		RecipeManager recipeManager = level.getRecipeManager();
		for(Recipe<?> recipe : getRecipes(recipeManager, RecipeTypes.COIN_MINT.get()))
		{
			if(recipe instanceof CoinMintRecipe mintRecipe)
			{
				if(mintRecipe.isValid())
				{
					results.recipes.add(mintRecipe);
				}
			}
		}
		return results;
	}

	public static Results<TicketStationRecipe> getValidTicketStationRecipes(Level level)
	{
		Results<TicketStationRecipe> results = new Results<>();
		RecipeManager recipeManager = level.getRecipeManager();
		for(Recipe<?> recipe : getRecipes(recipeManager, RecipeTypes.TICKET.get()))
		{
			if(recipe instanceof TicketStationRecipe tmr)
				results.recipes.add(tmr);
		}
		return results;
	}
	
	private static Collection<Recipe<?>> getRecipes(RecipeManager recipeManager, RecipeType<?> recipeType)
	{
		return recipeManager.getRecipes().stream().filter(recipe -> recipe.getType() == recipeType).collect(Collectors.toSet());
	}
	
	public static class Results<T extends Recipe<?>>
	{
		private final List<T> recipes = new ArrayList<>();
		public List<T> getRecipes() { return this.recipes; }
	}
	
}
