package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SortByOffers extends TerminalSortType {

    public static final SortByOffers INSTANCE = new SortByOffers();
    private SortByOffers() { super(VersionUtil.lcResource("offers")); }
    @Override
    protected int sort(TraderData a, TraderData b) {
        //Inverted so that larget number sorts first
        return Integer.compare(b.validTradeCount(),a.validTradeCount());
    }
}
