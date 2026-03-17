package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;

public class SortByOffers extends TerminalSortType {

    public static final SortByOffers INSTANCE = new SortByOffers();
    private SortByOffers() { super(LightmansCurrency.id("offers")); }
    @Override
    protected int sort(TraderData a, TraderData b) {
        //Inverted so that larget number sorts first
        return Integer.compare(b.validTradeCount(),a.validTradeCount());
    }
}
