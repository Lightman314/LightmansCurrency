package io.github.lightman314.lightmanscurrency.client.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.MessageMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.HorizontalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketRecipeData;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TicketStationScreen extends MessageMenuScreen<TicketStationMenu> implements IScrollable {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/ticket_station.png");

    public static final int ENTRIES = 10;

    public TicketStationScreen(TicketStationMenu menu,Inventory inventory,Component name) {
        super(menu,inventory,name,176,201);
    }

    private List<RecipeHolder<TicketStationRecipe>> availableRecipes = List.of();

    private TextBoxWrapper<String> codeInput;
    private TextBoxWrapper<Integer> discountInput;

    private int scroll = 0;
    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.availableRecipes.size(),ENTRIES); }

    @Override
    protected void initialize(ScreenArea area) {

        TicketRecipeData recipeData = this.menu.getRecipeData();
        //Code input
        this.codeInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(9,60))
                .ofWidth(82)
                .withMaxLength(TicketStationRecipeInput.MAX_CODE_LENGTH)
                .withOldWidget(this.codeInput)
                .withStartingValue(recipeData.code())
                .visible(this.menu::usesCodeInput)
                .allowedString(TicketStationRecipeInput::isValidCode)
                .withHandler(this.menu::setCode)
                .noBorder()
                .build());

        //Discount Input
        this.discountInput = this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(110,60))
                .parser(IntParser.builder().min(0).empty(0).build())
                .ofWidth(54)
                .withStartingValue(recipeData.durability())
                .withOldWidget(this.discountInput)
                .withHandler(this.menu::setDurability)
                .visible(this.menu::usesDurabilityInput)
                .withMaxLength(9)
                .noBorder()
                .build());

        //Scroll Area
        this.addChild(ScrollArea.builder()
                .withListener(this.buildScrollListener())
                .atPosition(area.pos.offset(8,77))
                .ofSize(162,28)
                .build());
        //Recipe Selection Buttons
        for(int x = 0; x < ENTRIES; ++x) {
            this.addChild(TicketRecipeSelectionButton.builder()
                    .atPos(area.pos.offset(8 + 16 * x,78))
                    .withDisplay(this.getDisplayItem(x))
                    .visible(this.hasRecipe(x))
                    .active(this.recipeNotSelected(x))
                    .onPress(this.selectRecipe(x))
                    .build());
        }
        //Scroll Bar
        this.addChild(HorizontalScrollBar.builder(this)
                .atPos(area.pos.offset(7,97))
                .ofWidth(162)
                .alwaysShow()
                .build());

    }

    @Override
    public void renderTick(ScreenPosition mousePos) {
        this.availableRecipes = this.getMenu().getAvailableRecipes();
        //LightmansCurrency.LogDebug("We have " + this.availableRecipes.size() + " available recipes!");
        this.validateScroll();
    }

    @Override
    protected void extractBackground(FancyGuiExtractor gui,ScreenArea area) {
        gui.blitBackground(LCApi.id("textures/gui/container/ticket_station.png"),area);

        if(this.menu.usesCodeInput()) {
            gui.text(TicketStationMenu.GUI_LABEL_CODE.get(),8,49,0xFF404040,false);
            gui.blitSprite(LCSprites.SEARCH_FIELD,7,58,90);
        }

        if(this.menu.usesDurabilityInput()) {
            gui.text(TicketStationMenu.GUI_LABEL_DURABILITY.get(),110,49,0xFF404040,false);
            gui.blitSprite(LCSprites.SEARCH_FIELD,108,58,62);
        }

    }

    private RecipeHolder<TicketStationRecipe> getRecipeOfDisplaySlot(int slot) {
        return ListHelper.getOrNull(this.availableRecipes,slot + this.scroll);
    }

    private Supplier<ItemStack> getDisplayItem(int slot) {
        return () -> {
            RecipeHolder<TicketStationRecipe> recipe = this.getRecipeOfDisplaySlot(slot);
            if(recipe != null)
                return recipe.value().assemble(this.menu.getRecipeInput());
            return ItemStack.EMPTY;
        };
    }

    private BooleanSupplier hasRecipe(int slot) { return () -> this.getRecipeOfDisplaySlot(slot) != null; }

    private BooleanSupplier recipeNotSelected(int slot) {
        return () -> {
            RecipeHolder<TicketStationRecipe> recipe = this.getRecipeOfDisplaySlot(slot);
            RecipeHolder<TicketStationRecipe> selectedRecipe = this.menu.getSelectedRecipe();
            return recipe == null || selectedRecipe == null || !selectedRecipe.id().equals(recipe.id());
        };
    }

    private Runnable selectRecipe(int slot) {
        return () -> this.menu.setSelectedRecipe(this.getRecipeOfDisplaySlot(slot));
    }

}