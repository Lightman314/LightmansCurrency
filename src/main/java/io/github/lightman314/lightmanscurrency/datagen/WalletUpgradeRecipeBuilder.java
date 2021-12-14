package io.github.lightman314.lightmanscurrency.datagen;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class WalletUpgradeRecipeBuilder {

	private String group;
	private final Item result;
	private final List<Ingredient> ingredients = Lists.newArrayList();
	
	public WalletUpgradeRecipeBuilder(IItemProvider walletInput, IItemProvider result, Ingredient... ingredients)
	{
		this(walletInput, result, "", ingredients);
	}
	
	public WalletUpgradeRecipeBuilder(IItemProvider walletInput, IItemProvider result, String group, Ingredient... ingredients)
	{
		this.group = group;
		this.result = result.asItem();
		this.ingredients.add(Ingredient.fromItems(walletInput));
		for(Ingredient i : ingredients)
		{
			this.addIngredient(i, 1);
		}
	}
	
	public static WalletUpgradeRecipeBuilder walletUpgrade(IItemProvider walletIn, IItemProvider walletOut, String group, Ingredient... ingredients)
	{
		return new WalletUpgradeRecipeBuilder(walletIn, walletOut, group, ingredients);
	}
	
	public static WalletUpgradeRecipeBuilder walletUpgrade(IItemProvider walletIn, IItemProvider walletOut, Ingredient... ingredients)
	{
		return new WalletUpgradeRecipeBuilder(walletIn, walletOut, ingredients);
	}
	
	/**
	 * Adds an ingredient that can be any item in the given tag.
	 */
	public WalletUpgradeRecipeBuilder addIngredient(ITag<Item> tagIn) {
		return this.addIngredient(Ingredient.fromTag(tagIn));
	}

	/**
	 * Adds an ingredient of the given item.
	 */
	public WalletUpgradeRecipeBuilder addIngredient(IItemProvider itemIn) {
		return this.addIngredient(itemIn, 1);
	}

	/**
	 * Adds the given ingredient multiple times.
	 */
	public WalletUpgradeRecipeBuilder addIngredient(IItemProvider itemIn, int quantity) {
		for(int i = 0; i < quantity; ++i) {
			this.addIngredient(Ingredient.fromItems(itemIn));
		}
		return this;
	}
	
	/**
	 * Adds an ingredient.
	 */
	public WalletUpgradeRecipeBuilder addIngredient(Ingredient ingredientIn) {
		return this.addIngredient(ingredientIn, 1);
	}
	
	/**
	 * Adds an ingredient multiple times.
	 */
	public WalletUpgradeRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
		for(int i = 0; i < quantity; ++i) {
			this.ingredients.add(ingredientIn);
		}
		return this;
	}
	
	public WalletUpgradeRecipeBuilder setGroup(String groupIn) {
		this.group = groupIn;
		return this;
	}
	
	public void save(Consumer<IFinishedRecipe> consumer, String name)
	{
		this.save(consumer, new ResourceLocation(name));
	}
	
	public void save(Consumer<IFinishedRecipe> consumer, ResourceLocation id)
	{
		consumer.accept(new Result(id, this.result, this.group, this.ingredients));
	}
	
	public static class Result implements IFinishedRecipe {
	      private final ResourceLocation id;
	      private final Item result;
	      private final String group;
	      private final List<Ingredient> ingredients;

	      public Result(ResourceLocation idIn, Item resultIn, String groupIn, List<Ingredient> ingredientsIn) {
	         this.id = idIn;
	         this.result = resultIn;
	         this.group = groupIn;
	         this.ingredients = ingredientsIn;
	      }

	      public void serialize(JsonObject json) {
	    	  if (!this.group.isEmpty()) {
	    		  json.addProperty("group", this.group);
	    	  }

	    	  JsonArray jsonarray = new JsonArray();

	    	  for(Ingredient ingredient : this.ingredients) {
	    		  jsonarray.add(ingredient.serialize());
	    	  }

	    	  json.add("ingredients", jsonarray);
	    	  JsonObject jsonobject = new JsonObject();
	    	  jsonobject.addProperty("item", this.result.getRegistryName().toString());

	    	  json.add("result", jsonobject);
	      }

	      public IRecipeSerializer<?> getSerializer() {
	         return ModRecipes.WALLET_UPGRADE;
	      }

	      /**
	       * Gets the ID for the recipe.
	       */
	      public ResourceLocation getID() {
	         return this.id;
	      }

	      /**
	       * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
	       */
	      @Nullable
	      public JsonObject getAdvancementJson() {
	         return null;
	      }

	      /**
	       * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson}
	       * is non-null.
	       */
	      @Nullable
	      public ResourceLocation getAdvancementID() {
	    	  return null;
	      }
	}
}
