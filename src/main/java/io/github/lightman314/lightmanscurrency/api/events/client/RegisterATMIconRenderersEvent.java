package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer.ATMIconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterATMIconRenderersEvent extends Event implements IModBusEvent {

    private final Map<ResourceLocation,ATMIconRenderer> results = new HashMap<>();

    public RegisterATMIconRenderersEvent() {}

    public Map<ResourceLocation,ATMIconRenderer> getIconRenderers() { return ImmutableMap.copyOf(this.results); }

    public void register(IconType type,ATMIconRenderer renderer) { this.register(type.type,renderer); }
    public void register(ResourceLocation type,ATMIconRenderer renderer)
    {
        if(this.results.put(type,renderer) != null)
            LightmansCurrency.LogWarning("Duplicate ATM icon renderer was registered for " + type);
    }

    public void registerSet(ATMIconRenderer renderer,IconType... types)
    {
        for(IconType t : types)
            this.register(t.type,renderer);
    }
    public void registerSet(ATMIconRenderer renderer,ResourceLocation... types)
    {
        for(ResourceLocation t : types)
            register(t,renderer);
    }

}