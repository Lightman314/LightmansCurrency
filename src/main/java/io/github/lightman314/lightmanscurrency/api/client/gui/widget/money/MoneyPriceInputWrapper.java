package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;

public final class MoneyPriceInputWrapper extends PriceInputHandler {

    private final MoneyInputHandler handler;
    public MoneyPriceInputWrapper(MoneyInputHandler inputHandler) {
        this.handler = inputHandler;
        this.handler.internalSetup(this::isVisible,this,this::getCurrentMoneyValue,this::acceptChange);
    }

    @Override
    public DropdownOption inputOption() { return this.handler.inputOption(); }

    @Override
    public TradePriceType<?> getPriceType() { return MoneyPrice.TYPE; }
    @Override
    public DualKey getKey() { return DualKey.create(MoneyPrice.TYPE,this.handler.getKey().toString()); }
    public DualKey getMoneyKey() { return this.handler.getKey(); }
    @Override
    public boolean isForValue(TradePrice price) { return price instanceof MoneyPrice mp && this.handler.isForValue(mp.getPrice()); }

    private MoneyValue getCurrentMoneyValue() {
        TradePrice price = this.getCurrentValue();
        if(price instanceof MoneyPrice mp)
            return mp.getPrice();
        return MoneyValue.empty();
    }

    private void acceptChange(MoneyValue value) {
        this.sendPriceChange(FancyPacketMap.map().set("setPrice",LCFancyPacketTypes.MONEY,value));
    }

    @Override
    protected void copyHandlerState(PriceInputHandler lastHandler) {
        if(lastHandler instanceof MoneyPriceInputWrapper wrapper)
            this.handler.copyHandlerState(wrapper.handler);
    }

    private ScreenArea createMoneyArea(ScreenArea area) {
        return area.pos.offset(area.halfWidth() - MoneyValueWidget.HALF_WIDTH,12).asArea(MoneyValueWidget.WIDTH,MoneyValueWidget.HEIGHT);
    }

    @Override
    public void initialize(ScreenArea area) {
        area = this.createMoneyArea(area);
        //Cache this as the last selected handler for future checks
        MoneyValueWidget.lastSelectedHandler = this.handler.getKey();
        //Add free toggle
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(area.width - 14,4))
                .withSprite(MoneyValueWidget.SPRITE_FREE_TOGGLE)
                .onPress(this::toggleFree)
                .visible(this::isVisible)
                .build());
        //Then add the normal handler widgets
        this.handler.initialize(area);
    }

    private void toggleFree() {
        MoneyValue newValue = this.getCurrentMoneyValue().isFree() ? MoneyValue.empty() : MoneyValue.free();
        this.acceptChange(newValue);
        //Inform the handler about the value change
        this.handler.onValueChanged(newValue);
    }

    @Override
    public void renderTick(ScreenPosition mousePos) {
        this.handler.renderTick(mousePos);
    }

    @Override
    protected void extractBG(FancyGuiExtractor gui,ScreenArea area,TextSettings settings) {
        area = this.createMoneyArea(area);
        //Push the gui extractor to the new simulated corner
        gui.push(area.pos);
        this.handler.extractBG(gui,area,settings);
        //Render the current price in the top-right corner
        Component text = this.getCurrentMoneyValue().getText();
        int width = gui.getFont().width(text);
        gui.text(text,area.width - 15 - width,5,settings.textColor(),false);
        gui.pop();
    }

}
