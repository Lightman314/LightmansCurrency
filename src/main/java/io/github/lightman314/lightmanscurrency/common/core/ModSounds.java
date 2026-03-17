package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, LightmansCurrency.MODID);

    public static final Supplier<SoundEvent> COINS_CLINKING = register("coins_clinking", () -> SoundEvent.createVariableRangeEvent(LightmansCurrency.id("coins_clinking")));

    public static DeferredHolder<SoundEvent,SoundEvent> register(String name,Supplier<SoundEvent> factory) {
        return REGISTER.register(name,factory);
    }
	
}
