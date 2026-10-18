package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.DemandPricing;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;

public class DemandPricingClientTab extends RuleClientTab<DemandPricing> {

    public DemandPricingClientTab(TradeRulesClientTab tab) { super(tab); }

    private MoneyValueWidget priceSelection;

    @Override
    public TradeRuleType<DemandPricing> getType() { return DemandPricing.TYPE;}

    @Override
    protected void initialize(ScreenArea area) {

        DemandPricing rule = this.getRule();

        this.priceSelection = this.addChild(MoneyValueWidget.builder()
                .atPos(area.pos.offset(area.halfWidth() - MoneyValueWidget.HALF_WIDTH,30))
                .oldWidget(this.priceSelection)
                .startingValue(rule.getOtherPrice())
                .handler(this::onValueChanged)
                .typeChangeListener(this::onWidgetHandlerChanged)
                .allowHandlerChange(this::allowHandlerChange)
                .allowFreeInput(this::allowFreePrice)
                .build());
        this.onWidgetHandlerChanged(this.priceSelection);

        this.addChild(TextBoxWrapper.intBuilder()
                .parser(IntParser.builder().min(1).max(this::getSmallStockMax).empty(1).build())
                .atPos(area.pos.offset(10,110))
                .ofWidth(80)
                .withStartingValue(rule.getSmallStock())
                .withHandler(this::smallStockChanged)
                .build());

        this.addChild(TextBoxWrapper.intBuilder()
                .parser(IntParser.builder().min(this::getLargeStockMin).max(DemandPricing.UPPER_STOCK_LIMIT).empty(() -> this.getRule().getSmallStock() + 1).build())
                .atPos(area.pos.offset(area.halfWidth() + 10,110))
                .ofWidth(80)
                .withStartingValue(rule.getLargeStock())
                .withHandler(this::largeStockChanged)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        //Draw Info
        Component infoText = this.getRule().getInfo();
        gui.centeredTextWithWordWrap(infoText,area.halfWidth(),6,area.width - 40,0xFF404040,false);

        //Draw STock Input Labels
        gui.text(DemandPricing.GUI_SMALL_STOCK.get(),10,101,0xFF404040,false);
        gui.text(DemandPricing.GUI_LARGE_STOCK.get(),area.halfWidth() + 10,101,0xFF404040,false);

    }

    private boolean allowFreePrice() {
        if(this.getRule().getOtherPrice().isFree())
            return true;
        return this.getCommonTab().getRuleHolder() instanceof TradeData trade && !trade.getInternalPrice().isFree();
    }

    private boolean allowHandlerChange() {
        if(this.getCommonTab().getRuleHolder() instanceof TradeData trade && trade.getInternalPrice() instanceof MoneyPrice mp)
            return mp.getPrice().isEmpty();
        return true;
    }

    private void onWidgetHandlerChanged(MoneyValueWidget widget) {
        //Attempt to force the current handller to be the same as the trades price
        if(this.getCommonTab().getRuleHolder() instanceof TradeData trade && trade.getInternalPrice() instanceof MoneyPrice mp)
            widget.tryMatchHandler(mp.getPrice());
    }

    private void onValueChanged(MoneyValue newValue) {
        this.requestChange(FancyPacketMap.map().set("changePrice", LCFancyPacketTypes.MONEY,newValue));
    }

    private int getSmallStockMax() { return this.getRule().getLargeStock() - 1; }
    private int getLargeStockMin() { return this.getRule().getSmallStock() + 1; }

    private void smallStockChanged(int smallStock) {
        this.requestChange(FancyPacketMap.map().setInt("changeSmallStock",smallStock));
    }

    private void largeStockChanged(int largeStock) {
        this.requestChange(FancyPacketMap.map().setInt("changeLargeStock",largeStock));
    }

}
