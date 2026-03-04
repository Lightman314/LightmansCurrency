package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.IconIcon;

public class IconIconRenderer extends IconRenderer<IconIcon> {
    @Override
    protected void renderInternal(IconIcon icon, EasyGuiGraphics gui, int x, int y) {
        gui.blit(icon.location,x,y,0,0,16,16,16,16);
    }
}
