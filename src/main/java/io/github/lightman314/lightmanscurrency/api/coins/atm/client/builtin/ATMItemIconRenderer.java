package io.github.lightman314.lightmanscurrency.api.coins.atm.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMItemIcon;

public final class ATMItemIconRenderer extends ATMIconRenderer {

    public static final ATMItemIconRenderer INSTANCE = new ATMItemIconRenderer();

    private ATMItemIconRenderer() {}

    @Override
    protected void render(ATMIconData icon, FancyGuiExtractor gui, boolean hovered) {
        if(icon instanceof ATMItemIcon i) {
            gui.item(i.item.create(),icon.xPos,icon.yPos);
        }
    }

}
