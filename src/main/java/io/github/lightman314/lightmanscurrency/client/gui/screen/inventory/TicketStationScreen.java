package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TicketStationScreen extends EasyMenuScreen<TicketStationMenu> implements IScrollable {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 158;

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/ticket_machine.png");

	private static final NormalSprite SPRITE_ARROW_NORMAL = new NormalSprite(new SpriteSource(GUI_TEXTURE, 176, 0, 24, 16));
    private static final NormalSprite SPRITE_ARROW_HOVERED = new NormalSprite(new SpriteSource(GUI_TEXTURE, 176, 16, 24, 16));
    public static final FixedSizeSprite SPRITE_ARROW = WidgetStateSprite.lazyHoverable(SPRITE_ARROW_NORMAL,SPRITE_ARROW_HOVERED);

	private static final ScreenArea SELECTION_AREA = ScreenArea.of(153, 7, 16, 16);

	private TextBoxWrapper<String> codeInput;

	private RecipeHolder<TicketStationRecipe> selectedRecipe = null;
	public List<RecipeHolder<TicketStationRecipe>> getMatchingRecipes() {
		TicketStationRecipeInput input = this.menu.blockEntity.getRecipeInput(TicketStationRecipe.ExtraData.EMPTY);
		return this.menu.getAllRecipes().stream().filter(r -> r.value().matches(input, this.menu.blockEntity.getLevel())).toList();
	}

	public TicketStationScreen(TicketStationMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.resize(WIDTH,HEIGHT);
	}

    @Override
    protected void initialize(ScreenArea screenArea)
    {
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(79,21))
                .pressAction(this::craftTicket)
                .sprite(SPRITE_ARROW)
                .addon(EasyAddonHelper.visibleCheck(() -> this.menu.validInputs() && this.selectedRecipe != null && this.selectedRecipe.value().validData(this.menu.getExtraData()) && this.menu.roomForOutput(this.selectedRecipe.value())))
                .addon(EasyAddonHelper.tooltip(this::getArrowTooltip))
                .build());
        //Add scroll area for recipe selection.
        this.addChild(ScrollListener.builder()
                .area(SELECTION_AREA.offsetPosition(screenArea.pos))
                .listener(this)
                .build());
        //Add code input for coupon recipes
        this.codeInput = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(9,52))
                .width(107)
                .maxLength(16)
                .filter(TicketStationRecipe.CODE_INPUT_PREDICATE)
                .handler(this.menu::setCode)
                .startingString(this.codeInput != null ? this.codeInput.getValue() : "")
                .noBorder()
                .wrap()
                .addon(EasyAddonHelper.visibleCheck(this::codeInputVisible))
                .build());

        //Durability +/- buttons
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(159,46))
                .sprite(SpriteUtil.BUTTON_SIGN_PLUS)
                .pressAction(this::incrementDurability)
                .addon(EasyAddonHelper.visibleCheck(this::durabilityInputVisible))
                .addon(EasyAddonHelper.activeCheck(this::canAddDurability))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(159,56))
                .sprite(SpriteUtil.BUTTON_SIGN_MINUS)
                .pressAction(this::decrementDurability)
                .addon(EasyAddonHelper.visibleCheck(this::durabilityInputVisible))
                .addon(EasyAddonHelper.activeCheck(this::canRemoveDurability))
                .build());

        this.validateSelectedRecipe();
    }

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		gui.renderNormalBackground(GUI_TEXTURE, this);

        gui.drawString(this.title, 8, 6, 0x404040);
		if(this.codeInputVisible()) //Draw the code input bg
        {
            gui.drawString(LCText.GUI_TICKET_STATION_LABEL_CODE.get(),7,42,0x404040);
            gui.blit(GUI_TEXTURE,6,49,0,HEIGHT + 40,107,14);
        }

        //Draw the durability input
        if(this.durabilityInputVisible())
        {
            int durability = this.menu.getDurability();
            Component child;
            if(durability <= 0)
                child = LCText.GUI_TICKET_STATION_LABEL_DURABILITY_INFINITE.get();
            else
                child = EasyText.literal(String.valueOf(durability));
            Component text = LCText.GUI_TICKET_STATION_LABEL_DURABILITY.get(child);
            int width = this.font.width(text);
            gui.drawString(text,157-width,53,0x404040);
        }

		gui.drawString(this.playerInventoryTitle, 8, (this.getYSize() - 94), 0x404040);

		if(this.selectedRecipe != null)
			gui.renderItem(this.selectedRecipe.value().peekAtResult(this.menu.blockEntity.getStorage(),this.menu.getExtraData()), SELECTION_AREA.pos);

		//Reset the color
		gui.resetColor();

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		//Render tooltip
		if(this.selectedRecipe != null && SELECTION_AREA.offsetPosition(this.getCorner()).isMouseInArea(gui.mousePos))
		{
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(LCText.TOOLTIP_TICKET_STATION_RECIPE_INFO.get(this.selectedRecipe.value().peekAtResult(this.menu.blockEntity.getStorage(),this.menu.getExtraData()).getHoverName()));
			if(this.getMatchingRecipes().size() > 1)
				tooltip.add(LCText.TOOLTIP_TICKET_STATION_SELECT_RECIPE.get());
			gui.renderComponentTooltip(tooltip);
		}
	}

    public boolean codeInputVisible() { return this.selectedRecipe != null && this.selectedRecipe.value().requiredCodeInput(); }

    public boolean durabilityInputVisible() { return this.selectedRecipe != null && this.selectedRecipe.value().requiredDurabilityInput(); }

    private boolean canAddDurability() { return this.selectedRecipe != null && this.menu.getDurability() < this.selectedRecipe.value().getDurabilityData().max; }
    private boolean canRemoveDurability() {
        if(this.selectedRecipe == null)
            return false;
        DurabilityData data = this.selectedRecipe.value().getDurabilityData();
        return data.allowInfinite ? this.menu.getDurability() > 0 : this.menu.getDurability() > data.min;
    }

	@Override
	protected void screenTick() { this.validateSelectedRecipe(); }

	private Component getArrowTooltip()
	{
		if(this.selectedRecipe != null)
			return LCText.TOOLTIP_TICKET_STATION_CRAFT.get(this.selectedRecipe.value().peekAtResult(this.menu.blockEntity.getStorage(),this.menu.getExtraData()).getHoverName());
		return EasyText.empty();
	}

	private void validateSelectedRecipe()
	{
		//Auto-void and select matching recipes
		List<RecipeHolder<TicketStationRecipe>> matchingRecipes = this.getMatchingRecipes();
		if(this.selectedRecipe != null && !matchingRecipes.contains(this.selectedRecipe))
		{
			if(!matchingRecipes.isEmpty())
				this.selectedRecipe = matchingRecipes.getFirst();
			else
				this.selectedRecipe = null;
			return;
		}
		if(this.selectedRecipe == null && !matchingRecipes.isEmpty())
			this.selectedRecipe = matchingRecipes.getFirst();
	}

	private void craftTicket(EasyButton button) {
		this.validateSelectedRecipe();
		if(this.selectedRecipe == null)
			return;
		this.menu.SendCraftTicketsMessage(Screen.hasShiftDown(),this.selectedRecipe.id());
	}

	@Override
	public int currentScroll() {
		this.validateSelectedRecipe();
		if(this.selectedRecipe == null)
			return 0;
		return this.getMatchingRecipes().indexOf(this.selectedRecipe);
	}

	@Override
	public void setScroll(int newScroll) {
		List<RecipeHolder<TicketStationRecipe>> matchingRecipes = this.getMatchingRecipes();
		if(matchingRecipes.isEmpty())
			this.selectedRecipe = null;
		else if(newScroll < 0 || newScroll >= matchingRecipes.size())
			this.setScroll(0);
		else
			this.selectedRecipe = matchingRecipes.get(newScroll);
        this.validateDurability();
	}

    private void validateDurability()
    {
        int old = this.menu.getDurability();
        int newValue = this.validateDurability(old,false);
        if(newValue != old)
            this.menu.setDurability(newValue);
    }

    private int validateDurability(int value,boolean roundUp)
    {
        if(this.selectedRecipe == null)
            return value;
        return this.selectedRecipe.value().validateDurability(value,roundUp);
    }

    public void incrementDurability()
    {
        if(!this.durabilityInputVisible() || this.selectedRecipe == null)
            return;
        TicketStationRecipe r = this.selectedRecipe.value();
        int current = this.menu.getDurability();
        int add = Screen.hasShiftDown() ? 10 : 1;
        if(Screen.hasControlDown())
            add *= 10;
        this.menu.setDurability(this.validateDurability(current + add,true));
    }

    public void decrementDurability()
    {
        if(!this.durabilityInputVisible() || this.selectedRecipe == null)
            return;
        TicketStationRecipe r = this.selectedRecipe.value();
        int current = this.menu.getDurability();
        int remove = Screen.hasShiftDown() ? 10 : 1;
        if(Screen.hasControlDown())
            remove *= 10;
        this.menu.setDurability(this.validateDurability(current - remove,false));
    }

    @Override
	public int getMaxScroll() { return this.getMatchingRecipes().size() - 1; }

	//Prevent the screen from being closed when the text field is present
	@Override
	public boolean blockInventoryClosing() { return this.codeInputVisible(); }

}
