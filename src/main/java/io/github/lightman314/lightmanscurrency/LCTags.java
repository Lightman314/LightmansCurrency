package io.github.lightman314.lightmanscurrency;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class LCTags {

    public static class Blocks {

        public static final TagKey<Block> MULTI_BLOCK = tag("multi_block");
        public static final TagKey<Block> SAFE_INTERACTABLE = tag("safe_interactable");
        public static final TagKey<Block> OWNER_PROTECTED = tag("owner_protected");

        private static TagKey<Block> tag(String id) { return BlockTags.create(new ResourceLocation(LightmansCurrency.MODID, id)); }

    }

    public static class Items {

        //Coin Tag for MyFirstPenny advancement
        public static final TagKey<Item> COINS = tag("coins");
        public static final TagKey<Item> COIN_MINTING_MATERIAL = tag("coin_minting_material");
        //Wallet Tag for Wallet Advancement
        public static final TagKey<Item> WALLET = tag("wallet");

        //Trader Tags for Advancements
        public static final TagKey<Item> TRADER = tag("trader");
        public static final TagKey<Item> TRADER_NORMAL = tag("trader_normal");
        public static final TagKey<Item> TRADER_NETWORK = tag("trader_network");
        public static final TagKey<Item> TRADER_SPECIALTY = tag("trader_specialty");
        public static final TagKey<Item> TRADER_INTERFACE = tag("trader_interface");

        public static final TagKey<Item> NETWORK_TERMINAL = tag("network_terminal");
        @Deprecated
        public static final TagKey<Item> TRADING_TERMINAL = tag("trading_terminal");
        public static final TagKey<Item> ATM = tag("atm");

        public static final TagKey<Item> COIN_JAR_NORMAL = tag("coin_jar/normal");
        public static final TagKey<Item> COIN_JAR_ALL = tag("coin_jar/all");

        //Ticket Tags
        public static final TagKey<Item> TICKET = tag("ticket");
        public static final TagKey<Item> TICKET_MATERIAL = tag("ticket_material");


        //Tradable tags
        public static final TagKey<Item> TRADABLE_BOOK = tag("tradable/book");

        private static TagKey<Item> tag(String id) { return ItemTags.create(new ResourceLocation(LightmansCurrency.MODID, id)); }

    }

}