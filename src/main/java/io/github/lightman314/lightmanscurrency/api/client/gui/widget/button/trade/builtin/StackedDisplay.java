package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StackedDisplay extends TradeDisplayEntry {

    private final List<TradeDisplayEntry> entries;
    private StackedDisplay(List<TradeDisplayEntry> children) { this.entries = children; }

    public static TradeDisplayEntry of(TradeDisplayEntry... entries) { return of(ImmutableList.copyOf(entries)); }
    public static TradeDisplayEntry of(List<TradeDisplayEntry> list) {
        if(list.isEmpty())
            return EmptyDisplay.INSTANCE;
        if(list.size() == 1)
            return list.getFirst();
        return new StackedDisplay(ImmutableList.copyOf(list));
    }

    @Override
    public int desiredWidth() {
        int maxWidth = 0;
        for(TradeDisplayEntry entry : this.entries)
            maxWidth = Math.max(entry.desiredWidth(),maxWidth);
        return maxWidth;
    }

    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        for(TradeDisplayEntry entry : this.entries)
            entry.extractContents(gui,x,y);
    }

    @Override
    public void appendTooltip(List<Component> tooltip) {
        for(TradeDisplayEntry entry : this.entries)
            entry.appendTooltip(tooltip);
    }

}
