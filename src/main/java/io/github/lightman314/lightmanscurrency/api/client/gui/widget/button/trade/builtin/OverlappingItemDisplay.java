package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlappingItemDisplay extends TradeDisplayEntry {

    private final List<ItemStack> items;
    private final List<Component> tooltips;
    private final int width;
    private OverlappingItemDisplay(List<ItemStack> items,List<Component> tooltip,int width) {
        this.items = items;
        this.tooltips = tooltip;
        this.width = width;
    }

    public static OverlappingItemDisplay of(List<ItemStack> items,int width) { return of(items,List.of(),width); }
    public static OverlappingItemDisplay of(List<ItemStack> items,List<Component> tooltips,int width) { return new OverlappingItemDisplay(items,tooltips,width); }

    @Override
    public int desiredWidth() { return this.width; }

    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        int currentX;
        int offset = 17;
        int size = this.items.size();
        if((size * 17) - 1 > this.width)
        {
            //Calculate how much space to give each item
            currentX = x;
            offset = (this.width - 16) / (size - 1);
        }
        else
        {
            //Calculate the new starting X position to center the items
            currentX = x + (this.width - (8 * size) - (size - 1)) / 2;
        }
        //Render the items
        for(ItemStack item : this.items)
        {
            gui.item(item,currentX,1);
            currentX += offset;
        }
    }

    @Override
    public void appendTooltip(List<Component> tooltip) {
        tooltip.addAll(this.tooltips);
    }

}