package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.util.ClientRegistry;

import java.util.HashSet;
import java.util.Set;

public abstract class ATMIconRenderer {

    public static final ClientRegistry<ATMIconType,ATMIconRenderer> REGISTRY = new ClientRegistry<>(LCRegistries.ATM_ICON_TYPE,"ATM Icon Type");

    private static final Set<ATMIconType> warned = new HashSet<>();

    public static <T extends ATMIconData> void renderIcon(ATMExchangeButton button, ATMIconData icon, EasyGuiGraphics gui, boolean isHovered)
    {
        ATMIconRenderer renderer = REGISTRY.getOrDefault(icon.getType());
        if(renderer != null)
            renderer.render(button,icon,gui,isHovered);
        else if(!warned.contains(icon.getType()))
        {
            warned.add(icon.getType());
            LightmansCurrency.LogWarning("ATM Icon of type " + icon.getType() + " does not have a renderer registered for it!");
        }
    }

    public abstract void render(ATMExchangeButton button, ATMIconData icon, EasyGuiGraphics gui, boolean isHovered);

}
