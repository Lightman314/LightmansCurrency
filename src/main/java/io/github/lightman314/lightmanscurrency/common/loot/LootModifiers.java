package io.github.lightman314.lightmanscurrency.common.loot;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.RegistryObject;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> new CoinsInChestsModifier.Serializer());
		
	}
	
	public static final RegistryObject<GlobalLootModifierSerializer<CoinsInChestsModifier>> COINS_IN_CHESTS;
	
}