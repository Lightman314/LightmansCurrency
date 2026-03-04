package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.NumberIcon;

public class NumberIconRenderer extends IconRenderer<NumberIcon> {
    @Override
    protected void renderInternal(NumberIcon icon, EasyGuiGraphics gui, int x, int y) {
        String text = String.valueOf(icon.number);
        int width = gui.font.width(text);
        gui.drawShadowed(text,x + 17 - width,y + 9,0xFFFFFF);
    }
}
