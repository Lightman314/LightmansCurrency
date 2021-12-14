package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements ICraftingRecipe {
	private final ResourceLocation id;
	private final String group;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> recipeItems;
	private final boolean isSimple;

	public WalletUpgradeRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn, NonNullList<Ingredient> recipeItemsIn) {
		this.id = idIn;
		this.group = groupIn;
		this.recipeOutput = recipeOutputIn;
		this.recipeItems = recipeItemsIn;
		this.isSimple = recipeItemsIn.stream().allMatch(Ingredient::isSimple);
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public IRecipeSerializer<?> getSerializer() {
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
	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	public NonNullList<Ingredient> getIngredients() {
		return this.recipeItems;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	public boolean matches(CraftingInventory inv, World worldIn) {
		RecipeItemHelper recipeitemhelper = new RecipeItemHelper();
		java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
		int i = 0;

		for(int j = 0; j < inv.getSizeInventory(); ++j) {
			ItemStack itemstack = inv.getStackInSlot(j);
			if (!itemstack.isEmpty()) {
				++i;
				if (isSimple)
					recipeitemhelper.func_221264_a(itemstack, 1);
				else inputs.add(itemstack);
			}
		}

		return i == this.recipeItems.size() && (isSimple ? recipeitemhelper.canCraft(this, (IntList)null) : net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs,  this.recipeItems) != null);
	}
	
	/**
	 * Returns an Item that is the result of this recipe
	 */
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack output = this.recipeOutput.copy();
		ItemStack walletStack = this.getWalletStack(inv);
		if(!walletStack.isEmpty())
			WalletItem.CopyWalletContents(walletStack, output);
		return output;
	}
	
	private ItemStack getWalletStack(CraftingInventory inv) {
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.getItem() instanceof WalletItem)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	public boolean canFit(int width, int height) {
		return width * height >= this.recipeItems.size();
	}
	
	public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<WalletUpgradeRecipe> {
	    
		
		public WalletUpgradeRecipe read(ResourceLocation recipeId, JsonObject json) {
			String s = JSONUtils.getString(json, "group", "");
			NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
			} else if (nonnulllist.size() > 3 * 3) {
				throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + (3 * 3));
			} else {
				ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
				return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
			}
	    }

		private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for(int i = 0; i < ingredientArray.size(); ++i) {
				Ingredient ingredient = Ingredient.deserialize(ingredientArray.get(i));
				if (!ingredient.hasNoMatchingItems()) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

    	public WalletUpgradeRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
    		String s = buffer.readString(32767);
    		int i = buffer.readVarInt();
    		NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

    		for(int j = 0; j < nonnulllist.size(); ++j) {
    			nonnulllist.set(j, Ingredient.read(buffer));
    		}

    		ItemStack itemstack = buffer.readItemStack();
    		return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
    	}

	    public void write(PacketBuffer buffer, WalletUpgradeRecipe recipe) {
	    	
    		buffer.writeString(recipe.group);
	    	buffer.writeVarInt(recipe.recipeItems.size());

	    	for(Ingredient ingredient : recipe.recipeItems) {
	    		ingredient.write(buffer);
	    	}

	    	buffer.writeItemStack(recipe.recipeOutput);
	    }
	}
}
