package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 0, 138, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MACHINE_MINT.get()));
	}
	
	@Override
	public @NotNull RecipeType<CoinMintRecipe> getRecipeType() { return LCJeiPlugin.COIN_MINT_TYPE; }

	@Override
	public @NotNull IDrawable getBackground() { return this.background; }
	
	@Override
	public @NotNull IDrawable getIcon() { return this.icon; }

	@Override
	public @NotNull Component getTitle() { return Component.translatable("gui.lightmanscurrency.coinmint.title"); }
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CoinMintRecipe recipe, @NotNull IFocusGroup focus) {
		IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		inputSlot.addIngredients(VanillaTypes.ITEM_STACK, Lists.newArrayList(SetStackCount(recipe.getIngredient().getItems(), recipe.ingredientCount)));
		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5);
		outputSlot.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem());
	}

	private static ItemStack[] SetStackCount(ItemStack[] results, int count)
	{
		for(ItemStack stack : results)
			stack.setCount(count);
		return results;
	}
	
}
