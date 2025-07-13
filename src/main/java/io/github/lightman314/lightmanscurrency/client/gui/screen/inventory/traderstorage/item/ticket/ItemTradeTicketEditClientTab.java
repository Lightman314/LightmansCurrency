package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ticket;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget.IItemEditListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ticket.ItemTradeTicketEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemTradeTicketEditClientTab extends TraderStorageClientTab<ItemTradeTicketEditTab> implements TradeInteractionHandler, IItemEditListener, IMouseListener {

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 71;
    private static final int COLUMNS = 10;
    private static final int ROWS = 3;

    public ItemTradeTicketEditClientTab(Object screen, ItemTradeTicketEditTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.Null(); }

    @Override
    public MutableComponent getTooltip() { return EasyText.empty(); }

    @Override
    public boolean tabVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

    TradeButton tradeDisplay;
    MoneyValueWidget priceSelection;
    EditBox customNameInput;
    EditBox codeInput;

    ItemEditWidget itemEdit = null;
    ScreenArea recipeArea = null;

    EasyButton buttonToggleTradeType;
    PlainButton buttonToggleNBTEnforcement;

    private int selection = -1;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        TicketItemTrade trade = this.getTrade();

        this.tradeDisplay = this.addChild(TradeButton.builder()
                .position(screenArea.pos.offset(10,18))
                .context(this.menu::getContext)
                .trade(this.commonTab::getTrade)
                .build());
        this.priceSelection = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 40))
                .oldIfNotFirst(firstOpen,this.priceSelection)
                .startingValue(trade)
                .valueHandler(this::onValueChanged)
                .build());

        this.itemEdit = this.addChild(ItemEditWidget.builder()
                .position(screenArea.pos.offset(X_OFFSET,Y_OFFSET))
                .columns(COLUMNS)
                .rows(ROWS)
                .oldWidget(this.itemEdit)
                .handler(this)
                .build());

        int labelWidth = this.getFont().width(LCText.GUI_NAME.get());
        this.customNameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15 + labelWidth, screenArea.y + 38, screenArea.width - 28 - labelWidth, 18, EasyText.empty()));
        if(this.selection >= 0 && this.selection < 2 && trade != null)
            this.customNameInput.setValue(trade.getCustomName(this.selection));

        this.codeInput = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(15, 70))
                .width(screenArea.width - 28)
                .maxLength(16)
                .handler(v -> this.commonTab.ChangeCode(v,this.selection))
                .build());
        if(this.selection >= 0 && this.selection < 2 && trade != null)
            this.codeInput.setValue(trade.getTicketData(this.selection).getCode());

        this.recipeArea = ScreenArea.of((screenArea.width / 2) - 8,100,18,18);

        this.buttonToggleTradeType = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(113,15))
                .width(80)
                .pressAction(this::ToggleTradeType)
                .build());
        this.buttonToggleNBTEnforcement = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(113,4))
                .pressAction(this::ToggleNBTEnforcement)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::getEnforceNBTState))
                .build());

        this.addChild(ScrollListener.builder()
                .listener(this::scrollRecipe)
                .area(ScreenArea.of(screenArea.pos.offset((screenArea.width / 2) - 9,100),18,18))
                .build());

    }

    private boolean getEnforceNBTState() {
        ItemTradeData trade = this.getTrade();
        if(trade != null)
            return trade.getEnforceNBT(this.selection);
        return true;
    }

    @Override
    public void closeAction() { this.selection = -1; }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TicketItemTrade trade = this.getTrade();
        if(trade == null)
            return;

        this.validateRenderables();

        //Render a down arrow over the selected position
        gui.resetColor();

        gui.blit(TraderScreen.GUI_TEXTURE, this.getArrowPosition(), 10, TraderScreen.WIDTH + 8, 18, 8, 6);

        if(this.customNameInput.visible)
            gui.drawString(LCText.GUI_NAME.get(), 13, 42, 0x404040);

        if(this.codeInput.visible)
            gui.drawString(LCText.GUI_TICKET_STATION_LABEL_CODE.get(), 13, 62, 0x404040);

        //Render Recipe Selection
        if(this.selection >= 0 && this.selection < 2 && trade.getTicketData(this.selection).isPotentiallyRecipeMode())
        {
            TicketItemTrade.TicketSaleData data = trade.getTicketData(this.selection);
            gui.renderSlot(this.screen,this.recipeArea.pos);
            TicketStationRecipe recipe = data.tryGetRecipe();
            ItemStack displayItem = new ItemStack(Items.BARRIER);
            if(recipe != null)
                displayItem = data.getCraftingResult(false);
            gui.renderItem(displayItem,this.recipeArea.pos);
            gui.popOffset();
        }

        if(this.isNBTButtonVisible())
            gui.drawString(LCText.GUI_TRADER_ITEM_ENFORCE_NBT.get(), 124, 5, 0x404040);

    }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        TicketItemTrade trade = this.getTrade();
        if(trade == null)
            return;

        if(this.selection >= 0 && this.selection < 2 && trade.getTicketData(this.selection).isPotentiallyRecipeMode())
        {
            //Render Recipe Selection Tooltip
            if(this.recipeArea.offsetPosition(this.screen.getCorner()).isMouseInArea(gui.mousePos))
            {
                ItemStack result = trade.getTicketData(this.selection).getCraftingResult(false);
                Component tooltip;
                if(trade.getTicketData(this.selection).isRecipeMode())
                    tooltip = LCText.TOOLTIP_TICKET_STATION_CRAFT.get(result.getHoverName());
                else
                    tooltip = LCText.TOOLTIP_TICKET_KIOSK_CRAFT_NULL.get(result.getHoverName());
                gui.renderTooltip(tooltip);
            }
        }

    }

    private int getArrowPosition() {

        ItemTradeData trade = this.getTrade();
        if(this.selection == -1)
        {
            if(trade.isSale())
                return 25;
            if(trade.isPurchase())
                return 81;
            else
                return -1000;
        }
        else
        {
            if(this.selection >= 2 && !trade.isBarter())
                return -1000;
            int horizSlot = this.selection;
            if(trade.isSale() || trade.isBarter())
                horizSlot += 2;
            int spacing = horizSlot % 4 >= 2 ? 20 : 0;
            return 16 + (18 * (horizSlot % 4)) + spacing;
        }
    }

    private void validateRenderables() {
        TicketItemTrade trade = this.getTrade();
        if(trade == null)
        {
            this.priceSelection.visible = this.itemEdit.visible = this.customNameInput.visible = false;
            return;
        }
        this.priceSelection.visible = this.selection < 0 && !trade.isBarter();
        this.itemEdit.visible = (trade.isBarter() && this.selection >=2) || (trade.isPurchase() && this.selection >= 0);
        this.customNameInput.visible = this.selection >= 0 && this.selection < 2 && !trade.isPurchase();
        if(this.customNameInput.visible && !this.customNameInput.getValue().contentEquals(trade.getCustomName(this.selection)))
            this.commonTab.setCustomName(this.selection, this.customNameInput.getValue());
        this.codeInput.visible = this.selection >= 0 && this.selection < 2 && !trade.isPurchase() && trade.getTicketData(this.selection).requestingCodeInput();
        this.buttonToggleTradeType.setMessage(LCText.GUI_TRADE_DIRECTION.get(this.getTrade().getTradeDirection()).get());
    }

    @Override
    public void tick() {
        //Change NBT toggle button visibility
        this.buttonToggleNBTEnforcement.visible = this.isNBTButtonVisible();
    }

    private boolean isNBTButtonVisible() {
        ItemTradeData trade = this.getTrade();
        if(trade == null)
            return false;
        else
            return this.selection >= 0 && !trade.alwaysEnforcesNBT(this.selection);
    }

    @Override
    public void OpenMessage(@Nonnull LazyPacketData message) {
        if(message.contains("StartingSlot"))
            this.selection = message.getInt("StartingSlot");
    }

    @Override
    public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
        if(trade instanceof ItemTradeData t)
        {
            ItemStack heldItem = this.menu.getHeldItem();
            if(t.isSale())
                this.changeSelection(-1);
            else if(t.isPurchase())
            {
                if(this.selection != index && heldItem.isEmpty())
                    this.changeSelection(index);
                else
                    this.commonTab.defaultInteraction(index, heldItem, data.mouseButton());
            }
            else if(t.isBarter())
            {
                if(this.selection != (index + 2) && heldItem.isEmpty())
                    this.changeSelection(index + 2);
                else
                    this.commonTab.defaultInteraction(index + 2, heldItem, data.mouseButton());
            }

        }
    }

    @Override
    public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data,int index) {
        if(trade instanceof ItemTradeData t)
        {
            ItemStack heldItem = this.menu.getHeldItem();
            if(t.isSale() || t.isBarter())
            {
                if(this.selection != index && heldItem.isEmpty())
                    this.changeSelection(index);
                else
                    this.commonTab.defaultInteraction(index, heldItem, data.mouseButton());
            }
            else if(t.isPurchase())
                this.changeSelection(-1);
        }
    }

    private void changeSelection(int newSelection) {
        this.selection = newSelection;
        if(this.selection == -1)
            this.priceSelection.changeValue(this.getTrade().getCost());
        if(this.selection >= 0 && this.selection < 2)
        {
            this.itemEdit.refreshSearch();
            this.customNameInput.setValue(this.commonTab.getTrade().getCustomName(this.selection));
            this.codeInput.setValue(this.commonTab.getTrade().getTicketData(this.selection).getCode());
        }
        if(this.selection >= 2)
            this.itemEdit.refreshSearch();
    }

    @Override
    public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) { }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.HandleInteractionClick((int)mouseX, (int)mouseY, button, this);
        return false;
    }

    public void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

    @Override
    public TicketItemTrade getTrade() { return this.commonTab.getTrade(); }

    @Override
    public boolean restrictItemEditItems() { return this.selection < 2; }

    @Override
    public void onItemClicked(ItemStack item) { this.commonTab.setSelectedItem(this.selection, item); }

    private void ToggleTradeType(EasyButton button) {
        if(this.getTrade() != null)
        {
            this.commonTab.setType(ItemTradeData.getNextInCycle(this.getTrade().getTradeDirection()));
            this.itemEdit.refreshSearch();
        }
    }

    private void ToggleNBTEnforcement(EasyButton button) {
        if(this.getTrade() != null)
            this.commonTab.setNBTEnforced(this.selection, !this.getTrade().getEnforceNBT(this.selection));
    }

    private boolean scrollRecipe(double deltaX)
    {
        TicketItemTrade trade = this.getTrade();
        if(trade == null)
            return false;
        if(this.selection >= 0 && this.selection < 2 && !trade.isPurchase())
        {
            TicketItemTrade.TicketSaleData data = trade.getTicketData(this.selection);
            List<TicketStationRecipe> matchingRecipes = data.getMatchingRecipes();
            if(matchingRecipes.isEmpty())
                return false;
            int deltaIndex = deltaX > 0 ? 1 : -1;
            ResourceLocation currentRecipe = data.getRecipe();
            int previousIndex = deltaIndex > 0 ? -1 : matchingRecipes.size() + 1;
            for(int i = 0; i < matchingRecipes.size(); ++i)
            {
                if(Objects.equals(getId(matchingRecipes.get(i)),currentRecipe))
                {
                    previousIndex = i;
                    break;
                }
            }
            int newIndex = previousIndex + deltaIndex;
            if(newIndex >= matchingRecipes.size())
                newIndex = 0;
            if(newIndex < 0)
                newIndex = matchingRecipes.size() - 1;
            //Change the recipe
            this.commonTab.ChangeRecipe(getId(matchingRecipes.get(newIndex)),this.selection);
            return true;
        }
        return false;
    }

    private static ResourceLocation getId(@Nullable TicketStationRecipe recipe) { return recipe == null ? null : recipe.getId(); }

}