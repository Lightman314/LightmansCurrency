package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.enchantments.LCEnchantmentCategories;

public class ModCreativeGroups {

    public static void setupCreativeTabs() {
        LightmansCurrency.COIN_GROUP.setEnchantmentCategories(LCEnchantmentCategories.WALLET_CATEGORY, LCEnchantmentCategories.WALLET_PICKUP_CATEGORY);
        LightmansCurrency.COIN_GROUP.startInit().add(
                //Coin -> Coin Pile -> Coin Block by type
                ModItems.COIN_COPPER, ModBlocks.COINPILE_COPPER, ModBlocks.COINBLOCK_COPPER,
                ModItems.COIN_IRON, ModBlocks.COINPILE_IRON, ModBlocks.COINBLOCK_IRON,
                ModItems.COIN_GOLD, ModBlocks.COINPILE_GOLD, ModBlocks.COINBLOCK_GOLD,
                ModItems.COIN_EMERALD, ModBlocks.COINPILE_EMERALD, ModBlocks.COINBLOCK_EMERALD,
                ModItems.COIN_DIAMOND, ModBlocks.COINPILE_DIAMOND, ModBlocks.COINBLOCK_DIAMOND,
                ModItems.COIN_NETHERITE, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_NETHERITE,
                //Wallets
                ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.WALLET_GOLD,
                ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE,
                //Trading Core
                ModItems.TRADING_CORE
        ).build();

        LightmansCurrency.MACHINE_GROUP.startInit().add(
                //Coin Mint
                ModBlocks.MACHINE_MINT,
                //ATM
                ModBlocks.MACHINE_ATM, ModItems.PORTABLE_ATM,
                //Cash Register
                ModBlocks.CASH_REGISTER,
                //Terminal
                ModBlocks.TERMINAL, ModItems.PORTABLE_TERMINAL,
                ModBlocks.GEM_TERMINAL, ModItems.PORTABLE_GEM_TERMINAL,
                //Trader Interface
                ModBlocks.ITEM_TRADER_INTERFACE,
                //Ticket Machine
                ModBlocks.TICKET_STATION,
                //Tickets
                ModItems.TICKET_MASTER,
                ModItems.TICKET,
                ModItems.TICKET_STUB,
                //Coin Jars
                ModBlocks.PIGGY_BANK,
                ModBlocks.COINJAR_BLUE
        ).build();

        LightmansCurrency.TRADING_GROUP.startInit().add(
                //Item Traders (normal)
                ModBlocks.SHELF,
                ModBlocks.DISPLAY_CASE,
                ModBlocks.CARD_DISPLAY,
                ModBlocks.VENDING_MACHINE,
                ModBlocks.FREEZER,
                ModBlocks.VENDING_MACHINE_LARGE,
                //Item Traders (specialty)
                ModBlocks.ARMOR_DISPLAY, ModBlocks.TICKET_KIOSK,
                //Item Traders (network)
                ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2,
                ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4,
                //Paygate
                ModBlocks.PAYGATE
        ).build();

        LightmansCurrency.UPGRADE_GROUP.startInit().add(
                //Item Capacity
                ModItems.ITEM_CAPACITY_UPGRADE_1, ModItems.ITEM_CAPACITY_UPGRADE_2, ModItems.ITEM_CAPACITY_UPGRADE_3,
                //Speed
                ModItems.SPEED_UPGRADE_1, ModItems.SPEED_UPGRADE_2, ModItems.SPEED_UPGRADE_3,
                ModItems.SPEED_UPGRADE_4, ModItems.SPEED_UPGRADE_5,
                //Extra
                ModItems.NETWORK_UPGRADE, ModItems.HOPPER_UPGRADE
        ).build();

    }

}
