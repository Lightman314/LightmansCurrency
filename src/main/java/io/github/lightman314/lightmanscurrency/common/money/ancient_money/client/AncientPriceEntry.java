package io.github.lightman314.lightmanscurrency.common.money.ancient_money.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AncientPriceEntry extends DisplayEntry {

    private final AncientMoneyValue price;

    public AncientPriceEntry(@Nonnull AncientMoneyValue price, @Nullable List<Component> additionalTooltips, boolean tooltipOverride)
    {
        super(getTooltip(price,additionalTooltips,tooltipOverride));
        this.price = price;
    }

    private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

    private static List<Component> getTooltip(@Nonnull AncientMoneyValue price, @Nullable List<Component> additionalTooltips, boolean tooltipOverride) {
        List<Component> tooltips = new ArrayList<>();
        //Put bonus tooltips first
        if(additionalTooltips != null)
            tooltips.addAll(additionalTooltips);
        if(tooltipOverride && additionalTooltips != null)
            return additionalTooltips;
        if(!price.isFree() && !price.isEmpty())
            tooltips.add(LCText.ANCIENT_COIN_VALUE_DISPLAY.get(price.count,price.type.asItem().getHoverName()));
        return tooltips;
    }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        gui.resetColor();
        int top = this.getTopLeft(y + area.yOffset(), area.height());
        int left = this.getTopLeft(x + area.xOffset(),area.width()) + (area.width() / 2) - 16;
        //Draw Coin Sprite
        AncientCoinType type = this.price.type;
        ItemStack stack = type.asItem(this.price.count);
        gui.renderItem(stack, left, top);
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = x + area.xOffset();
        int top = y + area.yOffset();
        return mouseX >= left && mouseX < left + area.width() && mouseY >= top && mouseY < top + area.height();
    }

}
