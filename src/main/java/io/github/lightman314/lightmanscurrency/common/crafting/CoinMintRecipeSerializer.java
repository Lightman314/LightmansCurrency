package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe.MintType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class CoinMintRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CoinMintRecipe>{

	@Nonnull
	@Override
	public CoinMintRecipe fromJson(@Nonnull ResourceLocation recipeId, JsonObject json) {
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
		ItemStack result = ShapedRecipe.itemFromJson(json.get("result").getAsJsonObject());
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
	public CoinMintRecipe fromNetwork(@Nonnull ResourceLocation recipeId, PacketBuffer buffer) {
		CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readUtf());
		Ingredient ingredient = Ingredient.fromNetwork(buffer);
		int ingredientCount = buffer.readInt();
		ItemStack result = buffer.readItem();
		return new CoinMintRecipe(recipeId, type, ingredient, ingredientCount, result);
	}

	@Override
	public void toNetwork(PacketBuffer buffer, CoinMintRecipe recipe) {
		buffer.writeUtf(recipe.getMintType().name());
		recipe.getIngredient().toNetwork(buffer);
		buffer.writeInt(recipe.ingredientCount);
		buffer.writeItemStack(recipe.getResultItem(), false);
	}



}
