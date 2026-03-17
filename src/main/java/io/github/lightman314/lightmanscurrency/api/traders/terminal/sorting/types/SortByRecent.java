package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InteractionNode;

public class SortByRecent extends TerminalSortType {

    public static final SortByRecent INSTANCE = new SortByRecent();
    private SortByRecent() { super(LightmansCurrency.id("recent")); }

    @Override
    protected int sort(TraderData a, TraderData b) {
        //Inverted sorting so that bigger number goes first
        return Long.compare(b.findNodeValue(InteractionNode.TYPE,InteractionNode::getLastInteractionTime,0).longValue(),a.findNodeValue(InteractionNode.TYPE,InteractionNode::getLastInteractionTime,0).longValue());
    }

}
