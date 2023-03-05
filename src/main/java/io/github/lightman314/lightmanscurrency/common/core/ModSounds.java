package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

	public static void init() {}
	
	static {
		
		COINS_CLINKING = ModRegistries.SOUND_EVENTS.register("coins_clinking", () -> new SoundEvent(new ResourceLocation(LightmansCurrency.MODID, "coins_clinking")));
		
	}
	
	public static final RegistryObject<SoundEvent> COINS_CLINKING;
	
}
