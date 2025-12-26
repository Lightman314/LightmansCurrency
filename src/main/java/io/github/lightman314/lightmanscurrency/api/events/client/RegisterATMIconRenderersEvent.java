package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer.ATMIconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterATMIconRenderersEvent extends Event implements IModBusEvent {

    private final Map<ResourceLocation, ATMIconRenderer> results = new HashMap<>();

    public RegisterATMIconRenderersEvent() {}

    public Map<ResourceLocation, ATMIconRenderer> getIconRenderers() { return ImmutableMap.copyOf(this.results); }

    public void register(ResourceLocation type, ATMIconRenderer renderer)
    {
        if(this.results.put(type,renderer) != null)
            LightmansCurrency.LogWarning("Duplicate ATM icon renderer was registered for " + type);
    }

    public void registerSet(ATMIconRenderer renderer,ResourceLocation... types)
    {
        for(ResourceLocation t : types)
            register(t,renderer);
    }

}
