package io.github.lightman314.lightmanscurrency.api.icon.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.TextIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

public class TextIconRenderer extends IconRenderer {

    public static final IconRenderer INSTANCE = new TextIconRenderer();
    private TextIconRenderer() {}

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,IconData icon,int x,int y) {
        if(icon instanceof TextIcon ti)
            gui.centeredText(ti.getText(),x + 8,y + 4,-1,true);

    }
}
