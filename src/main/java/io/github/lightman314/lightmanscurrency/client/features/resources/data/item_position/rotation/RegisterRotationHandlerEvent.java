package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public final class RegisterRotationHandlerEvent extends Event implements IModBusEvent {

    private final Map<Identifier,MapCodec<? extends RotationHandler>> registry;
    @ApiStatus.Internal
    public RegisterRotationHandlerEvent(Map<Identifier,MapCodec<? extends RotationHandler>> registry) { this.registry = registry; }

    public void register(Identifier id,MapCodec<? extends RotationHandler> codec) {
        this.registry.put(id,codec);
    }

}