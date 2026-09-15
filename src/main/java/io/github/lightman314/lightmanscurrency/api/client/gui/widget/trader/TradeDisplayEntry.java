package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

public abstract class TradeDisplayEntry {

    public abstract int desiredWidth();

    public abstract void extractContents(FancyGuiExtractor gui, int x, int y);

    public abstract void appendTooltip(TradeTooltipBuilder tooltip);

    public static abstract class ManualTooltipRender extends TradeDisplayEntry {

        @Override
        public void appendTooltip(TradeTooltipBuilder builder) {}

        public boolean shouldRenderTooltip() { return true; }

        public abstract void renderCustomTooltip(FancyGuiExtractor gui, TradeTooltipBuilder.Results tooltip, ClientTooltipPositioner positioner);

    }

}