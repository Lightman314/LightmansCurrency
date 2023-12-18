package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;

	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 55, 16, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.COIN_MINT.get()));
		this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25L).build(new CacheLoader<>() {
			@Nonnull
			@Override
			public IDrawableAnimated load(@Nonnull Integer mintTime) {
				return guiHelper.drawableBuilder(MintScreen.GUI_TEXTURE, 176, 0, 22, 16).buildAnimated(mintTime, IDrawableAnimated.StartDirection.LEFT, false);
			}
		});
	}

	protected IDrawableAnimated getArrow(CoinMintRecipe recipe) {
		int mintTime = recipe.getDuration();
		if (mintTime <= 0) {
			mintTime = 100;
		}

		return this.cachedArrows.getUnchecked(mintTime);
	}
	
	@Nonnull
	@Override
	public RecipeType<CoinMintRecipe> getRecipeType() { return LCJeiPlugin.COIN_MINT_TYPE; }

	@Nonnull
	@Override
	public IDrawable getBackground() { return this.background; }

	@Override
	public void draw(@Nonnull CoinMintRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		IDrawableAnimated arrow = this.getArrow(recipe);
		arrow.draw(guiGraphics, 25, 5);
	}

	@Nonnull
	@Override
	public IDrawable getIcon() { return this.icon; }

	@Nonnull
	@Override
	public Component getTitle() { return Component.translatable("gui.lightmanscurrency.coinmint.title"); }
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CoinMintRecipe recipe, @Nonnull IFocusGroup focus) {
		IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		inputSlot.addIngredients(VanillaTypes.ITEM_STACK, Lists.newArrayList(SetStackCount(recipe.getIngredient().getItems(), recipe.ingredientCount)));
		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5);
		outputSlot.addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputItem());
	}

	private static ItemStack[] SetStackCount(ItemStack[] results, int count)
	{
		for(ItemStack stack : results)
			stack.setCount(count);
		return results;
	}
	
}
