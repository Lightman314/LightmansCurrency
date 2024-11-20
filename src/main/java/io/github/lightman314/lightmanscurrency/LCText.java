package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.event_coins.ChocolateEventCoins;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.taxes.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.*;
import io.github.lightman314.lightmanscurrency.common.text.*;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.*;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.CustomProfessions;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;

public class LCText {

    private static final String MODID = LightmansCurrency.MODID;

    public static final TextEntry CREATIVE_GROUP_COINS = TextEntry.creativeTab(MODID,"coins");
    public static final TextEntry CREATIVE_GROUP_MACHINES = TextEntry.creativeTab(MODID,"machines");
    public static final TextEntry CREATIVE_GROUP_TRADING = TextEntry.creativeTab(MODID,"trading");
    public static final TextEntry CREATIVE_GROUP_UPGRADES = TextEntry.creativeTab(MODID,"upgrades");
    public static final TextEntry CREATIVE_GROUP_EXTRA = TextEntry.creativeTab(MODID,"extra");

    //Items
    public static final TextEntry ITEM_COIN_COPPER = TextEntry.item(ModItems.COIN_COPPER);
    public static final TextEntry ITEM_COIN_COPPER_PLURAL = TextEntry.plural(ITEM_COIN_COPPER);
    public static final TextEntry ITEM_COIN_COPPER_INITIAL = TextEntry.initial(ITEM_COIN_COPPER);
    public static final TextEntry ITEM_COIN_IRON = TextEntry.item(ModItems.COIN_IRON);
    public static final TextEntry ITEM_COIN_IRON_PLURAL = TextEntry.plural(ITEM_COIN_IRON);
    public static final TextEntry ITEM_COIN_IRON_INITIAL = TextEntry.initial(ITEM_COIN_IRON);
    public static final TextEntry ITEM_COIN_GOLD = TextEntry.item(ModItems.COIN_GOLD);
    public static final TextEntry ITEM_COIN_GOLD_PLURAL = TextEntry.plural(ITEM_COIN_GOLD);
    public static final TextEntry ITEM_COIN_GOLD_INITIAL = TextEntry.initial(ITEM_COIN_GOLD);
    public static final TextEntry ITEM_COIN_EMERALD = TextEntry.item(ModItems.COIN_EMERALD);
    public static final TextEntry ITEM_COIN_EMERALD_PLURAL = TextEntry.plural(ITEM_COIN_EMERALD);
    public static final TextEntry ITEM_COIN_EMERALD_INITIAL = TextEntry.initial(ITEM_COIN_EMERALD);
    public static final TextEntry ITEM_COIN_DIAMOND = TextEntry.item(ModItems.COIN_DIAMOND);
    public static final TextEntry ITEM_COIN_DIAMOND_PLURAL = TextEntry.plural(ITEM_COIN_DIAMOND);
    public static final TextEntry ITEM_COIN_DIAMOND_INITIAL = TextEntry.initial(ITEM_COIN_DIAMOND);
    public static final TextEntry ITEM_COIN_NETHERITE = TextEntry.item(ModItems.COIN_NETHERITE);
    public static final TextEntry ITEM_COIN_NETHERITE_PLURAL = TextEntry.plural(ITEM_COIN_NETHERITE);
    public static final TextEntry ITEM_COIN_NETHERITE_INITIAL = TextEntry.initial(ITEM_COIN_NETHERITE);

