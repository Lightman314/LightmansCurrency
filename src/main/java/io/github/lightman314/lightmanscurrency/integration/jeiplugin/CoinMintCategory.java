package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe;
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
import net.minecraft.util.text.TranslationTextComponent;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 0, 138, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.MACHINE_MINT));
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public Class<? extends CoinMintRecipe> getRecipeClass() {
		return CoinMintRecipe.class;
	}

	@Override
	public String getTitle() {
		return new TranslationTextComponent("gui.lightmanscurrency.coinmint.title").getString();
	}

	@Override
	public ResourceLocation getUid() {
		return LCJeiPlugin.COIN_MINT_UID;
	}

	@Override
	public void setIngredients(CoinMintRecipe recipe, IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, Lists.newArrayList(recipe.getIngredient().getMatchingStacks()));
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CoinMintRecipe recipe, IIngredients ingredients) {
		
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		
		stacks.init(0, true, 0, 4);
		stacks.init(1, false, 60, 4);
		stacks.set(ingredients);
		
	}
	
	
}
