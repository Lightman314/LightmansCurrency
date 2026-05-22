package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipeSerializers;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements CraftingRecipe {

    public static final MapCodec<WalletUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.STRING.optionalFieldOf("group","").forGetter(r -> r.group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(r -> r.category),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.recipeOutput),
                    Ingredient.CODEC_NONEMPTY.listOf(1,9).fieldOf("ingredients").forGetter(r -> r.ingredients)
            ).apply(builder,WalletUpgradeRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,WalletUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,r -> r.group,
            CraftingBookCategory.STREAM_CODEC,r -> r.category,
            ItemStack.STREAM_CODEC,r -> r.recipeOutput,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),r -> r.ingredients,
            WalletUpgradeRecipe::new);

	private final String group;
	private final CraftingBookCategory category;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> ingredients;
	private final boolean isSimple;

	public WalletUpgradeRecipe(String groupIn, CraftingBookCategory category, ItemStack recipeOutputIn, List<Ingredient> ingredients) {
		this.group = groupIn;
		this.category = category;
		this.recipeOutput = recipeOutputIn;
		this.ingredients = NonNullList.copyOf(ingredients);
		this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
	}

	@Override
	public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.WALLET_UPGRADE.get(); }

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public String getGroup() { return this.group; }

	/**
	 * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
	 * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
	 */
	@Override
	
	public ItemStack getResultItem(HolderLookup.Provider lookup) { return this.recipeOutput; }

	@Override
	
	public NonNullList<Ingredient> getIngredients() { return this.ingredients; }

	/**
	 * Used to check if a recipe matches current crafting items
	 */
	@Override
	public boolean matches(CraftingInput container, Level level) {
	      StackedContents stackedcontents = new StackedContents();
	      List<ItemStack> inputs = new ArrayList<>();
	      int i = 0;

	      for(int j = 0; j < container.size(); ++j) {
	         ItemStack itemstack = container.getItem(j);
	         if (!itemstack.isEmpty()) {
	            ++i;
	            if (this.isSimple)
	            stackedcontents.accountStack(itemstack, 1);
	            else inputs.add(itemstack);
	         }
	      }

	      return i == this.ingredients.size() && (this.isSimple ? stackedcontents.canCraft(this, null) : RecipeMatcher.findMatches(inputs,  this.ingredients) != null);
	   }
	
	/**
	 * Returns an Item that is the result of this recipe
	 */
	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider lookup) {
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
	public CraftingBookCategory category() { return this.category; }

}
