package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;


public class RecipeValidator {

	public static List<CoinMintRecipe> getAllMintRecipes(Level level)
	{
		RecipeManager recipeManager = level.getRecipeManager();
		return recipeManager.getAllRecipesFor(RecipeTypes.COIN_MINT.get()).stream().map(RecipeHolder::value).toList();
	}

	public static List<CoinMintRecipe> getValidMintRecipes(Level level)
	{
		List<CoinMintRecipe> results = new ArrayList<>();
		for(CoinMintRecipe recipe : getAllMintRecipes(level))
		{
			if(recipe.isValid())
				results.add(recipe);
		}
		return ImmutableList.copyOf(results);
	}

	public static List<RecipeHolder<TicketStationRecipe>> getTicketStationRecipes(Level level)
	{
		RecipeManager recipeManager = level.getRecipeManager();
		return recipeManager.getAllRecipesFor(RecipeTypes.TICKET.get());
	}

	public static List<TicketStationRecipe> getTicketStationRecipeList(Level level)
	{
		return getTicketStationRecipes(level).stream().map(RecipeHolder::value).toList();
	}


	
}
