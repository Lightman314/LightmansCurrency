package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.features.loot.functions.RandomizeEnchantmentFunction;
import io.github.lightman314.lightmanscurrency.features.loot.functions.RandomizeItemFunction;
import io.github.lightman314.lightmanscurrency.features.villagers.LCVillagerTrades;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.List;
import java.util.Optional;

public final class LCVillagerTradeProvider {

    private LCVillagerTradeProvider() {}

    public static void bootstrap(BootstrapContext<VillagerTrade> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

        //Wandering Trader Trades
        context.register(LCVillagerTrades.WANDERING_TRADER_ATM,new VillagerTrade(
                new TradeCost(LCItems.COIN_GOLD,1),
                new ItemStackTemplate(LCBlocks.ATM.asItem()),
                1,1,0.05f, Optional.empty(), List.of()
        ));
        context.register(LCVillagerTrades.WANDERING_TRADER_CASH_REGISTER,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,5),
                new ItemStackTemplate(LCBlocks.CASH_REGISTER.asItem()),
                2,1,0.05f,Optional.empty(),List.of()
        ));
        context.register(LCVillagerTrades.WANDERING_TRADER_NETWORK_TERMINAL,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,5),
                new ItemStackTemplate(LCBlocks.TRADING_TERMINAL.asItem()),
                2,1,0.05f,Optional.empty(),List.of()
        ));
        HolderSet<Item> displayCases = items.getOrThrow(LCTags.BlocksAndItems.GROUP_DISPLAY_CASE.item());
        context.register(LCVillagerTrades.WANDERING_TRADER_DISPLAY_CASE,new VillagerTrade(
                new TradeCost(LCItems.COIN_GOLD,2),
                new ItemStackTemplate(LCBlocks.DISPLAY_CASE.get(VanillaColor.WHITE).asItem()),
                12,1,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(displayCases))
        ));

        //Banker Trades
        HolderSet<Item> atms = items.getOrThrow(LCTags.Items.ATM);
        context.register(LCVillagerTrades.BANKER_1_COIN_MINT,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,5),
                new ItemStackTemplate(LCBlocks.COIN_MINT.asItem()),
                12,1,0.05f,Optional.empty(),List.of()
        ));
        context.register(LCVillagerTrades.BANKER_1_ATM,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,8),
                new ItemStackTemplate(LCBlocks.ATM.asItem()),
                12,1,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(atms))
        ));
        context.register(LCVillagerTrades.BANKER_1_CASH_REGISTER,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,5),
                new ItemStackTemplate(LCBlocks.CASH_REGISTER.asItem()),
                12,1,0.05f,Optional.empty(),List.of()
        ));
        //TODO Ticket Station Trade
        context.register(LCVillagerTrades.BANKER_1_TRADING_MECHANISM,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,4),
                Optional.of(new TradeCost(LCItems.COIN_COPPER,8)),
                new ItemStackTemplate(LCItems.TRADING_MECHANISM.asItem()),
                12,1,0.05f,Optional.empty(),List.of()
        ));

        HolderSet<Item> shelves = items.getOrThrow(LCTags.BlocksAndItems.GROUP_SINGLE_SHELF.item());
        context.register(LCVillagerTrades.BANKER_2_SHELF,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,6),
                new ItemStackTemplate(LCBlocks.SINGLE_SHELF.get(WoodType.OAK).asItem()),
                12,5,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(shelves))
        ));
        HolderSet<Item> doubleShelves = items.getOrThrow(LCTags.BlocksAndItems.GROUP_DOUBLE_SHELF.item());
        context.register(LCVillagerTrades.BANKER_2_DOUBLE_SHELF,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,14),
                new ItemStackTemplate(LCBlocks.DOUBLE_SHELF.get(WoodType.OAK).asItem()),
                12,5,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(doubleShelves))
        ));
        //TODO Coin Chest Trade
        context.register(LCVillagerTrades.BANKER_2_DISPLAY_CASE,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,10),
                new ItemStackTemplate(LCBlocks.DISPLAY_CASE.get(VanillaColor.WHITE).asItem()),
                12,5,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(displayCases))
        ));
        //TODO Paygate Trade

        HolderSet<Item> cardDisplays = items.getOrThrow(LCTags.BlocksAndItems.GROUP_CARD_DISPLAY.item());
        context.register(LCVillagerTrades.BANKER_3_CARD_DISPLAY,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,15),
                new ItemStackTemplate(LCBlocks.CARD_DISPLAY.get(WoodType.OAK,VanillaColor.RED).asItem()),
                12,10,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(cardDisplays))
        ));
        //TODO Armor Display Trade
        //TODO Ticket Kiosk Trade
        HolderSet<Item> networkTerminals = items.getOrThrow(LCTags.Items.NETWORK_TERMINAL);
        context.register(LCVillagerTrades.BANKER_3_TERMINAL,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,10),
                new ItemStackTemplate(LCBlocks.TRADING_TERMINAL.asItem()),
                12,10,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(networkTerminals))
        ));
        context.register(LCVillagerTrades.BANKER_3_NETWORK_TRADER,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,15),
                new ItemStackTemplate(LCBlocks.ITEM_NETWORK_TRADER.get(1).asItem()),
                12,10,0.05f,Optional.empty(),List.of()
        ));


        HolderSet<Item> vendingMachines = items.getOrThrow(LCTags.BlocksAndItems.GROUP_VENDING_MACHINE.item());
        context.register(LCVillagerTrades.BANKER_4_VENDING_MACHINE,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,25),
                new ItemStackTemplate(LCBlocks.VENDING_MACHINE.get(VanillaColor.WHITE).asItem()),
                12,20,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(vendingMachines))
        ));
        context.register(LCVillagerTrades.BANKER_4_NETWORK_TRADER,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,30),
                new ItemStackTemplate(LCBlocks.ITEM_NETWORK_TRADER.get(2).asItem()),
                12,20,0.05f,Optional.empty(),List.of()
        ));
        //TODO Freezer Trade
        //TODO Bookshelf Trade
        HolderSet<Enchantment> moneyMendingEnchants = enchantments.getOrThrow(LCTags.Enchantments.MONEY_MENDING);
        context.register(LCVillagerTrades.BANKER_4_MONEY_MENDING,new VillagerTrade(
                new TradeCost(LCItems.COIN_EMERALD,10),
                new ItemStackTemplate(Items.ENCHANTED_BOOK),
                12,20,0.05f,Optional.empty(),
                List.of(new RandomizeEnchantmentFunction(moneyMendingEnchants))
        ));

        HolderSet<Item> largeVendingMachines = items.getOrThrow(LCTags.BlocksAndItems.GROUP_LARGE_VENDING_MACHINE.item());
        context.register(LCVillagerTrades.BANKER_5_LARGE_VENDING_MACHINE,new VillagerTrade(
                new TradeCost(LCItems.COIN_IRON,25),
                new ItemStackTemplate(LCBlocks.LARGE_VENDING_MACHINE.get(VanillaColor.WHITE).asItem()),
                12,30,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(largeVendingMachines))
        ));
        HolderSet<Item> lastItemNetworkTraders = HolderSet.direct(BuiltInRegistries.ITEM::wrapAsHolder,LCBlocks.ITEM_NETWORK_TRADER.get(3).asItem(),LCBlocks.ITEM_NETWORK_TRADER.get(4).asItem());
        context.register(LCVillagerTrades.BANKER_5_NETWORK_TRADER,new VillagerTrade(
                new TradeCost(LCItems.COIN_GOLD,8),
                new ItemStackTemplate(LCBlocks.ITEM_NETWORK_TRADER.get(3).asItem()),
                12,30,0.05f,Optional.empty(),
                List.of(new RandomizeItemFunction(lastItemNetworkTraders))
        ));
        //TODO Slot Machine Trade
        //TODO Trader Interface Trade
        context.register(LCVillagerTrades.BANKER_5_MONEY_MENDING,new VillagerTrade(
                new TradeCost(LCItems.COIN_EMERALD,10),
                new ItemStackTemplate(Items.ENCHANTED_BOOK),
                12,30,0.05f,Optional.empty(),
                List.of(new RandomizeEnchantmentFunction(moneyMendingEnchants))
        ));

    }

}
