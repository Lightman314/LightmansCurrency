package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSounds {

	public static void init() {}
	
	static {
		
		COINS_CLINKING = ModRegistries.SOUND_EVENTS.register("coins_clinking", () -> SoundEvent.createVariableRangeEvent(VersionUtil.lcResource("coins_clinking")));
		
	}
	
	public static final Supplier<SoundEvent> COINS_CLINKING;
	
}
