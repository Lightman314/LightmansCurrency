package io.github.lightman314.lightmanscurrency.common.loot;

import com.mojang.serialization.Codec;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.*;
import net.minecraftforge.registries.RegistryObject;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> CoinsInChestsModifier.CODEC);

		BONUS_ITEM = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("bonus_item", () -> BonusItemModifier.CODEC);

	}
	
	public static final RegistryObject<Codec<CoinsInChestsModifier>> COINS_IN_CHESTS;
	public static final RegistryObject<Codec<BonusItemModifier>> BONUS_ITEM;

}
