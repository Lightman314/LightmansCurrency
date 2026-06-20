package io.github.lightman314.lightmanscurrency.api.icon.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.EmptyRenderer;

public abstract class IconRenderer {

    public static final ClientPairedRegistry<IconType<?>,IconRenderer> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Misc.ICON_TYPE,IconRenderer::getEmpty);

    private static IconRenderer getEmpty() { return EmptyRenderer.INSTANCE; }

    public static void extractState(FancyGuiExtractor gui,IconData icon,int x,int y) {
        IconRenderer r = REGISTRY.getValue(icon.getType());
        r.extractRenderState(gui,icon,x,y);
    }

    protected abstract void extractRenderState(FancyGuiExtractor gui,IconData icon,int x,int y);

}