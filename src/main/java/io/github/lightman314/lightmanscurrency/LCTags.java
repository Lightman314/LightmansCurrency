package io.github.lightman314.lightmanscurrency;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class LCTags {

    public static class Blocks {

        public static final TagKey<Block> MULTI_BLOCK = tag("multi_block");
        public static final TagKey<Block> SAFE_INTERACTABLE = tag("safe_interactable");
        public static final TagKey<Block> OWNER_PROTECTED = tag("owner_protected");

        public static final TagKey<Block> AUCTION_STAND = tag("auction_stand");
        public static final TagKey<Block> CARD_DISPLAY = tag("card_display");
        public static final TagKey<Block> SHELF = tag("shelf");
        public static final TagKey<Block> SHELF_2x2 = tag("shelf_2x2");

        private static TagKey<Block> tag(String id) { return BlockTags.create(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, id)); }

    }

    public static class Items {

        //Coin Tag for MyFirstPenny advancement
        public static final TagKey<Item> COINS = tag("coins");
        public static final TagKey<Item> EVENT_COINS = tag("event_coins");
        public static final TagKey<Item> EVENT_COIN_CHOCOLATE = tag("event_coins/chocolate");
        public static final TagKey<Item> COIN_MINTING_MATERIAL = tag("coin_minting_material");
        //Wallet Tag for Wallet Advancement
        public static final TagKey<Item> WALLET = tag("wallet");
        public static final TagKey<Item> WALLET_PICKUP = tag("wallet/pickup");
        public static final TagKey<Item> WALLET_EXCHANGE = tag("wallet/exchange");
        public static final TagKey<Item> WALLET_BANK = tag("wallet/bank");
        public static final TagKey<Item> WALLET_UPGRADE_MATERIAL = tag("wallet_upgrade_material");

        //Trader Tags for Advancements
        public static final TagKey<Item> TRADER = tag("trader");
        public static final TagKey<Item> TRADER_NORMAL = tag("trader_normal");

        public static final TagKey<Item> TRADER_DISPLAY_CASE = tag("traders/display_case");
        public static final TagKey<Item> TRADER_SHELF = tag("traders/shelf");
        public static final TagKey<Item> TRADER_SHELF_2x2 = tag("traders/shelf_2x2");
        public static final TagKey<Item> TRADER_CARD_DISPLAY = tag("traders/card_display");
        public static final TagKey<Item> TRADER_VENDING_MACHINE = tag("traders/vending_machine");
        public static final TagKey<Item> TRADER_FREEZER = tag("traders/freezer");
        public static final TagKey<Item> TRADER_LARGE_VENDING_MACHINE = tag("traders/large_vending_machine");

        public static final TagKey<Item> TRADER_NETWORK = tag("trader_network");
        public static final TagKey<Item> TRADER_NETWORK_ITEM = tag("traders/network/item");
        public static final TagKey<Item> TRADER_SPECIALTY = tag("trader_specialty");
        public static final TagKey<Item> TRADER_SPECIALTY_ARMOR_DISPLAY = tag("traders/special/armor_display");
        public static final TagKey<Item> TRADER_SPECIALTY_TICKET_KIOSK = tag("traders/special/ticket_kiosk");
        public static final TagKey<Item> TRADER_SPECIALTY_BOOKSHELF = tag("traders/special/bookshelf");
        public static final TagKey<Item> TRADER_SPECIALTY_SLOT_MACHINE = tag("traders/special/slot_machine");
        public static final TagKey<Item> TRADER_SPECIALTY_GACHA_MACHINE = tag("traders/special/gacha_machine");
        public static final TagKey<Item> TRADER_SPECIALTY_PAYGATE = tag("traders/special/paygate");

        public static final TagKey<Item> TRADER_INTERFACE = tag("trader_interface");

        public static final TagKey<Item> NETWORK_TERMINAL = tag("network_terminal");
        public static final TagKey<Item> ATM = tag("atm");
        public static final TagKey<Item> AUCTION_STAND = tag("auction_stand");

        public static final TagKey<Item> COIN_JAR_NORMAL = tag("coin_jar/normal");
        public static final TagKey<Item> COIN_JAR_ALL = tag("coin_jar/all");

        //Ticket Tags
        /**
         * Tag for all ticket items regardless of their material or type<br>
         * Type being ticket/pass/master ticket<br>
         * Material being paper/gold, etc.
         */
        public static final TagKey<Item> TICKETS = tag("tickets");
        public static final TagKey<Item> TICKETS_TICKET = tag("tickets/ticket");
        public static final TagKey<Item> TICKETS_PASS = tag("tickets/pass");
        public static final TagKey<Item> TICKETS_MASTER = tag("tickets/master");
        //Ticket Materials
        public static final TagKey<Item> TICKET_MATERIAL = tag("ticket_material");
        public static final TagKey<Item> TICKET_MATERIAL_PAPER = tag("ticket_material/paper");
        public static final TagKey<Item> TICKET_MATERIAL_GOLD = tag("ticket_material/gold");

        //Tradable tags
        public static final TagKey<Item> TRADABLE_BOOK = tag("tradable/book");

        public static final TagKey<Item> GACHA_BLACKLIST = tag("gacha_ball_blacklist");

        //Settings tags
        public static final TagKey<Item> SETTINGS_WRITABLE = tag("settings/writable");
        public static final TagKey<Item> SETTINGS_READABLE = tag("settings/readable");
        public static final TagKey<Item> SETTINGS_REPLACE_WITH_WRITTEN_BOOK = tag("settings/replace_with_written_book");

        private static TagKey<Item> tag(String id) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, id)); }

    }

    public static class Enchantments {

        public static final TagKey<Enchantment> EXCUSIVE_SET_MENDING = common("exclusive_set/mending");
        public static final TagKey<Enchantment> WALLET_ENCHANTMENT = tag("wallet_enchantment");
        public static final TagKey<Enchantment> MONEY_MENDING = tag("money_mending");

        private static TagKey<Enchantment> common(@Nonnull String id) { return TagKey.create(Registries.ENCHANTMENT,ResourceLocation.fromNamespaceAndPath("c",id)); }
        private static TagKey<Enchantment> tag(@Nonnull String id) { return TagKey.create(Registries.ENCHANTMENT,ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,id)); }

    }

}
