package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.MultiIcon;

public class MultiIconRenderer extends IconRenderer<MultiIcon> {
    @Override
    protected void renderInternal(MultiIcon icon, EasyGuiGraphics gui, int x, int y) {
        for(IconData i : icon.icons)
            IconRenderer.renderIcon(i,gui,x,y);
    }
}
