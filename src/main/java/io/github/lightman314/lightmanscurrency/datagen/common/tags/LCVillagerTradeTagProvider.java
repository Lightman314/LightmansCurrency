package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.features.villagers.LCVillagerTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VillagerTradesTagsProvider;
import net.minecraft.tags.VillagerTradeTags;

import java.util.concurrent.CompletableFuture;

public class LCVillagerTradeTagProvider extends VillagerTradesTagsProvider {

    public LCVillagerTradeTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        this.tag(VillagerTradeTags.WANDERING_TRADER_COMMON)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_ATM)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_TICKET_STATION)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_CASH_REGISTER)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_NETWORK_TERMINAL);
        this.tag(VillagerTradeTags.WANDERING_TRADER_UNCOMMON)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_DISPLAY_CASE)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_ARMOR_DISPLAY)
                .addOptional(LCVillagerTrades.WANDERING_TRADER_TICKET_KIOSK);

        //Banker Trades
        this.tag(LCTags.VillagerTrades.BANKER_LEVEL_1)
                .addOptional(LCVillagerTrades.BANKER_1_COIN_MINT)
                .addOptional(LCVillagerTrades.BANKER_1_ATM)
                .addOptional(LCVillagerTrades.BANKER_1_CASH_REGISTER)
                .addOptional(LCVillagerTrades.BANKER_1_TICKET_STATION)
                .addOptional(LCVillagerTrades.BANKER_1_TRADING_MECHANISM);
        this.tag(LCTags.VillagerTrades.BANKER_LEVEL_2)
                .addOptional(LCVillagerTrades.BANKER_2_SHELF)
                .addOptional(LCVillagerTrades.BANKER_2_DOUBLE_SHELF)
                .addOptional(LCVillagerTrades.BANKER_2_COIN_CHEST)
                .addOptional(LCVillagerTrades.BANKER_2_DISPLAY_CASE)
                .addOptional(LCVillagerTrades.BANKER_2_PAYGATE);
        this.tag(LCTags.VillagerTrades.BANKER_LEVEL_3)
                .addOptional(LCVillagerTrades.BANKER_3_CARD_DISPLAY)
                .addOptional(LCVillagerTrades.BANKER_3_ARMOR_DISPLAY)
                .addOptional(LCVillagerTrades.BANKER_3_TICKET_KIOSK)
                .addOptional(LCVillagerTrades.BANKER_3_TERMINAL)
                .addOptional(LCVillagerTrades.BANKER_3_NETWORK_TRADER);

        this.tag(LCTags.VillagerTrades.BANKER_LEVEL_4)
                .addOptional(LCVillagerTrades.BANKER_4_VENDING_MACHINE)
                .addOptional(LCVillagerTrades.BANKER_4_NETWORK_TRADER)
                .addOptional(LCVillagerTrades.BANKER_4_FREEZER)
                .addOptional(LCVillagerTrades.BANKER_4_BOOKSHELF)
                .addOptional(LCVillagerTrades.BANKER_4_MONEY_MENDING);

        this.tag(LCTags.VillagerTrades.BANKER_LEVEL_5)
                .addOptional(LCVillagerTrades.BANKER_5_LARGE_VENDING_MACHINE)
                .addOptional(LCVillagerTrades.BANKER_5_NETWORK_TRADER)
                .addOptional(LCVillagerTrades.BANKER_5_SLOT_MACHINE)
                .addOptional(LCVillagerTrades.BANKER_5_TRADER_INTERFACE)
                .addOptional(LCVillagerTrades.BANKER_5_MONEY_MENDING);

        //Cashier Tags just to ensure they exist
        this.tag(LCTags.VillagerTrades.CASHIER_LEVEL_1);
        this.tag(LCTags.VillagerTrades.CASHIER_LEVEL_2);
        this.tag(LCTags.VillagerTrades.CASHIER_LEVEL_3);
        this.tag(LCTags.VillagerTrades.CASHIER_LEVEL_4);
        this.tag(LCTags.VillagerTrades.CASHIER_LEVEL_5);

    }

}