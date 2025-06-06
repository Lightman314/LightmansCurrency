package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValuePair;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CoinPriceEntry extends DisplayEntry {
    private final CoinValue price;

    public CoinPriceEntry(@Nonnull CoinValue price, @Nullable List<Component> additionalTooltips, boolean tooltipOverride) {
        super(getTooltip(price, additionalTooltips, tooltipOverride));
        this.price = price;
    }

    private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

    private static List<Component> getTooltip(@Nonnull MoneyValue price, @Nullable List<Component> additionalTooltips, boolean tooltipOverride) {
        List<Component> tooltips = new ArrayList<>();
        //Put bonus tooltips first
        if(additionalTooltips != null)
            tooltips.addAll(additionalTooltips);
        if(tooltipOverride && additionalTooltips != null)
            return additionalTooltips;
        if(!price.isFree() && !price.isEmpty())
            tooltips.add(price.getText());
        return tooltips;
    }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        gui.resetColor();
        List<CoinValuePair> entries = this.price.getEntries();
        if(entries.size() * 16 <= area.width() || entries.size() == 1)
        {
            List<DisplayData> entryPositions = area.divide(entries.size());
            for(int i = 0; i < entryPositions.size() && i < entries.size(); ++i)
            {
                DisplayData pos = entryPositions.get(i);
                int left = this.getTopLeft(x + pos.xOffset(), pos.width());
                int top = this.getTopLeft(y + pos.yOffset(), pos.height());
                ItemStack stack = new ItemStack(entries.get(i).coin);
                stack.setCount(entries.get(i).amount);
                gui.renderItem(stack, left, top);
            }
        }
        else if(entries.size() > 1)
        {
            int spacing = (area.width() - 16) / (entries.size() - 1);
            int top = this.getTopLeft(y + area.yOffset(), area.height());
            int left = x + area.xOffset() + area.width() - 16;
            //Draw cheapest to most expensive
            for(int i = entries.size() - 1; i >= 0; --i)
            {
                ItemStack stack = new ItemStack(entries.get(i).coin);
                stack.setCount(entries.get(i).amount);
                gui.renderItem(stack, left, top);
                left -= spacing;
            }
        }

    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = x + area.xOffset();
        int top = y + area.yOffset();
        return mouseX >= left && mouseX < left + area.width() && mouseY >= top && mouseY < top + area.height();
    }

}
