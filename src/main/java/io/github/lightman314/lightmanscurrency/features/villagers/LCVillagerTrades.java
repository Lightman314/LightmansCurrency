package io.github.lightman314.lightmanscurrency.features.villagers;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.VillagerTrade;

public final class LCVillagerTrades {

    private LCVillagerTrades() {}

    //Wandering Trader Common
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_ATM = key("wandering_trader/atm");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_TICKET_STATION = key("wandering_trader/ticket_station");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_CASH_REGISTER = key("wandering_trader/cash_register");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_NETWORK_TERMINAL = key("wandering_trader/network_terminal");
    //Wandering Trader Rare
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_DISPLAY_CASE = key("wandering_trader/display_case");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_ARMOR_DISPLAY = key("wandering_trader/armor_display");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_TICKET_KIOSK = key("wandering_trader/ticket_kiosk");

    //Banker
    public static final ResourceKey<VillagerTrade> BANKER_1_COIN_MINT = key("banker/1/coin_mint");
    public static final ResourceKey<VillagerTrade> BANKER_1_ATM = key("banker/1/atm");
    public static final ResourceKey<VillagerTrade> BANKER_1_CASH_REGISTER = key("banker/1/cash_register");
    public static final ResourceKey<VillagerTrade> BANKER_1_TICKET_STATION = key("banker/1/ticket_station");
    public static final ResourceKey<VillagerTrade> BANKER_1_TRADING_MECHANISM = key("banker/1/trading_mechanism");

    public static final ResourceKey<VillagerTrade> BANKER_2_SHELF = key("banker/2/shelf");
    public static final ResourceKey<VillagerTrade> BANKER_2_DOUBLE_SHELF = key("banker/2/double_shelf");
    public static final ResourceKey<VillagerTrade> BANKER_2_COIN_CHEST = key("banker/2/coin_chest");
    public static final ResourceKey<VillagerTrade> BANKER_2_DISPLAY_CASE = key("banker/2/display_case");
    public static final ResourceKey<VillagerTrade> BANKER_2_PAYGATE = key("banker/2/paygate");

    public static final ResourceKey<VillagerTrade> BANKER_3_CARD_DISPLAY = key("banker/3/card_display");
    public static final ResourceKey<VillagerTrade> BANKER_3_ARMOR_DISPLAY = key("banker/3/armor_display");
    public static final ResourceKey<VillagerTrade> BANKER_3_TICKET_KIOSK = key("banker/3/ticket_kiosk");
    public static final ResourceKey<VillagerTrade> BANKER_3_TERMINAL = key("banker/3/terminal");
    public static final ResourceKey<VillagerTrade> BANKER_3_NETWORK_TRADER = key("banker/3/network_trader");

    public static final ResourceKey<VillagerTrade> BANKER_4_VENDING_MACHINE = key("banker/4/vending_machine");
    public static final ResourceKey<VillagerTrade> BANKER_4_NETWORK_TRADER = key("banker/4/network_trader");
    public static final ResourceKey<VillagerTrade> BANKER_4_FREEZER = key("banker/4/freezer");
    public static final ResourceKey<VillagerTrade> BANKER_4_BOOKSHELF = key("banker/4/bookshelf");
    public static final ResourceKey<VillagerTrade> BANKER_4_MONEY_MENDING = key("banker/4/money_mending");

    public static final ResourceKey<VillagerTrade> BANKER_5_LARGE_VENDING_MACHINE = key("banker/5/large_vending_machine");
    public static final ResourceKey<VillagerTrade> BANKER_5_NETWORK_TRADER = key("banker/5/network_trader");
    public static final ResourceKey<VillagerTrade> BANKER_5_SLOT_MACHINE = key("banker/5/slot_machine");
    public static final ResourceKey<VillagerTrade> BANKER_5_TRADER_INTERFACE = key("banker/5/trader_interface");
    public static final ResourceKey<VillagerTrade> BANKER_5_MONEY_MENDING = key("banker/5/money_mending");

    private static ResourceKey<VillagerTrade> key(String id) { return ResourceKey.create(Registries.VILLAGER_TRADE,LCApi.id(id)); }

}
