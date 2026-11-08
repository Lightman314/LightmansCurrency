package io.github.lightman314.lightmanscurrency.client.features.tickets;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketTradeResources;
import io.github.lightman314.lightmanscurrency.features.tickets.trader.TicketPrice;

public class TicketPriceInputHandler extends PriceInputHandler {

    @Override
    public DropdownOption inputOption() { return new DropdownOption(TicketPrice.OPTION_NAME.get()); }

    @Override
    public TradePriceType<?> getPriceType() { return TicketPrice.TYPE; }

    @Override
    public boolean isForValue(TradePrice price) { return price instanceof TicketPrice; }

    @Override
    public void initialize(ScreenArea area) {

        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(20,40))
                .withSprite(LCSprites.CHECKBOX.buildSprite(this::getStoreTicketStubs))
                .onPress(this::toggleStoreStubs)
                .visible(this::canStoreTicketStubs)
                .build());

    }

    private boolean getStoreTicketStubs() {
        if(this.getCurrentValue() instanceof TicketPrice tp)
            return tp.storeTicketStubs();
        return false;
    }

    private void toggleStoreStubs() {
        this.sendPriceChange(FancyPacketMap.map()
                .setBoolean("setStoreStub",!this.getStoreTicketStubs()));
    }

    private boolean canStoreTicketStubs() {
        TraderData trader = this.getTrader();
        return trader != null && (trader.providesResources(BuiltInResourceTypes.ITEM) || trader.providesResources(TicketTradeResources.STUB_COLLECTOR));
    }

    @Override
    protected void extractBG(FancyGuiExtractor gui, ScreenArea area, TextSettings settings) {

        //Store Ticket Stubs label
        gui.text(TicketPrice.GUI_STORE_TICKET_STUBS.get(),30,41,0xFF404040,false);

    }

}
