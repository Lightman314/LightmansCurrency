package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements CraftingRecipe {
	private final ResourceLocation id;
	private final String group;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> ingredients;
	private final boolean isSimple;

	public WalletUpgradeRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn, NonNullList<Ingredient> ingredients) {
		this.id = idIn;
		this.group = groupIn;
		this.recipeOutput = recipeOutputIn;
		this.ingredients = ingredients;
		this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.WALLET_UPGRADE;
	}

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public String getGroup() {
		return this.group;
	}

	/**
	 * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
	 * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
	 */
	@Override
	public ItemStack getResultItem() {
		return this.recipeOutput;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.ingredients;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(CraftingContainer container, Level level) {
	      StackedContents stackedcontents = new StackedContents();
	      java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
	      int i = 0;

	      for(int j = 0; j < container.getContainerSize(); ++j) {
	         ItemStack itemstack = container.getItem(j);
	         if (!itemstack.isEmpty()) {
	            ++i;
	            if (isSimple)
	            stackedcontents.accountStack(itemstack, 1);
	            else inputs.add(itemstack);
	         }
	      }

	      return i == this.ingredients.size() && (isSimple ? stackedcontents.canCraft(this, (IntList)null) : net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs,  this.ingredients) != null);
	   }
	
	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(CraftingContainer inv) {
		ItemStack output = this.recipeOutput.copy();
		ItemStack walletStack = this.getWalletStack(inv);
		if(!walletStack.isEmpty())
			WalletItem.CopyWalletContents(walletStack, output);
		return output;
	}
	
	private ItemStack getWalletStack(CraftingContainer inv) {
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);
			if(stack.getItem() instanceof WalletItem)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= this.ingredients.size();
	}
	
	public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<WalletUpgradeRecipe> {
	    
		
		@Override
		public WalletUpgradeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			String s = GsonHelper.getAsString(json, "group", "");
			NonNullList<Ingredient> nonnulllist = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
			} else if (nonnulllist.size() > 3 * 3) {
				throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + (3 * 3));
			} else {
				ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
				return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
			}
	    }

		private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for(int i = 0; i < ingredientArray.size(); ++i) {
				Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
				if (!ingredient.isEmpty()) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		@Override
    	public WalletUpgradeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    		String s = buffer.readUtf(32767);
    		int i = buffer.readVarInt();
    		NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

    		for(int j = 0; j < nonnulllist.size(); ++j) {
    			nonnulllist.set(j, Ingredient.fromNetwork(buffer));
    		}

    		ItemStack itemstack = buffer.readItem();
    		return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
    	}

    	@Override
	    public void toNetwork(FriendlyByteBuf buffer, WalletUpgradeRecipe recipe) {
	    	
    		buffer.writeUtf(recipe.group);
	    	buffer.writeVarInt(recipe.ingredients.size());

	    	for(Ingredient ingredient : recipe.ingredients) {
	    		ingredient.toNetwork(buffer);
	    	}

	    	buffer.writeItemStack(recipe.recipeOutput, false);
	    }
	}
}
