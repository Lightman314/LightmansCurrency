package io.github.lightman314.lightmanscurrency.common.loot;

import com.mojang.serialization.Codec;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import net.minecraftforge.registries.RegistryObject;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", CoinsInChestsModifier.Serializer::new);
		
	}
	
	public static final RegistryObject<Codec<CoinsInChestsModifier>> COINS_IN_CHESTS;
	
}
