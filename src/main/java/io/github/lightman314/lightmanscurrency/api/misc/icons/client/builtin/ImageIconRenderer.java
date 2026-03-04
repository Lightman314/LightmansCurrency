package io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ImageIcon;

public class ImageIconRenderer extends IconRenderer<ImageIcon> {
    @Override
    protected void renderInternal(ImageIcon icon, EasyGuiGraphics gui, int x, int y) {
        new NormalSprite(icon.sprite).render(gui,x,y);
    }
}
