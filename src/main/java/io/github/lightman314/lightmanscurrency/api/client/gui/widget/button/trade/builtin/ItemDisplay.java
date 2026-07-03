package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
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
    private final Consumer<List<Component>> tooltipEditor;
    public ItemDisplay(ItemStack stack,@Nullable String countText,@Nullable Identifier emptyBackground,Consumer<List<Component>> tooltipEditor) {
        this.stack = stack;
        this.countText = countText;
        this.emptyBackground = emptyBackground;
        this.tooltipEditor = tooltipEditor;
    }

    public static ItemDisplay of(ItemStack stack) { return new ItemDisplay(stack,null,null,t -> {}); }
    public static ItemDisplay of(ItemStack stack,Identifier emptyBackground) { return new ItemDisplay(stack,null,emptyBackground,t -> {}); }
    public static ItemDisplay of(ItemStack stack,Consumer<List<Component>> tooltipEditor) { return new ItemDisplay(stack,null,null,tooltipEditor); }
    public static ItemDisplay of(ItemStack stack,Identifier emptyBackground,Consumer<List<Component>> tooltipEditor) { return new ItemDisplay(stack,null,null,tooltipEditor); }

    @Override
    public int desiredWidth() { return 16; }
    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        if(this.stack.isEmpty())
            gui.blitSprite(this.emptyBackground,x,y + 1,16,16);
        else
            gui.item(this.stack,x,y,this.countText);
    }

    @Override
    public void renderCustomTooltip(FancyGuiExtractor gui,List<Component> tooltips) {
        if(this.stack.isEmpty())
            gui.renderTooltipAtMouse(tooltips);
        else
            gui.renderItemTooltipAtMouse(this.stack,this.tooltipEditor.andThen(l -> l.addAll(tooltips)));
    }

}