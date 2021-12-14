package io.github.lightman314.lightmanscurrency.datagen;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class CoinMintRecipeBuilder {

	private final IRecipeSerializer<?> serializer;
	private final MintType type;
	private final Ingredient ingredient;
	private final Item result;
	
	public CoinMintRecipeBuilder(IRecipeSerializer<?> serializer, MintType type, Ingredient ingredient, IItemProvider result)
	{
		this.serializer = serializer;
		this.type = type;
		this.ingredient = ingredient;
		this.result = result.asItem();
	}
	
	public static CoinMintRecipeBuilder minting(Ingredient ingredient, IItemProvider result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT, MintType.MINT, ingredient, result);
	}
	
	public static CoinMintRecipeBuilder melting(Ingredient ingredient, IItemProvider result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT, MintType.MELT, ingredient, result);
	}
	
	public static CoinMintRecipeBuilder other(Ingredient ingredient, IItemProvider result)
	{
		return new CoinMintRecipeBuilder(ModRecipes.COIN_MINT, MintType.OTHER, ingredient, result);
	}
	
	public void save(Consumer<IFinishedRecipe> consumer, String name)
	{
		this.save(consumer, new ResourceLocation(name));
	}
	
	public void save(Consumer<IFinishedRecipe> consumer, ResourceLocation id)
	{
		consumer.accept(new Result(id, this.serializer, this.type, this.ingredient, this.result));
	}
	
	public static class Result implements IFinishedRecipe
	{
		private final ResourceLocation id;
		private final IRecipeSerializer<?> serializer;
		private final MintType type;
		private final Ingredient ingredient;
		private final Item result;
		
		private Result(ResourceLocation id, IRecipeSerializer<?> serializer, MintType type, Ingredient ingredient, Item result)
		{
			this.id = id;
			this.serializer = serializer;
			this.type = type;
			this.ingredient = ingredient;
			this.result = result;
		}
		
		@Override
		public void serialize(JsonObject object)
		{
			object.addProperty("mintType", this.type.name());
			object.add("ingredient", this.ingredient.serialize());
			object.addProperty("result", this.result.getRegistryName().toString());
		}
		
		@Override
		public ResourceLocation getID() { return this.id; }

		@Override
		public IRecipeSerializer<?> getSerializer() {
			return this.serializer;
		}

		@Override
		public JsonObject getAdvancementJson() { return null; }

		@Override
		public ResourceLocation getAdvancementID() { return null; }
	}
	
}
