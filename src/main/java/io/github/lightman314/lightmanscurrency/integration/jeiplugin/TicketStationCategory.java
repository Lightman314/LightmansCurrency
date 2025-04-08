package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TicketStationScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
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

import javax.annotation.Nonnull;

public class TicketStationCategory implements IRecipeCategory<TicketStationRecipe> {


    private final IDrawableStatic background;
    private final IDrawable icon;

    public TicketStationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TicketStationScreen.GUI_TEXTURE, 0, 138, 118, 26);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.TICKET_STATION.get()));
    }

    @Nonnull
    @Override
    public RecipeType<TicketStationRecipe> getRecipeType() { return LCJeiPlugin.TICKET_TYPE; }

    @Nonnull
    @Override
    public Component getTitle() { return LCText.GUI_TICKET_STATION_TITLE.get(); }

    @Nonnull
    @Override
    public IDrawable getBackground() { return this.background; }

    @Nonnull
    @Override
    public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull TicketStationRecipe recipe, @Nonnull IFocusGroup focus) {
        IRecipeSlotBuilder modifierSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
        modifierSlot.addIngredients(VanillaTypes.ITEM_STACK, recipe.jeiModifierList());
        IRecipeSlotBuilder ingredientSlot = builder.addSlot(RecipeIngredientRole.INPUT, 37, 5);
        ingredientSlot.addIngredients(recipe.getIngredient());

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 5);
        outputSlot.addIngredient(VanillaTypes.ITEM_STACK, recipe.exampleResult());
    }
}
