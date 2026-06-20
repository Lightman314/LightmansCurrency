package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class TradeDisplayEntry {

    public abstract int desiredWidth();

    public abstract void extractContents(FancyGuiExtractor gui, int x, int y);

    public boolean appendTooltip(FancyGuiExtractor gui,List<Component> tooltip) { return false; }

}