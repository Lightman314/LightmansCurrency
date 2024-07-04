package io.github.lightman314.lightmanscurrency.common.loot;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.function.Supplier;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> CoinsInChestsModifier.SERIALIZER);
		
	}
	
	public static final Supplier<MapCodec<? extends IGlobalLootModifier>> COINS_IN_CHESTS;
	
}
