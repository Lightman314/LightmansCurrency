package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeTooltipBuilder;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemDisplay extends TradeDisplayEntry.ManualTooltipRender {

    private final ItemStack stack;
    @Nullable
    private final String countText;
    @Nullable
    private final Identifier emptyBackground;
    private final ItemTooltipModifier tooltipEditor;
    public ItemDisplay(ItemStack stack,@Nullable String countText,@Nullable Identifier emptyBackground,ItemTooltipModifier tooltipEditor) {
        this.stack = stack;
        this.countText = countText;
        this.emptyBackground = emptyBackground;
        this.tooltipEditor = tooltipEditor;
    }

    public static ItemDisplay of(ItemStack stack) { return new ItemDisplay(stack,null,null,(t,i) -> {}); }
    public static ItemDisplay of(ItemStack stack,Identifier emptyBackground) { return new ItemDisplay(stack,null,emptyBackground,(t,i) -> {}); }
    public static ItemDisplay of(ItemStack stack,ItemTooltipModifier tooltipEditor) { return new ItemDisplay(stack,null,null,tooltipEditor); }
    public static ItemDisplay of(ItemStack stack,Identifier emptyBackground,ItemTooltipModifier tooltipEditor) { return new ItemDisplay(stack,null,emptyBackground,tooltipEditor); }

    @Override
    public int desiredWidth() { return 16; }
    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        if(this.stack.isEmpty())
        {
            if(this.emptyBackground != null)
                gui.blitSprite(this.emptyBackground,x,y + 1,16,16);
        }
        else
            gui.item(this.stack,x,y + 1,this.countText);
    }

    @Override
    public boolean shouldRenderTooltip() { return !this.stack.isEmpty(); }

    @Override
    public void renderCustomTooltip(FancyGuiExtractor gui,TradeTooltipBuilder.Results tooltips,ClientTooltipPositioner positioner) {
        if(this.stack.isEmpty())
            gui.renderPositionedTooltip(tooltips.getAllLines(),positioner);
        else
            gui.renderPositionedItemTooltip(this.stack,this.itemTooltipModifier(tooltips),positioner);
    }

    private Consumer<List<Component>> itemTooltipModifier(TradeTooltipBuilder.Results tooltips) {
        return list -> {
            this.tooltipEditor.modifyItemTooltips(list,tooltips::addInfo);
            list.addAll(tooltips.getAllLines());
        };
    }

    public interface ItemTooltipModifier {
        void modifyItemTooltips(List<Component> itemTooltips,Consumer<Component> infoBuilder);
    }

}