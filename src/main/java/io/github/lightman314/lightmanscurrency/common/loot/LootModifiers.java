package io.github.lightman314.lightmanscurrency.common.loot;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.variants.block.loot.VariantDataModifier;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.loot.glm.*;

import java.util.function.Supplier;

public class LootModifiers {
	
	public static void init() {}
	
	static {
		
		COINS_IN_CHESTS = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("coins_in_chests", () -> CoinsInChestsModifier.SERIALIZER);
		BONUS_ITEM = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("bonus_item", () -> BonusItemModifier.CODEC);
		MODEL_VARIANT = ModRegistries.GLOBAL_LOOT_MODIFIERS.register("model_variant", () -> VariantDataModifier.CODEC);

	}
	
	public static final Supplier<MapCodec<CoinsInChestsModifier>> COINS_IN_CHESTS;
	public static final Supplier<MapCodec<BonusItemModifier>> BONUS_ITEM;
	public static final Supplier<MapCodec<VariantDataModifier>> MODEL_VARIANT;

}
