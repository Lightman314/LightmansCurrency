package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.features.tickets.TicketStationScreen;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.DurabilityData;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;

public class TicketStationCategory implements IRecipeCategory<RecipeHolder<TicketStationRecipe>> {

    public static final IRecipeType<RecipeHolder<TicketStationRecipe>> TYPE = IRecipeType.create(LCRecipeTypes.TICKET_STATION.get());

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawable codeField;
    private final IDrawable infoField;

    private final ScreenArea durabilityArea = ScreenArea.of(142,35,10,10);

    public TicketStationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TicketStationScreen.GUI_TEXTURE,12,11,152,46);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(LCBlocks.TICKET_STATION));
        this.codeField = new SizedSpriteDrawable(LCSprites.SEARCH_FIELD.build(90));
        this.infoField = new SizedSpriteDrawable(LCSprites.GENERIC_INFO);
    }

    @Override
    public IRecipeType<RecipeHolder<TicketStationRecipe>> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return LCBlocks.TICKET_STATION.get().getName(); }

    @Override
    public int getWidth() { return 152; }

    @Override
    public int getHeight() { return 46; }

    @Override
    @Nullable
    public IDrawable getIcon() { return this.icon; }

    @Override
    public void draw(RecipeHolder<TicketStationRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        if(recipe.value().requiredCodeInput()) {
            this.codeField.draw(guiGraphics,0,34);
            guiGraphics.text(Minecraft.getInstance().font,"Example_Code",2,36,-1,true);
        }
        if(recipe.value().requiredDurabilityInput())
            this.infoField.draw(guiGraphics,142,35);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<TicketStationRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if(recipe.value().requiredDurabilityInput() && this.durabilityArea.isInArea(mouseX,mouseY)) {
            DurabilityData data = recipe.value().getDurabilityData();
            int min = data.min;
            int max = data.max;
            boolean allowInfinite = data.allowInfinite;
            tooltip.addAll(TooltipHelper.splitTooltips(TicketStationRecipe.JEI_INFO_DURABILITY.get(min,max)));
            if(allowInfinite)
                tooltip.addAll(TooltipHelper.splitTooltips(TicketStationRecipe.JEI_INFO_DURABILITY_INFINITE.get()));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,RecipeHolder<TicketStationRecipe> holder,IFocusGroup focuses) {
        TicketStationRecipe recipe = holder.value();
        IRecipeSlotBuilder modifierSlot = builder.addInputSlot(5,9);
        modifierSlot.add(recipe.jeiModifierList());
        IRecipeSlotBuilder inputSlot = builder.addInputSlot(54,9);
        inputSlot.add(recipe.getIngredient());
        IRecipeSlotBuilder outputSlot = builder.addOutputSlot(131,9);
        outputSlot.addIngredients(VanillaTypes.ITEM_STACK,recipe.jeiResultList());
    }

}
