package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements CraftingRecipe {

	private final String group;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> ingredients;
	private final boolean isSimple;

	public WalletUpgradeRecipe(String groupIn, ItemStack recipeOutputIn, NonNullList<Ingredient> ingredients) {
		this.group = groupIn;
		this.recipeOutput = recipeOutputIn;
		this.ingredients = ingredients;
		this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
	}

	@Override
	public @Nonnull RecipeSerializer<?> getSerializer() {
		return ModRecipes.WALLET_UPGRADE.get();
	}

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public @Nonnull String getGroup() {
		return this.group;
	}

	/**
	 * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
	 * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
	 */
	@Override
	@Nonnull
	public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) { return this.recipeOutput; }
	public ItemStack getResultItem() { return this.recipeOutput; }

	@Override
	@Nonnull
	public NonNullList<Ingredient> getIngredients() {
		return this.ingredients;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(CraftingContainer container, @Nonnull Level level) {
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

	      return i == this.ingredients.size() && (isSimple ? stackedcontents.canCraft(this, null) : net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs,  this.ingredients) != null);
	   }
	
	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public @Nonnull ItemStack assemble(@Nonnull CraftingContainer inv, @Nonnull RegistryAccess registryAccess) {
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

	@Override
	public @Nonnull CraftingBookCategory category() { return CraftingBookCategory.MISC; }

	public static class Serializer implements RecipeSerializer<WalletUpgradeRecipe> {

		private static final Codec<WalletUpgradeRecipe> CODEC = RecordCodecBuilder.create((b) -> b.group(
				ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(WalletUpgradeRecipe::getGroup),
				CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(WalletUpgradeRecipe::getResultItem),
				Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((list) ->
				{
					Ingredient[] aingredient = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
					if (aingredient.length == 0) {
						return DataResult.error(() -> "No ingredients for shapeless recipe");
					} else {
						return aingredient.length > 3 * 3 ? DataResult.error(() -> "Too many ingredients for shapeless recipe") : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
					}
				}, DataResult::success).forGetter(WalletUpgradeRecipe::getIngredients))
				.apply(b, WalletUpgradeRecipe::new));

		@Nonnull
		@Override
		public Codec<WalletUpgradeRecipe> codec() { return CODEC; }

		@Override
    	public WalletUpgradeRecipe fromNetwork(FriendlyByteBuf buffer) {
    		String s = buffer.readUtf(32767);
    		int i = buffer.readVarInt();
    		NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

			nonnulllist.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

    		ItemStack itemstack = buffer.readItem();
    		return new WalletUpgradeRecipe(s, itemstack, nonnulllist);
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
