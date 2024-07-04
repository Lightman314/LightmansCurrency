package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSounds {

	public static void init() {}
	
	static {
		
		COINS_CLINKING = ModRegistries.SOUND_EVENTS.register("coins_clinking", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "coins_clinking")));
		
	}
	
	public static final Supplier<SoundEvent> COINS_CLINKING;
	
}
