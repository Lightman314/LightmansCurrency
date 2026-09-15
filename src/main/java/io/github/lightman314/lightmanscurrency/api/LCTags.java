package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.block.Block;

public final class LCTags {
    private LCTags() {}

    public static final class Items {
        private Items() {}

        public static final TagKey<Item> MONEY = key("money");
        public static final TagKey<Item> MONEY_COINS = key("money/coins");
        public static final TagKey<Item> MONEY_EMERALDS = key("money/emeralds");
        public static final TagKey<Item> MONEY_CHOCOLATE_COINS = key("money/chocolate_coins");

        //Wallet Tags
        public static final TagKey<Item> WALLET = key("wallet");
        public static final TagKey<Item> WALLET_UPGRADE_MATERIAL = key("wallet_upgrade_material");

        public static final TagKey<Item> ATM = key("atm");

        //Misc Tags
        public static final TagKey<Item> COIN_MINTING_MATERIAL = key("coin_minting_material");

        public static final TagKey<Item> NETWORK_TERMINAL = key("network_terminal");

        //Trader Tags
        public static final TagKey<Item> TRADERS = key("traders");
        public static final TagKey<Item> TRADERS_ITEM = key("traders/item");
        public static final TagKey<Item> TRADERS_NETWORK = key("traders/network");

        public static final TagKey<Item> GROUP_DISPLAY_CASE = key("groups/display_case");
        public static final TagKey<Item> GROUP_CARD_DISPLAY = key("groups/card_display");

        public static TagKey<Item> key(String name) { return TagKey.create(Registries.ITEM,LCApi.id(name)); }

    }

    public static final class Blocks {

        private Blocks() {}

        public static final TagKey<Block> MULTI_BLOCK = key("multi_block");
        public static final TagKey<Block> OWNER_PROTECTED = key("owner_protected");
        public static final TagKey<Block> SAFE_INTERACTABLE = key("safe_interactable");

        public static final TagKey<Block> GROUP_CARD_DISPLAY = key("groups/card_display");
        public static final TagKey<Block> GROUP_DISPLAY_CASE = key("groups/display_case");


        public static TagKey<Block> key(String name) { return TagKey.create(Registries.BLOCK,LCApi.id(name)); }

    }

    public static final class Enchantments {
        private Enchantments() {}

        public static final TagKey<Enchantment> EXCLUSIVE_SET_MENDING = key("c","exclusive_set/mending");

        public static final TagKey<Enchantment> WALLET_ENCHANTMENT = key("wallet_enchantment");
        public static final TagKey<Enchantment> MONEY_MENDING = key("money_mending");

        public static TagKey<Enchantment> key(String modid,String name) { return TagKey.create(Registries.ENCHANTMENT,Identifier.fromNamespaceAndPath(modid,name)); }
        public static TagKey<Enchantment> key(String name) { return key(LCApi.MODID,name); }

    }

    public static final class TraderNodes {
        private TraderNodes() {}

        public static final TagKey<TraderNodeType<?>> DENY_ITEM_BARTERING = key("deny_item_bartering");

        public static TagKey<TraderNodeType<?>> key(String name) { return TagKey.create(LCRegistries.Trader.TRADER_NODE_TYPE_KEY,LCApi.id(name)); }

    }

    public static final class TraderTypes {
        private TraderTypes() {}

        public static final TagKey<TraderType> ITEM_TRADER = key("item_trader");

        public static TagKey<TraderType> key(String name) { return TagKey.create(LCRegistries.Trader.TRADER_TYPE_KEY,LCApi.id(name)); }

    }

    public static final class VillagerTrades {

        public static final TagKey<VillagerTrade> BANKER_LEVEL_1 = leveled("banker",1);
        public static final TagKey<VillagerTrade> BANKER_LEVEL_2 = leveled("banker",2);
        public static final TagKey<VillagerTrade> BANKER_LEVEL_3 = leveled("banker",3);
        public static final TagKey<VillagerTrade> BANKER_LEVEL_4 = leveled("banker",4);
        public static final TagKey<VillagerTrade> BANKER_LEVEL_5 = leveled("banker",5);

        public static final TagKey<VillagerTrade> CASHIER_LEVEL_1 = leveled("cashier",1);
        public static final TagKey<VillagerTrade> CASHIER_LEVEL_2 = leveled("cashier",2);
        public static final TagKey<VillagerTrade> CASHIER_LEVEL_3 = leveled("cashier",3);
        public static final TagKey<VillagerTrade> CASHIER_LEVEL_4 = leveled("cashier",4);
        public static final TagKey<VillagerTrade> CASHIER_LEVEL_5 = leveled("cashier",5);

        public static TagKey<VillagerTrade> leveled(String name,int level) { return TagKey.create(Registries.VILLAGER_TRADE,LCApi.id(name + "/level_" + level)); }
        public static TagKey<VillagerTrade> key(String name) { return TagKey.create(Registries.VILLAGER_TRADE,LCApi.id(name)); }

    }

}