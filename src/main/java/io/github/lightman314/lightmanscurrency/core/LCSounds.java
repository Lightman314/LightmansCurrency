package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class LCSounds {
    private LCSounds() { }

    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT,LCApi.MODID);

    public static final DeferredHolder<SoundEvent,SoundEvent> COINS_CLINKING = register("coins_clinking",SoundEvent::createVariableRangeEvent);

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name, Function<Identifier,SoundEvent> factory) {
        return REGISTER.register(name,factory);
    }

}
