package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SortByPopularity extends TerminalSortType {

    public static final SortByPopularity INSTANCE = new SortByPopularity();
    private SortByPopularity() { super(VersionUtil.lcResource("popularity")); }
    @Override
    protected int sort(TraderData a, TraderData b) {
        int popA = a.statTracker.getStat(StatKeys.Traders.TRADES_EXECUTED,0);
        int popB = b.statTracker.getStat(StatKeys.Traders.TRADES_EXECUTED,0);
        //Invert sorting so that bigger number goes first
        return Integer.compare(popB,popA);
    }
}
