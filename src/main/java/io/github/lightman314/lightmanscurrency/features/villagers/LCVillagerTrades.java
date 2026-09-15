package io.github.lightman314.lightmanscurrency.features.villagers;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.VillagerTrade;

public final class LCVillagerTrades {

    private LCVillagerTrades() {}

    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_ATM = key("wandering_trader/atm");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_TICKET_MACHINE = key("wandering_trader/ticket_machine");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_CASH_REGISTER = key("wandering_trader/cash_register");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_CASH_TERMINAL = key("wandering_trader/cash_register");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_DISPLAY_CASE = key("wandering_trader/display_case");

    private static ResourceKey<VillagerTrade> key(String id) { return ResourceKey.create(Registries.VILLAGER_TRADE,LCApi.id(id)); }

}
