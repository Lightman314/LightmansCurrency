package io.github.lightman314.lightmanscurrency.loot;

import com.mojang.serialization.Codec;

import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.loot.glm.CoinsInChestsModifier;
import net.minecraftforge.registries.RegistryObject;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> new CoinsInChestsModifier.Serializer());
		
	}
	
	public static final RegistryObject<Codec<CoinsInChestsModifier>> COINS_IN_CHESTS;
	
}
