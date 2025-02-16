package io.github.lightman314.lightmanscurrency.datagen.client.language;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;

public class EnglishProvider extends TranslationProvider {

    public EnglishProvider(PackOutput output) { super(output, LightmansCurrency.MODID, "en_us"); }

    @Override
    protected void addTranslations() {

        //Creative Groups
        this.translate(LCText.CREATIVE_GROUP_COINS,"Coins & Items");
        this.translate(LCText.CREATIVE_GROUP_MACHINES,"Misc Machines");
        this.translate(LCText.CREATIVE_GROUP_TRADING,"Trading Machines");
        this.translate(LCText.CREATIVE_GROUP_UPGRADES,"Machine Upgrades");
        this.translate(LCText.CREATIVE_GROUP_EXTRA,"Extra Machines");

        //Items
        this.translate(LCText.ITEM_COIN_COPPER,"Copper Coin");
        this.translate(LCText.ITEM_COIN_COPPER_PLURAL,"Copper Coins");
        this.translate(LCText.ITEM_COIN_COPPER_INITIAL,"c");
        this.translate(LCText.ITEM_COIN_IRON,"Iron Coin");
        this.translate(LCText.ITEM_COIN_IRON_PLURAL,"Iron Coins");
        this.translate(LCText.ITEM_COIN_IRON_INITIAL,"i");
        this.translate(LCText.ITEM_COIN_GOLD,"Gold Coin");
        this.translate(LCText.ITEM_COIN_GOLD_PLURAL,"Gold Coins");
        this.translate(LCText.ITEM_COIN_GOLD_INITIAL,"g");
        this.translate(LCText.ITEM_COIN_EMERALD,"Emerald Coin");
        this.translate(LCText.ITEM_COIN_EMERALD_PLURAL,"Emerald Coins");
        this.translate(LCText.ITEM_COIN_EMERALD_INITIAL,"e");
        this.translate(LCText.ITEM_COIN_DIAMOND,"Diamond Coin");
        this.translate(LCText.ITEM_COIN_DIAMOND_PLURAL,"Diamond Coins");
        this.translate(LCText.ITEM_COIN_DIAMOND_INITIAL,"d");
        this.translate(LCText.ITEM_COIN_NETHERITE,"Netherite Coin");
        this.translate(LCText.ITEM_COIN_NETHERITE_PLURAL,"Netherite Coins");
        this.translate(LCText.ITEM_COIN_NETHERITE_INITIAL,"n");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_COPPER,"Chocolate Copper Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_COPPER_PLURAL,"Chocolate Copper Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_COPPER_INITIAL,"c");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_IRON,"Chocolate Iron Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_IRON_PLURAL,"Chocolate Iron Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_IRON_INITIAL,"i");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_GOLD,"Chocolate Gold Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_GOLD_PLURAL,"Chocolate Gold Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_GOLD_INITIAL,"g");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_EMERALD,"Chocolate Emerald Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_EMERALD_PLURAL,"Chocolate Emerald Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_EMERALD_INITIAL,"e");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_DIAMOND,"Chocolate Diamond Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_DIAMOND_PLURAL,"Chocolate Diamond Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_DIAMOND_INITIAL,"d");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_NETHERITE,"Chocolate Netherite Coin");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_NETHERITE_PLURAL,"Chocolate Netherite Coins");
        this.translate(LCText.ITEM_COIN_CHOCOLATE_NETHERITE_INITIAL,"n");

        this.translateAncientCoin(AncientCoinType.COPPER,"Ancient Copper Coin");
        this.translateAncientCoinInitial(AncientCoinType.COPPER,"AC");
        this.translateAncientCoin(AncientCoinType.IRON,"Ancient Iron Coin");
        this.translateAncientCoinInitial(AncientCoinType.IRON,"AI");
        this.translateAncientCoin(AncientCoinType.GOLD,"Ancient Gold Coin");
        this.translateAncientCoinInitial(AncientCoinType.GOLD,"AG");
        this.translateAncientCoin(AncientCoinType.EMERALD,"Ancient Emerald Coin");
        this.translateAncientCoinInitial(AncientCoinType.EMERALD,"AE");
        this.translateAncientCoin(AncientCoinType.DIAMOND,"Ancient Diamond Coin");
        this.translateAncientCoinInitial(AncientCoinType.DIAMOND,"AD");
        this.translateAncientCoin(AncientCoinType.NETHERITE_H,"Ancient Netherite Coin");
        this.translateAncientCoinInitial(AncientCoinType.NETHERITE_H,"AN");
        this.translateAncientCoin(AncientCoinType.LAPIS,"Ancient Lapis Coin");
        this.translateAncientCoinInitial(AncientCoinType.LAPIS,"AL");
        this.translateAncientCoin(AncientCoinType.ENDER_PEARL,"Ancient Ender Pearl Coin");
        this.translateAncientCoinInitial(AncientCoinType.ENDER_PEARL,"AP");

        this.translate(LCText.ITEM_WALLET_LEATHER, "Basic Wallet");
        this.translate(LCText.ITEM_WALLET_COPPER, "Wallet (Copper)");
        this.translate(LCText.ITEM_WALLET_IRON, "Wallet (Iron)");
        this.translate(LCText.ITEM_WALLET_GOLD, "Wallet (Gold)");
        this.translate(LCText.ITEM_WALLET_EMERALD, "Wallet (Emerald)");
        this.translate(LCText.ITEM_WALLET_DIAMOND, "Wallet (Diamond)");
        this.translate(LCText.ITEM_WALLET_NETHERITE, "Wallet (Netherite)");
        this.translate(LCText.ITEM_WALLET_NETHER_STAR, "Wallet (Nether Star)");
        this.translate(LCText.ITEM_WALLET_ENDER_DRAGON, "Wallet (Ender Dragon)");

        this.translate(LCText.ITEM_TRADING_CORE,"Trading Core");

        this.translate(LCText.ITEM_ATM_CARD,"ATM Card");
        this.translate(LCText.ITEM_PREPAID_CARD,"Prepaid Card");

        this.translate(LCText.ITEM_TICKET,"Ticket");
        this.translate(LCText.ITEM_PASS,"Pass");
        this.translate(LCText.ITEM_MASTER_TICKET,"Master Ticket");
        this.translate(LCText.ITEM_TICKET_STUB,"Ticket Stub");
        this.translate(LCText.ITEM_GOLDEN_TICKET,"Golden Ticket");
        this.translate(LCText.ITEM_GOLDEN_PASS,"Golden Pass");
        this.translate(LCText.ITEM_GOLDEN_MASTER_TICKET,"Golden Master Ticket");
        this.translate(LCText.ITEM_GOLDEN_TICKET_STUB,"Golden Ticket Stub");

        this.translate(LCText.ITEM_UPGRADE_ITEM_CAPACITY_1, "Item Capacity Upgrade (Iron)");
        this.translate(LCText.ITEM_UPGRADE_ITEM_CAPACITY_2, "Item Capacity Upgrade (Gold)");
        this.translate(LCText.ITEM_UPGRADE_ITEM_CAPACITY_3, "Item Capacity Upgrade (Diamond)");
        this.translate(LCText.ITEM_UPGRADE_ITEM_CAPACITY_4, "Item Capacity Upgrade (Netherite)");
        this.translate(LCText.ITEM_UPGRADE_SPEED_1, "Speed Upgrade (Iron)");
        this.translate(LCText.ITEM_UPGRADE_SPEED_2, "Speed Upgrade (Gold)");
        this.translate(LCText.ITEM_UPGRADE_SPEED_3, "Speed Upgrade (Emerald)");
        this.translate(LCText.ITEM_UPGRADE_SPEED_4, "Speed Upgrade (Diamond)");
        this.translate(LCText.ITEM_UPGRADE_SPEED_5, "Speed Upgrade (Netherite)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_1, "Trade Offer Upgrade (Copper)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_2, "Trade Offer Upgrade (Iron)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_3, "Trade Offer Upgrade (Gold)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_4, "Trade Offer Upgrade (Emerald)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_5, "Trade Offer Upgrade (Diamond)");
        this.translate(LCText.ITEM_UPGRADE_OFFER_6, "Trade Offer Upgrade (Netherite)");
        this.translate(LCText.ITEM_UPGRADE_NETWORK, "Network Upgrade");
        this.translate(LCText.ITEM_UPGRADE_VOID, "Void Upgrade");
        this.translate(LCText.ITEM_UPGRADE_HOPPER, "Hopper Upgrade");
        this.translate(LCText.ITEM_UPGRADE_INTERACTION_1, "Interaction Upgrade (Emerald)");
        this.translate(LCText.ITEM_UPGRADE_INTERACTION_2, "Interaction Upgrade (Diamond)");
        this.translate(LCText.ITEM_UPGRADE_INTERACTION_3, "Interaction Upgrade (Netherite)");
        this.translate(LCText.ITEM_UPGRADE_COIN_EXCHANGE, "Coin Exchange Upgrade");
        this.translate(LCText.ITEM_UPGRADE_MAGNET_1, "Magnet Upgrade (Copper)");
        this.translate(LCText.ITEM_UPGRADE_MAGNET_2, "Magnet Upgrade (Iron)");
        this.translate(LCText.ITEM_UPGRADE_MAGNET_3, "Magnet Upgrade (Gold)");
        this.translate(LCText.ITEM_UPGRADE_MAGNET_4, "Magnet Upgrade (Emerald)");
        this.translate(LCText.ITEM_UPGRADE_BANK, "Bank Upgrade");
        this.translate(LCText.ITEM_UPGRADE_SECURITY, "Security Upgrade");

        this.translate(LCText.ITEM_UPGRADE_TEMPLATE,"Smithing Template");

        //Blocks
        this.translate(LCText.BLOCK_COINPILE_COPPER,"Copper Coinpile");
        this.translate(LCText.BLOCK_COINPILE_COPPER_PLURAL,"Copper Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_IRON,"Iron Coinpile");
        this.translate(LCText.BLOCK_COINPILE_IRON_PLURAL,"Iron Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_GOLD,"Gold Coinpile");
        this.translate(LCText.BLOCK_COINPILE_GOLD_PLURAL,"Gold Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_EMERALD,"Emerald Coinpile");
        this.translate(LCText.BLOCK_COINPILE_EMERALD_PLURAL,"Emerald Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_DIAMOND,"Diamond Coinpile");
        this.translate(LCText.BLOCK_COINPILE_DIAMOND_PLURAL,"Diamond Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_NETHERITE,"Netherite Coinpile");
        this.translate(LCText.BLOCK_COINPILE_NETHERITE_PLURAL,"Netherite Coinpiles");
        this.translate(LCText.BLOCK_COINBLOCK_COPPER,"Block of Copper Coins");
        this.translate(LCText.BLOCK_COINBLOCK_COPPER_PLURAL,"Blocks of Copper Coins");
        this.translate(LCText.BLOCK_COINBLOCK_IRON,"Block of Iron Coins");
        this.translate(LCText.BLOCK_COINBLOCK_IRON_PLURAL,"Blocks of Iron Coins");
        this.translate(LCText.BLOCK_COINBLOCK_GOLD,"Block of Gold Coins");
        this.translate(LCText.BLOCK_COINBLOCK_GOLD_PLURAL,"Blocks of Gold Coins");
        this.translate(LCText.BLOCK_COINBLOCK_EMERALD,"Block of Emerald Coins");
        this.translate(LCText.BLOCK_COINBLOCK_EMERALD_PLURAL,"Blocks of Emerald Coins");
        this.translate(LCText.BLOCK_COINBLOCK_DIAMOND,"Block of Diamond Coins");
        this.translate(LCText.BLOCK_COINBLOCK_DIAMOND_PLURAL,"Blocks of Diamond Coins");
        this.translate(LCText.BLOCK_COINBLOCK_NETHERITE,"Block of Netherite Coins");
        this.translate(LCText.BLOCK_COINBLOCK_NETHERITE_PLURAL,"Blocks of Netherite Coins");

        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_COPPER,"Chocolate Copper Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_COPPER_PLURAL,"Chocolate Copper Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_IRON,"Chocolate Iron Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_IRON_PLURAL,"Chocolate Iron Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_GOLD,"Chocolate Gold Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_GOLD_PLURAL,"Chocolate Gold Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_EMERALD,"Chocolate Emerald Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_EMERALD_PLURAL,"Chocolate Emerald Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_DIAMOND,"Chocolate Diamond Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_DIAMOND_PLURAL,"Chocolate Diamond Coinpiles");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_NETHERITE,"Chocolate Netherite Coinpile");
        this.translate(LCText.BLOCK_COINPILE_CHOCOLATE_NETHERITE_PLURAL,"Chocolate Netherite Coinpiles");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_COPPER,"Chocolate Block of Copper Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_COPPER_PLURAL,"Chocolate Blocks of Copper Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_IRON,"Chocolate Block of Iron Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_IRON_PLURAL,"Chocolate Blocks of Iron Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_GOLD,"Chocolate Block of Gold Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_GOLD_PLURAL,"Chocolate Blocks of Gold Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_EMERALD,"Chocolate Block of Emerald Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_EMERALD_PLURAL,"Chocolate Blocks of Emerald Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_DIAMOND,"Chocolate Block of Diamond Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_DIAMOND_PLURAL,"Chocolate Blocks of Diamond Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_NETHERITE,"Chocolate Block of Netherite Coins");
        this.translate(LCText.BLOCK_COINBLOCK_CHOCOLATE_NETHERITE_PLURAL,"Chocolate Blocks of Netherite Coins");

        this.translate(LCText.BLOCK_CASH_REGISTER, "Cash Register");
        this.translate(LCText.BLOCK_COIN_MINT, "Coin Minting Machine");
        this.translate(LCText.BLOCK_TICKET_STATION, "Ticket Station");
        this.translate(LCText.BLOCK_MONEY_CHEST, "Money Chest");

        this.translate(LCText.BLOCK_DISPLAY_CASE,"Display Case");
        this.translateWooden(LCText.BLOCK_SHELF, "%s Shelf");
        this.translateWooden(LCText.BLOCK_SHELF_2x2, "%s Double Shelf");
        this.translateWooden(LCText.BLOCK_CARD_DISPLAY, "%s Card Display");
        this.translateColored(LCText.BLOCK_VENDING_MACHINE, "%s Vending Machine");
        this.translateColored(LCText.BLOCK_FREEZER, "%s Freezer");
        this.translateColored(LCText.BLOCK_LARGE_VENDING_MACHINE, "Large %s Vending Machine");

        this.translate(LCText.BLOCK_PAYGATE, "Paygate");
        this.translate(LCText.BLOCK_TICKET_KIOSK, "Ticket Kiosk");
        this.translate(LCText.BLOCK_SLOT_MACHINE, "Slot Machine");
        this.translate(LCText.BLOCK_ARMOR_DISPLAY, "Armor Display");
        this.translateWooden(LCText.BLOCK_BOOKSHELF_TRADER, "%s Bookshelf Trader");
        this.translate(LCText.BLOCK_COMMAND_TRADER, "Command Trader");

        this.translate(LCText.BLOCK_ITEM_NETWORK_TRADER_1, "Item Network Trader T1");
        this.translate(LCText.BLOCK_ITEM_NETWORK_TRADER_2, "Item Network Trader T2");
        this.translate(LCText.BLOCK_ITEM_NETWORK_TRADER_3, "Item Network Trader T3");
        this.translate(LCText.BLOCK_ITEM_NETWORK_TRADER_4, "Item Network Trader T4");

        this.translate(LCText.BLOCK_ITEM_TRADER_INTERFACE, "Item Trader Interface");

        this.translate(LCText.BLOCK_TAX_COLLECTOR, "Tax Collector");

        this.translateWooden(LCText.BLOCK_AUCTION_STAND, "%s Auction Stand");

        this.translate(LCText.BLOCK_JAR_PIGGY_BANK, "Piggy Bank");
        this.translate(LCText.BLOCK_JAR_BLUE, "Coin Jar");
        this.translate(LCText.BLOCK_JAR_SUS, "Jar of Sus");

        //Items & Blocks
        this.translate(LCText.ITEM_BLOCK_TERMINAL, "Trading Terminal");
        this.translate(LCText.ITEM_BLOCK_ATM, "ATM");

        //Enchantments
        this.translate(LCText.ENCHANTMENT_MONEY_MENDING, "Money Mending");
        this.translate(LCText.ENCHANTMENT_MONEY_MENDING_DESCRIPTION, "Repairs the item using money from your equipped wallet.");
        this.translate(LCText.ENCHANTMENT_COIN_MAGNET, "Coin Magnet");
        this.translate(LCText.ENCHANTMENT_COIN_MAGNET_DESCRIPTION, "Lets your wallet collect coins from a larger range.");

        //Game Rules
        this.translate(LCText.GAMERULE_KEEP_WALLET, "Keep equipped wallet on death");
        this.translate(LCText.GAMERULE_COIN_DROP_PERCENT, "Percentage of wallet coins dropped after death");

        //Villager Professions
        this.translate(LCText.PROFESSION_BANKER, "Banker");
        this.translate(LCText.PROFESSION_CASHIER, "Cashier");

        //Key Binds
        this.translate(LCText.KEY_WALLET,"Open Wallet");
        this.translate(LCText.KEY_PORTABLE_TERMINAL, "Access Portable Terminal");
        this.translate(LCText.KEY_PORTABLE_ATM, "Access Portable ATM");

        //Sound Subtitles
        this.translate(LCText.SOUND_COINS_CLINKING,"Coins Clinking");

        //Tooltips
        this.translate(LCText.TOOLTIP_SMITHING_TEMPLATE_DESCRIPTION,"Lightman's Currency Upgrades");
        this.translate(LCText.TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO,"Miscellaneous Items & Upgrades");
        this.translate(LCText.TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS,"Redstone or Copper to Netherite Materials");
        this.translate(LCText.TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION,"Add Item or Upgrade");
        this.translate(LCText.TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION,"Add Material");
        this.translate(LCText.TOOLTIP_HEALING,"Heals %s health.");
        this.translate(LCText.TOOLTIP_BETA, "Feature in beta stage. May have issues!");
        this.translate(LCText.TOOLTIP_DISABLED, "DISABLED");
        this.translate(LCText.TOOLTIP_INFO_BLURB, "Hold SHIFT for more information.");

        this.translate(LCText.TOOLTIP_PAYMENT_CARD_USAGE,"Can be placed in a traders money slots to be used as payment");
        this.translate(LCText.TOOLTIP_ATM_CARD_NOT_LINKED,"Not yet linked to a bank account");
        this.translate(LCText.TOOLTIP_ATM_CARD_ACCOUNT,"Linked: %s");
        this.translate(LCText.TOOLTIP_ATM_CARD_LINK_INVALID,"Link has been invalidated!");
        this.translate(LCText.TOOLTIP_ATM_CARD_ACCOUNT_LOCKED,"Account Locked!");
        this.translate(LCText.TOOLTIP_PREPAID_CARD_DELETE_WARNING,"Card will disappear once emptied");

        this.translate(LCText.TOOLTIP_TICKET_ID, "TicketID: %s");
        this.translate(LCText.TOOLTIP_PASS, "Will not be consumed by Paygates");
        this.translate(LCText.TOOLTIP_ITEM_TRADER, "Item Trader:","Trades: %s","Can be used to Sell, Purchase, or Barter items with other players");
        this.translate(LCText.TOOLTIP_ITEM_TRADER_ARMOR, "Armor Trader:", "Trades: %s","Can be used to Sell, Purchase, or Barter armor pieces with other players");
        this.translate(LCText.TOOLTIP_ITEM_TRADER_TICKET, "Ticket Trader:", "Trades: %s", "Can be used to Sell, Purchase, or Barter tickets and ticket materials with other players","A master ticket is required in order to sell tickets","Tickets being sold can consume ticket materials from storage to print a new ticket for the customer");
        this.translate(LCText.TOOLTIP_ITEM_TRADER_BOOK, "Book Trader:", "Trades: %s", "Can be used to Sell, Purchase, or Barter books with other players");
        this.translate(LCText.TOOLTIP_ITEM_TRADER_NETWORK, "Item Network Trader:","Trades: %s", "Can be used to Sell, Purchase, or Barter items with other players", "Accessible from any location via the Trading Terminal");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE, "Slot Machine:","Can be used to Sell owner-defined random Items or Money to other players with owner-defined odds of receiving each loot pool");
        this.translate(LCText.TOOLTIP_PAYGATE, "Paygate:","Can be used to trigger a redstone signal for a price","Can be linked to a ticket for easier customer interactions");
        this.translate(LCText.TOOLTIP_COMMAND_TRADER, "Command Trader:","Can be used to execute commands for a price","Permission level can be customized in trader settings within the upper limit defined in the server config");
        this.translate(LCText.TOOLTIP_TERMINAL,"Can access Network Traders remotely from any location");
        this.translate(LCText.TOOLTIP_TERMINAL_KEY_BIND,"Press [%s] while equipped to access the terminal");
        this.translate(LCText.TOOLTIP_INTERFACE_ITEM,"Item Trader Interface Terminal:","Can be used to automatically purchase or sell items from any network item trader", "Can be used to automatically drain or restock items remotely to/from any item trader you have access to");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR, "Tax Collector:","Can be placed and used to collect taxes from trader sales within an area");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_ADMIN_ONLY, "Can only be activated by an admin!");
        this.translate(LCText.TOOLTIP_ATM,"Can exchange coins for other coins of higher or lower values","Can deposit or withdraw coins from any bank account you have access to", "Can transfer money from your account to another player or teams account");
        this.translate(LCText.TOOLTIP_ATM_KEY_BIND,"Press [%s] while equipped to access the ATM");
        this.translate(LCText.TOOLTIP_TICKET_STATION, "Can be used to print unique Master and Slave tickets","Tickets can be used with the Paygate, or used was coupons for barter trades");
        this.translate(LCText.TOOLTIP_CASH_REGISTER, "Can be used to link and interact with multiple local traders from the same menu");
        this.translate(LCText.TOOLTIP_COIN_JAR,"Coin Jar:","When placed, up to 64 coins can be inserted by interacting with the jar while holding a coin","If mined with silk touch the jar will drop with the coins still inside","If mined without silk touch the jar will break and its contents will be dropped instead");
        this.translate(LCText.TOOLTIP_COIN_JAR_COLORED,"Can be dyed in a crafting table");
        this.translate(LCText.TOOLTIP_COLORED_ITEM, "Color: %s");

        this.translate(LCText.TOOLTIP_TRADER_ITEM_WITH_DATA,"Linked to existing Trader Data");
        this.translate(LCText.TOOLTIP_TRADER_ITEM_WITH_DATA_TRADER_ID,"Trader ID: %s");
        this.translate(LCText.TOOLTIP_ANARCHY_WARNING,"Server is in Anarchy Mode. Block will not be break-protected!");
        this.translate(LCText.MESSAGE_DIMENSION_QUARANTINED_BANK,"Bank Accounts cannot be accessed from this dimension!");
        this.translate(LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL,"Network Traders cannot be accessed from this dimension!");
        this.translate(LCText.TOOLTIP_DIMENSION_QUARANTINED_NETWORK_TRADER,"Network Traders will not be visible from terminals when in this dimension!");

        //Wallet
        this.translate(LCText.MESSAGE_WALLET_NONE_EQUIPPED, "No wallet equipped to your wallet slot. Cannot open wallet.");
        this.translate(LCText.TOOLTIP_WALLET_KEY_BIND, "Press [%s] while equipped to access your wallet");
        this.translate(LCText.TOOLTIP_WALLET_STORED_MONEY, "Contains:");
        this.translate(LCText.TOOLTIP_WALLET_CAPACITY, "Has %s coin slots");
        this.translate(LCText.TOOLTIP_WALLET_UPGRADEABLE_CAPACITY, "Use a [%1$s] on this in your inventory to increase the wallets capacity by %2$s");
        this.translate(LCText.TOOLTIP_WALLET_PICKUP, "Automatically collects any coins that you pick up");
        this.translate(LCText.TOOLTIP_WALLET_PICKUP_MAGNET, "Can collect coins up to %sm away");
        this.translate(LCText.TOOLTIP_WALLET_EXCHANGE_MANUAL, "Can exchange coins manually in the UI");
        this.translate(LCText.TOOLTIP_WALLET_EXCHANGE_AUTO, "AutoExchange: %s");
        this.translate(LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_ON, "ON");
        this.translate(LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_OFF, "OFF");
        this.translate(LCText.TOOLTIP_WALLET_BANK_ACCOUNT,"Can deposit or withdraw coins to/from your bank account in the UI");

        //Cash Register
        this.translate(LCText.MESSAGE_CASH_REGISTER_NOT_LINKED, "Cash Register is not linked to any traders");
        this.translate(LCText.TOOLTIP_CASH_REGISTER_INFO,"Linked with %s traders");
        this.translate(LCText.TOOLTIP_CASH_REGISTER_INSTRUCTIONS,"Crouch & Interact with a trader to register");
        this.translate(LCText.TOOLTIP_CASH_REGISTER_HOLD_SHIFT, "Hold SHIFT for details");
        this.translate(LCText.TOOLTIP_CASH_REGISTER_DETAILS,"Trader %1$s at %2$s %3$s %4$s");

        //Coin Jar
        this.translate(LCText.TOOLTIP_COIN_JAR_HOLD_SHIFT, "Hold SHIFT to view contents");
        this.translate(LCText.TOOLTIP_COIN_JAR_CONTENTS_SINGLE, "Contains a %s");
        this.translate(LCText.TOOLTIP_COIN_JAR_CONTENTS_MULTIPLE, "Contains %1$sx %2$s");

        //Coin Tooltips
        this.translate(LCText.TOOLTIP_COIN_WORTH_DOWN,"Worth %1$s %2$s");
        this.translate(LCText.TOOLTIP_COIN_WORTH_UP,"%1$s of these are worth 1 %2$s");
        this.translate(LCText.TOOLTIP_COIN_WORTH_VALUE,"Worth %s");
        this.translate(LCText.TOOLTIP_COIN_WORTH_VALUE_STACK,"Stack worth %s");
        this.translate(LCText.TOOLTIP_COIN_ADVANCED_CHAIN,"Chain: %s");
        this.translate(LCText.TOOLTIP_COIN_ADVANCED_VALUE,"Value: %s");
        this.translate(LCText.TOOLTIP_COIN_ADVANCED_CORE_CHAIN,"Core Chain");
        this.translate(LCText.TOOLTIP_COIN_ADVANCED_SIDE_CHAIN,"Side Chain");
        this.translate(LCText.TOOLTIP_ANCIENT_COIN_ADVANCED_TYPE,"Type: %s");

        //Upgrade Tooltips
        this.translate(LCText.TOOLTIP_UPGRADE_TARGETS,"Upgrade can be used by:");
        this.translate(LCText.TOOLTIP_UPGRADE_UNIQUE,"Unique Upgrade");
        this.translate(LCText.TOOLTIP_UPGRADE_TRADE_OFFER, "Increases traders offer limit by %s");
        this.translate(LCText.TOOLTIP_UPGRADE_ITEM_CAPACITY, "Increases item storage capacity by %s");
        this.translate(LCText.TOOLTIP_UPGRADE_SPEED, "Decreases the delay between interactions by %s ticks");
        this.translate(LCText.TOOLTIP_UPGRADE_INTERACTION, "Increases the number of Traders or Trades that the Trader Interface can interact with by %s");
        this.translate(LCText.TOOLTIP_UPGRADE_NETWORK,"Makes a trader visible from a trading terminal");
        this.translate(LCText.TOOLTIP_UPGRADE_VOID,"Makes a trader void all goods purchased from a customer","Intended for Admin Use Only to keep pseudo-creative traders from getting full");
        this.translate(LCText.TOOLTIP_UPGRADE_HOPPER,"Allows the block to collect inputs from its neighboring containers");

        this.translate(LCText.TOOLTIP_UPGRADE_COIN_EXCHANGE, "Allows the Money Chest to automatically exchange coins upon insertion");
        this.translate(LCText.TOOLTIP_UPGRADE_MAGNET, "Allows the Money Chest to collect coin items up to %sm away");
        this.translate(LCText.TOOLTIP_UPGRADE_BANK, "Allows the Money Chest to automatically deposit or withdraw coins from a selected bank account");
        this.translate(LCText.TOOLTIP_UPGRADE_SECURITY, "Allows the Money Chest to be protected from destruction by other players","Also blocks container access from other players, but not from automation (i.e. hoppers)","Requires set-up, will not have an owner defined automatically");

        this.translate(LCText.TOOLTIP_UPGRADE_TARGET_TRADER,"All Traders");
        this.translate(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_NOT_NETWORK,"All Non-Network Traders");
        this.translate(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM,"All Item Traders");
        this.translate(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE,"All Trader Interfaces");

        //Misc Money Tooltips
        this.translate(LCText.TOOLTIP_MONEY_SOURCE_BANK,"Bank Account:");
        this.translate(LCText.TOOLTIP_MONEY_SOURCE_SLOTS,"Money Slots:");
        this.translate(LCText.TOOLTIP_MONEY_SOURCE_PLAYER,"On Your Person:");
        this.translate(LCText.TOOLTIP_MONEY_SOURCE_STORAGE, "Money Storage:");

        //Owner Tooltips
        this.translate(LCText.TOOLTIP_OWNER_PLAYER,"Player: %s");
        this.translate(LCText.TOOLTIP_OWNER_TEAM,"Team: %1$s","%2$s members");
        this.translate(LCText.TOOLTIP_OWNER_TEAM_FTB,"FTB Team: %1$s","%2$s members");

        //Enchantment Tooltips
        this.translate(LCText.TOOLTIP_MONEY_MENDING_COST, "Costs %s per durability repaired");

        //Trader Tooltips
        this.translate(LCText.TOOLTIP_OUT_OF_STOCK,"No Stock");
        this.translate(LCText.TOOLTIP_OUT_OF_SPACE,"Trader Full");
        this.translate(LCText.TOOLTIP_CANNOT_AFFORD,"Cannot Afford");
        this.translate(LCText.TOOLTIP_TAX_LIMIT,"Tax Rate too High");
        this.translate(LCText.TOOLTIP_DENIED,"DENIED");

        this.translate(LCText.TOOLTIP_SLOT_MACHINE_TO_INFO,"Click to view possible results");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_TO_INTERACT,"Click to return to slots");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_ROLL_ONCE, "Try your luck!","Costs %2$s");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_ROLL_MULTI, "Try your luck %1$s times!","Costs %2$s per roll");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_NORMAL_COST, "Normally costs %s");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_COST_FREE, "nothing");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_UNDEFINED,"Slot Machine is not yet set up!");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_MONEY,"Pays %s");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_WEIGHT,"Weight: %s");
        this.translate(LCText.TOOLTIP_SLOT_MACHINE_ODDS,"%s%% change of receiving");

        //Network Terminal Menu
        this.translate(LCText.GUI_NETWORK_TERMINAL_TITLE,"Trading Terminal");
        this.translate(LCText.GUI_NETWORK_TERMINAL_SEARCH,"Search Traders");
        this.translate(LCText.TOOLTIP_NETWORK_TERMINAL_OPEN_ALL,"Open All Network Traders");
        this.translate(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT,"%s trade(s)");
        this.translate(LCText.TOOLTIP_NETWORK_TERMINAL_OUT_OF_STOCK_COUNT,"%s trade(s) out of stock");
        this.translate(LCText.TOOLTIP_NETWORK_TERMINAL_AUCTION_HOUSE,"%s auction(s) available");

        //Notification Menu
        this.translate(LCText.BUTTON_NOTIFICATIONS_MARK_AS_READ,"Mark As Read");

        //Team Management Menu
        this.translate(LCText.TOOLTIP_TEAM_SELECT,"Team Selection");
        this.translate(LCText.BUTTON_TEAM_CREATE,"Create");
        this.translate(LCText.GUI_TEAM_SELECT,"Select Team to Manage:");
        this.translate(LCText.GUI_TEAM_CREATE,"Create New Team");
        this.translate(LCText.TOOLTIP_TEAM_MEMBERS,"Members");
        this.translate(LCText.BUTTON_TEAM_MEMBER_PROMOTE,"Promote");
        this.translate(LCText.BUTTON_TEAM_MEMBER_DEMOTE,"Demote");
        this.translate(LCText.TOOLTIP_TEAM_BANK,"Bank Account Settings");
        this.translate(LCText.BUTTON_TEAM_BANK_CREATE,"Create Bank Account");
        this.translate(LCText.BUTTON_TEAM_BANK_LIMIT,"Limit To: %s");
        this.translate(LCText.TOOLTIP_TEAM_SALARY_INFO,"Salary Information");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_DISABLED,"Auto-Salary is currently DISABLED");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_DELAY,"Auto-Salary will occur every %s");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_NEXT_TRIGGER,"Next Salary in %s");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_SALARY_MEMBERS,"Members will be paid %s");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_SALARY_ADMINS,"Admins will be paid %s");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_REQUIRED_FUNDS,"Required Salary Funds:");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_INSUFFICIENT_FUNDS,"Not enough funds are currently available to pay next salary!");
        this.translate(LCText.GUI_TEAM_SALARY_INFO_LAST_ATTEMPT_FAILED,"Last attempt to pay the salary failed!");
        this.translate(LCText.TOOLTIP_TEAM_SALARY_SETTINGS,"Salary Settings");
        this.translate(LCText.BUTTON_TEAM_SALARY_SETTINGS_ENABLE,"Enable Auto-Salary");
        this.translate(LCText.BUTTON_TEAM_SALARY_SETTINGS_DISABLE,"Disable Auto-Salary");
        this.translate(LCText.GUI_TEAM_SALARY_SETTINGS_NOTIFICATION,"Push Notification");
        this.translate(LCText.GUI_TEAM_SALARY_SETTINGS_DELAY,"Auto-Salary Delay");
        this.translate(LCText.BUTTON_TEAM_SALARY_SETTINGS_TRIGGER_SALARY,"Trigger Salary Payment");
        this.translate(LCText.TOOLTIP_TEAM_SALARY_PAYMENTS,"Salary Payment Settings");
        this.translate(LCText.TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_ENABLE,"Enable Creative Salary");
        this.translate(LCText.TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_DISABLE,"Disable Creative Salary");
        this.translate(LCText.GUI_TEAM_SALARY_PAYMENTS_MEMBER_SALARY,"Member Salary:");
        this.translate(LCText.GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY_SEPERATION,"Seperate Admin Salaries");
        this.translate(LCText.GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY,"Admin Salary:");
        this.translate(LCText.TOOLTIP_TEAM_STATS,"Team Statistics");
        this.translate(LCText.TOOLTIP_TEAM_NAME,"Name");
        this.translate(LCText.BUTTON_TEAM_RENAME,"Rename Team");
        this.translate(LCText.GUI_TEAM_NAME_CURRENT,"Current Name: %s");
        this.translate(LCText.TOOLTIP_TEAM_NAME_AND_OWNER,"Name & Ownership");
        this.translate(LCText.BUTTON_TEAM_DISBAND,"Disband Team");
        this.translate(LCText.GUI_TEAM_ID,"Team ID: %s");

        //ATM
        this.translate(LCText.TOOLTIP_ATM_EXCHANGE,"Exchange Coins");
        this.translate(LCText.TOOLTIP_ATM_SELECTION,"Select Account");
        this.translate(LCText.BUTTON_BANK_MY_ACCOUNT,"My Account");
        this.translate(LCText.BUTTON_BANK_PLAYER_ACCOUNT,"Select Players Account");
        this.translate(LCText.GUI_BANK_SELECT_PLAYER_SUCCESS,"Selected %s's Bank Account");
        this.translate(LCText.TOOLTIP_ATM_INTERACT,"Withdraw or Deposit");
        this.translate(LCText.TOOLTIP_ATM_NOTIFICATIONS,"Balance Notification Settings");
        this.translate(LCText.GUI_BANK_NOTIFICATIONS_DISABLED,"You will not receive any account balance notifications");
        this.translate(LCText.GUI_BANK_NOTIFICATIONS_DETAILS,"You will receive a notification if your account balance goes below %s");
        this.translate(LCText.BUTTON_BANK_CARD_VERIFCATION_RESET,"Reset Card Verification");
        this.translate(LCText.TOOLTIP_BANK_CARD_VERIFCATION_RESET,"Will make all ATM Cards linked to this bank account no longer function until re-assigned");
        this.translate(LCText.TOOLTIP_ATM_LOGS,"Account Logs");
        this.translate(LCText.TOOLTIP_ATM_TRANSFER,"Transfer Money");
        this.translate(LCText.TOOLTIP_ATM_TRANSFER_MODE_PLAYER,"Select From Players");
        this.translate(LCText.TOOLTIP_ATM_TRANSFER_MODE_LIST,"Select From List");
        this.translate(LCText.TOOLTIP_ATM_TRANSFER_TRIGGER,"Transfer %1$s to %2$s");

        //Coin Chest
        this.translate(LCText.BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_YES,"Always Allowed");
        this.translate(LCText.BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_NO,"Block When Open");

        //Security Upgrade
        this.translate(LCText.MESSAGE_COIN_CHEST_PROTECTION_WARNING,"You do not have permission to access this chest");

        //Bank Upgrade
        this.translate(LCText.BUTTON_BANK_UPGRADE_MODE_DEPOSIT,"Deposit Money");
        this.translate(LCText.BUTTON_BANK_UPGRADE_MODE_WITHDRAW,"Withdraw Money");
        this.translate(LCText.GUI_BANK_UPGRADE_DETAILS_NO_ACCOUNT,"Must select a bank account!");
        this.translate(LCText.GUI_BANK_UPGRADE_DETAILS_DEPOSIT_UNLIMITED,"Deposits all money within the Money Chest");
        this.translate(LCText.GUI_BANK_UPGRADE_DETAILS_DEPOSIT_LIMITED,"Deposits money after the Money Chest contains at least %s");
        this.translate(LCText.GUI_BANK_UPGRADE_DETAILS_WITHDRAW,"Withdraws money until the Money Chest contains at least %s");
        this.translate(LCText.GUI_BANK_UPGRADE_DETAILS_WITHDRAW_INVALID,"Withdraw Limit must be defined!");

        //Ejection Menu
        this.translate(LCText.GUI_EJECTION_NO_DATA,"No Ejection Data Available");
        this.translate(LCText.TOOLTIP_EJECTION_SPLIT_GENERIC,"Dismantle Object into internal contents");
        this.translate(LCText.TOOLTIP_EJECTION_SPLIT_TRADER,"Dismantle %s into it's stored items");

        //Coin Mint Menu
        this.translate(LCText.GUI_COIN_MINT_TITLE, "Coin Mint");

        //Player Trade Menu
        this.translate(LCText.BUTTON_PLAYER_TRADING_PROPOSE,"Propose");
        this.translate(LCText.BUTTON_PLAYER_TRADING_ACCEPT,"Accept");
        this.translate(LCText.BUTTON_PLAYER_TRADING_CANCEL,"Cancel");
        this.translate(LCText.TOOLTIP_PLAYER_TRADING_MONEY_OPEN,"Open Money Offer Input");
        this.translate(LCText.TOOLTIP_PLAYER_TRADING_MONEY_CLOSE,"Close Money Offer Input");
        this.translate(LCText.TOOLTIP_PLAYER_TRADING_CHAT_OPEN,"Open Chat");
        this.translate(LCText.TOOLTIP_PLAYER_TRADING_CHAT_CLOSE,"Close Chat");

        //Tax Collector Menu
        this.translate(LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER,"Traders in this area are susceptible to taxes!");
        this.translate(LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_SERVER_ONLY,"All Traders on this server are susceptible to a server tax!");
        this.translate(LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_INFO,"Check your traders Tax Info tab for more information.");
        this.translate(LCText.GUI_TAX_COLLECTOR_DEFAULT_NAME,"%s's Taxes");
        this.translate(LCText.GUI_TAX_COLLECTOR_DEFAULT_NAME_SERVER,"Server");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_BASIC,"Basic Settings");
        this.translate(LCText.GUI_TAX_COLLECTOR_ACTIVE,"Active");
        this.translate(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_LABEL,"Area Visible To:");
        this.translate(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_NONE,"None");
        this.translate(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_MEMBERS,"Members");
        this.translate(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_ALL,"All");
        this.translate(LCText.GUI_TAX_COLLECTOR_TAX_RATE,"Tax Rate: %s%%");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_LABEL,"Area:");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_VOID,"All Dimensions");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_DIMENSION,"All of '%s'");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_RADIUS,"Radius");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_HEIGHT,"Height");
        this.translate(LCText.GUI_TAX_COLLECTOR_AREA_VERTOFFSET,"Y-Offset");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_LOGS,"Logs");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_INFO,"Collection Statistics");
        this.translate(LCText.BUTTON_TAX_COLLECTOR_STATS_CLEAR,"Clear Stats");
        this.translate(LCText.GUI_TAX_COLLECTOR_STATS_TOTAL_COLLECTED,"Total Taxes Collected:");
        this.translate(LCText.GUI_TAX_COLLECTOR_STATS_UNIQUE_TAXABLES,"Unique Machines Taxed: %s");
        this.translate(LCText.GUI_TAX_COLLECTOR_STATS_MOST_TAXED_LABEL,"Most Taxes Machine:");
        this.translate(LCText.GUI_TAX_COLLECTOR_STATS_MOST_TAXED_FORMAT,"%s interactions taxed");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_OWNER,"Ownership");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_ADMIN,"Admin Settings");
        this.translate(LCText.GUI_TAX_COLLECTOR_FORCE_ACCEPTANCE,"Force Acceptance");
        this.translate(LCText.GUI_TAX_COLLECTOR_INFINITE_RANGE,"Infinite Range");
        this.translate(LCText.GUI_TAX_COLLECTOR_TAXABLE_ACCEPT_COLLECTOR,"Accept");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_FORCE_IGNORE,"Ignore this Tax Collector!");
        this.translate(LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_PARDON_IGNORED,"Stop ignoring this Tax Collector!");
        this.translate(LCText.MESSAGE_TAX_COLLECTOR_WARNING_MISSING_DATA,"Tax Entry was missing for this block. Re-initializing with you as the owner!");
        this.translate(LCText.MESSAGE_TAX_COLLECTOR_WARNING_NO_ACCESS,"You do not have access to this Tax Collector");

        //Ticket Station Menu
        this.translate(LCText.GUI_TICKET_STATION_TITLE,"Ticket Station");
        this.translate(LCText.TOOLTIP_TICKET_STATION_RECIPE_INFO,"Will make %s");
        this.translate(LCText.TOOLTIP_TICKET_STATION_SELECT_RECIPE,"Scroll to change recipe");
        this.translate(LCText.TOOLTIP_TICKET_STATION_CRAFT,"Craft %s");

        //Trader Interface Menu
        this.translate(LCText.GUI_INTERFACE_INTERACTION_TYPE.get(InteractionType.RESTOCK_AND_DRAIN),"Restock & Drain");
        this.translate(LCText.GUI_INTERFACE_INTERACTION_TYPE.get(InteractionType.RESTOCK),"Restock");
        this.translate(LCText.GUI_INTERFACE_INTERACTION_TYPE.get(InteractionType.DRAIN),"Drain");
        this.translate(LCText.GUI_INTERFACE_INTERACTION_TYPE.get(InteractionType.TRADE),"Trade");
        this.translate(LCText.GUI_INTERFACE_ACTIVE_MODE.get(ActiveMode.DISABLED),"Disabled");
        this.translate(LCText.GUI_INTERFACE_ACTIVE_MODE.get(ActiveMode.REDSTONE_OFF),"No Redstone Signal");
        this.translate(LCText.GUI_INTERFACE_ACTIVE_MODE.get(ActiveMode.REDSTONE_ONLY),"Redstone Signal");
        this.translate(LCText.GUI_INTERFACE_ACTIVE_MODE.get(ActiveMode.ALWAYS_ON),"Always Active");
        this.translate(LCText.TOOLTIP_INTERFACE_ONLINE_MODE_ON, "Requires Owner to be Online");
        this.translate(LCText.TOOLTIP_INTERFACE_ONLINE_MODE_OFF, "On even if Owner is Offline");
        this.translate(LCText.TOOLTIP_INTERFACE_INFO,"Status");
        this.translate(LCText.TOOLTIP_INTERFACE_INFO_ACCEPT_CHANGES,"Accept Trade Changes");
        this.translate(LCText.GUI_INTERFACE_INFO_MISSING_PERMISSIONS,"You no longer have permission to link with %s");
        this.translate(LCText.GUI_INTERFACE_INFO_TRADER_NULL,"Not linked to any trader");
        this.translate(LCText.GUI_INTERFACE_INFO_TRADER_REMOVED,"Linked Trader no longer exists");
        this.translate(LCText.GUI_INTERFACE_INFO_TRADE_NOT_DEFINED,"No Trade linked");
        this.translate(LCText.GUI_INTERFACE_INFO_TRADE_MISSING,"Trade no longer exists");
        this.translate(LCText.TOOLTIP_INTERFACE_TRADER_SELECT,"Trader Select");
        this.translate(LCText.TOOLTIP_INTERFACE_TRADE_SELECT,"Trade Select");
        this.translate(LCText.TOOLTIP_INTERFACE_STORAGE,"Interface Storage");
        this.translate(LCText.TOOLTIP_INTERFACE_STATS,"Interface Stats");

        //Trade Result
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_OUT_OF_STOCK),"Trader is out of stock");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_CANNOT_AFFORD),"You can no longer afford this trade");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_NO_OUTPUT_SPACE),"Insufficient space to output the purchased product");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_NO_INPUT_SPACE),"Trader has insufficient space to store the collected product");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_TRADE_RULE_DENIAL),"A Trade Rule has denied your ability to interact with the trade");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_TAX_EXCEEDED_LIMIT),"This Trader's Tax Collection exceeds its defined limits, and has locked the trader until it can be resolved");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_INVALID_TRADE),"The trade is no longer a valid trade");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_NOT_SUPPORTED),"This trader does not support this type of trade interaction");
        this.translate(LCText.GUI_TRADE_RESULT.get(TradeResult.FAIL_NULL),"The trade or trader no longer exists");

        //Trade Comparison
        this.translate(LCText.GUI_TRADE_DIFFERENCE_MISSING,"Trade or Trader no longer exists");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_TYPE,"Trade is a different trade type");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_MONEY_TYPE,"Trade expects a different type of money");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_CHEAPER,"Trade is %s cheaper");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_EXPENSIVE,"Trade is %s more expensive");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_PURCHASE_CHEAPER,"Trade pays %s less");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_PURCHASE_EXPENSIVE,"Trade pays %s more");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_SELLING, "selling");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_PURCHASING, "expecting");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_TYPE,"Trade is %s a different item");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_NBT,"Trade is %s an item with different NBT data");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_MORE,"Trade is %1$s %2$s more item(s)");
        this.translate(LCText.GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_LESS,"Trade is %1$s %2$s fewer item(s)");

        //Wallet Menu
        this.translate(LCText.TOOLTIP_WALLET_EXCHANGE, "Compress Coins to Highest Value Coinage");
        this.translate(LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE, "Enable Auto-Exchange");
        this.translate(LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE, "Disable Auto-Exchange");
        this.translate(LCText.TOOLTIP_WALLET_OPEN_BANK,"Access Bank Account");
        this.translate(LCText.TOOLTIP_WALLET_OPEN_WALLET,"Return to Wallet");

        //Trader Menu
        this.translate(LCText.GUI_TRADER_TITLE,"%1$s (%2$s)");
        this.translate(LCText.GUI_TRADER_DEFAULT_NAME, "Trader");
        this.translate(LCText.GUI_TRADER_ALL_NETWORK_TRADERS, "All Network Traders");
        this.translate(LCText.GUI_TRADER_SEARCH_TRADES,"Search Trades");
        this.translate(LCText.TOOLTIP_TRADER_OPEN_STORAGE,"Open Trader Storage");
        this.translate(LCText.TOOLTIP_TRADER_COLLECT_COINS,"Collect Stored Money:");
        this.translate(LCText.TOOLTIP_TRADER_NETWORK_BACK,"Back to Network Terminal");
        this.translate(LCText.TOOLTIP_TRADER_OPEN_TRADES,"Return to Trades");
        this.translate(LCText.TOOLTIP_TRADER_STORE_COINS,"Store Coins");
        this.translate(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADER,"Trader Rules");
        this.translate(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE,"Edit Trade-Specific Rules");
        this.translate(LCText.GUI_TRADER_NO_TRADES, "No Available Trades");
        this.translate(LCText.TOOLTIP_TRADER_EDIT_TRADES,"Edit Trades");
        this.translate(LCText.TOOLTIP_TRADER_SELECT_ALL_TRADES,"Select All Trades");
        this.translate(LCText.TOOLTIP_TRADER_DESELECT_ALL_TRADES,"Deselect All Trades");
        this.translate(LCText.TOOLTIP_TRADER_OPEN_MULTI_EDIT_SELECTED,"Edit Price for All %s Selected Trade(s)");
        this.translate(LCText.BUTTON_TRADER_SET_ALL_PRICES,"Set Price for %s Trade(s)");
        this.translate(LCText.TOOLTIP_TRADER_UPGRADES,"Upgrades");
        this.translate(LCText.TOOLTIP_TRADER_LOGS,"Trader Logs");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS,"Trader Settings");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_NAME,"Trader Name");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE,"Creative Trader");
        this.translate(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_ENABLED,"Trader Mode: CREATIVE");
        this.translate(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_DISABLED,"Trader Mode: STANDARD");
        this.translate(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_ADD_TRADE,"Add Trade Offer");
        this.translate(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_REMOVE_TRADE,"Remove Trade Offer");
        this.translate(LCText.GUI_TRADER_SETTINGS_CREATIVE_TRADE_COUNT,"Trade Offers: %s");
        this.translate(LCText.GUI_TRADER_SETTINGS_CREATIVE_STORE_MONEY,"Store Earned Money");
        this.translate(LCText.GUI_TRADER_SETTINGS_CUSTOM_ICON,"Custom Terminal Icon");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_PERSISTENT,"Build Persistent Copy");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_ALLY, "Allies");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_ALLY_PERMS, "Ally Permissions");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_MISC,"Misc Settings");
        this.translate(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_ENABLED,"Notifications Enabled");
        this.translate(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT,"Notify in Chat");
        this.translate(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_TARGET,"Send To: %s");
        this.translate(LCText.GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX,"Enable Search Box");
        this.translate(LCText.BUTTON_TRADER_SETTINGS_PICKUP_TRADER,"Pickup Entire Trader");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER,"Returns the trader to its item form but retains the trader's current state once the block is placed");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER_ADVANCED,"Hold SHIFT to keep the trader accessible after pickup");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_TAXES,"Tax Options");
        this.translate(LCText.GUI_TRADER_SETTINGS_TAXES_ACCEPTABLE_RATE,"Acceptable Tax Rate: %s%%");
        this.translate(LCText.GUI_TRADER_SETTINGS_TAXES_IGNORE_TAXES,"Ignore All Taxes");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_INPUT_GENERIC,"External Input & Output");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM,"External Item Input & Output");
        this.translate(LCText.TOOLTIP_TRADER_STATS,"Trader Stats");
        this.translate(LCText.BUTTON_TRADER_STATS_CLEAR,"Clear Data");
        this.translate(LCText.GUI_TRADER_STATS_EMPTY,"No Statistics Recorded");
        this.translate(LCText.TOOLTIP_TRADER_TAXES,"Tax Information");
        this.translate(LCText.GUI_TRADER_TAXES_TOTAL_RATE,"Total Tax Rate: %s%%");
        this.translate(LCText.GUI_TRADER_TAXES_NO_TAX_COLLECTORS,"No active Tax Collectors in range");
        this.translate(LCText.TOOLTIP_TRADER_STORAGE,"Trader Storage");
        this.translate(LCText.MESSAGE_TRADER_WARNING_MISSING_DATA, "Trader Data was missing for this block. Re-initializing the trader with you as the owner!");

        //General Trade Tooltips
        this.translate(LCText.TOOLTIP_TRADE_EDIT_PRICE, "Click to Edit Price");
        this.translate(LCText.TOOLTIP_TRADE_SELECT, "Ctrl + Click to Select Trade");
        this.translate(LCText.TOOLTIP_TRADE_INFO_TITLE, "Trade Info:");
        this.translate(LCText.TOOLTIP_TRADE_INFO_ORIGINAL_NAME, "Original Name: %s");
        this.translate(LCText.TOOLTIP_TRADE_INFO_STOCK, "%s trade(s) in stock");
        this.translate(LCText.TOOLTIP_TRADE_INFO_STOCK_INFINITE, "Infinite");

        //Item Trader Specific
        this.translate(LCText.GUI_TRADER_ITEM_ENFORCE_NBT,"Enforce NBT");
        this.translate(LCText.TOOLTIP_TRADE_ITEM_EDIT_EMPTY,"Click to Set Item");
        this.translate(LCText.TOOLTIP_TRADE_ITEM_EDIT_SHIFT,"Hold SHIFT & Click to access Item Settings");
        this.translate(LCText.TOOLTIP_TRADE_ITEM_NBT_WARNING_SALE,"NBT is NOT enforced. Item may have unexpected NBT data");
        this.translate(LCText.TOOLTIP_TRADE_ITEM_NBT_WARNING_PURCHASE,"Accepts all NBT states");
        this.translate(LCText.GUI_ITEM_EDIT_SEARCH,"Search Items");
        this.translate(LCText.TOOLTIP_ITEM_EDIT_SCROLL,"Scroll to change stack size");

        //Paygate Specific
        this.translate(LCText.TOOLTIP_TRADER_PAYGATE_COLLECT_TICKET_STUBS,"Collect Ticket Stubs (%s)");
        this.translate(LCText.GUI_TRADER_PAYGATE_DURATION,"Duration:");
        this.translate(LCText.GUI_TRADER_PAYGATE_DURATION_UNIT,"ticks");
        this.translate(LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_KEEP,"Store Ticket Stubs");
        this.translate(LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_GIVE,"Give Ticket Stubs to the Customer");
        this.translate(LCText.TOOLTIP_TRADER_PAYGATE_ALREADY_ACTIVE,"Already Active");

        //Auction House Specific
        this.translate(LCText.GUI_TRADER_AUCTION_HOUSE,"Auction House");
        this.translate(LCText.GUI_TRADER_AUCTION_HOUSE_OWNER,"Server");
        this.translate(LCText.BUTTON_TRADER_AUCTION_BID,"Submit Bid");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_STORAGE,"Stored Winnings & Earnings");
        this.translate(LCText.GUI_TRADER_AUCTION_STORAGE_ITEMS_NONE,"No items in storage");
        this.translate(LCText.GUI_TRADER_AUCTION_STORAGE_MONEY,"Storing %s");
        this.translate(LCText.GUI_TRADER_AUCTION_STORAGE_MONEY_NONE,"No money stored");
        this.translate(LCText.GUI_TRADER_AUCTION_CANCEL,"Cancel Auction and...");
        this.translate(LCText.BUTTON_TRADER_AUCTION_CANCEL_SELF,"Return Items to Self");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_CANCEL_SELF,"Will cancel the auction. The latest bid will be refunded, and your items will be returned to your inventory");
        this.translate(LCText.BUTTON_TRADER_AUCTION_CANCEL_STORAGE,"Send Items to Storage");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_CANCEL_STORAGE,"Will cancel the auction. The latest bid will be refunded, and your items will be sent to your storage");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_CREATE,"Setup an auction");
        this.translate(LCText.GUI_TRADER_AUCTION_OVERTIME,"Allow Overtime");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_OVERTIME,"Overtime Mode:","If enabled, any bids made within the last 5 minutes of the auctions timer will reset the remaining time to bid back to 5m.","Enable if you wish to avoid last second bid-snatching");
        this.translate(LCText.BUTTON_TRADER_AUCTION_PRICE_MODE_STARTING_BID,"Start Bid");
        this.translate(LCText.BUTTON_TRADER_AUCTION_PRICE_MODE_MIN_BID_SIZE,"Bid Size");
        this.translate(LCText.BUTTON_TRADER_AUCTION_CREATE,"Create Auction");
        this.translate(LCText.GUI_TRADER_AUCTION_ITEMS,"Items:");
        this.translate(LCText.GUI_TRADER_AUCTION_CREATE_SUCCESS,"Successfully Created!");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_INFO_NO_BIDDER,"Nobody has bid yet");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_INFO_STARTING_BID,"Starting Bid: %s");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_INFO_LAST_BIDDER,"%s currently has the highest bid");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_INFO_LAST_BID,"Last Bid: %s");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_INFO_MIN_BID,"Minimum Bid: %s");
        this.translate(LCText.TOOLTIP_TRADER_AUCTION_TIME_REMAINING,"Time Remaining: %s");

        //Slot Machine Specific
        this.translate(LCText.TOOLTIP_TRADER_SLOT_MACHINE_EDIT_ENTRIES,"Edit Entries");
        this.translate(LCText.GUI_TRADER_SLOT_MACHINE_WEIGHT_LABEL,"Weight: ");
        this.translate(LCText.GUI_TRADER_SLOT_MACHINE_ENTRY_LABEL,"Entry #%s");
        this.translate(LCText.GUI_TRADER_SLOT_MACHINE_ODDS_LABEL,"Chance: %s%%");
        this.translate(LCText.TOOLTIP_TRADER_SLOT_MACHINE_EDIT_PRICE,"Edit Price");

        //Command Trader Specific
        this.translate(LCText.GUI_TRADER_COMMAND_LABEL,"Command:");
        this.translate(LCText.TOOLTIP_TRADER_SETTINGS_COMMAND,"Command Settings");
        this.translate(LCText.GUI_TRADER_SETTINGS_COMMAND_PERMISSION_LEVEL,"Permission Level: %s");

        //Trade Data
        this.translate(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.SALE), "Sale");
        this.translate(LCText.GUI_TRADE_DIRECTION_ACTION.get(TradeDirection.SALE),"bought");
        this.translate(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.PURCHASE), "Purchase");
        this.translate(LCText.GUI_TRADE_DIRECTION_ACTION.get(TradeDirection.PURCHASE), "sold");
        this.translate(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.BARTER), "Barter");
        this.translate(LCText.GUI_TRADE_DIRECTION_ACTION.get(TradeDirection.BARTER), "bartered");
        this.translate(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.OTHER), "Other");
        this.translate(LCText.GUI_TRADE_DIRECTION_ACTION.get(TradeDirection.OTHER), "ERROR");


        //Trade Rules
        this.translate(LCText.GUI_TRADE_RULES_LIST,"Trade Rules:");
        this.translate(LCText.TOOLTIP_TRADE_RULES_MANAGER,"Rule Management");
        this.translate(LCText.TRADE_RULE_PLAYER_LISTING,"Whitelist/Blacklist");
        this.translate(LCText.TRADE_RULE_PLAYER_LISTING_DENIAL_BLACKLIST,"You are blacklisted");
        this.translate(LCText.TRADE_RULE_PLAYER_LISTING_DENIAL_WHITELIST,"You are not whitelisted");
        this.translate(LCText.TRADE_RULE_PLAYER_LISTING_ALLOWED,"You are whitelisted");
        this.translate(LCText.BUTTON_PLAYER_LISTING_MODE_WHITELIST,"Mode: Whitelist");
        this.translate(LCText.BUTTON_PLAYER_LISTING_MODE_BLACKLIST,"Mode: Blacklist");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT,"Player Trade Limit");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIMED,"You have done this trade %1$s times within the last %2$s");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL,"You have done this trade %s times already");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT,"Limit is %s");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO_TIMED,"You have done this trade %1$s of %2$s times within the last %3$s");
        this.translate(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO,"You have done this trade %1$s of %2$s times");
        this.translate(LCText.TOOLTIP_TRADE_LIMIT_CLEAR_MEMORY,"Clears the memory of how many trades have been handled");
        this.translate(LCText.GUI_TRADE_LIMIT_INFO,"Players can do %s trade(s)");
        this.translate(LCText.GUI_PLAYER_TRADE_LIMIT_DURATION,"Forget after %s");
        this.translate(LCText.GUI_PLAYER_TRADE_LIMIT_NO_DURATION,"Never forget interactions");
        this.translate(LCText.TRADE_RULE_PLAYER_DISCOUNTS,"Discount");
        this.translate(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_SALE,"You have been given a %s%% discount");
        this.translate(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_PURCHASE,"You will be paid %s%% more");
        this.translate(LCText.GUI_PLAYER_DISCOUNTS_INFO,"% Discount");
        this.translate(LCText.TRADE_RULE_TIMED_SALE,"Sale");
        this.translate(LCText.TRADE_RULE_TIMED_SALE_INFO_SALE,"This trade is on sale for %1$s%% for $2$s");
        this.translate(LCText.TRADE_RULE_TIMED_SALE_INFO_PURCHASE,"This trade is on sale and will pay %1$s%% more for $2$s");
        this.translate(LCText.GUI_TIMED_SALE_INFO_ACTIVE,"%s until the sale ends");
        this.translate(LCText.GUI_TIMED_SALE_INFO_INACTIVE,"Pending Duration: %s");
        this.translate(LCText.BUTTON_TIMED_SALE_START,"Start");
        this.translate(LCText.BUTTON_TIMED_SALE_STOP,"Stop");
        this.translate(LCText.TOOLTIP_TIMED_SALE_START,"Start the Sale");
        this.translate(LCText.TOOLTIP_TIMED_SALE_STOP,"Stop the Sale early");
        this.translate(LCText.TRADE_RULE_TRADE_LIMIT,"Trade Limit");
        this.translate(LCText.TRADE_RULE_TRADE_LIMIT_DENIAL,"This trade has been done %s times already");
        this.translate(LCText.TRADE_RULE_TRADE_LIMIT_INFO,"This trade has been done %1$s of %2$s times");
        this.translate(LCText.TRADE_RULE_FREE_SAMPLE,"Free Sample");
        this.translate(LCText.TRADE_RULE_FREE_SAMPLE_INFO_SINGLE,"Your first purchase is free!");
        this.translate(LCText.TRADE_RULE_FREE_SAMPLE_INFO_MULTI,"Your first %s purchases are free!");
        this.translate(LCText.TRADE_RULE_FREE_SAMPLE_INFO_USED,"You have used %1$s of your %2$s free samples");
        this.translate(LCText.TRADE_RULE_FREE_SAMPLE_INFO_TIMED,"Free sample resets after %s");
        this.translate(LCText.BUTTON_FREE_SAMPLE_RESET,"Reset Free Samples");
        this.translate(LCText.TOOLTIP_FREE_SAMPLE_RESET,"Forgets who has received their free sample(s) so that they may receive it again");
        this.translate(LCText.GUI_FREE_SAMPLE_PLAYER_COUNT,"%s free samples have been given!");
        this.translate(LCText.GUI_FREE_SAMPLE_INFO,"Players can claim %s free sample(s)");
        this.translate(LCText.TRADE_RULE_PRICE_FLUCTUATION,"Price Fluctuation");
        this.translate(LCText.GUI_PRICE_FLUCTUATION_LABEL,"% Fluctuation");
        this.translate(LCText.GUI_PRICE_FLUCTUATION_INFO,"Randomly increases or decreases the price by up to %1$s%% of the base price every %2$s");
        this.translate(LCText.TRADE_RULE_DEMAND_PRICING,"Demand-Based Pricing");
        this.translate(LCText.TRADE_RULE_DEMAND_PRICING_INFO,"Trade Price changes depending on available stock");
        this.translate(LCText.GUI_DEMAND_PRICING_INFO, "Price will fluctuate between %1$s (stock <= %4$s) to %2$s (stock >= %3$s)");
        this.translate(LCText.GUI_DEMAND_PRICING_INFO_INVALID_PRICE, "Price inputs are invalid!");
        this.translate(LCText.GUI_DEMAND_PRICING_INFO_INVALID_STOCK, "Upper stock limit <= Lower stock limit");
        this.translate(LCText.GUI_DEMAND_PRICING_INFO_INVALID_HOST, "Trade is not a Sale which means stock count may be dependent on the price");
        this.translate(LCText.GUI_DEMAND_PRICING_STOCK_SMALL, "Lower Stock:");
        this.translate(LCText.GUI_DEMAND_PRICING_STOCK_LARGE, "Upper Stock:");
        this.translate(LCText.TRADE_RULE_DAILY_TRADES,"Daily Trade Progression");
        this.translate(LCText.TRADE_RULE_DAILY_TRADES_ALLOWED,"Trade is unlocked");
        this.translate(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_COMPLETE,"Trade has already been redeemed");
        this.translate(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_WAITING,"Trade can be redeemed in %s");
        this.translate(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_NOT_NEXT,"Waiting for previous trade to be redeemed");
        this.translate(LCText.BUTTON_DAILY_TRADES_RESET,"Reset Interaction Data");
        this.translate(LCText.TOOLTIP_DAILY_TRADES_RESET,"Forgets each players progress through the trade list, resetting them back to the initial trade");
        this.translate(LCText.GUI_DAILY_TRADES_INFO,"Players can interact with a trade %s after interacting with the previous one");

        //Permissions
        this.translate(LCText.PERMISSION_OPEN_STORAGE,"Open Storage");
        this.translate(LCText.PERMISSION_CHANGE_NAME,"Change Name");
        this.translate(LCText.PERMISSION_EDIT_TRADES,"Modify Trades");
        this.translate(LCText.PERMISSION_COLLECT_MONEY,"Collect Money");
        this.translate(LCText.PERMISSION_STORE_MONEY, "Store Money");
        this.translate(LCText.PERMISSION_EDIT_TRADE_RULES, "Edit Trade Rules");
        this.translate(LCText.PERMISSION_EDIT_SETTINGS, "Access Settings");
        this.translate(LCText.PERMISSION_EDIT_ALLIES, "Add/Remove Allies");
        this.translate(LCText.PERMISSION_EDIT_PERMISSIONS, "Edit Permissions");
        this.translate(LCText.PERMISSION_VIEW_LOGS, "View Logs");
        this.translate(LCText.PERMISSION_BANK_LINK, "Link Bank Account");
        this.translate(LCText.PERMISSION_BREAK_MACHINE, "Break Machine");
        this.translate(LCText.PERMISSION_TRANSFER_OWNERSHIP, "Transfer Ownership");
        this.translate(LCText.PERMISSION_INTERACTION_LINK, "Setup Interface");
        this.translate(LCText.PERMISSION_EDIT_INPUTS,"Change Input & Output Settings");

        //Inventory Buttons
        this.translate(LCText.TOOLTIP_NOTIFICATION_BUTTON,"Open Notification Manager");
        this.translate(LCText.TOOLTIP_TEAM_MANAGER_BUTTON,"Open Team Manager");
        this.translate(LCText.TOOLTIP_EJECTION_BUTTON,"Open Ejection Manager");
        this.translate(LCText.TOOLTIP_CHEST_COIN_COLLECTION_BUTTON,"Quick Move Coins Into Wallet");

        //Generic Settings
        this.translate(LCText.BUTTON_SETTINGS_CHANGE_NAME,"Change Name");
        this.translate(LCText.BUTTON_SETTINGS_RESET_NAME,"Reset Name");
        this.translate(LCText.GUI_SETTINGS_BANK_LINK,"Link to Bank Account");
        this.translate(LCText.TOOLTIP_SETTINGS_OWNER,"Transfer Ownership");
        this.translate(LCText.GUI_SETTINGS_INPUT_SIDE,"Input Sides");
        this.translate(LCText.GUI_SETTINGS_OUTPUT_SIDE,"Output Sides");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.DOWN),"Bottom");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.UP),"Top");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.NORTH),"Back");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.SOUTH),"Front");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.WEST),"Left");
        this.translate(LCText.GUI_INPUT_SIDES.get(Direction.EAST),"Right");

        //Persistent Data
        this.translate(LCText.GUI_PERSISTENT_ID, "ID: ");
        this.translate(LCText.GUI_PERSISTENT_OWNER, "PT Owner: ");
        //Trader
        this.translate(LCText.TOOLTIP_PERSISTENT_CREATE_TRADER, "Create a Persistent Trader with a copy of this traders trades & name.");
        this.translate(LCText.MESSAGE_PERSISTENT_TRADER_OVERWRITE,"Overwrote Persistent Trader with id %s");
        this.translate(LCText.MESSAGE_PERSISTENT_TRADER_ADD,"Added Persistent Trader with id %s");
        this.translate(LCText.MESSAGE_PERSISTENT_TRADER_FAIL,"Error occurred when attempting to add the Persistent Trader");
        //Auction
        this.translate(LCText.TOOLTIP_PERSISTENT_CREATE_AUCTION, "Create a Persistent Auction with the given data");
        this.translate(LCText.MESSAGE_PERSISTENT_AUCTION_OVERWRITE,"Overwrote Persistent Auction with id %s");
        this.translate(LCText.MESSAGE_PERSISTENT_AUCTION_ADD,"Added Persistent Auction with id %s");
        this.translate(LCText.MESSAGE_PERSISTENT_AUCTION_FAIL,"Error occurred when attempting to add the Persistent Auction");


        ///MISC/Shared Assets
        //Bank Gui's/Buttons
        this.translate(LCText.GUI_BANK_BALANCE,"Balance: %s");
        this.translate(LCText.GUI_BANK_NO_SELECTED_ACCOUNT,"No account selected");
        this.translate(LCText.BUTTON_BANK_DEPOSIT,"Deposit");
        this.translate(LCText.BUTTON_BANK_WITHDRAW,"Withdraw");
        this.translate(LCText.GUI_BANK_ACCOUNT_NAME,"%s's Bank Account");

        //Bank Transfer Messages
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_NULL_FROM,"Your selected bank account no longer exists");
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_ACCESS,"You no longer have access to your selected bank account!");
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_NULL_TARGET,"Target Bank Account does not exist");
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_AMOUNT,"Cannot transfer %s");
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_SAME,"Cannot transfer to the same account");
        this.translate(LCText.GUI_BANK_TRANSFER_ERROR_NO_BALANCE,"Cannot transfer %s, as your bank account has no money of that type");
        this.translate(LCText.GUI_BANK_TRANSFER_SUCCESS,"Transferred %1$s to %2$s");



        //Ownership Blurbs
        this.translate(LCText.BLURB_OWNERSHIP_MEMBERS,"Members");
        this.translate(LCText.BLURB_OWNERSHIP_ADMINS,"Admins");
        this.translate(LCText.BLURB_OWNERSHIP_OWNER,"Owner");
        this.translate(LCText.BUTTON_OWNER_SET_PLAYER,"Transfer to Player");
        this.translate(LCText.GUI_OWNER_CURRENT,"Current Owner: %s");
        this.translate(LCText.GUI_OWNER_NULL,"UNDEFINED");
        this.translate(LCText.TOOLTIP_OWNERSHIP_MODE_MANUAL,"Switch to Manual Player Input Mode");
        this.translate(LCText.TOOLTIP_OWNERSHIP_MODE_SELECTION,"Switch to List Selection Mode");

        //Bank Card Menu
        this.translate(LCText.MESSAGE_ATM_CARD_LOCKED,"Card's Account is locked and cannot be changed");
        this.translate(LCText.BUTTON_ATM_CARD_LOCK,"Lock Card's Account");
        this.translate(LCText.TOOLTIP_ATM_CARD_LOCK,"Will close the menu and permanently lock this cards selected account!");
        this.translate(LCText.BUTTON_ATM_CARD_UNLOCK,"Unlock Card's Account");

        //Money Text
        this.translate(LCText.GUI_MONEY_VALUE_FREE,"Free");
        this.translate(LCText.GUI_MONEY_STORAGE_EMPTY,"Nothing");
        this.translate(LCText.COIN_CHAIN_MAIN,"Main");
        this.translate(LCText.COIN_CHAIN_CHOCOLATE,"Chocolate");
        this.translate(LCText.COIN_CHAIN_CHOCOLATE_DISPLAY,"%sCC");
        this.translate(LCText.COIN_CHAIN_CHOCOLATE_DISPLAY_WORDY,"%s Chocolate Chunks");
        this.translate(LCText.COIN_CHAIN_EMERALDS,"Emeralds");
        this.translate(LCText.COIN_CHAIN_EMERALDS_DISPLAY,"%sE");
        this.translate(LCText.COIN_CHAIN_EMERALDS_DISPLAY_WORDY,"%s Emeralds");
        this.translate(LCText.ANCIENT_COIN_VALUE_NAME,"Ancient");
        this.translate(LCText.ANCIENT_COIN_VALUE_DISPLAY,"%1$s %2$s");

        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.COPPER),"Copper");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.IRON),"Iron");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.GOLD),"Gold");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.EMERALD),"Emerald");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.DIAMOND),"Diamond");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_H),"Netherite v1");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_E1),"Netherite v2");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_R1),"Netherite v3");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_O),"Netherite v4");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_B),"Netherite v5");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_R2),"Netherite v6");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_I),"Netherite v7");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_N),"Netherite v8");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.NETHERITE_E2),"Netherite v9");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.LAPIS),"Lapis");
        this.translate(LCText.ANCIENT_COIN_TYPE_LABEL.get(AncientCoinType.ENDER_PEARL),"Ender Pearl");

        //Generic Text
        this.translate(LCText.BUTTON_CHANGE_NAME_ICON,"Aa");
        this.translate(LCText.GUI_NAME,"Name:");
        this.translate(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE,"WARNING: Cannot be undone!");
        this.translate(LCText.TOOLTIP_ITEM_COUNT,"Count: %1$s/%2$s");
        this.translate(LCText.BUTTON_ADD,"Add");
        this.translate(LCText.BUTTON_REMOVE,"Remove");
        this.translate(LCText.BUTTON_SET,"Set");
        this.translate(LCText.BUTTON_CLEAR_MEMORY,"Clear Memory");
        this.translate(LCText.MISC_GENERIC_PLURAL, "%ss");
        this.translate(LCText.GUI_SEPERATOR,", ");
        this.translate(LCText.GUI_ADDED,"added");
        this.translate(LCText.GUI_REMOVED,"removed");
        this.translate(LCText.GUI_TO,"to");
        this.translate(LCText.GUI_FROM,"from");
        this.translate(LCText.GUI_AND,"%1$s and %2$s");

        //Time Unit
        this.translate(LCText.TIME_UNIT_DAY," day", " days", "d");
        this.translate(LCText.TIME_UNIT_HOUR," hour", " hours", "h");
        this.translate(LCText.TIME_UNIT_MINUTE," minute", " minutes", "m");
        this.translate(LCText.TIME_UNIT_SECOND," second", " seconds", "s");
        this.translate(LCText.TIME_UNIT_MILLISECOND," millisecond", " milliseconds", "ms");
        this.translate(LCText.TIME_UNIT_TICK," tick", " ticks", "t");

        //Notifications
        this.translate(LCText.NOTIFICATION_FORMAT_GENERAL,"%1$s: %2$s");
        this.translate(LCText.NOTIFICATION_FORMAT_CHAT,"%1$s %2$s");
        this.translate(LCText.NOTIFICATION_FORMAT_CHAT_TITLE,"[%s]");
        this.translate(LCText.NOTIFICATION_TIMESTAMP,"%s");
        this.translate(LCText.NOTIFICATION_SOURCE_GENERAL,"General");
        this.translate(LCText.TOOLTIP_NOTIFICATION_DELETE,"Delete");
        //Auction Notifications
        this.translate(LCText.NOTIFICATION_AUCTION_BID,"%1$s out-bid your bid for %2$s with a bid of %3$s");
        this.translate(LCText.NOTIFICATION_AUCTION_BUYER,"You won the auction for %1$s with a bid of %2$s");
        this.translate(LCText.NOTIFICATION_AUCTION_CANCEL,"The auction for %s has been canceled");
        this.translate(LCText.NOTIFICATION_AUCTION_SELLER_NO_BID,"Your auction for %s was not bid on within the time limit");
        this.translate(LCText.NOTIFICATION_AUCTION_SELLER,"%1$s won your auction for %2$s with a bid of %3$s");
        //Bank Notifications
        this.translate(LCText.NOTIFICATION_BANK_INTEREST,"Gained %s in interest");
        this.translate(LCText.NOTIFICATION_BANK_TRANSFER,"%1$s transferred %2$s %3$s %4$s");
        this.translate(LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW,"%1$s %2$s %3$s");
        this.translate(LCText.NOTIFICATION_BANK_DEPOSIT,"deposited");
        this.translate(LCText.NOTIFICATION_BANK_WITHDRAW,"withdrew");
        this.translate(LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW_SERVER,"An admin");
        this.translate(LCText.NOTIFICATION_BANK_LOW_BALANCE,"Bank Account is below %s");
        //Ejection Notifications
        this.translate(LCText.NOTIFICATION_EJECTION_ANARCHY,"%s was robbed or destroyed by another player");
        this.translate(LCText.NOTIFICATION_EJECTION_EJECTED,"%s was destroyed by unpreventable means, but its contents were safely ejected");
        this.translate(LCText.NOTIFICATION_EJECTION_DROPPED,"%s was destroyed by unpreventable means, and its contents were dropped");
        //Settings Notifications
        this.translate(LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_ALLY,"%1$s %2$s %3$s %4$s the list of allies");
        this.translate(LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_TRADE,"%1$s %2$s a trade slot. Trader now has %3$s trades");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS,"%1$s changed allies %2$s value from %3$s to %4$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS_SIMPLE,"%1$s changed allies %2$s value to %3$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE,"%1$s %2$s creative mode");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_ENABLED,"ENABLED");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_DISABLED,"DISABLED");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_NAME,"%1$s changed the name from %2$s to %3$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_SET,"%1$s set the name to %2$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_RESET,"%1$s reset the name to default");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_PASSED,"%1$s transferred ownership to %2$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TAKEN,"%1$s claimed ownership from %2$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TRANSFERRED,"%1$s transferred ownership from %2$s to %3$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_SIMPLE,"%1$s changed %2$s to %3$s");
        this.translate(LCText.NOTIFICATION_SETTINGS_CHANGE_ADVANCED,"%1$s changed %2$s from %3$s to %4$s");
        //Tax Notifications
        this.translate(LCText.NOTIFICATION_TAXES_COLLECTED,"Collected %1$s in taxes from %2$s");
        this.translate(LCText.NOTIFICATION_TAXES_PAID,"%s was paid in taxes");
        this.translate(LCText.NOTIFICATION_TAXES_PAID_NULL,"No taxes were paid???");
        //Trader Notifications
        this.translate(LCText.NOTIFICATION_TRADER_OUT_OF_STOCK,"Trade #%2$s is out of stock");
        this.translate(LCText.NOTIFICATION_TRADER_OUT_OF_STOCK_INDEXLESS,"Trader is out of stock");
        this.translate(LCText.NOTIFICATION_TRADE_ITEM,"%1$s %2$s %3$s for %4$s");
        this.translate(LCText.NOTIFICATION_TRADE_PAYGATE_TICKET,"%1$s activated the paygate for %3$s using Ticket ID: %2$s");
        this.translate(LCText.NOTIFICATION_TRADE_PAYGATE_PASS,"%1$s activated the paygate for %3$s using Pass ID: %2$s");
        this.translate(LCText.NOTIFICATION_TRADE_PAYGATE_MONEY,"%1$s activated the paygate for %3$s for %2$s");
        this.translate(LCText.NOTIFICATION_TRADE_SLOT_MACHINE,"%1$s paid %2$s and won %3$s");
        this.translate(LCText.NOTIFICATION_TRADE_COMMAND,"%1$s paid %2$s to run %3$s");
        //Item Notification Parts
        this.translate(LCText.NOTIFICATION_ITEM_FORMAT,"%1$sx %2$s");

        //LC Statistics
        //Generic
        this.translate(StatKeys.Generic.MONEY_EARNED,"Money Earned: %s");
        this.translate(StatKeys.Generic.MONEY_PAID,"Money Paid: %s");
        this.translate(StatKeys.Generic.SALARY_TRIGGERS,"Salary Payments Triggered: %s");
        //Trader
        this.translate(StatKeys.Traders.MONEY_EARNED,"Trader Earned: %s");
        this.translate(StatKeys.Traders.MONEY_PAID,"Trader Paid: %s");
        this.translate(StatKeys.Traders.TRADES_EXECUTED,"Trades Handled: %s");
        //Taxable
        this.translate(StatKeys.Taxables.TAXES_PAID,"Taxes Paid: %s");

        //Command Arguments
        this.translate(LCText.ARGUMENT_MONEY_VALUE_NOT_A_COIN,"'%s' is not a valid coin!");
        this.translate(LCText.ARGUMENT_MONEY_VALUE_NOT_AN_ANCIENT_COIN,"'%s' is not a valid ancient coin!");
        this.translate(LCText.ARGUMENT_MONEY_VALUE_NO_VALUE,"Value parsed had no value!");
        this.translate(LCText.ARGUMENT_MONEY_VALUE_NOT_EMPTY_OR_FREE,"Input is not 'free' or 'empty'!");
        this.translate(LCText.ARGUMENT_COLOR_INVALID,"Color could not be parsed");
        this.translate(LCText.ARGUMENT_TRADEID_INVALID,"Trade ID is not a valid numerical value");
        this.translate(LCText.ARGUMENT_TRADER_NOT_FOUND,"Could not find a trader with the given ID");
        this.translate(LCText.ARGUMENT_TRADER_NOT_RECOVERABLE,"Trader is not in a state that would require recovery");

        //Commands
        this.translate(LCText.COMMAND_BALTOP_NO_RESULTS,"No Bank Accounts found");
        this.translate(LCText.COMMAND_BALTOP_ERROR_PAGE,"Page is too large");
        this.translate(LCText.COMMAND_BALTOP_TITLE,"Top Bank Account Balances");
        this.translate(LCText.COMMAND_BALTOP_PAGE,"Page %1$s of %2$s");
        this.translate(LCText.COMMAND_BALTOP_ENTRY,"%1$s-%2$s (%3$s)");

        this.translate(LCText.COMMAND_BANK_TEAM_NULL,"No team found with the id %s");
        this.translate(LCText.COMMAND_BANK_TEAM_NO_BANK,"Team with id %s does not have a bank account");
        this.translate(LCText.COMMAND_BANK_GIVE_FAIL,"No bank accounts received your gift");
        this.translate(LCText.COMMAND_BANK_GIVE_SUCCESS,"Gave %1$s to %2$s bank accounts");
        this.translate(LCText.COMMAND_BANK_GIVE_SUCCESS_SINGLE,"Gave %1$s to %2$s");
        this.translate(LCText.COMMAND_BANK_TAKE_FAIL,"No bank accounts had money taken");
        this.translate(LCText.COMMAND_BANK_TAKE_SUCCESS,"Took up to %1$s from %2$s bank accounts");
        this.translate(LCText.COMMAND_BANK_TAKE_SUCCESS_SINGLE,"Took %1$s from %2$s");
        this.translate(LCText.COMMAND_BANK_DELETE_PLAYER_RESET,"Reset %s's bank account!");
        this.translate(LCText.COMMAND_BANK_DELETE_PLAYER_SUCCESS,"Deleted %s's bank account!");
        this.translate(LCText.COMMAND_BANK_DELETE_PLAYER_DOESNT_EXIST,"%s does not have a bank account to delete!");
        this.translate(LCText.COMMAND_BANK_DELETE_PLAYER_INVALID_INPUT,"'%s' is not a valid player name or UUID string!");

        this.translate(LCText.COMMAND_CONFIG_RELOAD, "Reloading config files");
        this.translate(LCText.COMMAND_CONFIG_EDIT_SUCCESS, "%1$s set to %2$s");
        this.translate(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE, "Error parsing input: %s");
        this.translate(LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS,"%1$s successfully removed");
        this.translate(LCText.COMMAND_CONFIG_VIEW,"Value of %s is:");
        this.translate(LCText.COMMAND_CONFIG_FAIL_MISSING, "Missing config option %s");

        this.translate(LCText.COMMAND_ADMIN_TOGGLE_ADMIN,"LC Admin Mode: %s");
        this.translate(LCText.COMMAND_ADMIN_TOGGLE_ADMIN_ENABLED,"ENABLED");
        this.translate(LCText.COMMAND_ADMIN_TOGGLE_ADMIN_DISABLED,"DISABLED");
        this.translate(LCText.COMMAND_ADMIN_PREPARE_FOR_STRUCTURE_ERROR,"No trader block at the given position");
        this.translate(LCText.COMMAND_ADMIN_PREPARE_FOR_STRUCTURE_SUCCESS,"Saved current trader data to the block entity. Can now be copied to a structure or schematic safely");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_TITLE,"-----Trader Data List-----");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_NONE,"There are no traders on this server");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_TRADER_ID,"Trader ID: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_TRADER_ID_TOOLTIP,"Copy Trader ID");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_PERSISTENT_ID,"Persistent ID: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_TYPE,"Type: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_STATE,"State: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_DIMENSION,"Dimension: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_POSITION,"BlockPos: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_POSITION_TOOLTIP,"Teleport to Trader");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_LIST_NAME,"Custom Name: %s");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_SEARCH_NONE,"No traders were found with the given search filter");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_DELETE_SUCCESS,"Successfully deleted the trader '%s'");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_RECOVER_SUCCESS,"Successfully recreated the traders item");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_RECOVER_FAIL_NO_ITEM,"Trader is missing it's block data!");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_ADD_TO_WHITELIST_SUCCESS,"Added %1$s player(s) to %2$s's whitelist");
        this.translate(LCText.COMMAND_ADMIN_TRADERDATA_ADD_TO_WHITELIST_MISSING,"Trader does not have a whitelist trade rule. Cannot add players to the whitelist.");
        this.translate(LCText.COMMAND_ADMIN_REPLACE_WALLET_NOT_A_WALLET, "Item given was not a valid wallet item or air");
        this.translate(LCText.COMMAND_ADMIN_TAXES_OPEN_SERVER_TAX_ERROR, "An error occurred accessing the server tax entry");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_TITLE, "-----Tax Collector List-----");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_ID, "Collector ID: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_ID_TOOLTIP, "Copy ID");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_DIMENSION, "Dimension: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_POSITION, "BlockPos: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_POSITION_TOOLTIP, "Teleport to Tax Collector");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_INFINITE_RANGE, "Infinite Range");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_RADIUS, "Radius: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_HEIGHT, "Height: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_OFFSET, "V-Offset: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_FORCE_ACCEPTANCE, "Forces Tax Acceptance");
        this.translate(LCText.COMMAND_ADMIN_TAXES_LIST_NAME, "Custom Name: %s");
        this.translate(LCText.COMMAND_ADMIN_TAXES_DELETE_SUCCESS, "Successfully deleted the Tax Collector '%s'");
        this.translate(LCText.COMMAND_ADMIN_TAXES_DELETE_FAIL, "No Tax Collector with that ID exists");
        this.translate(LCText.COMMAND_ADMIN_TAXES_FORCE_DISABLE_SUCCESS,"Disabled %s active Tax Collectors");
        this.translate(LCText.COMMAND_ADMIN_TAXES_FORCE_DISABLE_FAIL,"No Tax Collectors are currently active");
        this.translate(LCText.COMMAND_ADMIN_EVENT_LIST_NONE,"No event flags are unlocked for this player");
        this.translate(LCText.COMMAND_ADMIN_EVENT_UNLOCK_SUCCESS,"Unlocked %s for this player");
        this.translate(LCText.COMMAND_ADMIN_EVENT_UNLOCK_FAIL,"%s is already unlocked for this player");
        this.translate(LCText.COMMAND_ADMIN_EVENT_LOCK_SUCCESS,"Locked %s for this player");
        this.translate(LCText.COMMAND_ADMIN_EVENT_LOCK_FAIL,"%s was not unlocked for this player");
        this.translate(LCText.COMMAND_ADMIN_PREPAID_CARD_SUCCESS,"Created a prepaid card containing %1$s for %2$s player(s)");
        this.translate(LCText.COMMAND_ADMIN_PREPAID_CARD_FAIL,"Non valid targets found");

        this.translate(LCText.COMMAND_LCADMIN_DATA_OWNER_PLAYER,"Owner: %1$s (%2$s)");
        this.translate(LCText.COMMAND_LCADMIN_DATA_OWNER_TEAM,"Team Owner: %1$s (%2$s)");
        this.translate(LCText.COMMAND_LCADMIN_DATA_OWNER_CUSTOM,"Owner: %s");

        this.translate(LCText.COMMAND_TRADE_SELF,"You cannot request a trade with yourself");
        this.translate(LCText.COMMAND_TRADE_HOST_NOTIFY,"Trade request has been sent to %s");
        this.translate(LCText.COMMAND_TRADE_GUEST_NOTIFY,"%1$s has requested to trade with you. Click [%2$s] to accept");
        this.translate(LCText.COMMAND_TRADE_GUEST_NOTIFY_PROMPT,"here");
        this.translate(LCText.COMMAND_TRADE_ACCEPT_FAIL_OFFLINE,"The requestor is no longer online");
        this.translate(LCText.COMMAND_TRADE_ACCEPT_FAIL_DISTANCE,"You must be less than %sm from the requestor to trade with them");
        this.translate(LCText.COMMAND_TRADE_ACCEPT_FAIL_DIMENSION,"You must be in the same dimension as the requestor to trade with them");
        this.translate(LCText.COMMAND_TRADE_ACCEPT_ERROR,"An unexpected error occurred when accepting the trade request");
        this.translate(LCText.COMMAND_TRADE_ACCEPT_NOT_FOUND,"An unexpected error occurred when accepting the trade request");

        this.translate(LCText.COMMAND_TICKETS_COLOR_NOT_HELD,"You must be holding a valid ticket to change its color");

        this.translate(LCText.COMMAND_CLAIM_FAIL_NO_DATA,"Could not access your claim data!");
        this.translate(LCText.COMMAND_CLAIM_FAIL_INVALID_PRICE,"Could not process the purchase as the configured price is invalid");
        this.translate(LCText.COMMAND_CLAIM_INVALID,"INVALID");
        this.translate(LCText.COMMAND_CLAIM_INFO_CLAIMS,"You have bought %1$s of %2$s available claim chunks");
        this.translate(LCText.COMMAND_CLAIM_INFO_FORCELOAD,"You have bought %1$s of %2$s available forceload chunks");
        this.translate(LCText.COMMAND_CLAIM_INFO_PRICE,"Costs %s per chunk");
        this.translate(LCText.COMMAND_CLAIM_INFO_DISABLED,"Purchase of claim and forceload chunks is disabled on this server");
        this.translate(LCText.COMMAND_CLAIM_BUY_CLAIM_DISABLED,"You may not purchase claim chunks on this server");
        this.translate(LCText.COMMAND_CLAIM_BUY_CLAIM_LIMIT_REACHED,"You have reached the maximum number of claim chunks that you are allowed to purchase (%s)");
        this.translate(LCText.COMMAND_CLAIM_BUY_CLAIM_SUCCESS,"Purchased %s more claim chunk(s)");
        this.translate(LCText.COMMAND_CLAIM_BUY_CLAIM_CANNOT_AFFORD,"Cannot afford to purchase any claim chunks");
        this.translate(LCText.COMMAND_CLAIM_BUY_FORCELOAD_DISABLED,"You may not purchase forceload chunks on this server");
        this.translate(LCText.COMMAND_CLAIM_BUY_FORCELOAD_LIMIT_REACHED,"You have reached the maximum number of forceload chunks that you are allowed to purchase (%s)");
        this.translate(LCText.COMMAND_CLAIM_BUY_FORCELOAD_SUCCESS,"Purchased %s more forceload chunk(s)");
        this.translate(LCText.COMMAND_CLAIM_BUY_FORCELOAD_CANNOT_AFFORD,"Cannot afford to purchase any forceload chunks");

        //Advancements
        this.translate(LCText.ADVANCEMENT_ROOT,"Lightman's Currency","Collecting wealth and trading with players");
        this.translate(LCText.ADVANCEMENT_MY_FIRST_PENNY,"My first penny!","Get your first coin");
        this.translate(LCText.ADVANCEMENT_TRADING_CORE,"Trading Core","Craft a Trading Core! These are needed to make trading machines to trade from");
        this.translate(LCText.ADVANCEMENT_TRADER,"It's Tradin' Time!","Craft any Trader so that you can trade with your friends!");
        this.translate(LCText.ADVANCEMENT_SPECIALTY_TRADER,"Specialty Trading","Craft a specialty item trader. Be careful, as they have special rules limiting how their trades are defined");
        this.translate(LCText.ADVANCEMENT_NETWORK_TRADER,"Welcome to Amazon","Craft a network trader so that you can sell your items to anyone, anywhere!");
        this.translate(LCText.ADVANCEMENT_TRADER_INTERFACE,"Patching in","Craft a trader interface so that you can automate trading or restocking with a network trader!");
        this.translate(LCText.ADVANCEMENT_TERMINAL, "Wireless Trading","Craft a Trading Terminal. This will let you access network traders from anywhere");
        this.translate(LCText.ADVANCEMENT_ATM,"Now you're thinking with Banking","Craft an ATM Machine. You can use this to manually exchange coins and access your bank accounts");
        this.translate(LCText.ADVANCEMENT_BANKER_TRADE,"Trading with the establishment","Buy a currency-related item from a banker");
        this.translate(LCText.ADVANCEMENT_COIN_MINT,"Minty Fresh!","Craft a Coin Mint so that you can mint your own coins");
        this.translate(LCText.ADVANCEMENT_WALLET_CRAFTING,"Coin Storage","Craft your first wallet. You can use these to store your coins!");
        this.translate(LCText.ADVANCEMENT_NETHERITE_WALLET,"Fireproof Wallet","Craft a netherite wallet, one of the largest and most resilient wallets around!");
        this.translate(LCText.ADVANCEMENT_NETHER_STAR_WALLET,"A Monster Sized Wallet","Craft a wallet from a nether star. Only for those with a true need for coin storage!");
        this.translate(LCText.ADVANCEMENT_ENCHANTED_WALLET,"Magical Wallet","Enchant your wallet with the Coin Magnet enchantment");
        this.translate(LCText.ADVANCEMENT_CASH_REGISTER,"All in one interaction!","Craft a cash register so that players can interact with all of your nearby traders at once!");
        this.translate(LCText.ADVANCEMENT_CASHIER_TRADE,"Hourly work, hourly pay","Buy something for coins from a cashier");
        this.translate(LCText.ADVANCEMENT_JAR_OF_SUS,"Sus Bank Check","WHY DID IT HAVE TO BE SOUP!?");
        this.translate(LCText.ADVANCEMENT_ANCIENT_COIN,"Archaeology pays off!","Find an ancient coin in a suspicious area");

        this.translate(LCText.ADVANCEMENT_EVENT_CHOCOLATE,"Chocolate Wonders","Play during a special holiday! Unlocks the ability to use chocolate coins as money");
        this.translate(LCText.ADVANCEMENT_EVENT_CHRISTMAS,"A Christmas Shopping Spree!","Played during a very jolly time!");
        this.translate(LCText.ADVANCEMENT_EVENT_VALENTINES,"Love is in your wallet!","Played during the celebration of love");

        //Resource Pack Names
        this.translate(LCText.RESOURCE_PACK_RUPEES,"LC Rupees (WIP)");
        this.translate(LCText.RESOURCE_PACK_CLOSER_ITEMS,"LC Fancy Item Placement");
        this.translate(LCText.RESOURCE_PACK_LEGACY_COINS,"LC Legacy Coins");

        ///OTHER MODS

        //JEI Info
        this.translate(LCText.JEI_INFO_TICKET_STUB, "A broken ticket returned to the customer when a ticket is used on a Paygate");
        //Curios
        this.translate(LCText.CURIOS_SLOT_WALLET,"Wallet");

        //REI Groups
        this.translate(LCText.REI_GROUP_SHELF,"Shelves");
        this.translate(LCText.REI_GROUP_SHELF_2x2,"Double Shelves");
        this.translateWooden(LCText.REI_GROUP_CARD_DISPLAY,"%s Card Displays");
        this.translate(LCText.REI_GROUP_VENDING_MACHINE,"Vending Machines");
        this.translate(LCText.REI_GROUP_LARGE_VENDING_MACHINE,"Large Vending Machines");
        this.translate(LCText.REI_GROUP_FREEZER,"Freezers");
        this.translate(LCText.REI_GROUP_BOOKSHELF_TRADER,"Bookshelf Traders");
        this.translate(LCText.REI_GROUP_AUCTION_STAND,"Auction Stands");
        this.translate(LCText.REI_GROUP_JAR_OF_SUS,"Jars of Sus");
        this.translate(LCText.REI_GROUP_ATM_CARD,"ATM Cards");
        this.translate(LCText.REI_GROUP_ANCIENT_COINS,"Ancient Coins");

    }

}