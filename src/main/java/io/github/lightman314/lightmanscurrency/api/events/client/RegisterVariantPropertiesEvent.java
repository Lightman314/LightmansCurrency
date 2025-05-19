package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;


public class RegisterVariantPropertiesEvent extends Event implements IModBusEvent {

    private final Map<ResourceLocation, VariantProperty<?>> registry = new HashMap<>();
    public Map<ResourceLocation,VariantProperty<?>> getRegistry() { return ImmutableMap.copyOf(this.registry); }

    public void register(ResourceLocation type, VariantProperty<?> property)
    {
        if(this.registry.containsKey(type))
        {
            LightmansCurrency.LogWarning("Attempted to register Variant Property " + type + " twice!");
            return;
        }
        this.registry.put(type,property);
        property.setID(type);
    }

}
