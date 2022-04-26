package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CoinMintCategory implements IRecipeCategory<CoinMintRecipe>{

	private final IDrawableStatic background;
	private final IDrawable icon;
	
	public CoinMintCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createDrawable(MintScreen.GUI_TEXTURE, 0, 138, 82, 26);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.MACHINE_MINT));
	}
	
	@Override
	public RecipeType<CoinMintRecipe> getRecipeType() { return LCJeiPlugin.COIN_MINT_TYPE; }

	@Override
	public IDrawable getBackground() { return this.background; }

	@Override
	public IDrawable getIcon() { return this.icon; }

	@Override
	public Class<? extends CoinMintRecipe> getRecipeClass() { return CoinMintRecipe.class; }

	@Override
	public Component getTitle() { return new TranslatableComponent("gui.lightmanscurrency.coinmint.title"); }
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CoinMintRecipe recipe, IFocusGroup focus) {
		IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		inputSlot.addIngredients(VanillaTypes.ITEM, Lists.newArrayList(recipe.getIngredient().getItems()));
		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5);
		outputSlot.addIngredient(VanillaTypes.ITEM, recipe.getResult());
	}

	@Override
	public ResourceLocation getUid() { return LCJeiPlugin.COIN_MINT_UID; }
	
}
