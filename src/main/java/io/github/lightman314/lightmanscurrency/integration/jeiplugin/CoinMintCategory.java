package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 0, 138, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.MACHINE_MINT.get()));
	}

	@Override
	public @Nonnull IDrawable getBackground() { return this.background; }

	@Override
	public @Nonnull IDrawable getIcon() { return this.icon; }



	@Nonnull
	@Override
	public ResourceLocation getUid() { return LCJeiPlugin.COIN_MINT_UID; }

	@Override
	public @Nonnull Class<? extends CoinMintRecipe> getRecipeClass() { return CoinMintRecipe.class; }

	@Override
	public @Nonnull String getTitle() { return EasyText.translatable("gui.lightmanscurrency.coinmint.title").getString(); }

	@Override
	public void setIngredients(@Nonnull CoinMintRecipe recipe, @Nonnull IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, ListUtil.createList(SetStackCount(recipe.getIngredient().getItems(), recipe.ingredientCount)));
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, @Nonnull CoinMintRecipe recipe, @Nonnull IIngredients ingredients) {

		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		stacks.init(0, true, 0, 4);
		stacks.init(1, false, 60, 4);
		stacks.set(ingredients);
	}

	private static ItemStack[] SetStackCount(ItemStack[] results, int count)
	{
		for(ItemStack stack : results)
			stack.setCount(count);
		return results;
	}
	
}
