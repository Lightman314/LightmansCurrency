package io.github.lightman314.lightmanscurrency.client.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.ItemSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.AdvancedTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemTradeEditClientTab extends AdvancedTradeEditClientTab<ItemTradeEditTab> {

    public static final TabBuilder<TraderStorageMenu, ItemTradeEditTab,TraderStorageTab,TraderStorageScreen> BUILDER = ItemTradeEditClientTab::new;

    public static final ScreenPosition STRICT_BUTTON_POS = ScreenPosition.of(15,59);

    private ItemSelectionWidget itemSelection = null;
    private TextBoxWrapper<String> customNameInput;

    protected ItemTradeEditClientTab(TraderStorageMenu menu, ItemTradeEditTab commonTab, TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        super.initialize(area,message);

        TradeData trade = this.getTrade();

        //Item Selection
        this.itemSelection = this.addChild(ItemSelectionWidget.builder()
                .atPos(area.pos.offset(15,71))
                .withSize(10,3)
                .withFilter(this.getTradeFilter())
                .withOldWidget(this.itemSelection)
                .withListener(this::setTradeItem)
                .visible(this::isItemSelected)
                .build());

        int labelWidth = this.getFont().width(LCText.GENERIC_CUSTOM_NAME.get());
        this.customNameInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(15 + labelWidth,38))
                .ofSize(area.width - 28 - labelWidth,18)
                .visible(this::isItemSelected)
                .withMaxLength(TradeItem.MAX_NAME_LENGTH)
                .withStartingString(this.getCurrentCustomName())
                .withHandler(this::setCustomName)
                .build());

        //Type Dropdown
        this.addChild(DropdownWidget.builder()
                .atPos(area.pos.offset(113,18))
                .ofWidth(80)
                .withOption(TradeDirection.SALE.getName())
                .withOption(TradeDirection.PURCHASE.getName())
                .withCurrentlySelected(trade == null ? 0 : trade.isSale() ? 0 : 1)
                .withHandler(this::changeTradeType)
                .build());

        //Strict Checkbox
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(STRICT_BUTTON_POS))
                .onPress(this::toggleStrictItem)
                .withSprite(LCSprites.CHECKBOX.buildSprite(this::isStrictFilter))
                .visible(this::isStrictToggleVisible)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        super.extractBackground(gui, area);
        if(this.customNameInput.isVisible()) {
            gui.text(LCText.GENERIC_CUSTOM_NAME.get(), 13,42,0xFF404040,false);
        }
        if(this.isStrictToggleVisible()) {
            gui.text(TradeItem.GUI_TRADE_ITEM_ENFORCE_DATA.get(),STRICT_BUTTON_POS.x + 11, STRICT_BUTTON_POS.y + 1,0xFF404040,false);
        }
    }

    @Nullable
    private Identifier getTradeFilter() {
        if(this.getTrade() instanceof ItemTradeData t)
            return t.getItemSelectionFilter();
        return null;
    }

    protected boolean isItemSelected() { return this.getItemSelection() >= 0; }

    protected int getItemSelection() {
        if(this.getTrade() instanceof ItemTradeData trade) {
            TradeSlot slot = this.getSelectedSlot();
            if(slot.isInputOrOutput() && !slot.isPriceSlot(trade.getDirection())) {
                return slot.slot();
            }
        }
        return -1;
    }

    protected String getCurrentCustomName() {
        int slot = this.getItemSelection();
        if(slot >= 0 && this.getTrade() instanceof ItemTradeData trade)
            return trade.getItem(slot).getOptionalNameChange().orElse("");
        return "";
    }

    private void setCustomName(String newName) {
        int slot = this.getItemSelection();
        if(slot >= 0)
            this.getCommonTab().setCustomName(slot,newName);
    }

    private void setTradeItem(ItemStack stack) {
        int slot = this.getItemSelection();
        if(slot >= 0)
            this.getCommonTab().setItem(slot,stack);
    }

    private void changeTradeType(int type) {
        this.getCommonTab().setDirection(type == 0 ? TradeDirection.SALE : TradeDirection.PURCHASE);
    }

    protected boolean isStrictToggleVisible() {
        int slot = this.getItemSelection();
        if(slot >= 0 && this.getTrade() instanceof ItemTradeData trade)
            return !trade.getItem(slot).isFilter() && !trade.alwayStrictItem(slot);
        return false;
    }

    protected boolean isStrictFilter() {
        int slot = this.getItemSelection();
        if(slot >= 0 && this.getTrade() instanceof ItemTradeData trade)
            return trade.getItem(slot).isStrict();
        return false;
    }

    private void toggleStrictItem() {
        int slot = this.getItemSelection();
        if(slot >= 0 && this.getTrade() instanceof ItemTradeData trade) {
            this.getCommonTab().setStrict(slot,!trade.getItem(slot).isStrict());
        }
    }

    @Override
    protected void afterSelectionChange(TradeSlot oldSlot,TradeSlot newSlot) {
        int slot = this.getItemSelection();
        if(slot >= 0) //Update the custom name input with the current value
            this.customNameInput.setValue(this.getCurrentCustomName());
    }

}