package io.github.lightman314.lightmanscurrency.common.loot;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import io.github.lightman314.lightmanscurrency.common.loot.glm.BonusItemModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.function.Supplier;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> CoinsInChestsModifier.SERIALIZER);
		BONUS_ITEM = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("bonus_item", () -> BonusItemModifier.CODEC);

	}
	
	public static final Supplier<MapCodec<? extends IGlobalLootModifier>> COINS_IN_CHESTS;
	public static final Supplier<MapCodec<? extends IGlobalLootModifier>> BONUS_ITEM;
	
}
