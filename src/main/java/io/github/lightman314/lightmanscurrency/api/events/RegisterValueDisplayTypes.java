package io.github.lightman314.lightmanscurrency.api.events;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplaySerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Event to register the {@link ValueDisplaySerializer Value Display Serializers} to the system.
 * Run only once during the mods common setup phase.
 */
public final class RegisterValueDisplayTypes extends Event implements IModBusEvent {

    private final Map<ResourceLocation, ValueDisplaySerializer> registry = new HashMap<>();

    public Map<ResourceLocation,ValueDisplaySerializer> getResults() { return ImmutableMap.copyOf(this.registry); }

    /**
     * Registers the {@link ValueDisplaySerializer}
     */
    public void register(@Nonnull ValueDisplaySerializer serializer) {
        if(this.registry.containsKey(serializer.getType()))
        {
            LightmansCurrency.LogError("Value Display Serializer of type '" + serializer.getType() + "' has already been registered!");
            return;
        }
        this.registry.put(serializer.getType(), serializer);
    }

}
