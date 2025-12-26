package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterATMIconRenderersEvent;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ATMIconRenderer {

    private static Map<ResourceLocation,ATMIconRenderer> iconRenderers;
    private static final Set<ResourceLocation> warned = new HashSet<>();

    @ApiStatus.Internal
    public static void initialize()
    {
        if(iconRenderers == null)
            iconRenderers = ModLoader.postEventWithReturn(new RegisterATMIconRenderersEvent()).getIconRenderers();
    }

    public static <T extends ATMIconData> void renderIcon(ATMExchangeButton button, ATMIconData icon, EasyGuiGraphics gui, boolean isHovered)
    {
        ATMIconRenderer renderer = iconRenderers.get(icon.getType());
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
