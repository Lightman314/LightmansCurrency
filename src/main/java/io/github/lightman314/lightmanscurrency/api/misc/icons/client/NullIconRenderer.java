package io.github.lightman314.lightmanscurrency.api.misc.icons.client;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;

public final class NullIconRenderer extends IconRenderer<IconData> {

    public static final NullIconRenderer INSTANCE = new NullIconRenderer();

    private NullIconRenderer() {}

    @Override
    protected void renderInternal(IconData icon, EasyGuiGraphics gui, int x, int y) { }

}
