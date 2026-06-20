package io.github.lightman314.lightmanscurrency.api.icon.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

public final class ItemRenderer extends IconRenderer {

    public static final IconRenderer INSTANCE = new ItemRenderer();
    private ItemRenderer() {}

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, IconData icon, int x, int y) {
        if(icon instanceof ItemIcon i)
            gui.item(i.item(),x,y,i.countOverride().orElse(null));
    }
}