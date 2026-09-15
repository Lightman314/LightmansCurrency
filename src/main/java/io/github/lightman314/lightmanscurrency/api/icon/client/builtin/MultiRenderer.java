package io.github.lightman314.lightmanscurrency.api.icon.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.MultiIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

public final class MultiRenderer extends IconRenderer {

    public static final IconRenderer INSTANCE = new MultiRenderer();

    private MultiRenderer() {}

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, IconData icon, int x, int y) {
        if(icon instanceof MultiIcon i)
        {
            for(IconData child : i.icons)
                IconRenderer.extractState(gui,child,x,y);
        }
    }
}
