package io.github.lightman314.lightmanscurrency.api.coins.atm.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.HashSet;
import java.util.Set;

public abstract class ATMIconRenderer {

    public static final ClientPairedRegistry<ATMIconType,ATMIconRenderer> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Coins.ATM_ICON_TYPE);

    private static final Set<ATMIconType> warned = new HashSet<>();

    public static void renderIcon(ScreenPosition buttonPos, ATMIconData icon, FancyGuiExtractor gui,boolean hovered) {
        ATMIconRenderer renderer = REGISTRY.getNullableValue(icon.getType());
        if(renderer != null) {
            gui.push(buttonPos);
            renderer.render(icon,gui,hovered);
            gui.pop();
        }
        else if(!warned.contains(icon.getType())) {
            warned.add(icon.getType());
            LightmansCurrency.LogWarning("ATM Icon of type " + icon.getType() + " does not have a render registered for it!");
        }

    }

    protected abstract void render(ATMIconData icon,FancyGuiExtractor gui,boolean hovered);

}
