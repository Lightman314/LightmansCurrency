package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;

public class ItemIconRenderer extends IconRenderer<ItemIcon> {
    @Override
    protected void renderInternal(ItemIcon icon, EasyGuiGraphics gui, int x, int y) {
        gui.renderItem(icon.iconStack,x,y,icon.countTextOverride.orElse(null));
    }
}
