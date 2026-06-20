package io.github.lightman314.lightmanscurrency.api.icon.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

public final class SpriteRenderer extends IconRenderer {

    public static final IconRenderer INSTANCE = new SpriteRenderer();
    private SpriteRenderer() {}

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, IconData icon, int x, int y) {
        if(icon instanceof SpriteIcon i)
            gui.blitSprite(i.sprite(),x,y,16,16);
    }

}
