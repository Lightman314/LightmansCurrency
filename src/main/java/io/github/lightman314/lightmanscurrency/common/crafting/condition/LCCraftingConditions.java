package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;

public class LCCraftingConditions {

	public static void init() {}

	public static final ConfigCraftingCondition NETWORK_TRADER = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftNetworkTraders);
	public static final ConfigCraftingCondition TRADER_INTERFACE = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftTraderInterfaces);

	@Deprecated
	private static final MapCodec<ConfigCraftingCondition> NETWORK_TRADER_CODEC = MapCodec.unit(() -> NETWORK_TRADER);
	@Deprecated
	private static final MapCodec<ConfigCraftingCondition> TRADER_INTERFACE_CODEC = MapCodec.unit(() -> TRADER_INTERFACE);

	static {

		LightmansCurrency.LogDebug("Registering LC Crafting Conditions");
		ModRegistries.CRAFTING_CONDITIONS.register("configured", () -> ConfigCraftingCondition.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("network_trader_craftable", () -> NETWORK_TRADER_CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("trader_interface_craftable", () -> TRADER_INTERFACE_CODEC);

	}
	
}
