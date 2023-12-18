package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EmptyPriceEntry extends DisplayEntry {

    private final MoneyValue price;

    public EmptyPriceEntry(@Nonnull MoneyValue price, @Nullable List<Component> additionalTooltips) {
        super(additionalTooltips);
        this.price = price;
    }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        gui.resetColor();
        if (this.price.isFree()) {
            Font font = this.getFont();
            int left = x + area.xOffset() + (area.width() / 2) - (font.width(this.price.getText()) / 2);
            int top = y + area.yOffset() + (area.height() / 2) - (font.lineHeight / 2);
            gui.drawString(this.price.getText(), left, top, 0xFFFFFF);
        }
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = x + area.xOffset();
        int top = y + area.yOffset();
        return mouseX >= left && mouseX < left + area.width() && mouseY >= top && mouseY < top + area.height();
    }
}
