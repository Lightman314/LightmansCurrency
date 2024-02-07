package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;


public class RecipeValidator {
	
	public static List<CoinMintRecipe> getValidMintRecipes(Level level)
	{
		List<CoinMintRecipe> results = new ArrayList<>();
		RecipeManager recipeManager = level.getRecipeManager();
		for(Recipe<?> recipe : getRecipes(recipeManager, RecipeTypes.COIN_MINT.get()))
		{
			if(recipe instanceof CoinMintRecipe mintRecipe)
			{
				if(mintRecipe.isValid())
					results.add(mintRecipe);
			}
		}
		return ImmutableList.copyOf(results);
	}

	public static List<TicketStationRecipe> getValidTicketStationRecipes(Level level)
	{
		List<TicketStationRecipe> results = new ArrayList<>();
		RecipeManager recipeManager = level.getRecipeManager();
		for(Recipe<?> recipe : getRecipes(recipeManager, RecipeTypes.TICKET.get()))
		{
			if(recipe instanceof TicketStationRecipe tmr)
				results.add(tmr);
		}
		return results;
	}
	
	private static Collection<Recipe<?>> getRecipes(RecipeManager recipeManager, RecipeType<?> recipeType)
	{
		return recipeManager.getRecipes().stream().filter(recipe -> recipe.getType() == recipeType).collect(Collectors.toSet());
	}
	
}
