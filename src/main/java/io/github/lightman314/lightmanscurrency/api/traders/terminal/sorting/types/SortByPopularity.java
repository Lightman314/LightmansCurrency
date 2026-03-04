package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.LoggerNode;

public class SortByPopularity extends TerminalSortType {

    public static final SortByPopularity INSTANCE = new SortByPopularity();
    private SortByPopularity() { super(LightmansCurrency.id("popularity")); }
    @Override
    protected int sort(TraderData a, TraderData b) {
        int popA = a.findNodeValue(LoggerNode.TYPE, node -> node.statTracker.getStat(StatKeys.Traders.TRADES_EXECUTED,0),0);
        int popB = b.findNodeValue(LoggerNode.TYPE, node -> node.statTracker.getStat(StatKeys.Traders.TRADES_EXECUTED,0),0);
        //Invert sorting so that bigger number goes first
        return Integer.compare(popB,popA);
    }
}
