package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;

import javax.annotation.Nonnull;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements CraftingRecipe {

	private final String group;
	private final CraftingBookCategory category;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> ingredients;
	private final boolean isSimple;

	public WalletUpgradeRecipe(String groupIn, CraftingBookCategory category, ItemStack recipeOutputIn, NonNullList<Ingredient> ingredients) {
		this.group = groupIn;
		this.category = category;
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
	public ItemStack getResultItem(@Nonnull HolderLookup.Provider lookup) { return this.recipeOutput; }

	@Override
	@Nonnull
	public NonNullList<Ingredient> getIngredients() { return this.ingredients; }

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(CraftingInput container, @Nonnull Level level) {
	      StackedContents stackedcontents = new StackedContents();
	      java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
	      int i = 0;

	      for(int j = 0; j < container.size(); ++j) {
	         ItemStack itemstack = container.getItem(j);
	         if (!itemstack.isEmpty()) {
	            ++i;
	            if (isSimple)
	            stackedcontents.accountStack(itemstack, 1);
	            else inputs.add(itemstack);
	         }
	      }

	      return i == this.ingredients.size() && (isSimple ? stackedcontents.canCraft(this, null) : RecipeMatcher.findMatches(inputs,  this.ingredients) != null);
	   }
	
	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public @Nonnull ItemStack assemble(@Nonnull CraftingInput inv, @Nonnull HolderLookup.Provider lookup) {
		ItemStack output = this.recipeOutput.copy();
		ItemStack walletStack = this.getWalletStack(inv);
		if(!walletStack.isEmpty())
			output = walletStack.transmuteCopy(output.getItem(), 1);
		return output;
	}
	
	private ItemStack getWalletStack(CraftingInput inv) {
		for(int i = 0; i < inv.size(); i++)
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
	public @Nonnull CraftingBookCategory category() { return this.category; }

	public static class Serializer implements RecipeSerializer<WalletUpgradeRecipe> {

		@Nonnull
    	private static WalletUpgradeRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
    		String s = buffer.readUtf();
			CraftingBookCategory c = buffer.readEnum(CraftingBookCategory.class);
    		int i = buffer.readVarInt();
    		NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

			nonnulllist.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));

    		ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
    		return new WalletUpgradeRecipe(s, c, itemstack, nonnulllist);
    	}

	    private static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull WalletUpgradeRecipe recipe) {
    		buffer.writeUtf(recipe.group);
			buffer.writeEnum(recipe.category);
	    	buffer.writeVarInt(recipe.ingredients.size());

	    	for(Ingredient ingredient : recipe.ingredients) {
	    		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,ingredient);
	    	}

			ItemStack.STREAM_CODEC.encode(buffer,recipe.recipeOutput);

	    }

		@Nonnull
		@Override
		public MapCodec<WalletUpgradeRecipe> codec() {
			return RecordCodecBuilder.mapCodec(builder ->
					builder.group(
							Codec.STRING.optionalFieldOf("group","").forGetter(r -> r.group),
							CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(r -> r.category),
							ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.recipeOutput),
							Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(var -> {
								Ingredient[] aingredient = var.toArray(Ingredient[]::new);
								if (aingredient.length == 0) {
									return DataResult.error(() -> "No ingredients for shapeless recipe");
								} else {
									return aingredient.length > 3 * 3 ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(3 * 3)) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
								}
							},DataResult::success).forGetter(r -> r.ingredients)
					).apply(builder,WalletUpgradeRecipe::new)
			);
		}

		@Nonnull
		@Override
		public StreamCodec<RegistryFriendlyByteBuf, WalletUpgradeRecipe> streamCodec() { return StreamCodec.of(Serializer::toNetwork,Serializer::fromNetwork); }

	}

}
