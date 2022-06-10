package io.github.lightman314.lightmanscurrency.datagen;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public class CoinMintRecipeBuilder {

	private final RecipeSerializer<?> serializer;
	private final MintType type;
	private final Ingredient ingredient;
	private final Item result;
	
	public CoinMintRecipeBuilder(RecipeSerializer<?> serializer, MintType type, Ingredient ingredient, ItemLike result)
	{
		this.serializer = serializer;
		this.type = type;
		this.ingredient = ingredient;
		this.result = result.asItem();
	}
	
	public static CoinMintRecipeBuilder minting(Ingredient ingredient, ItemLike result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT.get(), MintType.MINT, ingredient, result);
	}
	
	public static CoinMintRecipeBuilder melting(Ingredient ingredient, ItemLike result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT.get(), MintType.MELT, ingredient, result);
	}
	
	public static CoinMintRecipeBuilder other(Ingredient ingredient, ItemLike result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT.get(), MintType.OTHER, ingredient, result);
	}
	
	public void save(Consumer<FinishedRecipe> consumer, String name)
	{
		this.save(consumer, new ResourceLocation(name));
	}
	
	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id)
	{
		consumer.accept(new Result(id, this.serializer, this.type, this.ingredient, this.result));
	}
	
	public static class Result implements FinishedRecipe
	{
		private final ResourceLocation id;
		private final RecipeSerializer<?> serializer;
		private final MintType type;
		private final Ingredient ingredient;
		private final Item result;
		
		private Result(ResourceLocation id, RecipeSerializer<?> serializer, MintType type, Ingredient ingredient, Item result)
		{
			this.id = id;
			this.serializer = serializer;
			this.type = type;
			this.ingredient = ingredient;
			this.result = result;
		}
		
		@Override
		public void serializeRecipeData(JsonObject object)
		{
			object.addProperty("mintType", this.type.name());
			object.add("ingredient", this.ingredient.toJson());
			object.addProperty("result", ForgeRegistries.ITEMS.getKey(this.result).toString());
		}
		
		@Override
		public ResourceLocation getId() { return this.id; }

		@Override
		public RecipeSerializer<?> getType() {
			return this.serializer;
		}

		@Override
		public JsonObject serializeAdvancement() { return null; }

		@Override
		public ResourceLocation getAdvancementId() { return null; }
	}
	
}