    public static final TextEntry ITEM_COIN_CHOCOLATE_COPPER = TextEntry.item(ModItems.COIN_CHOCOLATE_COPPER);
    public static final TextEntry ITEM_COIN_CHOCOLATE_COPPER_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_COPPER);
    public static final TextEntry ITEM_COIN_CHOCOLATE_COPPER_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_COPPER);
    public static final TextEntry ITEM_COIN_CHOCOLATE_IRON = TextEntry.item(ModItems.COIN_CHOCOLATE_IRON);
    public static final TextEntry ITEM_COIN_CHOCOLATE_IRON_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_IRON);
    public static final TextEntry ITEM_COIN_CHOCOLATE_IRON_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_IRON);
    public static final TextEntry ITEM_COIN_CHOCOLATE_GOLD = TextEntry.item(ModItems.COIN_CHOCOLATE_GOLD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_GOLD_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_GOLD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_GOLD_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_GOLD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_EMERALD = TextEntry.item(ModItems.COIN_CHOCOLATE_EMERALD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_EMERALD_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_EMERALD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_EMERALD_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_EMERALD);
    public static final TextEntry ITEM_COIN_CHOCOLATE_DIAMOND = TextEntry.item(ModItems.COIN_CHOCOLATE_DIAMOND);
    public static final TextEntry ITEM_COIN_CHOCOLATE_DIAMOND_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_DIAMOND);
    public static final TextEntry ITEM_COIN_CHOCOLATE_DIAMOND_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_DIAMOND);
    public static final TextEntry ITEM_COIN_CHOCOLATE_NETHERITE = TextEntry.item(ModItems.COIN_CHOCOLATE_NETHERITE);
    public static final TextEntry ITEM_COIN_CHOCOLATE_NETHERITE_PLURAL = TextEntry.plural(ITEM_COIN_CHOCOLATE_NETHERITE);
    public static final TextEntry ITEM_COIN_CHOCOLATE_NETHERITE_INITIAL = TextEntry.initial(ITEM_COIN_CHOCOLATE_NETHERITE);

    public static final TextEntry ITEM_WALLET_LEATHER = TextEntry.item(ModItems.WALLET_LEATHER);
    public static final TextEntry ITEM_WALLET_COPPER = TextEntry.item(ModItems.WALLET_COPPER);
    public static final TextEntry ITEM_WALLET_IRON = TextEntry.item(ModItems.WALLET_IRON);
    public static final TextEntry ITEM_WALLET_GOLD = TextEntry.item(ModItems.WALLET_GOLD);
    public static final TextEntry ITEM_WALLET_EMERALD = TextEntry.item(ModItems.WALLET_EMERALD);
    public static final TextEntry ITEM_WALLET_DIAMOND = TextEntry.item(ModItems.WALLET_DIAMOND);
    public static final TextEntry ITEM_WALLET_NETHERITE = TextEntry.item(ModItems.WALLET_NETHERITE);
    public static final TextEntry ITEM_WALLET_NETHER_STAR = TextEntry.item(ModItems.WALLET_NETHER_STAR);
    public static final TextEntry ITEM_WALLET_ENDER_DRAGON = TextEntry.item(ModItems.WALLET_ENDER_DRAGON);

    public static final TextEntry ITEM_TRADING_CORE = TextEntry.item(ModItems.TRADING_CORE);

    public static final TextEntry ITEM_ATM_CARD = TextEntry.item(ModItems.ATM_CARD);
    public static final TextEntry ITEM_PREPAID_CARD = TextEntry.item(ModItems.PREPAID_CARD);

    public static final TextEntry ITEM_TICKET = TextEntry.item(ModItems.TICKET);
    public static final TextEntry ITEM_PASS = TextEntry.item(ModItems.TICKET_PASS);
    public static final TextEntry ITEM_MASTER_TICKET = TextEntry.item(ModItems.TICKET_MASTER);
    public static final TextEntry ITEM_TICKET_STUB = TextEntry.item(ModItems.TICKET_STUB);

    public static final TextEntry ITEM_GOLDEN_TICKET = TextEntry.item(ModItems.GOLDEN_TICKET);
    public static final TextEntry ITEM_GOLDEN_PASS = TextEntry.item(ModItems.GOLDEN_TICKET_PASS);
    public static final TextEntry ITEM_GOLDEN_MASTER_TICKET = TextEntry.item(ModItems.GOLDEN_TICKET_MASTER);
    public static final TextEntry ITEM_GOLDEN_TICKET_STUB = TextEntry.item(ModItems.GOLDEN_TICKET_STUB);

    public static final TextEntry ITEM_UPGRADE_ITEM_CAPACITY_1 = TextEntry.item(ModItems.ITEM_CAPACITY_UPGRADE_1);
    public static final TextEntry ITEM_UPGRADE_ITEM_CAPACITY_2 = TextEntry.item(ModItems.ITEM_CAPACITY_UPGRADE_2);
    public static final TextEntry ITEM_UPGRADE_ITEM_CAPACITY_3 = TextEntry.item(ModItems.ITEM_CAPACITY_UPGRADE_3);
    public static final TextEntry ITEM_UPGRADE_ITEM_CAPACITY_4 = TextEntry.item(ModItems.ITEM_CAPACITY_UPGRADE_4);
    public static final TextEntry ITEM_UPGRADE_SPEED_1 = TextEntry.item(ModItems.SPEED_UPGRADE_1);
    public static final TextEntry ITEM_UPGRADE_SPEED_2 = TextEntry.item(ModItems.SPEED_UPGRADE_2);
    public static final TextEntry ITEM_UPGRADE_SPEED_3 = TextEntry.item(ModItems.SPEED_UPGRADE_3);
    public static final TextEntry ITEM_UPGRADE_SPEED_4 = TextEntry.item(ModItems.SPEED_UPGRADE_4);
    public static final TextEntry ITEM_UPGRADE_SPEED_5 = TextEntry.item(ModItems.SPEED_UPGRADE_5);
    public static final TextEntry ITEM_UPGRADE_OFFER_1 = TextEntry.item(ModItems.OFFER_UPGRADE_1);
    public static final TextEntry ITEM_UPGRADE_OFFER_2 = TextEntry.item(ModItems.OFFER_UPGRADE_2);
    public static final TextEntry ITEM_UPGRADE_OFFER_3 = TextEntry.item(ModItems.OFFER_UPGRADE_3);
    public static final TextEntry ITEM_UPGRADE_OFFER_4 = TextEntry.item(ModItems.OFFER_UPGRADE_4);
    public static final TextEntry ITEM_UPGRADE_OFFER_5 = TextEntry.item(ModItems.OFFER_UPGRADE_5);
    public static final TextEntry ITEM_UPGRADE_OFFER_6 = TextEntry.item(ModItems.OFFER_UPGRADE_6);
    public static final TextEntry ITEM_UPGRADE_NETWORK = TextEntry.item(ModItems.NETWORK_UPGRADE);
    public static final TextEntry ITEM_UPGRADE_VOID = TextEntry.item(ModItems.VOID_UPGRADE);
    public static final TextEntry ITEM_UPGRADE_HOPPER = TextEntry.item(ModItems.HOPPER_UPGRADE);
    public static final TextEntry ITEM_UPGRADE_COIN_EXCHANGE = TextEntry.item(ModItems.COIN_CHEST_EXCHANGE_UPGRADE);
    public static final TextEntry ITEM_UPGRADE_MAGNET_1 = TextEntry.item(ModItems.COIN_CHEST_MAGNET_UPGRADE_1);
    public static final TextEntry ITEM_UPGRADE_MAGNET_2 = TextEntry.item(ModItems.COIN_CHEST_MAGNET_UPGRADE_2);
    public static final TextEntry ITEM_UPGRADE_MAGNET_3 = TextEntry.item(ModItems.COIN_CHEST_MAGNET_UPGRADE_3);
    public static final TextEntry ITEM_UPGRADE_MAGNET_4 = TextEntry.item(ModItems.COIN_CHEST_MAGNET_UPGRADE_4);
    public static final TextEntry ITEM_UPGRADE_BANK = TextEntry.item(ModItems.COIN_CHEST_BANK_UPGRADE);
    public static final TextEntry ITEM_UPGRADE_SECURITY = TextEntry.item(ModItems.COIN_CHEST_SECURITY_UPGRADE);

    //1.20 exclusive
    public static final TextEntry ITEM_UPGRADE_TEMPLATE = TextEntry.item(ModItems.UPGRADE_SMITHING_TEMPLATE);

    //Blocks
    public static final TextEntry BLOCK_COINPILE_COPPER = TextEntry.block(ModBlocks.COINPILE_COPPER);
    public static final TextEntry BLOCK_COINPILE_COPPER_PLURAL = TextEntry.plural(BLOCK_COINPILE_COPPER);
    public static final TextEntry BLOCK_COINPILE_IRON = TextEntry.block(ModBlocks.COINPILE_IRON);
    public static final TextEntry BLOCK_COINPILE_IRON_PLURAL = TextEntry.plural(BLOCK_COINPILE_IRON);
    public static final TextEntry BLOCK_COINPILE_GOLD = TextEntry.block(ModBlocks.COINPILE_GOLD);
    public static final TextEntry BLOCK_COINPILE_GOLD_PLURAL = TextEntry.plural(BLOCK_COINPILE_GOLD);
    public static final TextEntry BLOCK_COINPILE_EMERALD = TextEntry.block(ModBlocks.COINPILE_EMERALD);
    public static final TextEntry BLOCK_COINPILE_EMERALD_PLURAL = TextEntry.plural(BLOCK_COINPILE_EMERALD);
    public static final TextEntry BLOCK_COINPILE_DIAMOND = TextEntry.block(ModBlocks.COINPILE_DIAMOND);
    public static final TextEntry BLOCK_COINPILE_DIAMOND_PLURAL = TextEntry.plural(BLOCK_COINPILE_DIAMOND);
    public static final TextEntry BLOCK_COINPILE_NETHERITE = TextEntry.block(ModBlocks.COINPILE_NETHERITE);
    public static final TextEntry BLOCK_COINPILE_NETHERITE_PLURAL = TextEntry.plural(BLOCK_COINPILE_NETHERITE);
    public static final TextEntry BLOCK_COINBLOCK_COPPER = TextEntry.block(ModBlocks.COINBLOCK_COPPER);
    public static final TextEntry BLOCK_COINBLOCK_COPPER_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_COPPER);
    public static final TextEntry BLOCK_COINBLOCK_IRON = TextEntry.block(ModBlocks.COINBLOCK_IRON);
    public static final TextEntry BLOCK_COINBLOCK_IRON_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_IRON);
    public static final TextEntry BLOCK_COINBLOCK_GOLD = TextEntry.block(ModBlocks.COINBLOCK_GOLD);
    public static final TextEntry BLOCK_COINBLOCK_GOLD_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_GOLD);
    public static final TextEntry BLOCK_COINBLOCK_EMERALD = TextEntry.block(ModBlocks.COINBLOCK_EMERALD);
    public static final TextEntry BLOCK_COINBLOCK_EMERALD_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_EMERALD);
    public static final TextEntry BLOCK_COINBLOCK_DIAMOND = TextEntry.block(ModBlocks.COINBLOCK_DIAMOND);
    public static final TextEntry BLOCK_COINBLOCK_DIAMOND_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_DIAMOND);
    public static final TextEntry BLOCK_COINBLOCK_NETHERITE = TextEntry.block(ModBlocks.COINBLOCK_NETHERITE);
    public static final TextEntry BLOCK_COINBLOCK_NETHERITE_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_NETHERITE);

    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_COPPER = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_COPPER);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_COPPER_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_COPPER);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_IRON = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_IRON);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_IRON_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_IRON);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_GOLD = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_GOLD);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_GOLD_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_GOLD);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_EMERALD = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_EMERALD);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_EMERALD_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_EMERALD);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_DIAMOND = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_DIAMOND);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_DIAMOND_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_DIAMOND);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_NETHERITE = TextEntry.block(ModBlocks.COINPILE_CHOCOLATE_NETHERITE);
    public static final TextEntry BLOCK_COINPILE_CHOCOLATE_NETHERITE_PLURAL = TextEntry.plural(BLOCK_COINPILE_CHOCOLATE_NETHERITE);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_COPPER = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_COPPER);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_COPPER_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_COPPER);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_IRON = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_IRON);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_IRON_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_IRON);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_GOLD = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_GOLD);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_GOLD_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_GOLD);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_EMERALD = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_EMERALD);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_EMERALD_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_EMERALD);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_DIAMOND = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_DIAMOND_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_DIAMOND);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_NETHERITE = TextEntry.block(ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE);
    public static final TextEntry BLOCK_COINBLOCK_CHOCOLATE_NETHERITE_PLURAL = TextEntry.plural(BLOCK_COINBLOCK_CHOCOLATE_NETHERITE);

    public static final TextEntry BLOCK_CASH_REGISTER = TextEntry.block(ModBlocks.CASH_REGISTER);
    public static final TextEntry BLOCK_COIN_MINT = TextEntry.block(ModBlocks.COIN_MINT);
    public static final TextEntry BLOCK_TICKET_STATION = TextEntry.block(ModBlocks.TICKET_STATION);
    public static final TextEntry BLOCK_MONEY_CHEST = TextEntry.block(ModBlocks.COIN_CHEST);

    public static final TextEntry BLOCK_DISPLAY_CASE = TextEntry.block(ModBlocks.DISPLAY_CASE);
    public static final TextEntryBundle<WoodType> BLOCK_SHELF = TextEntryBundle.of(ModBlocks.SHELF);
    public static final TextEntryBundle<WoodType> BLOCK_SHELF_2x2 = TextEntryBundle.of(ModBlocks.SHELF_2x2);
    public static final TextEntryBundle<WoodType> BLOCK_CARD_DISPLAY = TextEntryBundle.of(ModBlocks.CARD_DISPLAY);
    public static final TextEntryBundle<Color> BLOCK_VENDING_MACHINE = TextEntryBundle.of(ModBlocks.VENDING_MACHINE);
    public static final TextEntryBundle<Color> BLOCK_FREEZER = TextEntryBundle.of(ModBlocks.FREEZER);
    public static final TextEntryBundle<Color> BLOCK_LARGE_VENDING_MACHINE = TextEntryBundle.of(ModBlocks.VENDING_MACHINE_LARGE);

    public static final TextEntry BLOCK_PAYGATE = TextEntry.block(ModBlocks.PAYGATE);
    public static final TextEntry BLOCK_TICKET_KIOSK = TextEntry.block(ModBlocks.TICKET_KIOSK);
    public static final TextEntry BLOCK_SLOT_MACHINE = TextEntry.block(ModBlocks.SLOT_MACHINE);
    public static final TextEntry BLOCK_ARMOR_DISPLAY = TextEntry.block(ModBlocks.ARMOR_DISPLAY);
    public static final TextEntryBundle<WoodType> BLOCK_BOOKSHELF_TRADER = TextEntryBundle.of(ModBlocks.BOOKSHELF_TRADER);

    public static final TextEntry BLOCK_ITEM_NETWORK_TRADER_1 = TextEntry.block(ModBlocks.ITEM_NETWORK_TRADER_1);
    public static final TextEntry BLOCK_ITEM_NETWORK_TRADER_2 = TextEntry.block(ModBlocks.ITEM_NETWORK_TRADER_2);
    public static final TextEntry BLOCK_ITEM_NETWORK_TRADER_3 = TextEntry.block(ModBlocks.ITEM_NETWORK_TRADER_3);
    public static final TextEntry BLOCK_ITEM_NETWORK_TRADER_4 = TextEntry.block(ModBlocks.ITEM_NETWORK_TRADER_4);

    public static final TextEntry BLOCK_ITEM_TRADER_INTERFACE = TextEntry.block(ModBlocks.ITEM_TRADER_INTERFACE);

    public static final TextEntry BLOCK_TAX_COLLECTOR = TextEntry.block(ModBlocks.TAX_COLLECTOR);

    public static final TextEntryBundle<WoodType> BLOCK_AUCTION_STAND = TextEntryBundle.of(ModBlocks.AUCTION_STAND);

    public static final TextEntry BLOCK_JAR_PIGGY_BANK = TextEntry.block(ModBlocks.PIGGY_BANK);
    public static final TextEntry BLOCK_JAR_BLUE = TextEntry.block(ModBlocks.COINJAR_BLUE);
    public static final TextEntry BLOCK_JAR_SUS = TextEntry.block(ModBlocks.SUS_JAR);

    //Items & Blocks
    public static final CombinedTextEntry ITEM_BLOCK_TERMINAL = CombinedTextEntry.items(ModItems.PORTABLE_TERMINAL,ModItems.PORTABLE_GEM_TERMINAL, ModBlocks.TERMINAL, ModBlocks.GEM_TERMINAL);
    public static final CombinedTextEntry ITEM_BLOCK_ATM = CombinedTextEntry.items(ModItems.PORTABLE_ATM, ModBlocks.ATM);

    //Enchantments
    public static final TextEntry ENCHANTMENT_MONEY_MENDING = TextEntry.enchantment(ModEnchantments.MONEY_MENDING);
    public static final TextEntry ENCHANTMENT_MONEY_MENDING_DESCRIPTION = TextEntry.description(ENCHANTMENT_MONEY_MENDING);
    public static final TextEntry ENCHANTMENT_COIN_MAGNET = TextEntry.enchantment(ModEnchantments.COIN_MAGNET);
    public static final TextEntry ENCHANTMENT_COIN_MAGNET_DESCRIPTION = TextEntry.description(ENCHANTMENT_COIN_MAGNET);

    //Gamerules
    public static final TextEntry GAMERULE_KEEP_WALLET = TextEntry.gamerule("keepWallet");
    public static final TextEntry GAMERULE_COIN_DROP_PERCENT= TextEntry.gamerule("coinDropPercent");

    //Villager Professions
    public static final TextEntry PROFESSION_BANKER = TextEntry.profession(CustomProfessions.BANKER);
    public static final TextEntry PROFESSION_CASHIER = TextEntry.profession(CustomProfessions.CASHIER);

    //Key Binds
    public static final TextEntry KEY_WALLET = TextEntry.keyBind(MODID,"open_wallet");
    public static final TextEntry KEY_PORTABLE_TERMINAL = TextEntry.keyBind(MODID,"portable_terminal");
    public static final TextEntry KEY_PORTABLE_ATM = TextEntry.keyBind(MODID,"portable_atm");

    //Sound Subtitles
    public static final TextEntry SOUND_COINS_CLINKING = TextEntry.sound(MODID,"coins_clinking");

    //Item Tooltips
    public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.title");
    public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO = TextEntry.tooltip(MODID,"smithing_template.applies_to");
    public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS = TextEntry.tooltip(MODID,"smithing_template.ingredients");
    public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.base_slot_description");
    public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.additions_slot_description");

    public static final TextEntry TOOLTIP_HEALING = TextEntry.tooltip(MODID,"healing");

    public static final TextEntry TOOLTIP_BETA = TextEntry.tooltip(MODID,"beta");
    public static final TextEntry TOOLTIP_DISABLED = TextEntry.tooltip(MODID,"disabled");
    public static final TextEntry TOOLTIP_INFO_BLURB = TextEntry.tooltip(MODID,"info_blurb");

    public static final TextEntry TOOLTIP_ATM_CARD_NOT_LINKED = TextEntry.tooltip(MODID,"atm_card.not_linked");
    public static final TextEntry TOOLTIP_ATM_CARD_ACCOUNT = TextEntry.tooltip(MODID,"atm_card.account");
    public static final TextEntry TOOLTIP_ATM_CARD_LINK_INVALID = TextEntry.tooltip(MODID,"atm_card.link_invalid");
    public static final TextEntry TOOLTIP_ATM_CARD_ACCOUNT_LOCKED = TextEntry.tooltip(MODID,"atm_card.account_locked");
    public static final TextEntry TOOLTIP_PREPAID_CARD_DELETE_WARNING = TextEntry.tooltip(MODID,"prepaid_card.delete_warning");

    public static final TextEntry TOOLTIP_TICKET_ID = TextEntry.tooltip(MODID,"ticket.id");
    public static final TextEntry TOOLTIP_PASS = TextEntry.tooltip(MODID,"pass");
    public static final MultiLineTextEntry TOOLTIP_ITEM_TRADER = MultiLineTextEntry.tooltip(MODID,"trader.item");
    public static final MultiLineTextEntry TOOLTIP_ITEM_TRADER_ARMOR = MultiLineTextEntry.tooltip(MODID,"trader.item.armor");
    public static final MultiLineTextEntry TOOLTIP_ITEM_TRADER_TICKET = MultiLineTextEntry.tooltip(MODID,"trader.item.ticket");
    public static final MultiLineTextEntry TOOLTIP_ITEM_TRADER_BOOK = MultiLineTextEntry.tooltip(MODID,"trader.item.book");
    public static final MultiLineTextEntry TOOLTIP_ITEM_TRADER_NETWORK = MultiLineTextEntry.tooltip(MODID,"trader.network.item");
    public static final MultiLineTextEntry TOOLTIP_SLOT_MACHINE = MultiLineTextEntry.tooltip(MODID,"trader.slot_machine");
    public static final MultiLineTextEntry TOOLTIP_PAYGATE = MultiLineTextEntry.tooltip(MODID,"paygate");
    public static final MultiLineTextEntry TOOLTIP_TERMINAL = MultiLineTextEntry.tooltip(MODID,"terminal");
    public static final TextEntry TOOLTIP_TERMINAL_KEY_BIND = TextEntry.tooltip(MODID,"terminal.key_bind");
    public static final MultiLineTextEntry TOOLTIP_INTERFACE_ITEM = MultiLineTextEntry.tooltip(MODID,"interface.item");
    public static final MultiLineTextEntry TOOLTIP_TAX_COLLECTOR = MultiLineTextEntry.tooltip(MODID,"tax_collector");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_ADMIN_ONLY = TextEntry.tooltip(MODID,"tax_collector.admin_only");
    public static final MultiLineTextEntry TOOLTIP_ATM = MultiLineTextEntry.tooltip(MODID,"atm");
    public static final TextEntry TOOLTIP_ATM_KEY_BIND = TextEntry.tooltip(MODID,"atm.key_bind");
    public static final TextEntry TOOLTIP_COIN_MINT_MINTABLE = TextEntry.tooltip(MODID,"coinmint.mintable");
    public static final TextEntry TOOLTIP_COIN_MINT_MELTABLE = TextEntry.tooltip(MODID,"coinmint.meltable");
    public static final TextEntry TOOLTIP_COIN_MINT_DISABLED_TOP = TextEntry.tooltip(MODID,"coinmint.disabled.1");
    public static final TextEntry TOOLTIP_COIN_MINT_DISABLED_BOTTOM = TextEntry.tooltip(MODID,"coinmint.disabled.2");
    public static final MultiLineTextEntry TOOLTIP_TICKET_STATION = MultiLineTextEntry.tooltip(MODID,"ticketmachine");
    public static final MultiLineTextEntry TOOLTIP_CASH_REGISTER = MultiLineTextEntry.tooltip(MODID,"cash_register");
    public static final MultiLineTextEntry TOOLTIP_COIN_JAR = MultiLineTextEntry.tooltip(MODID,"coin_jar");
    public static final TextEntry TOOLTIP_COIN_JAR_COLORED = TextEntry.tooltip(MODID,"coin_jar.colored");
    public static final TextEntry TOOLTIP_COLORED_ITEM = TextEntry.tooltip(MODID,"colored_item");

    public static final TextEntry TOOLTIP_TRADER_ITEM_WITH_DATA = TextEntry.tooltip(MODID,"trader.item.contains_data");
    public static final TextEntry TOOLTIP_TRADER_ITEM_WITH_DATA_TRADER_ID = TextEntry.tooltip(MODID,"trader.item.contains_data.trader_id");
    public static final TextEntry TOOLTIP_ANARCHY_WARNING = TextEntry.tooltip(MODID,"ownable.anarchy_warning");
    public static final TextEntry MESSAGE_DIMENSION_QUARANTINED_BANK = TextEntry.message(MODID,"dimension_quarantined.bank");
    public static final TextEntry MESSAGE_DIMENSION_QUARANTINED_TERMINAL = TextEntry.message(MODID,"dimension_quarantined.terminal");
    public static final TextEntry TOOLTIP_DIMENSION_QUARANTINED_NETWORK_TRADER = TextEntry.message(MODID,"dimension_quarantined.network_traders");

    //Coin Tooltips
    public static final TextEntry TOOLTIP_COIN_WORTH_DOWN = TextEntry.tooltip(MODID,"coinworth.down");
    public static final TextEntry TOOLTIP_COIN_WORTH_UP = TextEntry.tooltip(MODID,"coinworth.up");
    public static final TextEntry TOOLTIP_COIN_WORTH_VALUE = TextEntry.tooltip(MODID,"coinworth.value");
    public static final TextEntry TOOLTIP_COIN_WORTH_VALUE_STACK = TextEntry.tooltip(MODID,"coinworth.value.stack");
    public static final TextEntry TOOLTIP_COIN_ADVANCED_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.chain");
    public static final TextEntry TOOLTIP_COIN_ADVANCED_VALUE = TextEntry.tooltip(MODID,"coin.advanced.value");
    public static final TextEntry TOOLTIP_COIN_ADVANCED_CORE_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.core_chain");
    public static final TextEntry TOOLTIP_COIN_ADVANCED_SIDE_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.side_chain");

    //Wallet
    public static final TextEntry MESSAGE_WALLET_NONE_EQUIPPED = TextEntry.message(MODID,"wallet.none_equipped");
    public static final TextEntry TOOLTIP_WALLET_KEY_BIND = TextEntry.tooltip(MODID,"wallet.key_bind");
    public static final TextEntry TOOLTIP_WALLET_STORED_MONEY = TextEntry.tooltip(MODID,"wallet.storedmoney");
    public static final TextEntry TOOLTIP_WALLET_CAPACITY = TextEntry.tooltip(MODID,"wallet.capacity");
    public static final TextEntry TOOLTIP_WALLET_UPGRADEABLE_CAPACITY = TextEntry.tooltip(MODID,"wallet.upgradeable.capacity");
    public static final TextEntry TOOLTIP_WALLET_PICKUP = TextEntry.tooltip(MODID,"wallet.pickup");
    public static final TextEntry TOOLTIP_WALLET_PICKUP_MAGNET = TextEntry.tooltip(MODID,"wallet.pickup.magnet");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_MANUAL = TextEntry.tooltip(MODID,"wallet.exchange.manual");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO = TextEntry.tooltip(MODID,"wallet.exchange.auto");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO_ON = TextEntry.tooltip(MODID,"wallet.exchange.auto.on");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO_OFF = TextEntry.tooltip(MODID,"wallet.exchange.auto.off");
    public static final TextEntry TOOLTIP_WALLET_BANK_ACCOUNT = TextEntry.tooltip(MODID,"wallet.bank_account");

    //Cash Register
    public static final TextEntry MESSAGE_CASH_REGISTER_NOT_LINKED = TextEntry.message(MODID,"cash_register.not_linked");
    public static final TextEntry TOOLTIP_CASH_REGISTER_INFO = TextEntry.tooltip(MODID,"cash_register.info");
    public static final TextEntry TOOLTIP_CASH_REGISTER_INSTRUCTIONS = TextEntry.tooltip(MODID,"cash_register.instructions");
    public static final TextEntry TOOLTIP_CASH_REGISTER_HOLD_SHIFT = TextEntry.tooltip(MODID,"cash_register.holdshift");
    public static final TextEntry TOOLTIP_CASH_REGISTER_DETAILS = TextEntry.tooltip(MODID,"cash_register.details");

    //Coin Jar
    public static final TextEntry TOOLTIP_COIN_JAR_HOLD_SHIFT = TextEntry.tooltip(MODID,"coinjar.holdshift");
    public static final TextEntry TOOLTIP_COIN_JAR_CONTENTS_SINGLE = TextEntry.tooltip(MODID,"coinjar.storedcoins.single");
    public static final TextEntry TOOLTIP_COIN_JAR_CONTENTS_MULTIPLE = TextEntry.tooltip(MODID,"coinjar.storedcoins.multiple");

    //Upgrades
    public static final TextEntry TOOLTIP_UPGRADE_TARGETS = TextEntry.tooltip(MODID,"upgrade.targets");
    public static final TextEntry TOOLTIP_UPGRADE_UNIQUE = TextEntry.tooltip(MODID,"upgrade.unique");
    public static final TextEntry TOOLTIP_UPGRADE_TRADE_OFFER = TextEntry.tooltip(MODID,"upgrade.trade_offer");
    public static final TextEntry TOOLTIP_UPGRADE_ITEM_CAPACITY = TextEntry.tooltip(MODID,"upgrade.item_capacity");
    public static final TextEntry TOOLTIP_UPGRADE_SPEED = TextEntry.tooltip(MODID,"upgrade.speed");
    public static final TextEntry TOOLTIP_UPGRADE_NETWORK = TextEntry.tooltip(MODID,"upgrade.network");
    public static final MultiLineTextEntry TOOLTIP_UPGRADE_VOID = MultiLineTextEntry.tooltip(MODID,"upgrade.void");
    public static final TextEntry TOOLTIP_UPGRADE_HOPPER = TextEntry.tooltip(MODID,"upgrade.hopper");

    public static final TextEntry TOOLTIP_UPGRADE_COIN_EXCHANGE = TextEntry.tooltip(MODID,"upgrade.coin_exchange");
    public static final TextEntry TOOLTIP_UPGRADE_MAGNET = TextEntry.tooltip(MODID,"upgrade.magnet");
    public static final TextEntry TOOLTIP_UPGRADE_BANK = TextEntry.tooltip(MODID,"upgrade.bank");
    public static final MultiLineTextEntry TOOLTIP_UPGRADE_SECURITY = MultiLineTextEntry.tooltip(MODID,"upgrade.security");

    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER = TextEntry.tooltip(MODID,"upgrade.target.traders");
    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_NOT_NETWORK = TextEntry.tooltip(MODID,"upgrade.target.traders.not_network");
    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_ITEM = TextEntry.tooltip(MODID,"upgrade.target.traders.item");
    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE = TextEntry.tooltip(MODID,"upgrade.target.trader_interface");

    //Money Source Tooltips
    public static final TextEntry TOOLTIP_MONEY_SOURCE_BANK = TextEntry.tooltip(MODID,"money_source.bank");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_SLOTS = TextEntry.tooltip(MODID,"money_source.slots");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_PLAYER = TextEntry.tooltip(MODID,"money_source.player");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_STORAGE = TextEntry.tooltip(MODID,"money_source.storage");

    //Ownership Tooltips
    public static final TextEntry TOOLTIP_OWNER_PLAYER = TextEntry.tooltip(MODID,"ownership.player");
    public static final MultiLineTextEntry TOOLTIP_OWNER_TEAM = MultiLineTextEntry.tooltip(MODID,"ownership.team");
    public static final MultiLineTextEntry TOOLTIP_OWNER_TEAM_FTB = MultiLineTextEntry.tooltip(MODID,"ownership.team.ftb");

    //Enchantment Tooltips
    public static final TextEntry TOOLTIP_MONEY_MENDING_COST = TextEntry.tooltip(MODID,"money_mending.price");

    //Trader Tooltips
    public static final TextEntry TOOLTIP_OUT_OF_STOCK = TextEntry.tooltip(MODID,"out_of_stock");
    public static final TextEntry TOOLTIP_OUT_OF_SPACE = TextEntry.tooltip(MODID,"out_of_space");
    public static final TextEntry TOOLTIP_CANNOT_AFFORD = TextEntry.tooltip(MODID,"cannot_afford");
    public static final TextEntry TOOLTIP_TAX_LIMIT = TextEntry.tooltip(MODID,"tax_limit");
    public static final TextEntry TOOLTIP_DENIED = TextEntry.tooltip(MODID,"denied");

    public static final TextEntry TOOLTIP_SLOT_MACHINE_TO_INTERACT = TextEntry.tooltip(MODID,"slot_machine.to_interact");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_TO_INFO = TextEntry.tooltip(MODID,"slot_machine.to_info");
    public static final MultiLineTextEntry TOOLTIP_SLOT_MACHINE_ROLL_ONCE = MultiLineTextEntry.tooltip(MODID,"slot_machine.roll.once");
    public static final MultiLineTextEntry TOOLTIP_SLOT_MACHINE_ROLL_MULTI = MultiLineTextEntry.tooltip(MODID,"slot_machine.roll.multi");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_NORMAL_COST = TextEntry.tooltip(MODID,"slot_machine.roll.normal_price");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_COST_FREE = TextEntry.tooltip(MODID,"slot_machine.roll.free");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_UNDEFINED = TextEntry.tooltip(MODID,"slot_machine.undefined");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_MONEY = TextEntry.tooltip(MODID,"slot_machine.money");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_WEIGHT = TextEntry.tooltip(MODID,"slot_machine.weight");
    public static final TextEntry TOOLTIP_SLOT_MACHINE_ODDS = TextEntry.tooltip(MODID,"slot_machine.odds");

    //Network Terminal Menu
    public static final TextEntry GUI_NETWORK_TERMINAL_TITLE = TextEntry.gui(MODID,"network_terminal.title");
    public static final TextEntry GUI_NETWORK_TERMINAL_SEARCH = TextEntry.gui(MODID,"network_terminal.search");
    public static final TextEntry TOOLTIP_NETWORK_TERMINAL_OPEN_ALL = TextEntry.tooltip(MODID,"network_terminal.open_all");
    public static final TextEntry TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT = TextEntry.tooltip(MODID,"terminal.info.trade_count");
    public static final TextEntry TOOLTIP_NETWORK_TERMINAL_OUT_OF_STOCK_COUNT = TextEntry.tooltip(MODID,"terminal.info.trade_count.out_of_stock");
    public static final TextEntry TOOLTIP_NETWORK_TERMINAL_AUCTION_HOUSE = TextEntry.tooltip(MODID,"terminal.info.auction_house");

    //Notification Menu
    public static final TextEntry BUTTON_NOTIFICATIONS_MARK_AS_READ = TextEntry.button(MODID,"notifications.mark_read");

    //Team Management Menu
    public static final TextEntry TOOLTIP_TEAM_SELECT = TextEntry.tooltip(MODID,"team.selection");
    public static final TextEntry BUTTON_TEAM_CREATE = TextEntry.button(MODID,"team.create");
    public static final TextEntry GUI_TEAM_SELECT = TextEntry.button(MODID,"team.select");
    public static final TextEntry GUI_TEAM_CREATE = TextEntry.gui(MODID,"team.create");
    public static final TextEntry TOOLTIP_TEAM_MEMBERS = TextEntry.tooltip(MODID,"team.members");
    public static final TextEntry TOOLTIP_TEAM_MEMBER_EDIT = TextEntry.tooltip(MODID,"team.member_edit");
    public static final TextEntry BUTTON_TEAM_MEMBER_PROMOTE = TextEntry.button(MODID,"team.member.promote");
    public static final TextEntry TOOLTIP_TEAM_BANK = TextEntry.tooltip(MODID,"team.bank");
    public static final TextEntry BUTTON_TEAM_BANK_CREATE = TextEntry.button(MODID,"team.bank.create");
    public static final TextEntry BUTTON_TEAM_BANK_LIMIT = TextEntry.button(MODID,"team.bank.limit");
    public static final TextEntry TOOLTIP_TEAM_SALARY_INFO = TextEntry.tooltip(MODID,"team.salary_info");
    public static final TextEntry GUI_TEAM_SALARY_INFO_DISABLED = TextEntry.gui(MODID,"team.salary_info.disabled");
    public static final TextEntry GUI_TEAM_SALARY_INFO_DELAY = TextEntry.gui(MODID,"team.salary_info.delay");
    public static final TextEntry GUI_TEAM_SALARY_INFO_NEXT_TRIGGER = TextEntry.gui(MODID,"team.salary_info.next_trigger");
    public static final TextEntry GUI_TEAM_SALARY_INFO_SALARY_MEMBERS = TextEntry.gui(MODID,"team.salary_info.salary.member");
    public static final TextEntry GUI_TEAM_SALARY_INFO_SALARY_ADMINS = TextEntry.gui(MODID,"team.salary_info.salary.admins");
    public static final TextEntry GUI_TEAM_SALARY_INFO_REQUIRED_FUNDS = TextEntry.gui(MODID,"team.salary_info.required_funds");
    public static final TextEntry GUI_TEAM_SALARY_INFO_INSUFFICIENT_FUNDS = TextEntry.gui(MODID,"team.salary_info.insufficient_funds");
    public static final TextEntry GUI_TEAM_SALARY_INFO_LAST_ATTEMPT_FAILED = TextEntry.gui(MODID,"team.salary_info.last_attempt_failed");
    public static final TextEntry TOOLTIP_TEAM_SALARY_SETTINGS = TextEntry.tooltip(MODID,"team.salary_settings");
    public static final TextEntry BUTTON_TEAM_SALARY_SETTINGS_ENABLE = TextEntry.button(MODID,"team.salary_settings.enable");
    public static final TextEntry BUTTON_TEAM_SALARY_SETTINGS_DISABLE = TextEntry.button(MODID,"team.salary_settings.disable");
    public static final TextEntry GUI_TEAM_SALARY_SETTINGS_NOTIFICATION = TextEntry.gui(MODID,"team.salary_settings.notification");
    public static final TextEntry GUI_TEAM_SALARY_SETTINGS_DELAY = TextEntry.gui(MODID,"team.salary_settings.delay");
    public static final TextEntry BUTTON_TEAM_SALARY_SETTINGS_TRIGGER_SALARY = TextEntry.button(MODID,"team.salary_settings.trigger_salary");
    public static final TextEntry TOOLTIP_TEAM_SALARY_PAYMENTS = TextEntry.tooltip(MODID,"team.salary_payments");
    public static final TextEntry TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_ENABLE = TextEntry.tooltip(MODID,"team.salary_payments.creative.enable");
    public static final TextEntry TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_DISABLE = TextEntry.tooltip(MODID,"team.salary_payments.creative.diusable");
    public static final TextEntry GUI_TEAM_SALARY_PAYMENTS_MEMBER_SALARY = TextEntry.gui(MODID,"team.salary_payments.salary.member");
    public static final TextEntry GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY_SEPERATION = TextEntry.gui(MODID,"team.salary_payments.admin_salary_seperation");
    public static final TextEntry GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY = TextEntry.gui(MODID,"team.salary_payments.salary.admin");
    public static final TextEntry TOOLTIP_TEAM_STATS = TextEntry.tooltip(MODID,"team.stats");
    public static final TextEntry TOOLTIP_TEAM_NAME = TextEntry.tooltip(MODID,"team.name");
    public static final TextEntry BUTTON_TEAM_RENAME = TextEntry.button(MODID,"team.rename");
    public static final TextEntry GUI_TEAM_NAME_CURRENT = TextEntry.gui(MODID,"team.name.current");
    public static final TextEntry TOOLTIP_TEAM_NAME_AND_OWNER = TextEntry.tooltip(MODID,"team.owner");
    public static final TextEntry BUTTON_TEAM_DISBAND = TextEntry.button(MODID,"team.disband");
    public static final TextEntry GUI_TEAM_ID = TextEntry.gui(MODID,"team.id");

    //ATM Menu
    public static final TextEntry TOOLTIP_ATM_EXCHANGE = TextEntry.tooltip(MODID,"atm.exchange");
    public static final TextEntry TOOLTIP_ATM_SELECTION = TextEntry.tooltip(MODID,"atm.selection");
    public static final TextEntry BUTTON_BANK_MY_ACCOUNT = TextEntry.button(MODID,"bank.my_account");
    public static final TextEntry BUTTON_BANK_PLAYER_ACCOUNT = TextEntry.button(MODID,"bank.player_account");
    public static final TextEntry GUI_BANK_SELECT_PLAYER_SUCCESS = TextEntry.gui(MODID,"bank.select.player.success");
    public static final TextEntry TOOLTIP_ATM_INTERACT = TextEntry.tooltip(MODID,"atm.interact");
    public static final TextEntry TOOLTIP_ATM_NOTIFICATIONS = TextEntry.tooltip(MODID,"atm.notification");
    public static final TextEntry GUI_BANK_NOTIFICATIONS_DISABLED = TextEntry.gui(MODID,"bank.notification.disabled");
    public static final TextEntry GUI_BANK_NOTIFICATIONS_DETAILS = TextEntry.gui(MODID,"bank.notification.details");
    public static final TextEntry BUTTON_BANK_CARD_VERIFCATION_RESET = TextEntry.button(MODID,"bank.atm_card.verification.reset");
    public static final TextEntry TOOLTIP_BANK_CARD_VERIFCATION_RESET = TextEntry.tooltip(MODID,"bank.atm_card.verification.reset");
    public static final TextEntry TOOLTIP_ATM_LOGS = TextEntry.tooltip(MODID,"atm.log");
    public static final TextEntry TOOLTIP_ATM_TRANSFER = TextEntry.tooltip(MODID,"atm.transfer");
    public static final TextEntry TOOLTIP_ATM_TRANSFER_MODE_PLAYER = TextEntry.tooltip(MODID,"atm.transfer.mode.player");
    public static final TextEntry TOOLTIP_ATM_TRANSFER_MODE_LIST = TextEntry.tooltip(MODID,"atm.transfer.mode.list");
    public static final TextEntry TOOLTIP_ATM_TRANSFER_TRIGGER = TextEntry.tooltip(MODID,"bank.transfer.list");

    //Money Chest Menu
    public static final TextEntry BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_YES = TextEntry.button(MODID,"upgrade.coin_chest.exchange.while_open.y");
    public static final TextEntry BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_NO = TextEntry.button(MODID,"upgrade.coin_chest.exchange.while_open.n");
    public static final TextEntry MESSAGE_COIN_CHEST_PROTECTION_WARNING = TextEntry.message(MODID,"coin_chest.protection.warning");

    //Bank Upgrade
    public static final TextEntry BUTTON_BANK_UPGRADE_MODE_DEPOSIT = TextEntry.button(MODID,"coin_chest.bank_upgrade.mode.deposit");
    public static final TextEntry BUTTON_BANK_UPGRADE_MODE_WITHDRAW = TextEntry.button(MODID,"coin_chest.bank_upgrade.mode.withdraw");
    public static final TextEntry GUI_BANK_UPGRADE_DETAILS_NO_ACCOUNT = TextEntry.gui(MODID,"coin_chest.bank_upgrade.details.no_account");
    public static final TextEntry GUI_BANK_UPGRADE_DETAILS_DEPOSIT_UNLIMITED = TextEntry.gui(MODID,"coin_chest.bank_upgrade.details.deposit.no_limit");
    public static final TextEntry GUI_BANK_UPGRADE_DETAILS_DEPOSIT_LIMITED = TextEntry.gui(MODID,"coin_chest.bank_upgrade.details.deposit.limit");
    public static final TextEntry GUI_BANK_UPGRADE_DETAILS_WITHDRAW = TextEntry.gui(MODID,"coin_chest.bank_upgrade.details.withdraw");
    public static final TextEntry GUI_BANK_UPGRADE_DETAILS_WITHDRAW_INVALID = TextEntry.gui(MODID,"coin_chest.bank_upgrade.details.withdraw.invalid");

    //Ejection Menu
    public static final TextEntry GUI_EJECTION_NO_DATA = TextEntry.gui(MODID,"ejection_menu.no_data");
    public static final TextEntry TOOLTIP_EJECTION_SPLIT_GENERIC = TextEntry.tooltip(MODID,"ejection_menu.split.generic");
    public static final TextEntry TOOLTIP_EJECTION_SPLIT_TRADER = TextEntry.tooltip(MODID,"ejection_menu.split.trader");

    //Coin Mint Menu
    public static final TextEntry GUI_COIN_MINT_TITLE = TextEntry.gui(MODID,"coinmint.title");

    //Player Trade Menu
    public static final TextEntry BUTTON_PLAYER_TRADING_PROPOSE = TextEntry.button(MODID,"player_trading.propose");
    public static final TextEntry BUTTON_PLAYER_TRADING_ACCEPT = TextEntry.button(MODID,"player_trading.accept");
    public static final TextEntry BUTTON_PLAYER_TRADING_CANCEL = TextEntry.button(MODID,"player_trading.cancel");
    public static final TextEntry TOOLTIP_PLAYER_TRADING_MONEY_OPEN = TextEntry.tooltip(MODID,"player_trading.money.open");
    public static final TextEntry TOOLTIP_PLAYER_TRADING_MONEY_CLOSE = TextEntry.tooltip(MODID,"player_trading.money.close");
    public static final TextEntry TOOLTIP_PLAYER_TRADING_CHAT_OPEN = TextEntry.tooltip(MODID,"player_trading.chat.open");
    public static final TextEntry TOOLTIP_PLAYER_TRADING_CHAT_CLOSE = TextEntry.tooltip(MODID,"player_trading.chat.close");

    //Tax Collector Menu
    public static final TextEntry MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER = TextEntry.message(MODID,"tax_collector.placement.trader");
    public static final TextEntry MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_SERVER_ONLY = TextEntry.message(MODID,"tax_collector.placement.trader.server_only");
    public static final TextEntry MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_INFO = TextEntry.message(MODID,"tax_collector.placement.trader.info");
    public static final TextEntry GUI_TAX_COLLECTOR_DEFAULT_NAME = TextEntry.gui(MODID,"tax_collector.default_name");
    public static final TextEntry GUI_TAX_COLLECTOR_DEFAULT_NAME_SERVER = TextEntry.gui(MODID,"tax_collector.default_name.server");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_BASIC = TextEntry.tooltip(MODID,"tax_collector.basic");
    public static final TextEntry GUI_TAX_COLLECTOR_ACTIVE = TextEntry.gui(MODID,"tax_collector.active");
    public static final TextEntry GUI_TAX_COLLECTOR_RENDER_MODE_LABEL = TextEntry.gui(MODID,"tax_collector.render_mode.label");
    public static final TextEntry GUI_TAX_COLLECTOR_RENDER_MODE_NONE = TextEntry.gui(MODID,"tax_collector.render_mode.0");
    public static final TextEntry GUI_TAX_COLLECTOR_RENDER_MODE_MEMBERS = TextEntry.gui(MODID,"tax_collector.render_mode.1");
    public static final TextEntry GUI_TAX_COLLECTOR_RENDER_MODE_ALL = TextEntry.gui(MODID,"tax_collector.render_mode.2");
    public static final TextEntry GUI_TAX_COLLECTOR_TAX_RATE = TextEntry.gui(MODID,"tax_collector.tax_rate");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_INFINITE_LABEL = TextEntry.gui(MODID,"tax_collector.area.infinite.label");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_INFINITE_VOID = TextEntry.gui(MODID,"tax_collector.area.infinite.void");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_INFINITE_DIMENSION = TextEntry.gui(MODID,"tax_collector.area.infinite.dimension");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_RADIUS = TextEntry.gui(MODID,"tax_collector.area.radius");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_HEIGHT = TextEntry.gui(MODID,"tax_collector.area.height");
    public static final TextEntry GUI_TAX_COLLECTOR_AREA_VERTOFFSET = TextEntry.gui(MODID,"tax_collector.area.vertOffset");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_LOGS = TextEntry.tooltip(MODID,"tax_collector.logs");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_INFO = TextEntry.tooltip(MODID,"tax_collector.info");
    public static final TextEntry BUTTON_TAX_COLLECTOR_STATS_CLEAR = TextEntry.button(MODID,"tax_collector.stats.clear");
    public static final TextEntry GUI_TAX_COLLECTOR_STATS_TOTAL_COLLECTED = TextEntry.button(MODID,"tax_collector.stats.total_collected");
    public static final TextEntry GUI_TAX_COLLECTOR_STATS_UNIQUE_TAXABLES = TextEntry.button(MODID,"tax_collector.stats.unique_taxables");
    public static final TextEntry GUI_TAX_COLLECTOR_STATS_MOST_TAXED_LABEL = TextEntry.button(MODID,"tax_collector.stats.most_taxed.label");
    public static final TextEntry GUI_TAX_COLLECTOR_STATS_MOST_TAXED_FORMAT = TextEntry.button(MODID,"tax_collector.stats.most_taxed.format");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_OWNER = TextEntry.tooltip(MODID,"tax_collector.owner");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_ADMIN = TextEntry.tooltip(MODID,"tax_collector.admin");
    public static final TextEntry GUI_TAX_COLLECTOR_FORCE_ACCEPTANCE = TextEntry.gui(MODID,"tax_collector.force_acceptance");
    public static final TextEntry GUI_TAX_COLLECTOR_INFINITE_RANGE = TextEntry.gui(MODID,"tax_collector.infinite_range");
    public static final TextEntry GUI_TAX_COLLECTOR_TAXABLE_ACCEPT_COLLECTOR = TextEntry.gui(MODID,"tax_collector.taxable.accept_collector");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_TAXABLE_FORCE_IGNORE = TextEntry.tooltip(MODID,"tax_collector.taxable.force_ignore_collector");
    public static final TextEntry TOOLTIP_TAX_COLLECTOR_TAXABLE_PARDON_IGNORED = TextEntry.tooltip(MODID,"tax_collector.taxable.pardon_ignored_collector");
    public static final TextEntry MESSAGE_TAX_COLLECTOR_WARNING_MISSING_DATA = TextEntry.message(MODID,"tax_collector.warning.missing_data");
    public static final TextEntry MESSAGE_TAX_COLLECTOR_WARNING_NO_ACCESS = TextEntry.message(MODID,"tax_collector.warning.no_access");

    //Ticket Station Menu
    public static final TextEntry GUI_TICKET_STATION_TITLE = TextEntry.gui(MODID,"ticket_station.title");
    public static final TextEntry TOOLTIP_TICKET_STATION_RECIPE_INFO = TextEntry.tooltip(MODID,"ticket_station.recipe_info");
    public static final TextEntry TOOLTIP_TICKET_STATION_SELECT_RECIPE = TextEntry.tooltip(MODID,"ticket_station.select_recipe");
    public static final TextEntry TOOLTIP_TICKET_STATION_CRAFT = TextEntry.tooltip(MODID,"ticket_station.craft_ticket");

    //Trader Interface Menu
    public static final TextEntryBundle<TraderInterfaceBlockEntity.InteractionType> GUI_INTERFACE_INTERACTION_TYPE = TextEntryBundle.of(TraderInterfaceBlockEntity.InteractionType.values(),"gui.lightmanscurrency.interface.type");
    public static final TextEntryBundle<TraderInterfaceBlockEntity.ActiveMode> GUI_INTERFACE_ACTIVE_MODE = TextEntryBundle.of(TraderInterfaceBlockEntity.ActiveMode.values(),"gui.lightmanscurrency.interface.mode");
    public static final TextEntry TOOLTIP_INTERFACE_ONLINE_MODE_ON = TextEntry.tooltip(MODID,"interface.onlinemode.true");
    public static final TextEntry TOOLTIP_INTERFACE_ONLINE_MODE_OFF = TextEntry.tooltip(MODID,"interface.onlinemode.false");
    public static final TextEntry TOOLTIP_INTERFACE_INFO = TextEntry.tooltip(MODID,"interface.info");
    public static final TextEntry TOOLTIP_INTERFACE_INFO_ACCEPT_CHANGES = TextEntry.tooltip(MODID,"interface.info.acceptchanges");
    public static final TextEntry GUI_INTERFACE_INFO_MISSING_PERMISSIONS = TextEntry.gui(MODID,"interface.info.trader.permissions");
    public static final TextEntry GUI_INTERFACE_INFO_TRADER_NULL = TextEntry.gui(MODID,"interface.info.trader.null");
    public static final TextEntry GUI_INTERFACE_INFO_TRADER_REMOVED = TextEntry.gui(MODID,"interface.info.trader.removed");
    public static final TextEntry GUI_INTERFACE_INFO_TRADE_NOT_DEFINED = TextEntry.gui(MODID,"interface.info.trade.notdefined");
    public static final TextEntry GUI_INTERFACE_INFO_TRADE_MISSING = TextEntry.gui(MODID,"interface.info.trader.missing");
    public static final TextEntry TOOLTIP_INTERFACE_TRADER_SELECT = TextEntry.tooltip(MODID,"interface.trader");
    public static final TextEntry TOOLTIP_INTERFACE_TRADE_SELECT = TextEntry.tooltip(MODID,"interface.trade");
    public static final TextEntry TOOLTIP_INTERFACE_STORAGE = TextEntry.tooltip(MODID,"interface.storage");
    public static final TextEntry TOOLTIP_INTERFACE_STATS = TextEntry.tooltip(MODID,"interface.stats");

    //Trade Result
    public static final TextEntryBundle<TradeResult> GUI_TRADE_RESULT = TextEntryBundle.of(TradeResult.ALL_WITH_MESSAGES,"gui.lightmanscurrency.trade_result");

    //Trade Comparison
    public static final TextEntry GUI_TRADE_DIFFERENCE_MISSING = TextEntry.gui(MODID,"interface.difference.missing");
    public static final TextEntry GUI_TRADE_DIFFERENCE_TYPE = TextEntry.gui(MODID,"interface.difference.type");
    public static final TextEntry GUI_TRADE_DIFFERENCE_MONEY_TYPE = TextEntry.gui(MODID,"interface.difference.money_type");
    public static final TextEntry GUI_TRADE_DIFFERENCE_CHEAPER = TextEntry.gui(MODID,"interface.difference.cheaper");
    public static final TextEntry GUI_TRADE_DIFFERENCE_EXPENSIVE = TextEntry.gui(MODID,"interface.difference.expensive");
    public static final TextEntry GUI_TRADE_DIFFERENCE_PURCHASE_CHEAPER = TextEntry.gui(MODID,"interface.difference.purchase.cheaper");
    public static final TextEntry GUI_TRADE_DIFFERENCE_PURCHASE_EXPENSIVE = TextEntry.gui(MODID,"interface.difference.purchase.expensive");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_SELLING = TextEntry.gui(MODID,"interface.item.difference.selling");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_PURCHASING = TextEntry.gui(MODID,"interface.item.difference.purchasing");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_TYPE = TextEntry.gui(MODID,"interface.item.difference.itemtype");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_NBT = TextEntry.gui(MODID,"interface.item.difference.itemnbt");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_MORE = TextEntry.gui(MODID,"interface.item.difference.quantity.more");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_LESS = TextEntry.gui(MODID,"interface.item.difference.quantity.less");

    //Wallet Menu
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE = TextEntry.tooltip(MODID,"wallet.exchange");
    public static final TextEntry TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE = TextEntry.tooltip(MODID,"wallet.auto_exchange.enable");
    public static final TextEntry TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE = TextEntry.tooltip(MODID,"wallet.auto_exchange.disable");
    public static final TextEntry TOOLTIP_WALLET_OPEN_BANK = TextEntry.tooltip(MODID,"wallet.open_bank");
    public static final TextEntry TOOLTIP_WALLET_OPEN_WALLET = TextEntry.tooltip(MODID,"wallet.open_wallet");

    //Trader Menu
    public static final TextEntry GUI_TRADER_TITLE = TextEntry.gui(MODID,"trader.title");
    public static final TextEntry GUI_TRADER_DEFAULT_NAME = TextEntry.gui(MODID,"trader.default_name");
    public static final TextEntry GUI_TRADER_ALL_NETWORK_TRADERS = TextEntry.gui(MODID,"trader.all_network");
    public static final TextEntry GUI_TRADER_SEARCH_TRADES = TextEntry.gui(MODID,"trader.search.trades");
    public static final TextEntry TOOLTIP_TRADER_OPEN_STORAGE = TextEntry.tooltip(MODID,"trader.open_storage");
    public static final TextEntry TOOLTIP_TRADER_COLLECT_COINS = TextEntry.tooltip(MODID,"trader.collect_coins");
    public static final TextEntry TOOLTIP_TRADER_NETWORK_BACK = TextEntry.tooltip(MODID,"trader.network.back");
    public static final TextEntry TOOLTIP_TRADER_OPEN_TRADES = TextEntry.tooltip(MODID,"trader.open_trades");
    public static final TextEntry TOOLTIP_TRADER_STORE_COINS = TextEntry.tooltip(MODID,"trader.store_coins");
    public static final TextEntry TOOLTIP_TRADER_TRADE_RULES_TRADER = TextEntry.tooltip(MODID,"trader.trade_rules.trader");
    public static final TextEntry TOOLTIP_TRADER_TRADE_RULES_TRADE = TextEntry.tooltip(MODID,"trader.trade_rules.trade");
    public static final TextEntry GUI_TRADER_NO_TRADES = TextEntry.gui(MODID,"notrades");
    public static final TextEntry TOOLTIP_TRADER_EDIT_TRADES = TextEntry.tooltip(MODID,"trader.edit_trades");
    public static final TextEntry TOOLTIP_TRADER_LOGS = TextEntry.tooltip(MODID,"trader.log");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS = TextEntry.tooltip(MODID,"trader.settings");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_NAME = TextEntry.tooltip(MODID,"trader.settings.name");
    public static final TextEntry GUI_TRADER_SETTINGS_CUSTOM_ICON = TextEntry.tooltip(MODID,"trader.settings.custom_icon");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_CREATIVE = TextEntry.tooltip(MODID,"trader.settings.creative");
    public static final TextEntry BUTTON_TRADER_SETTINGS_CREATIVE_ENABLED = TextEntry.button(MODID,"trader.settings.creative.enabled");
    public static final TextEntry BUTTON_TRADER_SETTINGS_CREATIVE_DISABLED = TextEntry.button(MODID,"trader.settings.creative.disabled");
    public static final TextEntry BUTTON_TRADER_SETTINGS_CREATIVE_ADD_TRADE = TextEntry.button(MODID,"trader.settings.creative.add_trade");
    public static final TextEntry BUTTON_TRADER_SETTINGS_CREATIVE_REMOVE_TRADE = TextEntry.button(MODID,"trader.settings.creative.remove_trade");
    public static final TextEntry GUI_TRADER_SETTINGS_CREATIVE_TRADE_COUNT = TextEntry.gui(MODID,"trader.settings.creative.trade_offers");
    public static final TextEntry GUI_TRADER_SETTINGS_CREATIVE_STORE_MONEY = TextEntry.tooltip(MODID,"trader.settings.creative.store_money");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_PERSISTENT = TextEntry.tooltip(MODID,"trader.settings.persistent");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_ALLY = TextEntry.tooltip(MODID,"trader.settings.ally");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_ALLY_PERMS = TextEntry.tooltip(MODID,"trader.settings.allyperms");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_MISC = TextEntry.tooltip(MODID,"trader.settings.misc");
    public static final TextEntry GUI_TRADER_SETTINGS_NOTIFICATIONS_ENABLED = TextEntry.gui(MODID,"trader.settings.notifications.enabled");
    public static final TextEntry GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT = TextEntry.gui(MODID,"trader.settings.notifications.chat");
    public static final TextEntry GUI_TRADER_SETTINGS_NOTIFICATIONS_TARGET = TextEntry.gui(MODID,"trader.settings.notifications.target");
    public static final TextEntry GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX = TextEntry.gui(MODID,"trader.settings.always_show_search_box");
    public static final TextEntry BUTTON_TRADER_SETTINGS_PICKUP_TRADER = TextEntry.button(MODID,"trader.settings.pickup_trader");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER = TextEntry.tooltip(MODID,"trader.settings.pickup_trader");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER_ADVANCED = TextEntry.tooltip(MODID,"trader.settings.pickup_trader.admin");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_TAXES = TextEntry.tooltip(MODID,"trader.settings.taxes");
    public static final TextEntry GUI_TRADER_SETTINGS_TAXES_ACCEPTABLE_RATE = TextEntry.tooltip(MODID,"trader.settings.acceptabletaxrate");
    public static final TextEntry GUI_TRADER_SETTINGS_TAXES_IGNORE_TAXES = TextEntry.tooltip(MODID,"trader.settings.ignoretaxes");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_INPUT_GENERIC = TextEntry.tooltip(MODID,"trader.settings.input");
    public static final TextEntry TOOLTIP_TRADER_SETTINGS_INPUT_ITEM = TextEntry.tooltip(MODID,"trader.settings.iteminput");
    public static final TextEntry TOOLTIP_TRADER_STATS = TextEntry.tooltip(MODID,"trader.stats");
    public static final TextEntry BUTTON_TRADER_STATS_CLEAR = TextEntry.button(MODID,"trader.stats.clear");
    public static final TextEntry GUI_TRADER_STATS_EMPTY = TextEntry.gui(MODID,"trader.stats.empty");
    public static final TextEntry TOOLTIP_TRADER_TAXES = TextEntry.tooltip(MODID,"trader.tax_info");
    public static final TextEntry GUI_TRADER_TAXES_TOTAL_RATE = TextEntry.tooltip(MODID,"trader.tax_info.total_rate");
    public static final TextEntry GUI_TRADER_TAXES_NO_TAX_COLLECTORS = TextEntry.gui(MODID,"trade.tax_info.no_tax_collectors");
    public static final TextEntry TOOLTIP_TRADER_STORAGE = TextEntry.tooltip(MODID,"trader.storage");
    public static final TextEntry MESSAGE_TRADER_WARNING_MISSING_DATA = TextEntry.message(MODID,"trader.warning.missing_data");

    //General Trade Tooltips
    public static final TextEntry TOOLTIP_TRADE_EDIT_PRICE = TextEntry.tooltip(MODID,"trade.edit_price");
    public static final TextEntry TOOLTIP_TRADE_INFO_TITLE = TextEntry.tooltip(MODID,"trade.info.title");
    public static final TextEntry TOOLTIP_TRADE_INFO_ORIGINAL_NAME = TextEntry.tooltip(MODID,"trade.info.original_name");
    public static final TextEntry TOOLTIP_TRADE_INFO_STOCK = TextEntry.tooltip(MODID,"trade.info.stock");
    public static final TextEntry TOOLTIP_TRADE_INFO_STOCK_INFINITE = TextEntry.tooltip(MODID,"trade.info.stock.infinite");

    //Item Trader Specific
    public static final TextEntry GUI_TRADER_ITEM_ENFORCE_NBT = TextEntry.gui(MODID,"trader.item.enforce_nbt");
    public static final TextEntry TOOLTIP_TRADE_ITEM_EDIT_EMPTY = TextEntry.tooltip(MODID,"trade.item.edit_item");
    public static final TextEntry TOOLTIP_TRADE_ITEM_EDIT_SHIFT = TextEntry.tooltip(MODID,"trade.item.shift_edit_item");
    public static final TextEntry TOOLTIP_TRADE_ITEM_NBT_WARNING_PURCHASE = TextEntry.tooltip(MODID,"trade.item.nbt_warning.purchase");
    public static final TextEntry TOOLTIP_TRADE_ITEM_NBT_WARNING_SALE = TextEntry.tooltip(MODID,"trade.item.nbt_warning.sale");
    public static final TextEntry GUI_ITEM_EDIT_SEARCH = TextEntry.gui(MODID,"item_edit.search");
    public static final TextEntry TOOLTIP_ITEM_EDIT_SCROLL = TextEntry.gui(MODID,"item_edit.scroll");

    //Paygate Specific
    public static final TextEntry TOOLTIP_TRADER_PAYGATE_COLLECT_TICKET_STUBS = TextEntry.tooltip(MODID,"trader.paygate.collect_ticket_stubs");
    public static final TextEntry GUI_TRADER_PAYGATE_DURATION = TextEntry.gui(MODID,"trader.paygate.duration");
    public static final TextEntry GUI_TRADER_PAYGATE_DURATION_UNIT = TextEntry.gui(MODID,"trader.paygate.duration.unit");
    public static final TextEntry TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_KEEP = TextEntry.tooltip(MODID,"trader.paygate.ticket_stubs.keep");
    public static final TextEntry TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_GIVE = TextEntry.tooltip(MODID,"trader.paygate.ticket_stubs.give");
    public static final TextEntry TOOLTIP_TRADER_PAYGATE_ALREADY_ACTIVE = TextEntry.tooltip(MODID,"trader.paygate.active");

    //Auction House Specific
    public static final TextEntry GUI_TRADER_AUCTION_HOUSE = TextEntry.gui(MODID,"trader.auction_house");
    public static final TextEntry GUI_TRADER_AUCTION_HOUSE_OWNER = TextEntry.gui(MODID,"trader.auction_house.owner");
    public static final TextEntry BUTTON_TRADER_AUCTION_BID = TextEntry.button(MODID,"trader.auction.bid");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_STORAGE = TextEntry.tooltip(MODID,"trader.auction.storage");
    public static final TextEntry GUI_TRADER_AUCTION_STORAGE_ITEMS_NONE = TextEntry.gui(MODID,"trader.auction.storage.items.none");
    public static final TextEntry GUI_TRADER_AUCTION_STORAGE_MONEY = TextEntry.gui(MODID,"trader.auction.storage.money");
    public static final TextEntry GUI_TRADER_AUCTION_STORAGE_MONEY_NONE = TextEntry.gui(MODID,"trader.auction.storage.money.none");
    public static final TextEntry GUI_TRADER_AUCTION_CANCEL = TextEntry.gui(MODID,"trader.auction.cancel");
    public static final TextEntry BUTTON_TRADER_AUCTION_CANCEL_SELF = TextEntry.button(MODID,"trader.auction.cancel.self");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_CANCEL_SELF = TextEntry.tooltip(MODID,"trader.auction.cancel.self");
    public static final TextEntry BUTTON_TRADER_AUCTION_CANCEL_STORAGE = TextEntry.button(MODID,"trader.auction.cancel.storage");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_CANCEL_STORAGE = TextEntry.tooltip(MODID,"trader.auction.cancel.storage");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_CREATE = TextEntry.tooltip(MODID,"trader.auction.create");
    public static final TextEntry GUI_TRADER_AUCTION_OVERTIME = TextEntry.gui(MODID,"trader.auction.overtime");
    public static final MultiLineTextEntry TOOLTIP_TRADER_AUCTION_OVERTIME = MultiLineTextEntry.tooltip(MODID,"trader.auction.overtime");
    public static final TextEntry BUTTON_TRADER_AUCTION_PRICE_MODE_STARTING_BID = TextEntry.button(MODID,"trader.auction.price_mode.starting_bid");
    public static final TextEntry BUTTON_TRADER_AUCTION_PRICE_MODE_MIN_BID_SIZE = TextEntry.button(MODID,"trader.auction.price_mode.min_bid_size");
    public static final TextEntry BUTTON_TRADER_AUCTION_CREATE = TextEntry.button(MODID,"trader.auction.create");
    public static final TextEntry GUI_TRADER_AUCTION_ITEMS = TextEntry.gui(MODID,"trader.auction.items");
    public static final TextEntry GUI_TRADER_AUCTION_CREATE_SUCCESS = TextEntry.gui(MODID,"trader.auction.create.success");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_INFO_NO_BIDDER = TextEntry.tooltip(MODID,"trader.auction.info.no_bidder");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_INFO_STARTING_BID = TextEntry.tooltip(MODID,"trader.auction.info.starting_bid");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_INFO_LAST_BIDDER = TextEntry.tooltip(MODID,"trader.auction.info.last_bidder");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_INFO_LAST_BID = TextEntry.tooltip(MODID,"trader.auction.info.last_bid");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_INFO_MIN_BID = TextEntry.tooltip(MODID,"trader.auction.info.min_bid");
    public static final TextEntry TOOLTIP_TRADER_AUCTION_TIME_REMAINING = TextEntry.tooltip(MODID,"trader.auction.time_remaining");

    //Slot Machine Specific
    public static final TextEntry TOOLTIP_TRADER_SLOT_MACHINE_EDIT_ENTRIES = TextEntry.tooltip(MODID,"trader.slot_machine.edit_entries");
    public static final TextEntry GUI_TRADER_SLOT_MACHINE_WEIGHT_LABEL = TextEntry.gui(MODID,"trader.slot_machine.weight_label");
    public static final TextEntry GUI_TRADER_SLOT_MACHINE_ENTRY_LABEL = TextEntry.gui(MODID,"trader.slot_machine.entry_label");
    public static final TextEntry GUI_TRADER_SLOT_MACHINE_ODDS_LABEL = TextEntry.gui(MODID,"trader.slot_machine.odds_label");
    public static final TextEntry TOOLTIP_TRADER_SLOT_MACHINE_EDIT_PRICE = TextEntry.gui(MODID,"trader.slot_machine.edit_price");

    //Trade Data
    public static final TextEntryBundle<TradeDirection> GUI_TRADE_DIRECTION = TextEntryBundle.of(TradeDirection.values(),"gui.lightmanscurrency.trade_direction");
    public static final TextEntryBundle<TradeDirection> GUI_TRADE_DIRECTION_ACTION = TextEntryBundle.of(TradeDirection.values(),"gui.lightmanscurrency.trade_direction.action");

    //Trade Rules
    public static final TextEntry GUI_TRADE_RULES_LIST = TextEntry.gui(MODID,"trade_rule.list");
    public static final TextEntry TOOLTIP_TRADE_RULES_MANAGER = TextEntry.gui(MODID,"trade_rule.manager");
    public static final TextEntry TRADE_RULE_PLAYER_LISTING = TextEntry.tradeRule(PlayerListing.TYPE);
    public static final TextEntry TRADE_RULE_PLAYER_LISTING_DENIAL_BLACKLIST = TextEntry.tradeRuleMessage(PlayerListing.TYPE,"denial.blacklist");
    public static final TextEntry TRADE_RULE_PLAYER_LISTING_DENIAL_WHITELIST = TextEntry.tradeRuleMessage(PlayerListing.TYPE,"denial.whitelist");
    public static final TextEntry TRADE_RULE_PLAYER_LISTING_ALLOWED = TextEntry.tradeRuleMessage(PlayerListing.TYPE,"allowed");
    public static final TextEntry BUTTON_PLAYER_LISTING_MODE_WHITELIST = TextEntry.button(MODID,"trade_rule.player_listing.mode.whitelist");
    public static final TextEntry BUTTON_PLAYER_LISTING_MODE_BLACKLIST = TextEntry.button(MODID,"trade_rule.player_listing.mode.blacklist");
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT = TextEntry.tradeRule(PlayerTradeLimit.TYPE);
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIMED = TextEntry.tradeRuleMessage(PlayerTradeLimit.TYPE,"denial.timed");
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL = TextEntry.tradeRuleMessage(PlayerTradeLimit.TYPE,"denial");
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT = TextEntry.tradeRuleMessage(PlayerTradeLimit.TYPE,"denial.limit");
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT_INFO_TIMED = TextEntry.tradeRuleMessage(PlayerTradeLimit.TYPE,"info.timed");
    public static final TextEntry TRADE_RULE_PLAYER_TRADE_LIMIT_INFO = TextEntry.tradeRuleMessage(PlayerTradeLimit.TYPE,"info");
    public static final TextEntry TOOLTIP_TRADE_LIMIT_CLEAR_MEMORY = TextEntry.tooltip(MODID,"trade_rule.player_trade_limit.clear_memory");
    public static final TextEntry GUI_TRADE_LIMIT_INFO = TextEntry.tooltip(MODID,"trade_rule.trade_limit.info");
    public static final TextEntry GUI_PLAYER_TRADE_LIMIT_DURATION = TextEntry.tooltip(MODID,"trade_rule.player_trade_limit.duration");
    public static final TextEntry GUI_PLAYER_TRADE_LIMIT_NO_DURATION = TextEntry.tooltip(MODID,"trade_rule.player_trade_limit.no_duration");
    public static final TextEntry TRADE_RULE_PLAYER_DISCOUNTS = TextEntry.tradeRule(PlayerDiscounts.TYPE);
    public static final TextEntry TRADE_RULE_PLAYER_DISCOUNTS_INFO_SALE = TextEntry.tradeRuleMessage(PlayerDiscounts.TYPE,"info.sale");
    public static final TextEntry TRADE_RULE_PLAYER_DISCOUNTS_INFO_PURCHASE = TextEntry.tradeRuleMessage(PlayerDiscounts.TYPE,"info.purchase");
    public static final TextEntry GUI_PLAYER_DISCOUNTS_INFO = TextEntry.gui(MODID,"trade_rule.discount_list.info");
    public static final TextEntry TRADE_RULE_TIMED_SALE = TextEntry.tradeRule(TimedSale.TYPE);
    public static final TextEntry TRADE_RULE_TIMED_SALE_INFO_SALE = TextEntry.tradeRuleMessage(TimedSale.TYPE,"info.sale");
    public static final TextEntry TRADE_RULE_TIMED_SALE_INFO_PURCHASE = TextEntry.tradeRuleMessage(TimedSale.TYPE,"info.purchase");
    public static final TextEntry GUI_TIMED_SALE_INFO_ACTIVE = TextEntry.gui(MODID,"trade_rule.timed_sale.info.active");
    public static final TextEntry GUI_TIMED_SALE_INFO_INACTIVE = TextEntry.gui(MODID,"trade_rule.timed_sale.info.inactive");
    public static final TextEntry BUTTON_TIMED_SALE_START = TextEntry.button(MODID,"trade_rule.timed_sale.start");
    public static final TextEntry TOOLTIP_TIMED_SALE_START = TextEntry.tooltip(MODID,"trade_rule.timed_sale.start");
    public static final TextEntry BUTTON_TIMED_SALE_STOP = TextEntry.button(MODID,"trade_rule.timed_sale.stop");
    public static final TextEntry TOOLTIP_TIMED_SALE_STOP = TextEntry.tooltip(MODID,"trade_rule.timed_sale.stop");
    public static final TextEntry TRADE_RULE_TRADE_LIMIT = TextEntry.tradeRule(TradeLimit.TYPE);
    public static final TextEntry TRADE_RULE_TRADE_LIMIT_DENIAL = TextEntry.tradeRuleMessage(TradeLimit.TYPE,"denial");
    public static final TextEntry TRADE_RULE_TRADE_LIMIT_INFO = TextEntry.tradeRuleMessage(TradeLimit.TYPE,"info");
    public static final TextEntry TRADE_RULE_FREE_SAMPLE = TextEntry.tradeRule(FreeSample.TYPE);
    public static final TextEntry TRADE_RULE_FREE_SAMPLE_INFO_SINGLE = TextEntry.tradeRuleMessage(FreeSample.TYPE,"info.single");
    public static final TextEntry TRADE_RULE_FREE_SAMPLE_INFO_MULTI = TextEntry.tradeRuleMessage(FreeSample.TYPE,"info.multi");
    public static final TextEntry TRADE_RULE_FREE_SAMPLE_INFO_USED = TextEntry.tradeRuleMessage(FreeSample.TYPE,"info.used");
    public static final TextEntry TRADE_RULE_FREE_SAMPLE_INFO_TIMED = TextEntry.tradeRuleMessage(FreeSample.TYPE,"info.timed");
    public static final TextEntry BUTTON_FREE_SAMPLE_RESET = TextEntry.button(MODID,"trade_rule.free_sample.reset");
    public static final TextEntry TOOLTIP_FREE_SAMPLE_RESET = TextEntry.tooltip(MODID,"trade_rule.free_sample.reset");
    public static final TextEntry GUI_FREE_SAMPLE_PLAYER_COUNT = TextEntry.gui(MODID,"trade_rule.free_sample.count");
    public static final TextEntry GUI_FREE_SAMPLE_INFO = TextEntry.gui(MODID,"trade_rule.free_sample.info");
    public static final TextEntry TRADE_RULE_PRICE_FLUCTUATION = TextEntry.tradeRule(PriceFluctuation.TYPE);
    public static final TextEntry GUI_PRICE_FLUCTUATION_LABEL = TextEntry.gui(MODID,"trade_rule.price_fluctuation.label");
    public static final TextEntry GUI_PRICE_FLUCTUATION_INFO = TextEntry.gui(MODID,"trade_rule.price_fluctuation.info");
    public static final TextEntry TRADE_RULE_DEMAND_PRICING = TextEntry.tradeRule(DemandPricing.TYPE);
    public static final TextEntry TRADE_RULE_DEMAND_PRICING_INFO = TextEntry.tradeRuleMessage(DemandPricing.TYPE,"info");
    public static final TextEntry GUI_DEMAND_PRICING_INFO = TextEntry.gui(MODID,"trade_rule.demand_pricing.info");
    public static final TextEntry GUI_DEMAND_PRICING_INFO_INVALID_PRICE = TextEntry.gui(MODID,"trade_rule.demand_pricing.info.invalid_price");
    public static final TextEntry GUI_DEMAND_PRICING_INFO_INVALID_STOCK = TextEntry.gui(MODID,"trade_rule.demand_pricing.info.invalid_stock");
    public static final TextEntry GUI_DEMAND_PRICING_INFO_INVALID_HOST = TextEntry.gui(MODID,"trade_rule.demand_pricing.info.invalid_host");
    public static final TextEntry GUI_DEMAND_PRICING_STOCK_SMALL = TextEntry.gui(MODID,"trade_rule.demand_pricing.small_stock");
    public static final TextEntry GUI_DEMAND_PRICING_STOCK_LARGE = TextEntry.gui(MODID,"trade_rule.demand_pricing.large_stock");


    //Permissions
    public static final TextEntry PERMISSION_OPEN_STORAGE = TextEntry.permission(Permissions.OPEN_STORAGE);
    public static final TextEntry PERMISSION_CHANGE_NAME = TextEntry.permission(Permissions.CHANGE_NAME);
    public static final TextEntry PERMISSION_EDIT_TRADES = TextEntry.permission(Permissions.EDIT_TRADES);
    public static final TextEntry PERMISSION_COLLECT_MONEY = TextEntry.permission(Permissions.COLLECT_COINS);
    public static final TextEntry PERMISSION_STORE_MONEY = TextEntry.permission(Permissions.STORE_COINS);
    public static final TextEntry PERMISSION_EDIT_TRADE_RULES = TextEntry.permission(Permissions.EDIT_TRADE_RULES);
    public static final TextEntry PERMISSION_EDIT_SETTINGS = TextEntry.permission(Permissions.EDIT_SETTINGS);
    public static final TextEntry PERMISSION_EDIT_ALLIES = TextEntry.permission(Permissions.ADD_REMOVE_ALLIES);
    public static final TextEntry PERMISSION_EDIT_PERMISSIONS = TextEntry.permission(Permissions.EDIT_PERMISSIONS);
    public static final TextEntry PERMISSION_VIEW_LOGS = TextEntry.permission(Permissions.VIEW_LOGS);
    public static final TextEntry PERMISSION_BREAK_MACHINE = TextEntry.permission(Permissions.BREAK_TRADER);
    public static final TextEntry PERMISSION_BANK_LINK = TextEntry.permission(Permissions.BANK_LINK);
    public static final TextEntry PERMISSION_INTERACTION_LINK = TextEntry.permission(Permissions.INTERACTION_LINK);
    public static final TextEntry PERMISSION_TRANSFER_OWNERSHIP = TextEntry.permission(Permissions.TRANSFER_OWNERSHIP);
    public static final TextEntry PERMISSION_EDIT_INPUTS = TextEntry.permission(Permissions.InputTrader.EXTERNAL_INPUTS);

    //Inventory Buttons
    public static final TextEntry TOOLTIP_NOTIFICATION_BUTTON = TextEntry.tooltip(MODID,"button.notification");
    public static final TextEntry TOOLTIP_TEAM_MANAGER_BUTTON = TextEntry.tooltip(MODID,"button.team_manager");
    public static final TextEntry TOOLTIP_EJECTION_BUTTON = TextEntry.tooltip(MODID,"button.ejection");
    public static final TextEntry TOOLTIP_CHEST_COIN_COLLECTION_BUTTON = TextEntry.tooltip(MODID,"button.chest.coin_collection");

    //Generic Settings
    public static final TextEntry BUTTON_SETTINGS_CHANGE_NAME = TextEntry.button(MODID,"settings.change_name");
    public static final TextEntry BUTTON_SETTINGS_RESET_NAME = TextEntry.button(MODID,"settings.reset_name");
    public static final TextEntry GUI_SETTINGS_BANK_LINK = TextEntry.gui(MODID,"settings.banklink");
    public static final TextEntry TOOLTIP_SETTINGS_OWNER = TextEntry.tooltip(MODID,"settings.owner");
    public static final TextEntry GUI_SETTINGS_INPUT_SIDE = TextEntry.gui(MODID,"settings.input.side");
    public static final TextEntry GUI_SETTINGS_OUTPUT_SIDE = TextEntry.gui(MODID,"settings.output.side");
    public static final TextEntryBundle<Direction> GUI_INPUT_SIDES = TextEntryBundle.of(Direction.values(),"gui.lightmanscurrency.settings.side");

    //Persistent Data
    public static final TextEntry GUI_PERSISTENT_ID = TextEntry.gui(MODID,"settings.persistent.id");
    public static final TextEntry GUI_PERSISTENT_OWNER = TextEntry.gui(MODID,"settings.persistent.owner");
    //Trader
    public static final TextEntry TOOLTIP_PERSISTENT_CREATE_TRADER = TextEntry.tooltip(MODID,"persistent.add.trader");
    public static final TextEntry MESSAGE_PERSISTENT_TRADER_OVERWRITE = TextEntry.message(MODID,"persistent.trader.overwrite");
    public static final TextEntry MESSAGE_PERSISTENT_TRADER_ADD = TextEntry.message(MODID,"persistent.trader.add");
    public static final TextEntry MESSAGE_PERSISTENT_TRADER_FAIL = TextEntry.message(MODID,"persistent.trader.fail");
    //Auction
    public static final TextEntry TOOLTIP_PERSISTENT_CREATE_AUCTION = TextEntry.tooltip(MODID,"persistent.add.auction");
    public static final TextEntry MESSAGE_PERSISTENT_AUCTION_OVERWRITE = TextEntry.message(MODID,"persistent.auction.overwrite");
    public static final TextEntry MESSAGE_PERSISTENT_AUCTION_ADD = TextEntry.message(MODID,"persistent.auction.add");
    public static final TextEntry MESSAGE_PERSISTENT_AUCTION_FAIL = TextEntry.message(MODID,"persistent.auction.fail");

    //Bank Gui's/Buttons
    public static final TextEntry GUI_BANK_BALANCE = TextEntry.gui(MODID,"bank.balance");
    public static final TextEntry GUI_BANK_NO_SELECTED_ACCOUNT = TextEntry.gui(MODID,"bank.null");
    public static final TextEntry BUTTON_BANK_DEPOSIT = TextEntry.button(MODID,"bank.deposit");
    public static final TextEntry BUTTON_BANK_WITHDRAW = TextEntry.button(MODID,"bank.withdraw");
    public static final TextEntry GUI_BANK_ACCOUNT_NAME = TextEntry.gui(MODID,"bank_account");

    //Bank Transfer Messages
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_NULL_FROM = TextEntry.gui(MODID,"bank.transfer.error.null");
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_ACCESS = TextEntry.gui(MODID,"bank.transfer.error.access");
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_NULL_TARGET = TextEntry.gui(MODID,"bank.error.null_account.target");
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_AMOUNT = TextEntry.gui(MODID,"bank.transfer.error.amount");
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_SAME = TextEntry.gui(MODID,"bank.transfer.error.same");
    public static final TextEntry GUI_BANK_TRANSFER_ERROR_NO_BALANCE = TextEntry.gui(MODID,"bank.transfer.error.no_balance");
    public static final TextEntry GUI_BANK_TRANSFER_SUCCESS = TextEntry.gui(MODID,"bank.transfer.success");

    //Ownership Blurbs
    public static final TextEntry BLURB_OWNERSHIP_MEMBERS = TextEntry.blurb(MODID,"ownership.members");
    public static final TextEntry BLURB_OWNERSHIP_ADMINS = TextEntry.blurb(MODID,"ownership.admins");
    public static final TextEntry BLURB_OWNERSHIP_OWNER = TextEntry.blurb(MODID,"ownership.owner");
    public static final TextEntry BUTTON_OWNER_SET_PLAYER = TextEntry.button(MODID,"ownership.set_player");
    public static final TextEntry GUI_OWNER_CURRENT = TextEntry.gui(MODID,"owner.current");
    public static final TextEntry GUI_OWNER_NULL = TextEntry.gui(MODID,"owner.null");
    public static final TextEntry TOOLTIP_OWNERSHIP_MODE_MANUAL = TextEntry.tooltip(MODID,"owner_mode.manual");
    public static final TextEntry TOOLTIP_OWNERSHIP_MODE_SELECTION = TextEntry.tooltip(MODID,"owner_mode.selection");

    //Bank Card Menu
    public static final TextEntry MESSAGE_ATM_CARD_LOCKED = TextEntry.message(MODID,"atm_card.locked");
    public static final TextEntry BUTTON_ATM_CARD_LOCK = TextEntry.button(MODID,"atm_card.lock");
    public static final TextEntry TOOLTIP_ATM_CARD_LOCK = TextEntry.tooltip(MODID,"atm_card.lock");
    public static final TextEntry BUTTON_ATM_CARD_UNLOCK = TextEntry.button(MODID,"atm_card.unlock");

    //Money Text
    public static final TextEntry GUI_MONEY_VALUE_FREE = TextEntry.gui(MODID,"money_value.free");
    public static final TextEntry GUI_MONEY_STORAGE_EMPTY = TextEntry.gui(MODID,"stored_money.empty");
    public static final TextEntry COIN_CHAIN_MAIN = TextEntry.chain(CoinAPI.MAIN_CHAIN);
    public static final TextEntry COIN_CHAIN_CHOCOLATE = TextEntry.chain(ChocolateEventCoins.CHAIN);
    public static final TextEntry COIN_CHAIN_CHOCOLATE_DISPLAY = TextEntry.chainDisplay(ChocolateEventCoins.CHAIN);
    public static final TextEntry COIN_CHAIN_CHOCOLATE_DISPLAY_WORDY = TextEntry.chainDisplayWordy(ChocolateEventCoins.CHAIN);
    public static final TextEntry COIN_CHAIN_EMERALDS = TextEntry.chain("emeralds");
    public static final TextEntry COIN_CHAIN_EMERALDS_DISPLAY = TextEntry.chainDisplay("emeralds");
    public static final TextEntry COIN_CHAIN_EMERALDS_DISPLAY_WORDY = TextEntry.chainDisplayWordy("emeralds");


    //Generic Text
    public static final TextEntry BUTTON_CHANGE_NAME_ICON = TextEntry.button(MODID,"change_name");
    public static final TextEntry GUI_NAME = TextEntry.gui(MODID,"customname");
    public static final TextEntry TOOLTIP_WARNING_CANT_BE_UNDONE = TextEntry.tooltip(MODID,"warning.cannot_be_undone");
    public static final TextEntry TOOLTIP_ITEM_COUNT = TextEntry.tooltip(MODID,"item.count");
    public static final TextEntry BUTTON_ADD = TextEntry.button(MODID,"add");
    public static final TextEntry BUTTON_REMOVE = TextEntry.button(MODID,"remove");
    public static final TextEntry BUTTON_SET = TextEntry.button(MODID,"set");
    public static final TextEntry BUTTON_CLEAR_MEMORY = TextEntry.button(MODID,"clear_memory");
    public static final TextEntry MISC_GENERIC_PLURAL = new TextEntry("item.lightmanscurrency.generic.plural");
    public static final TextEntry GUI_SEPERATOR = TextEntry.gui(MODID,"trader.title.seperator");
    public static final TextEntry GUI_ADDED = TextEntry.gui(MODID,"added");
    public static final TextEntry GUI_REMOVED = TextEntry.gui(MODID,"removed");
    public static final TextEntry GUI_TO = TextEntry.gui(MODID,"to");
    public static final TextEntry GUI_FROM = TextEntry.gui(MODID,"from");
    public static final TextEntry GUI_AND = TextEntry.gui(MODID,"and");

    //Time Text
    public static final TimeUnitTextEntry TIME_UNIT_DAY = TimeUnitTextEntry.of("day");
    public static final TimeUnitTextEntry TIME_UNIT_HOUR = TimeUnitTextEntry.of("hour");
    public static final TimeUnitTextEntry TIME_UNIT_MINUTE = TimeUnitTextEntry.of("minute");
    public static final TimeUnitTextEntry TIME_UNIT_SECOND = TimeUnitTextEntry.of("second");
    public static final TimeUnitTextEntry TIME_UNIT_MILLISECOND = TimeUnitTextEntry.of("millisecond");
    public static final TimeUnitTextEntry TIME_UNIT_TICK = TimeUnitTextEntry.of("tick");

    //Notifications
    public static final TextEntry NOTIFICATION_FORMAT_GENERAL = new TextEntry("notifications.format.general");
    public static final TextEntry NOTIFICATION_FORMAT_CHAT = new TextEntry("notifications.format.chat");
    public static final TextEntry NOTIFICATION_FORMAT_CHAT_TITLE = new TextEntry("notifications.format.chat.title");
    public static final TextEntry NOTIFICATION_TIMESTAMP = new TextEntry("notifications.timestamp");
    public static final TextEntry NOTIFICATION_SOURCE_GENERAL = new TextEntry("notifications.source.general");
    public static final TextEntry TOOLTIP_NOTIFICATION_DELETE = TextEntry.tooltip(MODID,"notifications.delete");
    //Auction Notifications
    public static final TextEntry NOTIFICATION_AUCTION_BID = TextEntry.notification(AuctionHouseBidNotification.TYPE);
    public static final TextEntry NOTIFICATION_AUCTION_BUYER = TextEntry.notification(AuctionHouseBuyerNotification.TYPE);
    public static final TextEntry NOTIFICATION_AUCTION_CANCEL = TextEntry.notification(AuctionHouseCancelNotification.TYPE);
    public static final TextEntry NOTIFICATION_AUCTION_SELLER_NO_BID = TextEntry.notification(AuctionHouseSellerNobidNotification.TYPE);
    public static final TextEntry NOTIFICATION_AUCTION_SELLER = TextEntry.notification(AuctionHouseSellerNotification.TYPE);
    //Bank Notifications
    public static final TextEntry NOTIFICATION_BANK_INTEREST = TextEntry.notification(BankInterestNotification.TYPE);
    public static final TextEntry NOTIFICATION_BANK_TRANSFER = TextEntry.notification(BankTransferNotification.TYPE);
    public static final TextEntry NOTIFICATION_BANK_DEPOSIT_WITHDRAW = TextEntry.notification(VersionUtil.lcResource("bank_deposit_or_withdraw"));
    public static final TextEntry NOTIFICATION_BANK_DEPOSIT = TextEntry.notification(VersionUtil.lcResource("bank_deposit"));
    public static final TextEntry NOTIFICATION_BANK_WITHDRAW = TextEntry.notification(VersionUtil.lcResource("bank_withdraw"));
    public static final TextEntry NOTIFICATION_BANK_DEPOSIT_WITHDRAW_SERVER = TextEntry.notification(VersionUtil.lcResource("bank_deposit_or_withdraw"),"server");
    public static final TextEntry NOTIFICATION_BANK_LOW_BALANCE = TextEntry.notification(LowBalanceNotification.TYPE);
    //Ejection Notifications
    public static final TextEntry NOTIFICATION_EJECTION_ANARCHY = TextEntry.notification(OwnableBlockEjectedNotification.TYPE,"anarchy");
    public static final TextEntry NOTIFICATION_EJECTION_EJECTED = TextEntry.notification(OwnableBlockEjectedNotification.TYPE,"ejected");
    public static final TextEntry NOTIFICATION_EJECTION_DROPPED = TextEntry.notification(OwnableBlockEjectedNotification.TYPE,"dropped");
    //Settings Notification
    public static final TextEntry NOTIFICATION_SETTINGS_ADD_REMOVE_ALLY = TextEntry.notification(AddRemoveAllyNotification.TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_ADD_REMOVE_TRADE = TextEntry.notification(AddRemoveTradeNotification.TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS = TextEntry.notification(ChangeAllyPermissionNotification.TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS_SIMPLE = TextEntry.notification(ChangeAllyPermissionNotification.TYPE,"simple");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_CREATIVE = TextEntry.notification(ChangeCreativeNotification.TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_CREATIVE_ENABLED = TextEntry.notification(ChangeCreativeNotification.TYPE,"enabled");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_CREATIVE_DISABLED = TextEntry.notification(ChangeCreativeNotification.TYPE,"disabled");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_NAME = TextEntry.notification(ChangeNameNotification.TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_NAME_SET = TextEntry.notification(ChangeNameNotification.TYPE,"set");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_NAME_RESET = TextEntry.notification(ChangeNameNotification.TYPE,"reset");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_OWNER_PASSED = TextEntry.notification(ChangeOwnerNotification.TYPE,"passed");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_OWNER_TAKEN = TextEntry.notification(ChangeOwnerNotification.TYPE,"taken");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_OWNER_TRANSFERRED = TextEntry.notification(ChangeOwnerNotification.TYPE,"transferred");
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_SIMPLE = TextEntry.notification(ChangeSettingNotification.SIMPLE_TYPE);
    public static final TextEntry NOTIFICATION_SETTINGS_CHANGE_ADVANCED = TextEntry.notification(ChangeSettingNotification.ADVANCED_TYPE);
    //Tax Notifications
    public static final TextEntry NOTIFICATION_TAXES_COLLECTED = TextEntry.notification(TaxesCollectedNotification.TYPE);
    public static final TextEntry NOTIFICATION_TAXES_PAID = TextEntry.notification(TaxesPaidNotification.TYPE);
    public static final TextEntry NOTIFICATION_TAXES_PAID_NULL = TextEntry.notification(TaxesPaidNotification.TYPE,"null");
    //Trader Notifications
    public static final TextEntry NOTIFICATION_TRADER_OUT_OF_STOCK = TextEntry.notification(OutOfStockNotification.TYPE);
    public static final TextEntry NOTIFICATION_TRADER_OUT_OF_STOCK_INDEXLESS = TextEntry.notification(OutOfStockNotification.TYPE,"indexless");
    public static final TextEntry NOTIFICATION_TRADE_ITEM = TextEntry.notification(ItemTradeNotification.TYPE);
    public static final TextEntry NOTIFICATION_TRADE_PAYGATE_TICKET = TextEntry.notification(PaygateNotification.TYPE,"ticket");
    public static final TextEntry NOTIFICATION_TRADE_PAYGATE_PASS = TextEntry.notification(PaygateNotification.TYPE,"pass");
    public static final TextEntry NOTIFICATION_TRADE_PAYGATE_MONEY = TextEntry.notification(PaygateNotification.TYPE,"money");
    public static final TextEntry NOTIFICATION_TRADE_SLOT_MACHINE = TextEntry.notification(SlotMachineTradeNotification.TYPE);
    //Item Notification Parts
    public static final TextEntry NOTIFICATION_ITEM_FORMAT = TextEntry.notification(VersionUtil.lcResource("items"),"format");

    //Command Arguments
    public static final TextEntry ARGUMENT_MONEY_VALUE_NOT_A_COIN = TextEntry.argument("money_value.not_a_coin");
    public static final TextEntry ARGUMENT_MONEY_VALUE_NO_VALUE = TextEntry.argument("money_value.no_value");
    public static final TextEntry ARGUMENT_MONEY_VALUE_NOT_EMPTY_OR_FREE = TextEntry.argument("money_value.not_free_or_empty");
    public static final TextEntry ARGUMENT_COLOR_INVALID = TextEntry.argument("color.invalid");
    public static final TextEntry ARGUMENT_TRADEID_INVALID = TextEntry.argument("tradeid.invalid");
    public static final TextEntry ARGUMENT_TRADER_NOT_FOUND = TextEntry.argument("trader.not_found");
    public static final TextEntry ARGUMENT_TRADER_NOT_RECOVERABLE = TextEntry.argument("trader.not_recoverable");

    //Commands
    public static final TextEntry COMMAND_BALTOP_NO_RESULTS = TextEntry.command(MODID,"lcbaltop.no_results");
    public static final TextEntry COMMAND_BALTOP_ERROR_PAGE = TextEntry.command(MODID,"lcbaltop.error.page");
    public static final TextEntry COMMAND_BALTOP_TITLE = TextEntry.command(MODID,"lcbaltop.title");
    public static final TextEntry COMMAND_BALTOP_PAGE = TextEntry.command(MODID,"lcbaltop.page");
    public static final TextEntry COMMAND_BALTOP_ENTRY = TextEntry.command(MODID,"lcbaltop.entry");

    public static final TextEntry COMMAND_BANK_TEAM_NULL = TextEntry.command(MODID,"lcbank.team.null");
    public static final TextEntry COMMAND_BANK_TEAM_NO_BANK = TextEntry.command(MODID,"lcbank.team.no_bank");
    public static final TextEntry COMMAND_BANK_GIVE_FAIL = TextEntry.command(MODID,"lcbank.give.fail");
    public static final TextEntry COMMAND_BANK_GIVE_SUCCESS = TextEntry.command(MODID,"lcbank.give.success");
    public static final TextEntry COMMAND_BANK_GIVE_SUCCESS_SINGLE = TextEntry.command(MODID,"lcbank.give.success.single");
    public static final TextEntry COMMAND_BANK_TAKE_FAIL = TextEntry.command(MODID,"lcbank.take.fail");
    public static final TextEntry COMMAND_BANK_TAKE_SUCCESS = TextEntry.command(MODID,"lcbank.take.success");
    public static final TextEntry COMMAND_BANK_TAKE_SUCCESS_SINGLE = TextEntry.command(MODID,"lcbank.take.success.single");
    public static final TextEntry COMMAND_BANK_DELETE_PLAYER_RESET = TextEntry.command(MODID,"lcbank.delete.player.reset");
    public static final TextEntry COMMAND_BANK_DELETE_PLAYER_SUCCESS = TextEntry.command(MODID,"lcbank.delete.player.success");
    public static final TextEntry COMMAND_BANK_DELETE_PLAYER_DOESNT_EXIST = TextEntry.command(MODID,"lcbank.delete.player.doesnt_exist");
    public static final TextEntry COMMAND_BANK_DELETE_PLAYER_INVALID_INPUT = TextEntry.command(MODID,"lcbank.delete.player.invalid_input");

    public static final TextEntry COMMAND_CONFIG_RELOAD = TextEntry.command(MODID,"lcconfig.reload");
    public static final TextEntry COMMAND_CONFIG_EDIT_SUCCESS = TextEntry.command(MODID,"lcconfig.edit.success");
    public static final TextEntry COMMAND_CONFIG_EDIT_FAIL_PARSE = TextEntry.command(MODID,"lcconfig.edit.fail.parse");
    public static final TextEntry COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS = TextEntry.command(MODID,"lcconfig.edit.list.remove.success");
    public static final TextEntry COMMAND_CONFIG_VIEW = TextEntry.command(MODID,"lcconfig.view");
    public static final TextEntry COMMAND_CONFIG_FAIL_MISSING = TextEntry.command(MODID,"lcconfig.fail.missing");

    public static final TextEntry COMMAND_ADMIN_TOGGLE_ADMIN = TextEntry.command(MODID,"lcadmin.toggleadmin");
    public static final TextEntry COMMAND_ADMIN_TOGGLE_ADMIN_ENABLED = TextEntry.command(MODID,"lcadmin.toggleadmin.enabled");
    public static final TextEntry COMMAND_ADMIN_TOGGLE_ADMIN_DISABLED = TextEntry.command(MODID,"lcadmin.toggleadmin.disabled");
    public static final TextEntry COMMAND_ADMIN_PREPARE_FOR_STRUCTURE_ERROR = TextEntry.command(MODID,"lcadmin.prepareForStructure.error");
    public static final TextEntry COMMAND_ADMIN_PREPARE_FOR_STRUCTURE_SUCCESS = TextEntry.command(MODID,"lcadmin.prepareForStructure.success");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_TITLE = TextEntry.command(MODID,"lcadmin.traderdata.list.title");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_NONE = TextEntry.command(MODID,"lcadmin.traderdata.list.none");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_TRADER_ID = TextEntry.command(MODID,"lcadmin.traderdata.list.trader_id");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_TRADER_ID_TOOLTIP = TextEntry.command(MODID,"lcadmin.traderdata.list.trader_id.tooltip");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_PERSISTENT_ID = TextEntry.command(MODID,"lcadmin.traderdata.list.persistent_id");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_TYPE = TextEntry.command(MODID,"lcadmin.traderdata.list.type");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_STATE = TextEntry.command(MODID,"lcadmin.traderdata.list.state");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_DIMENSION = TextEntry.command(MODID,"lcadmin.traderdata.list.dimension");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_POSITION = TextEntry.command(MODID,"lcadmin.traderdata.list.position");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_POSITION_TOOLTIP = TextEntry.command(MODID,"lcadmin.traderdata.list.position.tooltip");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_LIST_NAME = TextEntry.command(MODID,"lcadmin.traderdata.list.name");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_SEARCH_NONE = TextEntry.command(MODID,"lcadmin.traderdata.search.none");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_DELETE_SUCCESS = TextEntry.command(MODID,"lcadmin.traderdata.delete.success");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_RECOVER_SUCCESS = TextEntry.command(MODID,"lcadmin.traderdata.recover.success");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_RECOVER_FAIL_NO_ITEM = TextEntry.command(MODID,"lcadmin.traderdata.recover.fail.no_item_data");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_ADD_TO_WHITELIST_SUCCESS = TextEntry.command(MODID,"lcadmin.traderdata.addToWhitelist.success");
    public static final TextEntry COMMAND_ADMIN_TRADERDATA_ADD_TO_WHITELIST_MISSING = TextEntry.command(MODID,"lcadmin.traderdata.addToWhitelist.missing");
    public static final TextEntry COMMAND_ADMIN_REPLACE_WALLET_NOT_A_WALLET = TextEntry.command(MODID,"lcadmin.replaceWallet.not_a_wallet");
    public static final TextEntry COMMAND_ADMIN_TAXES_OPEN_SERVER_TAX_ERROR = TextEntry.command(MODID,"lcadmin.taxes.openServerTax.error");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_TITLE = TextEntry.command(MODID,"lcadmin.taxes.list.title");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_ID = TextEntry.command(MODID,"lcadmin.taxes.list.id");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_ID_TOOLTIP = TextEntry.command(MODID,"lcadmin.taxes.list.id.tooltip");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_DIMENSION = TextEntry.command(MODID,"lcadmin.taxes.list.dimension");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_POSITION = TextEntry.command(MODID,"lcadmin.taxes.list.position");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_POSITION_TOOLTIP = TextEntry.command(MODID,"lcadmin.taxes.list.position.tooltip");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_INFINITE_RANGE = TextEntry.command(MODID,"lcadmin.taxes.list.infinite_range");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_RADIUS = TextEntry.command(MODID,"lcadmin.taxes.list.radius");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_HEIGHT = TextEntry.command(MODID,"lcadmin.taxes.list.height");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_OFFSET = TextEntry.command(MODID,"lcadmin.taxes.list.offset");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_FORCE_ACCEPTANCE = TextEntry.command(MODID,"lcadmin.taxes.list.force_acceptance");
    public static final TextEntry COMMAND_ADMIN_TAXES_LIST_NAME = TextEntry.command(MODID,"lcadmin.taxes.list.name");
    public static final TextEntry COMMAND_ADMIN_TAXES_DELETE_SUCCESS = TextEntry.command(MODID,"lcadmin.taxes.delete.success");
    public static final TextEntry COMMAND_ADMIN_TAXES_DELETE_FAIL = TextEntry.command(MODID,"lcadmin.taxes.delete.fail");
    public static final TextEntry COMMAND_ADMIN_TAXES_FORCE_DISABLE_SUCCESS = TextEntry.command(MODID,"lcadmin.taxes.forceDisableTaxCollectors.success");
    public static final TextEntry COMMAND_ADMIN_TAXES_FORCE_DISABLE_FAIL = TextEntry.command(MODID,"lcadmin.taxes.forceDisableTaxCollectors.fail");
    public static final TextEntry COMMAND_ADMIN_EVENT_LIST_NONE = TextEntry.command(MODID,"lcadmin.event.list.none");
    public static final TextEntry COMMAND_ADMIN_EVENT_UNLOCK_SUCCESS = TextEntry.command(MODID,"lcadmin.event.unlock.success");
    public static final TextEntry COMMAND_ADMIN_EVENT_UNLOCK_FAIL = TextEntry.command(MODID,"lcadmin.event.unlock.fail");
    public static final TextEntry COMMAND_ADMIN_EVENT_LOCK_SUCCESS = TextEntry.command(MODID,"lcadmin.event.lock.success");
    public static final TextEntry COMMAND_ADMIN_EVENT_LOCK_FAIL = TextEntry.command(MODID,"lcadmin.event.lock.fail");
    public static final TextEntry COMMAND_ADMIN_PREPAID_CARD_SUCCESS = TextEntry.command(MODID,"lcadmin.makePrepaidCard.success");
    public static final TextEntry COMMAND_ADMIN_PREPAID_CARD_FAIL = TextEntry.command(MODID,"lcadmin.makePrepaidCard.fail");

    public static final TextEntry COMMAND_LCADMIN_DATA_OWNER_PLAYER = TextEntry.command(MODID,"lcadmin.data.list.owner.player");
    public static final TextEntry COMMAND_LCADMIN_DATA_OWNER_TEAM = TextEntry.command(MODID,"lcadmin.data.list.owner.team");
    public static final TextEntry COMMAND_LCADMIN_DATA_OWNER_CUSTOM = TextEntry.command(MODID,"lcadmin.data.list.owner.custom");

    public static final TextEntry COMMAND_TRADE_SELF = TextEntry.command(MODID,"lctrade.self");
    public static final TextEntry COMMAND_TRADE_HOST_NOTIFY = TextEntry.command(MODID,"lctrade.host.notify");
    public static final TextEntry COMMAND_TRADE_GUEST_NOTIFY = TextEntry.command(MODID,"lctrade.guest.notify");
    public static final TextEntry COMMAND_TRADE_GUEST_NOTIFY_PROMPT = TextEntry.command(MODID,"lctrade.guest.notify.prompt");
    public static final TextEntry COMMAND_TRADE_ACCEPT_FAIL_OFFLINE = TextEntry.command(MODID,"lctradeaccept.fail.offline");
    public static final TextEntry COMMAND_TRADE_ACCEPT_FAIL_DISTANCE = TextEntry.command(MODID,"lctradeaccept.fail.distance");
    public static final TextEntry COMMAND_TRADE_ACCEPT_FAIL_DIMENSION = TextEntry.command(MODID,"lctradeaccept.fail.dimension");
    public static final TextEntry COMMAND_TRADE_ACCEPT_ERROR = TextEntry.command(MODID,"lctradeaccept.error");
    public static final TextEntry COMMAND_TRADE_ACCEPT_NOT_FOUND = TextEntry.command(MODID,"lctradeaccept.not_found");

    public static final TextEntry COMMAND_TICKETS_COLOR_NOT_HELD = TextEntry.command(MODID,"ticket.color.not_held");

    public static final TextEntry COMMAND_CLAIM_FAIL_NO_DATA = TextEntry.command(MODID,"lcclaims.fail.no_data");
    public static final TextEntry COMMAND_CLAIM_FAIL_INVALID_PRICE = TextEntry.command(MODID,"lcclaims.fail.invalid_price");
    public static final TextEntry COMMAND_CLAIM_INVALID = TextEntry.command(MODID,"lcclaims.invalid");
    public static final TextEntry COMMAND_CLAIM_INFO_CLAIMS = TextEntry.command(MODID,"lcclaims.info.claims");
    public static final TextEntry COMMAND_CLAIM_INFO_FORCELOAD = TextEntry.command(MODID,"lcclaims.info.forceload");
    public static final TextEntry COMMAND_CLAIM_INFO_PRICE = TextEntry.command(MODID,"lcclaims.info.price");
    public static final TextEntry COMMAND_CLAIM_INFO_DISABLED = TextEntry.command(MODID,"lcclaims.info.disabled");
    public static final TextEntry COMMAND_CLAIM_BUY_CLAIM_DISABLED = TextEntry.command(MODID,"lcclaims.buy.claim.disabled");
    public static final TextEntry COMMAND_CLAIM_BUY_CLAIM_LIMIT_REACHED = TextEntry.command(MODID,"lcclaims.buy.claim.limit_reached");
    public static final TextEntry COMMAND_CLAIM_BUY_CLAIM_SUCCESS = TextEntry.command(MODID,"lcclaims.buy.claim.success");
    public static final TextEntry COMMAND_CLAIM_BUY_CLAIM_CANNOT_AFFORD = TextEntry.command(MODID,"lcclaims.buy.claim.cannot_afford");
    public static final TextEntry COMMAND_CLAIM_BUY_FORCELOAD_DISABLED = TextEntry.command(MODID,"lcclaims.buy.forceload.disabled");
    public static final TextEntry COMMAND_CLAIM_BUY_FORCELOAD_LIMIT_REACHED = TextEntry.command(MODID,"lcclaims.buy.forceload.limit_reached");
    public static final TextEntry COMMAND_CLAIM_BUY_FORCELOAD_SUCCESS = TextEntry.command(MODID,"lcclaims.buy.forceload.success");
    public static final TextEntry COMMAND_CLAIM_BUY_FORCELOAD_CANNOT_AFFORD = TextEntry.command(MODID,"lcclaims.buy.forceload.cannot_afford");

    //Advancements
    public static final AdvancementTextEntry ADVANCEMENT_ROOT = AdvancementTextEntry.of("lightmanscurrency.root");
    public static final AdvancementTextEntry ADVANCEMENT_MY_FIRST_PENNY = AdvancementTextEntry.of("lightmanscurrency.myfirstpenny");
    public static final AdvancementTextEntry ADVANCEMENT_TRADING_CORE = AdvancementTextEntry.of("lightmanscurrency.trading_core");
    public static final AdvancementTextEntry ADVANCEMENT_TRADER = AdvancementTextEntry.of("lightmanscurrency.trader");
    public static final AdvancementTextEntry ADVANCEMENT_SPECIALTY_TRADER = AdvancementTextEntry.of("lightmanscurrency.specialty_trader");
    public static final AdvancementTextEntry ADVANCEMENT_NETWORK_TRADER = AdvancementTextEntry.of("lightmanscurrency.network_trader");
    public static final AdvancementTextEntry ADVANCEMENT_TRADER_INTERFACE = AdvancementTextEntry.of("lightmanscurrency.trader_interface");
    public static final AdvancementTextEntry ADVANCEMENT_TERMINAL = AdvancementTextEntry.of("lightmanscurrency.terminal");
    public static final AdvancementTextEntry ADVANCEMENT_ATM = AdvancementTextEntry.of("lightmanscurrency.atm");
    public static final AdvancementTextEntry ADVANCEMENT_BANKER_TRADE = AdvancementTextEntry.of("lightmanscurrency.banker_trade");
    public static final AdvancementTextEntry ADVANCEMENT_COIN_MINT = AdvancementTextEntry.of("lightmanscurrency.coin_mint");
    public static final AdvancementTextEntry ADVANCEMENT_WALLET_CRAFTING = AdvancementTextEntry.of("lightmanscurrency.wallet_crafting");
    public static final AdvancementTextEntry ADVANCEMENT_NETHERITE_WALLET = AdvancementTextEntry.of("lightmanscurrency.netherite_wallet");
    public static final AdvancementTextEntry ADVANCEMENT_NETHER_STAR_WALLET = AdvancementTextEntry.of("lightmanscurrency.nether_star_wallet");
    public static final AdvancementTextEntry ADVANCEMENT_ENCHANTED_WALLET = AdvancementTextEntry.of("lightmanscurrency.enchanted_wallet");
    public static final AdvancementTextEntry ADVANCEMENT_CASH_REGISTER = AdvancementTextEntry.of("lightmanscurrency.cash_register");
    public static final AdvancementTextEntry ADVANCEMENT_CASHIER_TRADE = AdvancementTextEntry.of("lightmanscurrency.cashier_trade");
    public static final AdvancementTextEntry ADVANCEMENT_JAR_OF_SUS = AdvancementTextEntry.of("lightmanscurrency.jar_of_sus");

    public static final AdvancementTextEntry ADVANCEMENT_EVENT_CHOCOLATE = AdvancementTextEntry.of("lightmanscurrency.event.chocolate");
    public static final AdvancementTextEntry ADVANCEMENT_EVENT_CHRISTMAS = AdvancementTextEntry.of("lightmanscurrency.event.christmas");
    public static final AdvancementTextEntry ADVANCEMENT_EVENT_VALENTINES = AdvancementTextEntry.of("lightmanscurrency.event.valentines");

    //Resource Pack Names
    public static final TextEntry RESOURCE_PACK_RUPEES = TextEntry.resourcePack(MODID,"rupees");
    public static final TextEntry RESOURCE_PACK_CLOSER_ITEMS = TextEntry.resourcePack(MODID,"closer_items");
    public static final TextEntry RESOURCE_PACK_LEGACY_COINS = TextEntry.resourcePack(MODID,"legacy_coins");

    ///OTHER MODS

    //JEI Info
    public static final TextEntry JEI_INFO_TICKET_STUB = TextEntry.jeiInfo(MODID,"ticket_stub");
    //Curios
    public static final TextEntry CURIOS_SLOT_WALLET = TextEntry.curiosSlot("wallet");

    //REI Groups
    public static final TextEntry REI_GROUP_SHELF = TextEntry.reiGroup(MODID,"shelf");
    public static final TextEntry REI_GROUP_SHELF_2x2 = TextEntry.reiGroup(MODID,"shelf_2x2");
    public static final TextEntryBundle<WoodType> REI_GROUP_CARD_DISPLAY = TextEntryBundle.of(WoodType.validValues(),"rei.lightmanscurrency.group.card_display",WoodType::translationSegment);
    public static final TextEntry REI_GROUP_VENDING_MACHINE = TextEntry.reiGroup(MODID,"vending_machine");
    public static final TextEntry REI_GROUP_LARGE_VENDING_MACHINE = TextEntry.reiGroup(MODID,"large_vending_machine");
    public static final TextEntry REI_GROUP_FREEZER = TextEntry.reiGroup(MODID,"freezer");
    public static final TextEntry REI_GROUP_BOOKSHELF_TRADER = TextEntry.reiGroup(MODID,"bookshelf_trader");
    public static final TextEntry REI_GROUP_AUCTION_STAND = TextEntry.reiGroup(MODID,"auction_stand");
    public static final TextEntry REI_GROUP_JAR_OF_SUS = TextEntry.reiGroup(MODID,"jar_of_sus");
    public static final TextEntry REI_GROUP_ATM_CARD = TextEntry.reiGroup(MODID,"atm_card");

}