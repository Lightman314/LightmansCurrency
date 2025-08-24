package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TicketStationScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketStationCategory implements IRecipeCategory<TicketStationRecipe> {

    private final IDrawableStatic background;
    private final IDrawableStatic codeInput;
    private final IDrawableStatic durabilityInput;
    private final IDrawable icon;
    private final ScreenArea durabilityArea = ScreenArea.of(107,26,11,14);

    public TicketStationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TicketStationScreen.GUI_TEXTURE, 0, TicketStationScreen.HEIGHT, 118, 40);
        this.codeInput = guiHelper.createDrawable(TicketStationScreen.GUI_TEXTURE,0,TicketStationScreen.HEIGHT + 40,107,14);
        this.durabilityInput = guiHelper.createDrawable(TicketStationScreen.GUI_TEXTURE,107,TicketStationScreen.HEIGHT + 40,11,14);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.TICKET_STATION.get()));
    }

    @Override
    public RecipeType<TicketStationRecipe> getRecipeType() { return LCJeiPlugin.TICKET_TYPE; }

    @Override
    public IDrawable getBackground() { return this.background; }

    @Override
    public Component getTitle() { return LCText.GUI_TICKET_STATION_TITLE.get(); }

    @Override
    public IDrawable getIcon() { return this.icon; }

    @Override
    public int getWidth() { return 118; }

    @Override
    public int getHeight() { return 40; }

    @Override
    public void draw(TicketStationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        if(recipe.requiredCodeInput())
        {
            this.codeInput.draw(guiGraphics,0,26);
            //Draw example code
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font,"ExampleCode",3,29,0xFFFFFF,true);
        }
        if(recipe.requiredDurabilityInput())
            this.durabilityInput.draw(guiGraphics,107,26);
    }

    @Override
    public List<Component> getTooltipStrings(TicketStationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if(recipe.requiredDurabilityInput() && this.durabilityArea.isMouseInArea(mouseX,mouseY))
        {
            DurabilityData data = recipe.getDurabilityData();
            int min = data.min;
            int max = data.max;
            boolean allowInfinite = data.allowInfinite || data.min <= 0;
            if(min <= 0)
                min = 1;
            tooltip.add(LCText.JEI_INFO_TICKET_DURABILITY.get(min,max));
            if(allowInfinite)
                tooltip.add(LCText.JEI_INFO_TICKET_DURABILITY_INFINITE.get());
        }
        return tooltip;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TicketStationRecipe recipe, IFocusGroup focus) {
        IRecipeSlotBuilder modifierSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
        modifierSlot.addIngredients(VanillaTypes.ITEM_STACK, recipe.jeiModifierList());
        IRecipeSlotBuilder ingredientSlot = builder.addSlot(RecipeIngredientRole.INPUT, 37, 5);
        ingredientSlot.addIngredients(recipe.getIngredient());

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 5);
        outputSlot.addIngredient(VanillaTypes.ITEM_STACK, recipe.exampleResult());
    }
}