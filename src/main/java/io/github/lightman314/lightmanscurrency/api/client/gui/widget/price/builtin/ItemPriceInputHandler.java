package io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.ItemSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.AdvancedTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.world.item.ItemStack;

public class ItemPriceInputHandler extends PriceInputHandler {

    private ItemSelectionWidget itemSelection = null;

    private static final ScreenPosition LOCAL_STRICT_POS = ItemTradeEditClientTab.STRICT_BUTTON_POS.relativeTo(0,AdvancedTradeEditClientTab.PRICE_INPUT_Y);

    public ItemPriceInputHandler() {}

    @Override
    public DropdownOption inputOption() { return new DropdownOption(ItemPrice.OPTION_NAME.get()); }

    @Override
    public TradePriceType<?> getPriceType() { return ItemPrice.TYPE; }

    @Override
    public boolean isForValue(TradePrice price) { return price instanceof ItemPrice; }

    @Override
    public void initialize(ScreenArea area) {
        //Item Selection Widget with no filter defined
        this.itemSelection = this.addChild(ItemSelectionWidget.builder()
                .atPos(area.pos.offset(15,31))
                .withSize(10,3)
                .withOldWidget(this.itemSelection)
                .withListener(this::setItem)
                .withStackSizeOffset(area.width - 15,0)
                .visible(this::isVisible)
                .build());

        //Strict Checkbox
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(LOCAL_STRICT_POS))
                .onPress(this::toggleStrict)
                .withSprite(LCSprites.CHECKBOX.buildSprite(this::isStrict))
                .visible(this::isStrictOptionVisible)
                .build());

    }

    @Override
    protected void extractBG(FancyGuiExtractor gui, ScreenArea area, TextSettings settings) {
        //Strict price text
        if(this.getCurrentValue() instanceof ItemPrice ip && ip.getItem(this.getCurrentSlot()).allowStrictToggle())
            gui.text(TradeItem.GUI_TRADE_ITEM_ENFORCE_DATA.get(),LOCAL_STRICT_POS.x + 11, LOCAL_STRICT_POS.y + 1,0xFF404040,false);
    }

    private void setItem(ItemStack stack) {
        LightmansCurrency.LogDebug("Sending setItem packet for " + stack.getHoverName().getString() + " to slot " + this.getCurrentSlot());
        this.sendPriceChange(FancyPacketMap.map()
                .setItem("setItem",stack));
    }

    private boolean isStrict() {
        return this.getCurrentValue() instanceof ItemPrice ip && ip.getItem(this.getCurrentSlot()).isStrict();
    }

    private boolean isStrictOptionVisible() {
        return this.isVisible() && this.getCurrentValue() instanceof ItemPrice ip && ip.getItem(this.getCurrentSlot()).allowStrictToggle();
    }

    private void toggleStrict() {
        if(this.getCurrentValue() instanceof ItemPrice ip) {
            int slot = this.getCurrentSlot();
            boolean newValue = !ip.getItem(slot).isStrict();
            LightmansCurrency.LogDebug("Sending setStrict packet for slot " + slot + " " + newValue);
            this.sendPriceChange(FancyPacketMap.map()
                    .setBoolean("setStrict",newValue));
        }
    }

}
