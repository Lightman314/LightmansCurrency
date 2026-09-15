package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeTooltipBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EmptyDisplay extends TradeDisplayEntry {

    public static final EmptyDisplay INSTANCE = new EmptyDisplay(0);

    private final int width;
    private EmptyDisplay(int width) { this.width = width; }

    @Override
    public int desiredWidth() { return this.width; }
    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) { }
    @Override
    public void appendTooltip(TradeTooltipBuilder tooltip) { }

}