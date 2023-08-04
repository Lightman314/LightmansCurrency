package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;

	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 55, 16, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.COIN_MINT.get()));
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
	
	@Override
	public @NotNull RecipeType<CoinMintRecipe> getRecipeType() { return LCJeiPlugin.COIN_MINT_TYPE; }

	@Override
	public @NotNull IDrawable getBackground() { return this.background; }

	@Override
	public void draw(@Nonnull CoinMintRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull PoseStack pose, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, recipeSlotsView, pose, mouseX, mouseY);
		IDrawableAnimated arrow = this.getArrow(recipe);
		arrow.draw(pose, 25, 5);
	}

	@Override
	public @NotNull IDrawable getIcon() { return this.icon; }

	@Override
	public @NotNull Class<? extends CoinMintRecipe> getRecipeClass() { return CoinMintRecipe.class; }

	@Override
	public @NotNull Component getTitle() { return new TranslatableComponent("gui.lightmanscurrency.coinmint.title"); }
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CoinMintRecipe recipe, @NotNull IFocusGroup focus) {
		IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		inputSlot.addIngredients(VanillaTypes.ITEM, Lists.newArrayList(SetStackCount(recipe.getIngredient().getItems(), recipe.ingredientCount)));
		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5);
		outputSlot.addIngredient(VanillaTypes.ITEM, recipe.getResultItem());
	}

	private static ItemStack[] SetStackCount(ItemStack[] results, int count)
	{
		for(ItemStack stack : results)
			stack.setCount(count);
		return results;
	}

	@Override
	public @NotNull ResourceLocation getUid() { return LCJeiPlugin.COIN_MINT_UID; }
	
}
