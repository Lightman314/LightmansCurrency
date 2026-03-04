package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.TextIcon;

public class TextIconRenderer extends IconRenderer<TextIcon> {
    @Override
    protected void renderInternal(TextIcon icon, EasyGuiGraphics gui, int x, int y) {
        int xPos = x + 8 - (gui.font.width(icon.text)/2);
        int yPos = y + ((16 - gui.font.lineHeight) / 2);
        gui.drawShadowed(icon.text, xPos, yPos, icon.textColor);
    }
}
