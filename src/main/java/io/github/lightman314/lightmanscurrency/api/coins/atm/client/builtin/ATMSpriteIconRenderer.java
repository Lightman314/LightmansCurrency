package io.github.lightman314.lightmanscurrency.api.coins.atm.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;

public final class ATMSpriteIconRenderer extends ATMIconRenderer {

    public static final ATMSpriteIconRenderer INSTANCE = new ATMSpriteIconRenderer();

    private ATMSpriteIconRenderer() {}

    @Override
    protected void render(ATMIconData icon, FancyGuiExtractor gui, boolean hovered) {

    }
}
