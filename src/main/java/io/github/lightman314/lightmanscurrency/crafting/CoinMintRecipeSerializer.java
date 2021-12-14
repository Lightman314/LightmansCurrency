package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CoinMintRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CoinMintRecipe>{

	@Override
	public CoinMintRecipe read(ResourceLocation recipeId, JsonObject json) {
		if(!json.has("ingredient"))
		{
			throw new JsonSyntaxException("Missing ingredient, expected to find an item.");
		}
		Ingredient ingredient = Ingredient.deserialize(json.getAsJsonObject("ingredient"));
		if(!json.has("result"))
		{
			throw new JsonSyntaxException("Missing result. Expected to find an item.");
		}
		ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("result").getAsString())));
		if(result.isEmpty())
		{
			throw new JsonSyntaxException("Result is empty.");
		}
		MintType type = MintType.OTHER;
		if(json.has("mintType"))
			type = CoinMintRecipe.readType(json.get("mintType"));
		
		return new CoinMintRecipe(recipeId, type, ingredient, result.getItem());
	}

	@Override
	public CoinMintRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
		CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readString(16));
		Ingredient ingredient = Ingredient.read(buffer);
		ItemStack result = buffer.readItemStack();
		return new CoinMintRecipe(recipeId, type, ingredient, result.getItem());
	}

	@Override
	public void write(PacketBuffer buffer, CoinMintRecipe recipe) {
		buffer.writeString(recipe.getMintType().name(), 16);
		recipe.getIngredient().write(buffer);
		buffer.writeItemStack(recipe.getResult());
	}

	
	
}
