package io.github.lightman314.lightmanscurrency.common.crafting.conditions;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;

public class LCCraftingConditions {

    public static final ConfigCraftingCondition NETWORK_TRADER = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftNetworkTraders);
    public static final ConfigCraftingCondition TRADER_INTERFACE = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftTraderInterfaces);

}
