package io.github.lightman314.lightmanscurrency.api.icon.client.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.EmptyIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

import java.util.HashSet;
import java.util.Set;

public final class EmptyRenderer extends IconRenderer {

    public static final IconRenderer INSTANCE = new EmptyRenderer();

    private final Set<IconType<?>> warnedTypes = new HashSet<>();

    private EmptyRenderer() {}

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, IconData icon, int x, int y) {
        if(icon != EmptyIcon.INSTANCE && !this.warnedTypes.contains(icon.getType()))
        {
            LightmansCurrency.LogError("Missing Icon Renderer for " + icon.getType());
            this.warnedTypes.add(icon.getType());
        }
    }
}