package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

public class CoinMintRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CoinMintRecipe>{

	@Override
	public CoinMintRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
		if(!json.has("ingredient"))
		{
			throw new JsonSyntaxException("Missing ingredient, expected to find an item.");
		}
		Ingredient ingredient = Ingredient.fromJson(json.getAsJsonObject("ingredient"));
		int ingredientCount = 1;
		if(json.has("count"))
			ingredientCount = json.get("count").getAsInt();
		if(!json.has("result"))
		{
			throw new JsonSyntaxException("Missing result. Expected to find an item.");
		}
		ItemStack result = ShapedRecipe.itemStackFromJson(json.get("result").getAsJsonObject());
		if(result.isEmpty())
		{
			throw new JsonSyntaxException("Result is empty.");
		}
		MintType type = MintType.OTHER;
		if(json.has("mintType"))
			type = CoinMintRecipe.readType(json.get("mintType"));

		return new CoinMintRecipe(recipeId, type, ingredient, ingredientCount, result);
	}

	@Override
	public CoinMintRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
		CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readUtf());
		Ingredient ingredient = Ingredient.fromNetwork(buffer);
		int ingredientCount = buffer.readInt();
		ItemStack result = buffer.readItem();
		return new CoinMintRecipe(recipeId, type, ingredient, ingredientCount, result);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CoinMintRecipe recipe) {
		buffer.writeUtf(recipe.getMintType().name());
		recipe.getIngredient().toNetwork(buffer);
		buffer.writeInt(recipe.ingredientCount);
		buffer.writeItemStack(recipe.getResultItem(), false);
	}



}
