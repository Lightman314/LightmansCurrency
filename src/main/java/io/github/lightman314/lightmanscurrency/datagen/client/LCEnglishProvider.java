package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInteractionNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInterestNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankTransferNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.CoinDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValueParser;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.BonusTooltip;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyStats;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.impl.EmptyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.category.SingletonNotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.builtin.TimestampStatType;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatViewer;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.api.text.*;
import io.github.lightman314.lightmanscurrency.api.trader.TraderStats;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.CopiedTrader;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.EnumPermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.TriStatePermission;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.FreeSample;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.PlayerTradeLimit;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.NormalCustomerTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.builtin.NetworkTraderSelectionTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.client.features.resources.BuiltInResourcePacks;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.*;
import io.github.lightman314.lightmanscurrency.features.chocolate_coins.ChocolateCoinItem;
import io.github.lightman314.lightmanscurrency.features.colors.ColorDisplay;
import io.github.lightman314.lightmanscurrency.features.commands.LCAdminCommand;
import io.github.lightman314.lightmanscurrency.features.commands.LCConfigCommand;
import io.github.lightman314.lightmanscurrency.features.enchantments.LCEnchantments;
import io.github.lightman314.lightmanscurrency.features.enchantments.MoneyMendingEnchantmentHelper;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.notifications.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.AbstractWalletMenu;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class LCEnglishProvider extends LanguageProvider {

    public LCEnglishProvider(PackOutput output) { this(output, LCApi.MODID,"en_us"); }
    protected LCEnglishProvider(PackOutput output, String modid, String locale) { super(output, modid, locale); }

    @Override
    protected void addTranslations() {

        //Misc
        this.text(LCText.GENERIC_PLURAL,"%ss");
        this.text(LCText.GENERIC_AND,"%1$s and %2$s");
        this.text(LCText.GENERIC_CUSTOM_NAME,"Name:");
        this.text(ColorDisplay.TOOLTIP_COLORED_ITEM,"Color: %s");
        this.text(LCText.TOOLTIP_ITEM_COUNT,"Count: %1$s/%2$s");
        this.text(LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(true),"true");
        this.text(LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(false),"false");

        //Time
        this.timeText(TimeUnit.ENTRIES.get(TimeUnit.DAY)," day"," days","d");
        this.timeText(TimeUnit.ENTRIES.get(TimeUnit.HOUR)," hour"," hours","h");
        this.timeText(TimeUnit.ENTRIES.get(TimeUnit.MINUTE)," minute"," minutes","m");
        this.timeText(TimeUnit.ENTRIES.get(TimeUnit.SECOND)," second"," seconds","s");
        this.timeText(TimeUnit.MILLISECOND," millisecond"," milliseconds","ms");
        this.timeText(TimeUnit.TICK," tick"," ticks","t");

        //Built-in Resource Packs
        this.text(BuiltInResourcePacks.CLOSER_ITEMS_PACK,"LC Fancy Item Placement","Draws Item Trader stock items closer together in-world.");
        this.text(BuiltInResourcePacks.FANCY_ICONS_PACK,"LC Fancy Icons","Replaces coins initials with fancy icons in menus/tooltips");
        this.text(BuiltInResourcePacks.LEGACY_COINS_PACK,"LC Legacy Coins","Replaces coins with the original textures");
        this.text(BuiltInResourcePacks.RUPEES_PACK,"LC Rupees","Replaces the LC coins with rupees");

        //Creative Tabs
        this.text(LCText.Resources.CREATIVE_GROUP_COINS,"Coins & Items");
        this.text(LCText.Resources.CREATIVE_GROUP_MACHINES,"Misc Machines");
        this.text(LCText.Resources.CREATIVE_GROUP_TRADERS,"Trading Machines");
        this.text(LCText.Resources.CREATIVE_GROUP_UPGRADES,"Machine Upgrades");

        //Coins
        this.itemWithPluralAndInitial(LCItems.COIN_COPPER,"Copper Coin");
        this.itemWithPluralAndInitial(LCItems.COIN_IRON,"Iron Coin");
        this.itemWithPluralAndInitial(LCItems.COIN_GOLD,"Gold Coin");
        this.itemWithPluralAndInitial(LCItems.COIN_EMERALD,"Emerald Coin");
        this.itemWithPluralAndInitial(LCItems.COIN_DIAMOND,"Diamond Coin");
        this.itemWithPluralAndInitial(LCItems.COIN_NETHERITE,"Netherite Coin");

        //Chocolate Coins
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_COPPER,"Chocolate Copper Coin");
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_IRON,"Chocolate Iron Coin");
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_GOLD,"Chocolate Gold Coin");
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_EMERALD,"Chocolate Emerald Coin");
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_DIAMOND,"Chocolate Diamond Coin");
        this.itemWithPlural(LCItems.COIN_CHOCOLATE_NETHERITE,"Chocolate Netherite Coin");

        //Wallets
        this.item(LCItems.WALLET_COPPER,"Copper Wallet");
        this.item(LCItems.WALLET_IRON,"Iron Wallet");
        this.item(LCItems.WALLET_GOLD,"Gold Wallet");
        this.item(LCItems.WALLET_EMERALD,"Emerald Wallet");
        this.item(LCItems.WALLET_DIAMOND,"Diamond Wallet");
        this.item(LCItems.WALLET_NETHERITE,"Netherite Wallet");
        this.item(LCItems.WALLET_NETHER_STAR,"Nether Star Wallet");
        this.item(LCItems.WALLET_ENDER_DRAGON,"Ender Dragon Wallet");

        //Trading Core
        this.item(LCItems.TRADING_CORE,"Trading Mechanism");
        this.item(LCItems.UPGRADE_SMITHING_TEMPLATE,"Trading Upgrades");

        //Upgrades
        this.item(LCItems.ITEM_CAPACITY_UPGRADE_1,"Item Capacity Upgrade (Iron)");
        this.item(LCItems.ITEM_CAPACITY_UPGRADE_2,"Item Capacity Upgrade (Gold)");
        this.item(LCItems.ITEM_CAPACITY_UPGRADE_3,"Item Capacity Upgrade (Diamond)");
        this.item(LCItems.ITEM_CAPACITY_UPGRADE_4,"Item Capacity Upgrade (Netherite)");
        this.item(LCItems.NETWORK_UPGRADE,"Network Upgrade");

        //Coin Piles
        this.blockWithPlural(LCBlocks.COIN_PILE_COPPER,"Pile of Copper Coins","Piles of Copper Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_IRON,"Pile of Iron Coins","Piles of Iron Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_GOLD,"Pile of Gold Coins","Piles of Gold Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_EMERALD,"Pile of Emerald Coins","Piles of Emerald Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_DIAMOND,"Pile of Diamond Coins","Piles of Diamond Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_NETHERITE,"Pile of Netherite Coins","Piles of Netherite Coins");

        //Coin Blocks
        this.blockWithPlural(LCBlocks.COIN_BLOCK_COPPER,"Block of Copper Coins","Blocks of Copper Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_IRON,"Block of Iron Coins","Blocks of Iron Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_GOLD,"Block of Gold Coins","Blocks of Gold Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_EMERALD,"Block of Emerald Coins","Blocks of Emerald Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_DIAMOND,"Block of Diamond Coins","Blocks of Diamond Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_NETHERITE,"Block of Netherite Coins","Blocks of Netherite Coins");

        //Chocolate Coin Piles
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_COPPER,"Pile of Chocolate Copper Coins","Piles of Chocolate Copper Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_IRON,"Pile of Chocolate Iron Coins","Piles of Chocolate Iron Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_GOLD,"Pile of Chocolate Gold Coins","Piles of Chocolate Gold Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_EMERALD,"Pile of Chocolate Emerald Coins","Piles of Chocolate Emerald Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND,"Pile of Chocolate Diamond Coins","Piles of Chocolate Diamond Coins");
        this.blockWithPlural(LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE,"Pile of Chocolate Netherite Coins","Piles of Chocolate Netherite Coins");

        //Chocolate Coin Blocks
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER,"Block of Chocolate Copper Coins","Blocks of Chocolate Copper Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_IRON,"Block of Chocolate Iron Coins","Blocks of Chocolate Iron Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD,"Block of Chocolate Gold Coins","Blocks of Chocolate Gold Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD,"Block of Chocolate Emerald Coins","Blocks of Chocolate Emerald Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND,"Block of Chocolate Diamond Coins","Blocks of Chocolate Diamond Coins");
        this.blockWithPlural(LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE,"Block of Chocolate Netherite Coins","Blocks of Chocolate Netherite Coins");

        //Machines
        this.block(LCBlocks.COIN_MINT,"Coin Mint");
        this.block(LCBlocks.ATM,"ATM");
        this.item(LCItems.ATM_PORTABLE,"ATM");
        this.block(LCBlocks.TRADING_TERMINAL,"Trading Network Access Terminal");
        this.item(LCItems.TRADING_TERMINAL_PORTABLE,"Trading Network Access Terminal");

        //Display Cases
        this.coloredBlock(LCBlocks.DISPLAY_CASE,color -> color + " Display Case");
        //Small Shelf
        this.woodenBlock(LCBlocks.SINGLE_SHELF,wood -> wood + " Shelf Trader");
        //Large Shelf
        this.woodenBlock(LCBlocks.DOUBLE_SHELF,wood -> "Double " + wood + " Shelf Trader");
        //Card Display
        this.woodenBlock(LCBlocks.CARD_DISPLAY,wood -> wood + " Card Display");
        //Vending Machine
        this.coloredBlock(LCBlocks.VENDING_MACHINE,color -> color + " Vending Machine");
        //Large Vending Machine
        this.coloredBlock(LCBlocks.LARGE_VENDING_MACHINE,color -> "Large " + color + " Vending Machine");
        //Item Network Traders
        this.block(LCBlocks.ITEM_NETWORK_TRADER.getHolder(1),"Item Network Trader T1");
        this.block(LCBlocks.ITEM_NETWORK_TRADER.getHolder(2),"Item Network Trader T2");
        this.block(LCBlocks.ITEM_NETWORK_TRADER.getHolder(3),"Item Network Trader T3");
        this.block(LCBlocks.ITEM_NETWORK_TRADER.getHolder(4),"Item Network Trader T4");

        //Enchantments
        this.enchantment(LCEnchantments.COIN_MAGNET,"Coin Magnet","Lets your wallet collect coins from a larger range.");
        this.enchantment(LCEnchantments.MONEY_MENDING,"Money Mending","Repairs the item using money from your equipped wallet.");

        //Key Binds
        this.text(LCText.Resources.KEY_WALLET,"Open Wallet");

        //Wallet Tooltips
        this.text(WalletItem.MESSAGE_WALLET_NONE_EQUIPPED,"No wallet equipped to your wallet slot. Cannot open wallet.");
        this.text(WalletItem.TOOLTIP_WALLET_KEY_BIND,"Press [%s] while equipped to access your wallet");
        this.text(WalletItem.TOOLTIP_WALLET_CAPACITY,"Has %s coin slots");
        this.text(WalletItem.TOOLTIP_WALLET_UPGRADEABLE,"Use a [%1$s] on this in your inventory to increase the wallets capacity by %2$s","Can by upgraded %3$s more times");
        this.text(WalletItem.TOOLTIP_WALLET_FULLY_UPGRADED,"Wallet's capacity has been fully upgraded");
        this.text(WalletItem.TOOLTIP_WALLET_STORED_MONEY,"Contains:");

        this.text(WalletItem.TOOLTIP_WALLET_PICKUP, "Automatically collects any coins that you pick up");
        this.text(WalletItem.TOOLTIP_WALLET_PICKUP_MAGNET, "Can collect coins up to %sm away");
        this.text(WalletItem.TOOLTIP_WALLET_EXCHANGE_MANUAL, "Can exchange coins manually in the wallet menu");
        this.text(WalletItem.TOOLTIP_WALLET_EXCHANGE_AUTO, "AutoExchange: %s");
        this.text(WalletItem.TOOLTIP_WALLET_EXCHANGE_AUTO_ON, "ON");
        this.text(WalletItem.TOOLTIP_WALLET_EXCHANGE_AUTO_OFF, "OFF");
        this.text(WalletItem.TOOLTIP_WALLET_BANK_ACCOUNT,"Can deposit or withdraw coins to/from your bank account in the UI");

        this.text(AbstractWalletMenu.TOOLTIP_WALLET_EXCHANGE,"Exchange Coins for the Highest Value Coinage");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE,"Enable Auto-Exchange");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE,"Disable Auto-Exchange");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_ENABLE,"Enable Fancy Sounds");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_DISABLE,"Disable Fancy Sounds");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_OPEN_BANK,"Access Bank Account");
        this.text(AbstractWalletMenu.TOOLTIP_WALLET_OPEN_WALLET,"Return to Wallet");

        //Enchantment Tooltips
        this.text(MoneyMendingEnchantmentHelper.TOOLTIP_MONEY_MENDING_COST,"Costs %s per durability repaired");

        //Smithing Template Tooltips
        this.text(LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_DESCRIPTION,"Lightman's Currency Upgrades");
        this.text(LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO,"Miscellaneous Items & Upgrades");
        this.text(LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS,"Redstone or Copper to Netherite Materials");
        this.text(LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION,"Add Item or Upgrade");
        this.text(LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION,"Add Material");
        this.text(ChocolateCoinItem.TOOLTIP_HEALING,"Heals %s Health");

        //Upgrade Tooltips
        this.text(UpgradeType.TOOLTIP_UPGRADE_TARGETS,"Upgrade can be used by:");
        this.text(UpgradeType.TOOLTIP_UPGRADE_UNIQUE,"Unique Upgrade");
        this.text(LCText.Upgrades.TOOLTIP_UPGRADE_ITEM_CAPACITY,"Increases item storage capacity by %s");
        this.text(LCText.Upgrades.TOOLTIP_UPGRADE_TRADE_OFFERS,"Increases traders offer limit by %s");
        this.text(LCText.Upgrades.TOOLTIP_UPGRADE_NETWORK,"Makes a trader accessible on the Trading Terminal");

        this.text(LCText.Upgrades.TOOLTIP_UPGRADE_TARGET_TRADER,"All Traders");
        this.text(LCText.Upgrades.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM,"Item Traders");

        //Bonus Tooltips
        this.text(BonusTooltip.TOOLTIP_INFO_BLURB,"Hold SHIFT for more information.");
        this.text(ItemTraderBlock.TOOLTIP,"Item Trader:","Trades Offers: %s","Can be used to Sell, Purchase, or Barter items with other players");
        this.text(ItemTraderBlock.ARMOR_TOOLTIP,"Armor Trader:","Trades Offers: %s","Can be used to Sell, Purchase, or Barter armor pieces with other players");

        //Money Text
        this.text(EmptyValue.GUI_MONEY_VALUE_FREE,"Free");
        this.text(MoneyDisplayHelper.GUI_MONEY_STORAGE_EMPTY,"Nothing");

        this.text(CoinAPIImpl.COIN_CHAIN_MAIN,"Main");
        this.text(CoinAPIImpl.COIN_CHAIN_EMERALDS,"Emeralds");
        this.text(CoinAPIImpl.COIN_CHAIN_EMERALDS_DISPLAY,"%sE");
        this.text(CoinAPIImpl.COIN_CHAIN_EMERALDS_DISPLAY_WORDY,"%s Emeralds");
        this.text(CoinAPIImpl.COIN_CHAIN_CHOCOLATE,"Chocolate");
        this.text(CoinAPIImpl.COIN_CHAIN_CHOCOLATE_DISPLAY,"%sCC");
        this.text(CoinAPIImpl.COIN_CHAIN_CHOCOLATE_DISPLAY_WORDY,"%s Chocolate Chunks");

        //Price Text
        this.text(ItemPrice.OPTION_NAME,"Item Bartering");

        //Coin Display
        this.text(CoinDisplay.TOOLTIP_COIN_DISPLAY,"%1$s");
        this.text(CoinDisplay.TOOLTIP_COIN_DISPLAY_WORTH,"%1$s");
        this.text(NumberDisplay.TOOLTIP_COIN_DISPLAY_NUMBER,"%1$s");
        this.text(NumberDisplay.TOOLTIP_COIN_DISPLAY_NUMBER_WORDY,"%1$s");
        this.text(CoinDisplay.TOOLTIP_COIN_WORTH_DOWN,"Worth %1$s %2$s");
        this.text(CoinDisplay.TOOLTIP_COIN_WORTH_UP,"%1$s of these are worth 1 %2$s");
        this.text(NumberDisplay.TOOLTIP_COIN_WORTH_VALUE,"Worth %s");
        this.text(NumberDisplay.TOOLTIP_COIN_WORTH_VALUE_STACK,"Stack worth %s");
        this.text(ChainData.TOOLTIP_COIN_ADVANCED_CHAIN,"Chain: %s");
        this.text(ChainData.TOOLTIP_COIN_ADVANCED_VALUE,"Internal Value: %s");
        this.text(ChainData.TOOLTIP_COIN_ADVANCED_CORE_CHAIN,"Core Chain");
        this.text(ChainData.TOOLTIP_COIN_ADVANCED_SIDE_CHAIN,"Side Chain");

        //Ownership Text
        this.text(Owner.GUI_OWNER_NULL,"UNDEFINED");
        this.text(Owner.COMMAND_OWNER_LABEL_CUSTOM,"Owner: %s");
        this.text(PlayerOwner.TOOLTIP_OWNER_PLAYER,"Player: %s");
        this.text(PlayerOwner.COMMAND_OWNER_LABEL_PLAYER,"Owner: %1$s (%2$s)");

        this.text(MemberLevel.BLURB.get(MemberLevel.MEMBERS),"Members");
        this.text(MemberLevel.BLURB.get(MemberLevel.ADMINS),"Admins");
        this.text(MemberLevel.BLURB.get(MemberLevel.OWNER),"Owner");

        //Bank Account
        this.text(BankAccount.GUI_BANK_ACCOUNT_NAME,"%s's Bank Account");

        //Money
        this.text(SortableMoneyResourceHandler.TOOLTIP_MONEY_SOURCE_PLAYER,"On Your Person:");
        this.text(NormalCustomerTab.TOOLTIP_MONEY_SOURCE_SLOTS,"Money Slots:");
        this.text(NormalCustomerTab.TOOLTIP_MONEY_SOURCE_SLOTS_CAPABILITY,"Money Access Items:");

        //ATM
        this.text(CoinExchangeTab.TOOLTIP,"Exchange Coins");
        this.text(AccountSelectionTab.TOOLTIP,"Select Account");
        this.text(AccountSelectionTab.BUTTON_PLAYER_ACCOUNT,"Select Players Account");
        this.text(AccountSelectionTab.GUI_SELECT_PLAYER_SUCCESS,"Select Players Account");
        this.text(AccountInteractionTab.TOOLTIP,"Withdraw or Deposit");
        this.text(AccountInteractionTab.GUI_NO_SELECTED_ACCOUNT,"No account selected");
        this.text(AccountInteractionTab.BUTTON_DEPOSIT,"Deposit");
        this.text(AccountInteractionTab.BUTTON_WITHDRAW,"Withdraw");
        this.text(AccountSettingsTab.TOOLTIP,"Account Settings");
        this.text(AccountSettingsTab.GUI_NOTIFICATIONS_DISABLED,"You will not receive any account balance notifications");
        this.text(AccountSettingsTab.GUI_NOTIFICATIONS_DETAILS,"You will receive a notification if your account balance goes below %s");
        this.text(AccountSettingsTab.BUTTON_BANK_CARD_RESET,"Reset Card Verification");
        this.text(AccountSettingsTab.TOOLTIP_BANK_CARD_RESET,"Will make all ATM Cards linked to this bank account no longer function until re-assigned");
        this.text(AccountLogsTab.TOOLTIP,"Account Logs");
        this.text(MoneyTransferTab.TOOLTIP,"Transfer Money");
        this.text(MoneyTransferTab.TOOLTIP_TRANSFER_MODE_LIST,"Select From List");
        this.text(MoneyTransferTab.TOOLTIP_TRANSFER_MODE_PLAYER,"Select From Players");
        this.text(MoneyTransferTab.TOOLTIP_TRANSFER_TRIGGER,"Transfer %1$s to %2$s");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_NULL_SENDER,"Your selected bank account no longer exists");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_NULL_TARGET,"Target bank account does not exist");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_ACCESS,"You no longer have access to your selected bank account!");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_AMOUNT,"Cannot transfer nothing");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_SAME,"Cannot transfer to the same account");
        this.text(MoneyTransferTab.GUI_TRANSFER_ERROR_NO_BALANCE,"Cannot transfer %s, as your bank account has no money of that type");
        this.text(MoneyTransferTab.GUI_TRANSFER_SUCCESS,"Transferred %1$s to %2$s");


        //Trading Terminal Tooltips
        this.text(NetworkTraderSelectionTab.NAME,"Network Trader Access");
        this.text(NetworkTraderSelectionTab.TOOLTIP_OPEN_ALL_TRADERS,"Open All Network Traders");
        this.text(TradingNode.TOOLTIP_TERMINAL_TRADE_COUNT,"%s trade(s)");
        this.text(TradingNode.TOOLTIP_TERMINAL_OUT_OF_STOCK_COUNT,"%s trade(s) out of stock");

        //Trader Tooltips
        this.text(TradeData.TOOLTIP_OUT_OF_STOCK,"No Stock");
        this.text(TradeData.TOOLTIP_OUT_OF_SPACE,"Trader Full");
        this.text(TradeData.TOOLTIP_CANNOT_AFFORD,"Cannot Afford");

        this.text(StoredTrader.WITH_DATA,"Linked to existing Trader Data");
        this.text(StoredTrader.WITH_DATA_ID,"Trader ID: %s");
        this.text(StoredTrader.STILL_ACCESSIBLE,"Picked up in Admin Mode","Can still be accessed through the Trading Terminal");
        this.text(CopiedTrader.WITH_COPY,"Contains a Copy of a Trader's Data");
        this.text(NormalCustomerTab.TOOLTIP_CUSTOMER_TAB,"Trade Interaction");
        this.add(IDisplayNode.toTranslationKey(LCTraderTypes.ITEM_TRADER.getId()),"Item Trader");
        this.add(IDisplayNode.toTranslationKey(LCTraderTypes.ARMOR_DISPLAY.getId()),"Item Trader");
        this.text(SimpleTradeEditTab.TOOLTIP_TRADE_EDIT_TAB,"Edit Trades");
        this.text(MoneyStorageTab.TOOLTIP_MONEY_STORAGE,"Money Storage");
        this.text(MoneyStorageTab.GUI_TRADER_MONEY_STORAGE_CONTENTS,"Current Contents: %s");
        this.text(MoneyStorageTab.BUTTON_TRADER_STORE_MONEY,"Store Money");
        this.text(MoneyStorageTab.BUTTON_TRADER_COLLECT_MONEY,"Collect Money");
        this.text(AbstractTradeRuleTab.TOOLTIP_TRADER_TRADE_RULES_TRADER,"Trader Rules");
        this.text(AbstractTradeRuleTab.TOOLTIP_TRADER_TRADE_RULES_TRADE,"Edit Trade-Exclusive Rules");
        this.text(AbstractTradeRuleTab.GUI_TRADE_RULES_LIST,"Trade Rules:");
        this.text(AbstractTradeRuleTab.TOOLTIP_TRADE_RULES_MANAGER,"Rule Management");

        //Settings
        this.text(DirectionalSettingsState.TEXT.get(DirectionalSettingsState.NONE),"Nothing");
        this.text(DirectionalSettingsState.TEXT.get(DirectionalSettingsState.INPUT),"Input Only");
        this.text(DirectionalSettingsState.TEXT.get(DirectionalSettingsState.INPUT_AND_OUTPUT),"Input & Output");
        this.text(DirectionalSettingsState.TEXT.get(DirectionalSettingsState.OUTPUT),"Output Only");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.DOWN),"Bottom");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.UP),"Top");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.NORTH),"Back");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.SOUTH),"Front");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.WEST),"Left");
        this.text(DirectionalSettings.GUI_INPUT_SIDES.get(Direction.EAST),"Right");
        this.text(ExternalInteractionsNode.TOOLTIP_SETTINGS_DEFAULT,"External Input & Output");
        this.text(ExternalInteractionsNode.GUI_SETTINGS_LABEL,"Input Sides");
        this.text(ItemStorageNode.TOOLTIP_INPUT_SETTINGS,"External Item Input & Output");

        this.text(WorldNode.BUTTON_TRADER_SETTINGS_DESTROY_TRADER,"Delete Trader");
        this.text(WorldNode.TOOLTIP_TRADER_SETTINGS_DESTROY_TRADER,"Completely deletes the trader, turning it back into a data-less block item.","CANNOT BE UNDONE!");

        this.text(SettingsTab.TOOLTIP,"Trader Settings");
        this.text(SettingsTab.CATEGORY_MISC,"Misc Settings");
        //Display Settings
        this.text(DisplayNode.NAME,"Display Settings");
        this.text(DisplayNode.VALUE_TRADER_NAME,"Trader Name");
        this.text(DisplayNode.VALUE_CUSTOM_ICON,"Terminal Icon");
        this.text(DisplayNode.VALUE_SEARCH_BOX,"Search Box");

        this.text(DisplayNode.BUTTON_CHANGE_NAME,"Change Name");
        this.text(DisplayNode.BUTTON_RESET_NAME,"Reset Name");
        this.text(DisplayNode.GUI_ALWAYS_SHOW_SEARCH,"Enable Search Box");
        //Ally Settings
        this.text(AlliesNode.NAME,"Ally Settings");
        this.text(AlliesNode.VALUE_ALLIES,"Allies");
        this.text(AlliesNode.VALUE_ALLY_PERMS,"Ally Permissions");
        //Capability Interaction Node
        this.text(ExternalInteractionsNode.NAME,"External Interaction Settings");
        this.text(ExternalInteractionsNode.VALUE_SETTINGS_DUMB,"External Interaction States");
        this.text(ExternalInteractionsNode.VALUE_SETTINGS,"External Interaction Side: ");
        //Owner Settings
        this.text(OwnerNode.NAME,"Trader Ownership");
        this.text(OwnerNode.VALUE_OWNER,"Trader Owner");
        //Trade Rule Settings
        this.text(TradeRulesNode.NAME,"Global Trader Rules");
        //Trade Settings
        this.text(TradingNode.VALUE_TRADE,"Trade #%s Data");
        this.text(TradingNode.VALUE_TRADE_RULES,"Trade #%s Rules");
        this.text(TradingNode.VALUE_TRADE_COUNT,"Trade Offers");

        this.text(OwnerNode.SETTINGS_TOOLTIP,"Transfer Ownership");
        this.text(OwnerNode.BUTTON_OWNER_SET_PLAYER,"Transfer to Player");
        this.text(OwnerNode.BUTTON_OWNER_SET_FAKEPLAYER,"Assign Fake Owner");
        this.text(OwnerNode.GUI_OWNER_CURRENT,"Current Owner: %s");
        this.text(OwnerNode.TOOLTIP_OWNER_NODE_MANUAL,"Switch to Manual Player Input Mode");
        this.text(OwnerNode.TOOLTIP_OWNER_NODE_SELECTION,"Switch to List Selection Mode");

        //Notification Settings
        this.text(NotificationNode.NAME,"Notifications");
        this.text(NotificationNode.VALUE_NOTIFY_MEMBERS,"Notify Members");
        this.text(NotificationNode.VALUE_MEMBER_LEVEL,"Member Level");
        this.text(NotificationNode.VALUE_PUSH_TO_CHAT,"Push to Chat");
        this.text(NotificationNode.GUI_MEMBER_LEVEL,"Push to Team %s");


        //Settings Clipboard
        this.text(SettingsClipboardTab.TOOLTIP,"Settings Clipboard");
        this.text(SettingsClipboardTab.BUTTON_SETTINGS_COPY,"Copy");
        this.text(SettingsClipboardTab.BUTTON_SETTINGS_PASTE,"Paste");

        //Info
        this.text(InfoTab.TOOLTIP,"Trader Info");

        this.text(NotificationNode.TOOLTIP_TRADER_LOGS,"Trader Logs");
        this.text(NotificationNode.TOOLTIP_TRADER_LOGS_SETTINGS,"Settings Logs");

        this.text(TraderStatsNode.TOOLTIP_TRADER_STATS,"Trader Stats");

        this.text(ItemTradesNode.NAME,"Item Trades");
        this.text(TraderCustomerMenu.GUI_TRADER_TITLE,"%1$s (%2$s)");
        this.text(TraderCustomerMenu.TOOLTIP_TRADER_OPEN_STORAGE,"Open Trader Storage");
        this.text(TraderCustomerMenu.TOOLTIP_TRADER_COLLECT_MONEY,"Collect Stored Money");
        this.text(TraderCustomerMenu.TOOLTIP_TRADER_NETWORK_BACK,"Back to Network Terminal");
        this.text(TraderStorageMenu.TOOLTIP_TRADER_OPEN_TRADES,"Return to Customer Menu");

        this.text(TradePrice.TOOLTIP_TRADE_EDIT_PRICE,"Click to Edit Price");

        this.text(TradeData.TOOLTIP_TRADE_INFO_TITLE,"Trade Info:");
        this.text(TradeData.TOOLTIP_TRADE_INFO_STOCK,"%s trade(s) in stock");
        this.text(TradeData.TOOLTIP_TRADE_INFO_STOCK_INFINITE,"∞");

        this.text(TradeDirection.NAME_TEXT.get(TradeDirection.SALE),"Sale");
        this.text(TradeDirection.ACTION_TEXT.get(TradeDirection.SALE),"bought");
        this.text(TradeDirection.NAME_TEXT.get(TradeDirection.PURCHASE),"Purchase");
        this.text(TradeDirection.ACTION_TEXT.get(TradeDirection.PURCHASE),"sold");
        this.text(TradeDirection.NAME_TEXT.get(TradeDirection.OTHER),"Unknown");
        this.text(TradeDirection.ACTION_TEXT.get(TradeDirection.OTHER),"???");


        //Item Trader
        this.text(ItemStorageTab.TOOLTIP_ITEM_STORAGE,"Item Storage");
        this.text(TradeItem.GUI_TRADE_ITEM_ENFORCE_DATA,"Enforce Data");
        this.text(TradeItem.TOOLTIP_TRADE_ITEM_EDIT_EMPTY,"Click to Set Item");
        this.text(TradeItem.TOOLTIP_TRADE_ITEM_EDIT_SHIFT,"Hold SHIFT & click to access Item Settings");
        this.text(TradeItem.TOOLTIP_TRADE_ITEM_DATA_WARNING_OUTPUT,"Data Components are NOT enforced. Item may have unexpected data");
        this.text(TradeItem.TOOLTIP_TRADE_ITEM_DATA_WARNING_INPUT,"Accepts all data states");
        this.text(TradeItem.TOOLTIP_TRADE_INFO_ORIGINAL_NAME,"Original Name: %s");
        this.text(TradeItem.GUI_ITEM_EDIT_SEARCH,"Search Items");
        this.text(TradeItem.TOOLTIP_ITEM_EDIT_SCROLL,"Scroll to change stack size");

        //Trade Results
        this.add(TradeResult.FAIL_OUT_OF_STOCK.getTranslationKey(),"Trade is out of stock");
        this.add(TradeResult.FAIL_CANNOT_AFFORD.getTranslationKey(),"You can no longer afford this trade");
        this.add(TradeResult.FAIL_NO_OUTPUT_SPACE.getTranslationKey(),"Insufficient space to output the purchased product");
        this.add(TradeResult.FAIL_NO_INPUT_SPACE.getTranslationKey(),"Trader has insufficient space to store the collected price");
        this.add(TradeResult.FAIL_EVENT_DENIAL.getTranslationKey(),"A Trade Rule or other event has denied your ability to interact with the trade");
        this.add(TradeResult.FAIL_TAX_EXCEEDED_LIMIT.getTranslationKey(),"The Trader's Tax Collection exeeds its defined limits");
        this.add(TradeResult.FAIL_INVALID_TRADE.getTranslationKey(),"This trade is no longer valid");
        this.add(TradeResult.FAIL_NOT_SUPPORTED.getTranslationKey(),"This trader does not support this method of trade interaction");
        this.add(TradeResult.FAIL_NULL.getTranslationKey(),"The trade or trader no longer exists");

        //Trade Rules
        //Free Sample
        this.tradeRule(FreeSample.TYPE,"Free Sample");
        this.text(FreeSample.INFO_SINGLE,"Your first purchase is free!");
        this.text(FreeSample.INFO_MULTI,"Your first %s purchases are free!");
        this.text(FreeSample.INFO_USED,"You have used %1$s of your %2$s free samples");
        this.text(FreeSample.INFO_TIMED,"Free sample resets after %s");
        this.text(FreeSample.INFO_TIME_REMAINING,"You can get a free sample again after %s");
        this.text(FreeSample.BUTTON_CLEAR_MEMORY,"Reset Free Samples");
        this.text(FreeSample.TOOLTIP_CLEAR_MEMORY,"Forgets who has received their free sample(s) so that they may receive it again");
        this.text(FreeSample.GUI_INFO,"Players may claim %s free sample(s)");
        this.text(FreeSample.GUI_PLAYER_COUNT,"%s free samples have been given!");
        this.text(FreeSample.GUI_DURATION,"Forget free samples after %s");
        this.text(FreeSample.GUI_NO_DURATION,"Never forgets free samples");
        //Player Trade Limit
        this.tradeRule(PlayerTradeLimit.TYPE,"Player Trade Limits");
        this.text(PlayerTradeLimit.DENIAL,"You have done this trade %s times already");
        this.text(PlayerTradeLimit.DENIAL_TIMED,"You have done this trade %1$s times within the last %2$s");
        this.text(PlayerTradeLimit.DENIAL_TIME_REMAINING,"You can interact again in %s");
        this.text(PlayerTradeLimit.DENIAL_LIMIT,"Limit is %s");
        this.text(PlayerTradeLimit.INFO,"You have done this trade %1$s of %2$s times");
        this.text(PlayerTradeLimit.INFO_TIMED,"You have done this trade %1$s of %2$s times within the last %3$s");
        this.text(PlayerTradeLimit.BUTTON_CLEAR_MEMORY,"Clear Memory");
        this.text(PlayerTradeLimit.TOOLTIP_CLEAR_MEMORY,"Clears the memory of how many trades have been handled");
        this.text(PlayerTradeLimit.GUI_INFO,"Players can do %s trade(s)");
        this.text(PlayerTradeLimit.GUI_DURATION,"Forget interactions after %s");
        this.text(PlayerTradeLimit.GUI_NO_DURATION,"Never forget interactions");

        //Permissions
        this.permission(LCPermissions.OPEN_STORAGE,"Open Storage","Allows player to access the Storage Menu and add or remove stock from the traders storage.");
        this.permission(LCPermissions.EDIT_DISPLAY,"Display Settings","Allows player to change the traders display name & custom icon.");
        this.permission(LCPermissions.EDIT_TRADES,"Modify Trades","Allows the player to create, remove, and modify the trades.");
        this.permission(LCPermissions.EDIT_TRADE_RULES,"Edit Trade Rules","Allows the player to activate, deactivate, and otherwise edit the Trade Rules attached to this trader or its trades. Modify Trade Permissions are still required to edit Trade-Specific rules.");
        this.permission(LCPermissions.COLLECT_MONEY,"Collect Money","Allows the player to collect money from this traders internal money storage.");
        this.permission(LCPermissions.STORE_MONEY,"Store Money","Allows the player to deposit money into this traders internal money storage.");
        this.permission(LCPermissions.EDIT_SETTINGS,"General Settings","Allows the player to access the traders settings tab, and modify any setting not explicitly locked behind a different permission.");
        this.permission(LCPermissions.ADD_REMOVE_ALLIES,"Add/Remove Allies","Allows the player to add or remove other players (or themselves) from the list of Allies.");
        this.permission(LCPermissions.EDIT_ALLY_PERMS,"Edit Permissions","Allows the player to edit the permissions that allies have access to (exactly as you are doing now).");
        this.permission(LCPermissions.VIEW_LOGS,"Manage Logs","Allows the player to view this machines interaction logs. Higher levels allow deletion of undesired logs as well.");
        this.permissionEntry(LCPermissions.VIEW_LOGS,TriStatePermission.NONE,"None");
        this.permissionEntry(LCPermissions.VIEW_LOGS,TriStatePermission.LOW,"View");
        this.permissionEntry(LCPermissions.VIEW_LOGS,TriStatePermission.HIGH,"View & Delete");
        this.permission(LCPermissions.BREAK_TRADER,"Break or Move Machine","Allows the player to move this machines block. 'Delete' access required to fully delete and destroy the trader.");
        this.permissionEntry(LCPermissions.BREAK_TRADER,TriStatePermission.NONE,"None");
        this.permissionEntry(LCPermissions.BREAK_TRADER,TriStatePermission.LOW,"Move");
        this.permissionEntry(LCPermissions.BREAK_TRADER,TriStatePermission.HIGH,"Move & Delete");
        this.permission(LCPermissions.TRANSFER_OWNERSHIP,"Transfer Ownership","Allows the player to change the owner of this machine.");
        //TODO Trader Interface Permission
        this.permission(LCPermissions.EXTERNAL_ACCESS_SETTINGS,"External Access Settings","Allows the player to change with physical sides of the machine can have product inserted or extracted through automation.");
        //TODO CC Authorization Permission

        //Notifications
        this.text(Notification.NOTIFICATION_FORMAT_GENERAL,"%1$s: %2$s");
        this.text(Notification.NOTIFICATION_FORMAT_CHAT,"%1$s %2$s");
        this.text(Notification.NOTIFICATION_FORMAT_CHAT_TITLE,"[%s]");
        this.text(Notification.NOTIFICATION_TIMESTAMP,"%s");
        this.text(Notification.TOOLTIP_DELETE,"Delete");


        this.text(ItemHelper.NOTIFICATION_ITEM_FORMAT,"%1$sx %2$s");
        this.text(OutOfStockNotification.TEXT,"Trade #%2$s is out of stock");
        this.text(OutOfStockNotification.TEXT_INDEXLESS,"Trader is out of stock");
        this.text(ItemTradeNotification.TEXT,"%1$s %2$s %3$s for %4$s");

        this.text(LowBalanceNotification.TEXT,"Bank Account is below %s");
        this.text(BankInteractionNotification.TEXT_WITHDRAW,"%1$s withdrew %2$s");
        this.text(BankInteractionNotification.TEXT_DEPOSIT,"%1$s deposited %2$s");
        this.text(BankInteractionNotification.TEXT_INTERACTION_SERVER,"An admin");
        this.text(BankInterestNotification.TEXT,"Gained %s in interest");
        this.text(BankTransferNotification.Sent.TEXT,"%1$s transferred %2$s to %4$s");
        this.text(BankTransferNotification.Received.TEXT,"%1$s transferred %2$s from %3$s");

        this.text(TaxesPaidNotification.TEXT,"%s was paid in taxes");

        this.text(ChangeSettingNotification.Dumb.TEXT,"%1$s changed %2$s");
        this.text(ChangeSettingNotification.Simple.TEXT,"%1$s changed %2$s to %3$s");
        this.text(ChangeSettingNotification.Advanced.TEXT,"%1$s changed %2$s from %3$s to %4$s");

        //Stats
        this.text(StatViewer.GUI_STAT_LABEL,"%s: ");
        this.text(StatViewer.GUI_STATS_EMPTY,"No Statistics Recorded");
        this.text(StatHolder.BUTTON_CLEAR_STATS,"Clear Data");
        this.text(TimestampStatType.GUI_TIME_NEVER,"Never");
        this.statKeyName(MoneyStats.TAXES_PAID,"Taxes Paid");
        this.statKeyName(MoneyStats.MONEY_EARNED,"Money Earned");
        this.statKeyName(MoneyStats.MONEY_PAID,"Money Paid");
        this.statKeyName(TraderStats.LAST_INTERACTION,"Last Customer Interaction");
        this.statKeyName(TraderStats.INTERACTION_COUNT,"Trades Fulfilled");

        //Commands
        this.text(CoinValueParser.ARGUMENT_MONEY_VALUE_NOT_A_COIN,"'%s' is not a valid coin!");
        this.text(CoinValueParser.ARGUMENT_MONEY_VALUE_DIFFERENT_CHAIN,"'%1$s' is not on the same coin chain (was '%2$s', expected '%3$s'!");
        this.text(MoneyValueParser.ARGUMENT_MONEY_VALUE_NO_VALUE,"Value parsed had no value!");
        this.text(TraderArgument.ARGUMENT_TRADER_NOT_FOUND,"Could not find a trader with the given ID");
        this.text(TraderArgument.ARGUMENT_TRADER_NOT_RECOVERABLE,"Trader is not in a state that would require recovery");

        this.text(LCAdminCommand.TOGGLE_ADMIN,"LC Admin Mode is now %s");
        this.text(LCAdminCommand.TOGGLE_ADMIN_ENABLED,"ENABLED");
        this.text(LCAdminCommand.TOGGLE_ADMIN_DISABLED,"DISABLED");
        this.text(LCAdminCommand.TRADER_DELETE_SUCCESS,"Successfully deleted %s");
        this.text(LCAdminCommand.TRADER_RECOVER_SUCCESS,"Successfully gave %1$s a fresh item linked to %2$s");

        this.text(LCConfigCommand.RELOAD,"Reloaded %s Config Files");
        this.text(LCConfigCommand.RELOAD_FILE,"Reloaded %s");

        //Config
        this.text(LCText.Config.CONFIG_TITLE_FILES,"%s Config Files");
        this.text(LCText.Config.CONFIG_TITLE_SEPERATOR," -> ");
        this.text(LCText.Config.CONFIG_LABEL_FILE,"File: %s");
        this.text(LCText.Config.CONFIG_OPTION_COUNT,"%s Config Options");
        this.text(ConfigOption.CONFIG_OPTION_DEFAULT,"Default: %s");
        this.text(ConfigOption.CONFIG_OPTION_RANGE,"Range: %1$s -> %2$s");
        this.text(ConfigOption.CONFIG_OPTION_OPTIONS,"Options: %s");
        this.text(LCText.Config.CONFIG_OPTION_LIST_COUNT,"%s Entries");
        this.text(LCText.Config.CONFIG_OPTION_LIST_ENTRY,"Entry #%s");
        this.text(LCText.Config.CONFIG_OPTION_LIST_ADD,"Add Entry");
        this.text(LCText.Config.CONFIG_OPTION_LIST_REMOVE,"Delete this Entry");
        this.text(LCText.Config.CONFIG_OPTION_EDIT_TOOLTIP,"Click To Edit");
        this.text(LCText.Config.CONFIG_OPTION_NOT_SUPPORTED,"Not Yet Supported");
        this.text(LCText.Config.CONFIG_UNDO,"Undo");
        this.text(LCText.Config.CONFIG_UNDO_ALL,"Reset Changes");
        this.text(LCText.Config.CONFIG_RESET_DEFAULT,"Reset to Default");

        //Config Files
        //Client Config
        this.configName(LCConfig.CLIENT,"Client Config");
        //Quality
        this.configSection(LCConfig.CLIENT,"quality","Quality Settings","Contains settings related to increasing or lowering rendering quality to help improve FPS in areas with a lot of machines.");
        this.configOption(LCConfig.CLIENT.itemRenderLimit,"Item Render Limit",
                "Maximum number of items each Item Trader (and other miscellaneous item rendering machines) can render (per-trade) as stock. Lower to help improve framerate in trader-rich areas.",
                "Setting to 0 will disable item rendering entirely");
        //Item Scale and Item Render Blacklist
        this.configOption(LCConfig.CLIENT.drawGachaBallItem,"Gacha Ball Full Render",
                "Whether the Gacha Ball should render the item inside.",
                "Enabling will double the number of items being rendered, and can cause FPS issues near Gacha Machines if their fancy graphics are enabled.");
        this.configOption(LCConfig.CLIENT.gachaMachineFancyGraphics,"Gacha Machine Fancy Graphics",
                "Whether the Gacha Machine will render each Gacha Ball individually",
                "Disable if you're having FPS issues near the Gacha Machine, this will make the machine render a far more simplisitic representation of its items.");
        this.configOption(LCConfig.CLIENT.scrollMultiplier,"Scroll Multiplier");

        //Time
        this.configSection(LCConfig.CLIENT,"time","Time Formatting Settings");
        this.configOption(LCConfig.CLIENT.timeFormat,"Time Formatting",
                "How Timestamps are displayed.",
                "Follows SimpleDataFormat formatting: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html");
        //Wallet SLot
        this.configSection(LCConfig.CLIENT,"wallet_slot","Wallet Slot Settings",
                "Does nothing when Curios is installed.",
                "0 0 is the top-left corner of the menu, with x moving it further right and y moving it further down");
        this.configOption(LCConfig.CLIENT.walletSlot,"Wallet Slot Position",
                "The position of the wallet slot in the players inventory menu.");
        this.configOption(LCConfig.CLIENT.walletSlotCreative,"Creative Wallet Slot Position",
                "The position of the wallet slot in the players creative inventory menu.");
        this.configOption(LCConfig.CLIENT.walletButtonOffset,"Wallet Button Offset",
                "The position of the \"Open Wallet\" button relative to the wallet slot.");
        //Wallet Overlay
        this.configSection(LCConfig.CLIENT,"wallet_hud","Wallet Overlay Settings");
        this.configOption(LCConfig.CLIENT.walletOverlayEnabled,"Enabled",
                "Whether an overlay should be drawn on your HUD displaying your wallet and available money.");
        this.configOption(LCConfig.CLIENT.walletOverlayCorner,"Overlay Corner",
                "The corner of the screen that the overlay should be drawn on.");
        this.configOption(LCConfig.CLIENT.walletOverlayPosition,"Overlay Offset",
                "The distance from the corner that the overlay will be drawn at.");
        this.configOption(LCConfig.CLIENT.walletOverlayType,"Overlay Type",
                "The method that should be used to draw the available money/items.");
        //Terminal
        this.configSection(LCConfig.CLIENT,"network_terminal", "Trader Network Access Terminal Settings");
        this.configOption(LCConfig.CLIENT.terminalColumnLimit,"Column Limit",
                "The maximum number of columns the Network Terminal is allowed to display.");
        this.configOption(LCConfig.CLIENT.terminalRowLimit,"Row Limit",
                "The maximum number of rows the Network Terminal is allowed to display.");
        this.configOption(LCConfig.CLIENT.terminalBonusFilters,"Bonus Search Filters",
                "A default search filter that will be automatically added to the search parameters.");
        /*this.configOption(LCConfig.CLIENT.terminalDefaultSorting,"Default Sort Mode",
                "The default sorting mode that will be automatically selected when you first open the terminal.",
                "Note: The game will remember your last selection option within the same session, so editing this after the screen has been opened will not change anything until you close and re-open your game.",
                "Can also be saved as your current selection by clicking the small save button next to the sorting type dropdown.");//*/
        //Inventory Button
        this.configSection(LCConfig.CLIENT,"inventory_buttons","Inventory Button Settings");
        this.configOption(LCConfig.CLIENT.notificationAndTeamButtonPosition,"Normal Position","The position that the notification & team manager buttons will be placed at in the players items.");
        this.configOption(LCConfig.CLIENT.notificationAndTeamButtonCreativePosition,"Creative Position","The position that the notification & team manager buttons will be placed at in the creative players items.");
        //Chest Button
        this.configSection(LCConfig.CLIENT,"chest_buttons","Chest Button Settings");
        this.configOption(LCConfig.CLIENT.chestButtonVisible,"Enabled","Whether the 'Move Coins into Wallet' button will appear in the top-right corner of the Chest Screen if there are coins in the chest that can be collected.");
        this.configOption(LCConfig.CLIENT.chestButtonAllowSideChains,"Collect Side-Chain Coins","Whether the 'Move Coins into Wallet' button should collect coins from a side-chain.",
                "By default these would be the coin pile and coin block variants of the coins.");
        //Notification
        this.configSection(LCConfig.CLIENT,"notification","Notification Settings");
        this.configOption(LCConfig.CLIENT.pushNotificationsToChat,"Notifications In Chat","Whether notifications should be posted in your in-game chat when you receive them.");
        //Tax Warning
        this.configSection(LCConfig.CLIENT,"tax_warnings","Tax Warning Settings");
        this.configOption(LCConfig.CLIENT.serverTaxWarning,"Server Tax Warning","Whether you should recieve a message in chat whenever you place a machine when server-wide taxes are enabled");
        this.configOption(LCConfig.CLIENT.playerTaxWarning,"Player Tax Warning","Whether you should recieve a message in chat whenever you place a machine in an area that another player is taxing");
        //Slot Machine
        this.configSection(LCConfig.CLIENT,"slot_machine","Slot Machine Animation Settings");
        this.configOption(LCConfig.CLIENT.slotMachineAnimationTime,"Animation Duration","The number of Minecraft ticks the slot machine animation will last.",
                "Note: 20 ticks = 1 second",
                "Must be at least 20 ticks (1s) for coding reasons.");
        this.configOption(LCConfig.CLIENT.slotMachineAnimationRestTime,"Rest Duration","The number of Minecraft ticks the slot machine will pause before repeating the animation.");
        //Sound Settings
        this.configSection(LCConfig.CLIENT,"sounds","Sound Settings");
        this.configOption(LCConfig.CLIENT.moneyMendingClink,"Money Mending Sound","Whether the Money Mending enchantment should make a noise when triggered.");
        //Debug
        this.configSection(LCConfig.CLIENT,"debug","Debug Settings");
        this.configOption(LCConfig.CLIENT.debugScreens,"Debug Screen BG","Whether LC screens should render a white background for easier debugging & screenshots","Mostly used for creating wiki/guidebook content");

        // Common Config
        this.configName(LCConfig.COMMON,"Common Config");
        //Crafting
        this.configSection(LCConfig.COMMON,"crafting","Crafting Settings","/reload required for any changes made here to take effect.","Disabling will not remove any existing items/blocks from the world, nor prevent their use.");
        this.configOption(LCConfig.COMMON.canCraftNetworkTraders,"Network Traders","Whether Network Traders can be crafted.","Disabling does NOT disable the recipes of Network Upgrades or the Trading Terminals.");
        this.configOption(LCConfig.COMMON.canCraftTraderInterfaces,"Trader Interfaces","Whether Trader Interface blocks can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftAuctionStands,"Auction Stands","Whether Auction Stand blocks can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftTaxBlock,"Tax Collector","Whether Tax Collector blocks can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftATMCard,"ATM Card","Whether ATM Cards can be crafted.");
        //Crafting -> Coin Mint
        this.configSection(LCConfig.COMMON,"crafting.coin_mint","Coin Mint Crafting");
        this.configOption(LCConfig.COMMON.canCraftCoinMint,"Coin Mint Machine","Whether the Coin Mint machine can be crafted.");
        this.configOption(LCConfig.COMMON.coinMintCanMint,"Can Mint Coins","Whether or not built-in coin mint recipes that turn resources into coins will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintCanMelt,"Can Melt Coins","Whether or not built-in coin mint recipes that turn coins back into resources will be loaded.");
        //Crafting -> Coin Mint -> Mint
        this.configSection(LCConfig.COMMON,"crafting.coin_mint.mint","Targeted Minting Options","Does nothing if \"Can Mint Coins\" is already false/disabled.");
        this.configOption(LCConfig.COMMON.coinMintMintableCopper,"Copper Coins","Whether the default mint recipe to mint copper coins from copper ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMintableIron,"Iron Coins","Whether the default mint recipe to mint iron coins from iron ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMintableGold,"Gold Coins","Whether the default mint recipe to mint gold coins from gold ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMintableEmerald,"Emerald Coins","Whether the default mint recipe to mint emerald coins from emeralds will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMintableDiamond,"Diamond Coins","Whether the default mint recipe to mint diamond coins from diamonds will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMintableNetherite,"Netherite Coins","Whether the default mint recipe to mint netherite coins from netherite ingots will be loaded.");
        //Crafting -> Coin Mint -> Melt
        this.configSection(LCConfig.COMMON,"crafting.coin_mint.melt","Targeted Melting Options","Does nothing if \"Can Melt Coins\" is already false/disabled.");
        this.configOption(LCConfig.COMMON.coinMintMeltableCopper,"Copper Coins","Whether the default mint recipe to melt copper coins back into copper ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMeltableIron,"Iron Coins","Whether the default mint recipe to melt iron coins back into iron ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMeltableGold,"Gold Coins","Whether the default mint recipe to melt gold coins back into gold ingots will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMeltableEmerald,"Emerald Coins","Whether the default mint recipe to melt emerald coins back into emeralds will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMeltableDiamond,"Diamond Coins","Whether the default mint recipe to melt diamond coins back into diamonds will be loaded.");
        this.configOption(LCConfig.COMMON.coinMintMeltableNetherite,"Netherite Coins","Whether the default mint recipe to melt netherite coins back into netherite ingots will be loaded.");
        //Crafting -> Money Chest
        this.configSection(LCConfig.COMMON,"crafting.money_chest","Money Chest Crafting");
        this.configOption(LCConfig.COMMON.canCraftCoinChest,"Money Chest","Whether the Money Chest can be crafted.","Disabling does NOT disable the recipes of Money Chest Upgrades.");
        this.configOption(LCConfig.COMMON.canCraftCoinChestUpgradeExchange,"Exchange Upgrade","Whether the Money Chest Exchange Upgrade can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftCoinChestUpgradeMagnet,"Magnet Upgrade","Whether the Money Chest Magnet Upgrades can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftCoinChestUpgradeBank,"Bank Upgrade","Whether the Money Chest Bank Upgrade can be crafted.");
        this.configOption(LCConfig.COMMON.canCraftCoinChestUpgradeSecurity,"Security Upgrade","Whether the Money Chest Security Upgrade can be crafted.");
        //Events
        this.configSection(LCConfig.COMMON,"events","Event Settings");
        this.configOption(LCConfig.COMMON.chocolateEventCoins,"Chocolate Coins","Whether the Chocolate Event Coins will be added to the coin data.",
                "Note: Disabling will not remove any Chocolate Coin items that already exist, this simply makes them no longer function as money");
        this.configOption(LCConfig.COMMON.eventStartingRewards,"Event Starting Rewards","Whether custom events defined in the 'SeasonalEvents.json' config can give players the one-time reward for logging in during the event.");
        this.configOption(LCConfig.COMMON.eventLootReplacements,"Event Loot Replacements","Whether custom events can replace a portion (or all) of the default loot with custom event loot.");
        //Villagers
        this.configSection(LCConfig.COMMON,"villagers","Villager Related Settings","Note: Any changes to villagers requires a full reboot to be applied due to how Minecraft/Forge registers trades.");
        this.configOption(LCConfig.COMMON.piglinsBarterCoins,"Piglins Barter Coins","Whether Piglins will accept gold coins as a valid bartering item");
        this.configOption(LCConfig.COMMON.addCustomWanderingTrades,"Custom Wandering Trades","Whether the wandering trader will have additional trades that allow you to buy misc items with money.");
        this.configOption(LCConfig.COMMON.addBankerVillager,"Banker Trade Offers","Whether the banker villager profession (ATM) will have any registered trades.","The banker sells Lightman's Currency items for coins.");
        this.configOption(LCConfig.COMMON.addCashierVillager,"Cashier Trade Offers","Whether the cashier villager profession (Cash Register) will have any registered trades.","The cashier sells an amalgamation of vanilla traders products for coins.");
        //Villagers -> Modifications
        this.configSection(LCConfig.COMMON,"villagers.modification","Villager Trade Modification","Note: Changes made only apply to newly generated trades. Villagers with trades already defined will not be changed.");
        this.configOption(LCConfig.COMMON.changeVanillaTrades,"Change Vanilla Trade Offers","Whether vanilla villagers should have the Emeralds from their trades replaced with coins.");
        this.configOption(LCConfig.COMMON.changeModdedTrades,"Change Modded Trade Offers","Whether vanilla villagers added by other mods should have the Emeralds from their trades replaced with coins.");
        this.configOption(LCConfig.COMMON.changeWanderingTrades,"Change Wandering Trader Offers","Whether the wandering trader should have the emeralds from their trades replaced with the default replacement coin.");
        //this.configOption(LCConfig.COMMON.defaultEmeraldReplacementMod,"Default Replacement Item","The default coin to replace a trades emeralds with.");
        //this.configOption(LCConfig.COMMON.professionEmeraldReplacementOverrides,"Profession Replacement Items","List of replacement coin overrides for each villager profession.");
        //Loot
        this.configSection(LCConfig.COMMON,"loot","Loot Options");
        this.configOption(LCConfig.COMMON.lootItem1,"Loot Item Tier 1","Tier 1 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":1, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        this.configOption(LCConfig.COMMON.lootItem2,"Loot Item Tier 2","Tier 2 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":2, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        this.configOption(LCConfig.COMMON.lootItem3,"Loot Item Tier 3","Tier 3 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":3, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        this.configOption(LCConfig.COMMON.lootItem4,"Loot Item Tier 4","Tier 4 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":4, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        this.configOption(LCConfig.COMMON.lootItem5,"Loot Item Tier 5","Tier 5 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":5, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        this.configOption(LCConfig.COMMON.lootItem6,"Loot Item Tier 6","Tier 6 loot item.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":6, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.");
        //Loot -> Entities
        this.configSection(LCConfig.COMMON,"loot.entities","Entity Loot Settings");
        this.configOption(LCConfig.COMMON.enableEntityDrops,"Enabled","Whether coins can be dropped by entities.");
        this.configOption(LCConfig.COMMON.allowSpawnerEntityDrops,"Spawner Drops","Whether coins can be dropped by entities that were spawned by the vanilla spawner.");
        this.configOption(LCConfig.COMMON.allowFakePlayerCoinDrops,"Fake Player Drops","Whether modded machines that emulate player behaviour can trigger coin drops from entities.",
                "Set to false to help prevent autmated coin farming.");
        //Loot -> Entities -> Lists
        this.configSection(LCConfig.COMMON,"loot.entities.lists","Entity Drop Lists","Accepts the following inputs:",
                "Entity IDs. e.g. \"minecraft:cow\"",
                "Entity Tags. e.g. \"#minecraft:skeletons\"",
                "Every entity provided by a mod. e.g. \"minecraft:*\"",
                "Note: If an entity meets multiple criteria, it will drop the lowest tier loot that matches (starting with normal T1 -> T6 then boss T1 -> T6)");
        this.configOption(LCConfig.COMMON.entityDropsT1,"Tier 1","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier1\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.entityDropsT2,"Tier 2","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier2\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.entityDropsT3,"Tier 3","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier3\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.entityDropsT4,"Tier 4","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier4\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.entityDropsT5,"Tier 5","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier5\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.entityDropsT6,"Tier 6","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier6\" loot table.","Requires a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT1,"Boss Tier 1","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier1\" loot table.","Does NOT require a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT2,"Boss Tier 2","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier2\" loot table.","Does NOT require a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT3,"Boss Tier 3","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier3\" loot table.","Does NOT require a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT4,"Boss Tier 4","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier4\" loot table.","Does NOT require a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT5,"Boss Tier 5","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier5\" loot table.","Does NOT require a player kill to trigger coin drops.");
        this.configOption(LCConfig.COMMON.bossEntityDropsT6,"Boss Tier 6","List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier6\" loot table.","Does NOT require a player kill to trigger coin drops.");
        //Loot -> Chest
        this.configSection(LCConfig.COMMON,"loot.chests","Chest Loot Settings");
        this.configOption(LCConfig.COMMON.enableChestLoot,"Enabled","Whether coins can spawn in chests.");
        //Loot -> Chest -> Lists
        this.configSection(LCConfig.COMMON,"loot.chests.lists","Chest Spawn Lists");
        this.configOption(LCConfig.COMMON.chestDropsT1,"Tier 1","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier1\" loot table.");
        this.configOption(LCConfig.COMMON.chestDropsT2,"Tier 2","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier2\" loot table.");
        this.configOption(LCConfig.COMMON.chestDropsT3,"Tier 3","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier3\" loot table.");
        this.configOption(LCConfig.COMMON.chestDropsT4,"Tier 4","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier4\" loot table.");
        this.configOption(LCConfig.COMMON.chestDropsT5,"Tier 5","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier5\" loot table.");
        this.configOption(LCConfig.COMMON.chestDropsT6,"Tier 6","List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier6\" loot table.");
        //Structure Settings
        this.configSection(LCConfig.COMMON,"structures","Structure Settings","Requires a /reload command to be applied correctly");
        this.configOption(LCConfig.COMMON.structureVillageHouses,"Spawn Custom Village Houses","Whether new village structures will have a chance to spawn in vanilla villages");
        this.configOption(LCConfig.COMMON.structureAncientCity,"Spawn Custom Ancient City Pieces","Whether new structures will have a chance to spawn in ancient cities");
        //Mixin
        this.configSection(LCConfig.COMMON,"mixins","Mixin Options");
        this.configOption(LCConfig.COMMON.interceptGiveCommand,"Intercept Give Command","Whether coins given to a player with the /give command should be intercepted by the players wallet if one is equipped");
        this.configOption(LCConfig.COMMON.interceptInventoryHelper,"Intecept Item Helper","Whether coins give to the player via Forges ItemHandlerHelper utilities should be intercepted by the players wallet if one is equipped");
        //Compat
        this.configSection(LCConfig.COMMON,"compat","Mod Compat Options");
        this.configOption(LCConfig.COMMON.compatImpactor,"Enable Compactor Node","Whether the Impactor compat will be initialized.",
                "Requires a full reboot for changes to be applied!");

        //Server Config
        this.configName(LCConfig.SERVER,"Server Config");
        //Notification
        this.configSection(LCConfig.SERVER,"notifications","Notification Settings");
        this.configOption(LCConfig.SERVER.notificationLimit,"Notification Limit",
                "The maximum number of notifications each player and/or machine can have before old entries are deleted.",
                "Lower if you encounter packet size problems.");
        //Machine Protection
        this.configSection(LCConfig.SERVER,"machine_protection","Machine Protection Settings");
        this.configOption(LCConfig.SERVER.safelyEjectMachineContents,"Safe Ejection",
                "Whether illegally broken traders (such as being replaced with /setblock, or modded machines that break blocks) will safely eject their block/items into a temporary storage area for the owner to collect safely.",
                "If disabled, illegally broken traders will throw their items on the ground, and can thus be griefed by modded machines.",
                "Value ignored if anarchyMode is enabled!");
        this.configOption(LCConfig.SERVER.anarchyMode,"Anarchy Mode",
                "Whether block break protection will be disabled completely.",
                "Enable with caution as this will allow players to grief and rob other players shops and otherwise protected machinery.");
        this.configOption(LCConfig.SERVER.quarantinedDimensions,"Quarantined Dimensions",
                "A list of dimension ids that are quarantined from all cross-dimensional interactions.",
                "This includes disabling Trader Interfaces, Network Traders & Terminals (personal trader interactions & cash registers will still function), and all Bank Account access.",
                "Mostly intended to be used to allow the existence of 'Creative Dimensions' where money can be cheated in by your average player, but should not affect a players items/bank balance in the 'normal' dimensions.");
        //Coin Mint
        this.configSection(LCConfig.SERVER,"coin_mint","Coin Mint Settings");
        this.configOption(LCConfig.SERVER.coinMintDefaultDuration,"Mint Duration",
                "Default number of ticks it takes to process a Coin Mint recipe.",
                "Does not apply to Coin Mint recipes with a defined \"duration\" input.");
        this.configOption(LCConfig.SERVER.coinMintSoundVolume,"Volume","The volume of the noise played whenever the Coin Mint finishes the crafting process.");
        //Wallet Settings
        this.configSection(LCConfig.SERVER,"wallet","Wallet Settings");
        this.configOption(LCConfig.SERVER.walletDropRate,"Wallet Drop Rate",
                "The percentage of the wallets contents that will be dropped upon the players death.",
                "Still triggers regardless of the keepInventory or keepWallet game rules.");
        this.configOption(LCConfig.SERVER.walletCanExchange, "Exchange Ability List",
                "The lowest level wallet capable of exchanging coins.");
        this.configOption(LCConfig.SERVER.walletCanPickup,"Pickup Ability List",
                "The lowest level wallet capable of automatically collecting coins while equipped.");
        this.configOption(LCConfig.SERVER.walletCanBank,"Bank Ability List",
                "The lowest level wallet capable of allowing transfers to/from your bank account.");
        this.configOption(LCConfig.SERVER.walletCapacityUpgradeable,"Capacity is Upgradeable",
                "Whether wallets can have additional slots added by using an upgrade item on them from their items",
                "By default diamonds are the only valid upgrade item, but this can be changed by a datapack");
        this.configOption(LCConfig.SERVER.walletDropsManualSpawn,"Manully Spawn Drops",
                "Whether Wallet Drops should be manually spawned into the world instead of the default behaviour of being passed to the PlayerDropsEvent",
                "Wallet Drops will be either the Wallet itself, or the coins dropped when the `coinDropPercent` game rule is greater than 0.");
        //Money Bag
        this.configSection(LCConfig.SERVER,"money_bag","Money Bag Settings");
        this.configOption(LCConfig.SERVER.moneyBagBaseAttack,"Base Attack","The base Attack Damage that an empty Money Bag will have (not counting the base 1 attack damage the player has)");
        this.configOption(LCConfig.SERVER.moneyBagAttackPerSize,"Scaling Attack","The additional Attack Damage added by each additional size (up to a size of 3)");
        this.configOption(LCConfig.SERVER.moneyBagBaseAtkSpeed,"Base Attack Speed","The base Attack Speed that an empty Money Bag will have (not counting the base 4 attack speed the player has)",
                "Is negative because you typically want to make weapons such as these attack slower (vanilla sword attack speed is 1.5, which can be obtained with a value of -2.5)");
        this.configOption(LCConfig.SERVER.moneyBagAtkSpeedPerSize,"Scaling Attack Speed",
                "The additional Attack Speed added by each additional size (up to a size of 3)",
                "Is negative because you typically want to make weapons such as these attack slower",
                "Note: If the total attack speed additions are more than -4.0, the player will be unable to get a full-strength attack with that size of Money Bag.");
        this.configOption(LCConfig.SERVER.moneyBagBaseFallDamage,"Base Fall Damage","The base fall damage per distance an empty Money Bag will have");
        this.configOption(LCConfig.SERVER.moneyBagFallDamagerPerSize,"Scaling Fall Damage","The additional fall damage per distance added by each additional size (up to a size of 3)");
        this.configOption(LCConfig.SERVER.moneyBagMaxFallDamageBase,"Base Fall Damage Limit","The base upper limit for fall damage that an empty Money Bag will have");
        this.configOption(LCConfig.SERVER.moneyBagMaxFallDamagePerSize,"Scaling Fall Damage Limit","The additional upper limit for fall damage added by each additional size (up to a size of 3)");
        this.configOption(LCConfig.SERVER.moneyBagCoinLossChance,"Coin Loss Chance",
                "The chance of the Money Bag dropping a random coin when it's used to attack another entity or when it falls a significant distance",
                "0.0 is a 0% chance, and 1.0 is a 100% chance");
        this.configOption(LCConfig.SERVER.moneyBagCoinLossFallDistance,"Coin Loss Fall Distance","The minimum distance a Money Bag must fall before it has a chance to drop coins when it lands");
        //Upgrades
        this.configSection(LCConfig.SERVER,"upgrades","Upgrade Settings");
        //Upgrades -> Item Capacity
        this.configSection(LCConfig.SERVER,"upgrades.item_capacity","Item Capacity Upgrade");
        this.configOption(LCConfig.SERVER.itemCapacityUpgrade1,"T1 Capacity","The amount of item storage added by the Item Capacity Upgrade (Iron)");
        this.configOption(LCConfig.SERVER.itemCapacityUpgrade2,"T2 Capacity","The amount of item storage added by the Item Capacity Upgrade (Gold)");
        this.configOption(LCConfig.SERVER.itemCapacityUpgrade3,"T3 Capacity","The amount of item storage added by the Item Capacity Upgrade (Diamond)");
        this.configOption(LCConfig.SERVER.itemCapacityUpgrade4,"T4 Capacity","The amount of item storage added by the Item Capacity Upgrade (Netherite)");
        //Upgrades -> Interaction
        this.configSection(LCConfig.SERVER,"upgrades.interaction_upgrade","Interaction Upgrade");
        this.configOption(LCConfig.SERVER.interactionUpgrade1,"T1 Bonus Selections","The amount of bonus selections added by the Interaction Upgrade (Emerald)");
        this.configOption(LCConfig.SERVER.interactionUpgrade2,"T2 Bonus Selections","The amount of bonus selections added by the Interaction Upgrade (Diamond)");
        this.configOption(LCConfig.SERVER.interactionUpgrade3,"T3 Bonus Selections","The amount of bonus selections added by the Interaction Upgrade (Netherite)");
        //Upgrades -> Money Chest Magnet
        this.configSection(LCConfig.SERVER,"upgrades.money_chest_magnet","Money Chest Magnet Upgrade");
        this.configOption(LCConfig.SERVER.coinChestMagnetRange1,"T1 Magnet Radius","The radius (in meters) of the Money Chest Magnet Upgrade (Copper)'s coin collection.");
        this.configOption(LCConfig.SERVER.coinChestMagnetRange2,"T2 Magnet Radius","The radius (in meters) of the Money Chest Magnet Upgrade (Iron)'s coin collection.");
        this.configOption(LCConfig.SERVER.coinChestMagnetRange3,"T3 Magnet Radius","The radius (in meters) of the Money Chest Magnet Upgrade (Gold)'s coin collection.");
        this.configOption(LCConfig.SERVER.coinChestMagnetRange4,"T4 Magnet Radius","The radius (in meters) of the Money Chest Magnet Upgrade (Emerald)'s coin collection.");
        //Enchantments
        this.configSection(LCConfig.SERVER,"enchantments","Enchantment Settings");
        this.configOption(LCConfig.SERVER.enchantmentTickDelay,"Tick Delay",
                "The delay (in ticks) between Money Mending & Coin Magnet ticks.",
                "Increase if my enchantments are causing extreme lag.",
                "Note: 20 ticks = 1s");
        this.configOption(LCConfig.SERVER.moneyMendingRepairCost,"MM Repair Cost","The cost required to repair a single item durability point with the Money Mending enchantment.");
        this.configOption(LCConfig.SERVER.coinMagnetBaseRange,"CM Base Range","The coin collection radius of the Coin Magnet I enchantment.");
        this.configOption(LCConfig.SERVER.coinMagnetLeveledRange,"CM Leveled Range","The increase in the coin collection radius added by each additional level of the Coin Magnet enchantment.");
        this.configOption(LCConfig.SERVER.coinMagnetCalculationCap,"CM Calculation Cap",
                "The final level of Coin Magnet that will result in increased range calculations.",
                "Increase if you have another mod that increases the max level of the Coin Magnet enchantment",
                "and you wish for those levels to actually apply an effect.");
        //Auction House
        this.configSection(LCConfig.SERVER,"auction_house","Auction House Settings");
        this.configOption(LCConfig.SERVER.auctionHouseEnabled,"Enabled",
                "Whether the Auction House will be automatically generated and accessible.",
                "If disabled after players have interacted with it, items & money in the auction house cannot be accessed until re-enabled.",
                "If disabled, it is highly recommended that you also disable the Crafting -> Auction Stand option in the common config.");
        this.configOption(LCConfig.SERVER.auctionHouseOnTerminal,"Show On Terminal",
                "Whether the Auction House will appear in the trading terminal.",
                "If false, you will only be able to access the Auction House from an Auction Stand.");
        this.configOption(LCConfig.SERVER.auctionHouseDurationMin,"Min Auction Duration",
                "The minimum number of days an auction can have its duration set to.",
                "If given a 0 day minimum, the minimum auction duration will be 1 hour.");
        this.configOption(LCConfig.SERVER.auctionHouseDurationMax,"Max Auction Duration","The maxumim number of day an auction can have its duration set to.");
        this.configOption(LCConfig.SERVER.auctionHouseAllowOwnerBidding,"Allow Owner Bids","Whether the players are allowed to bid on their own Auctions");
        this.configOption(LCConfig.SERVER.auctionHouseAllowDoubleBidding,"Allow Double Bids","Whether players are allowed to bid on an auction when they were also the previous bidder");
        this.configOption(LCConfig.SERVER.auctionHouseFeePercentage,"Auction % Fee","The percentage of the final bid that will be collected as a fee instead of being given to the auctions owner when the auction is completed");
        this.configOption(LCConfig.SERVER.auctionHouseSubmitPrice,"Auction Submit Fee",
                "A flat fee paid on the creation of an auction by the player submitting it",
                "If they are unable to pay this fee, they will not be able to submit any auctions");
        this.configOption(LCConfig.SERVER.auctionHouseStoreFeeInServerTax,"Store Auction Fee",
                "Whether the auction fees collected should be stored in the server-wide tax collector as taxes",
                "Includes both the submission fee and the fee taken from the final bid",
                "Useful for those who wish to keep track of auction fees collected, and/or don't want money to destroyed in this process");
        this.configOption(LCConfig.SERVER.auctionHousePlayerLimit,"Auction Limit",
                "The maximum number of pending auctions each player is allowed to have",
                "Set to 0 to only allow auctions to be posted by admins in LC Admin Mode, or to limit the Auction House to only Persistent Auctions");
        //Bank Account
        this.configSection(LCConfig.SERVER,"bank_accounts","Bank Account Settings");
        this.configOption(LCConfig.SERVER.bankAccountInterestRate,"Interest Rate",
                "The interest rate that bank accounts will earn just by existing.",
                "Setting to 0 will disable interest and all interest-related ticks from happening.",
                "Note: Rate of 1.0 will result in doubling the accounts money each interest tick.",
                "Rate of 0.01 is equal to a 1% interest rate.");
        this.configOption(LCConfig.SERVER.bankAccountForceInterest,"Force Some Interest",
                "Whether interest applied to small amounts of money are guaranteed to give at least *some* money as long as there's money in the account.",
                "Example 1% interest applied to a bank account with only 1 copper coin will always give *at least* 1 copper coin.");
        this.configOption(LCConfig.SERVER.bankAccountInterestNotification,"Interest Notification",
                "Whether players will receive a personal notification whenever their bank account collects interest.",
                "Regardless of this value, the bank accounts logs will always display the interest interaction.");
        this.configOption(LCConfig.SERVER.bankAccountInterestTime,"Interest Delay",
                "The number of minecraft ticks that will pass before interest is applied.",
                "Helpful Notes:",
                "1s = 20 ticks",
                "1m = 1200 ticks",
                "1h = 72000 ticks",
                "1 day = 1728000 ticks",
                "1 week = 12096000 ticks",
                "30 days = 51840000 ticks",
                "365 days = 630720000 ticks");
        this.configOption(LCConfig.SERVER.bankAccountInterestLimits,"Interest Limits",
                "A list of upper interest limits.",
                "Example:",
                "Adding \"1n\" to this list will make it so that players will get no more than 1 netherite coin worth of interest even if they would normally get more.");
        this.configOption(LCConfig.SERVER.bankAccountInterestBlacklist,"Interest Blacklist",
                "A list of Money Value unique ids that should not have interest applied to them.",
                "Example:",
                "Adding \"lightmanscurrency:coins!chocolate_coins\" will prevent chocolate coins from getting interest,",
                "Adding \"lightmanscurrency:coins!*\" will prevent all built-in money types from getting interest");
        //Network Terminal
        this.configSection(LCConfig.SERVER,"terminal","Network Terminal Settings");
        this.configOption(LCConfig.SERVER.openTerminalCommand,"Terminal Command","Whether the /lcterminal command will exist allowing players to access the Trading Terminal without the physical item/block");
        this.configOption(LCConfig.SERVER.moveUnnamedTradersToBottom,"Sort Unnamed To Bottom","Whether Traders with no defined Custom Name will be sorted to the bottom of the Trader list on the Network Terminal.");
        //Paygate
        this.configSection(LCConfig.SERVER,"paygate","Paygate Settings");
        this.configOption(LCConfig.SERVER.paygateMaxDuration,"Max Duration","The maximum number of ticks that a paygate can be set to output a redstone signal for.");
        //Command Trader
        this.configSection(LCConfig.SERVER,"command_trader","Command Trader Settings");
        this.configOption(LCConfig.SERVER.commandTraderPlacementPermission,"Placement Permission","The permission level required to place the command trader block");
        this.configOption(LCConfig.SERVER.commandTraderMaxPermissionLevel,"Max Command Permission","The maximum permission level that can be set and used by a command trader");
        //Player Trading Options
        this.configSection(LCConfig.SERVER,"player_trading","Player <-> Player Trading Options");
        this.configOption(LCConfig.SERVER.playerTradingRange,"Trading Range",
                "The maximum distance allowed between players in order for a player trade to persist.",
                "-1 will always allow trading regardless of dimension.",
                "0 will allow infinite distance but require that both players be in the same dimension.");
        //Taxes
        this.configSection(LCConfig.SERVER,"taxes","Tax Settings");
        this.configOption(LCConfig.SERVER.taxCollectorAdminOnly,"Admin Only Activation",
                "Whether Tax Collectors can only be activated by an Admin in LC Admin Mode.",
                "Will not prevent players from crafting, placing, or configuring Tax Collectors.");
        this.configOption(LCConfig.SERVER.taxCollectorMaxRate,"Max Tax Rate",
                "The maximum tax rate (in %) a Tax Collector is allowed to enforce.",
                "Note: The sum of multiple tax collectors rates can still exceed this number.",
                "If a machine reaches a total tax rate of 100% it will forcibly prevent all monetary interactions until this is resolved.");
        this.configOption(LCConfig.SERVER.taxCollectorMaxRadius,"Max Radius","The maximum radius of a Tax Collectors area in meters.");
        this.configOption(LCConfig.SERVER.taxCollectorMaxHeight,"Max Height","The maximum height of a Tax Collectors area in meters.");
        this.configOption(LCConfig.SERVER.taxCollectorMaxVertOffset,"Max Vert Offset",
                "The maximum vertical offset of a Tax Collectors area in meters.",
                "Note: Vertical offset can be negative, so this will also enforce the lowest value.");
        //Chocolate Coins
        this.configSection(LCConfig.SERVER,"chocolate_coins","Chocolate Coin Settings");
        this.configOption(LCConfig.SERVER.chocolateCoinEffects,"Chocolate Effects","Whether the Chocolate Coins will give players custom potion and/or healing effects on consumption.");
        //Model Variants
        this.configSection(LCConfig.SERVER,"model_variants","Model Variant Settings");
        this.configOption(LCConfig.SERVER.variantBlacklist,"Variant Blacklist","A list of Model Variant ids that will be hidden from the Variant Select Menu on the client, and cannot be selected in said menu.");
        //Compat
        this.configSection(LCConfig.SERVER,"compat","Mod Compatibility Options");
        //Compat -> Claim Purchasing
        this.configSection(LCConfig.SERVER,"compat.claim_purchasing","Claim Purchasing Settings","Settings for compatbility with FTB Chunks, Cadmus, and Flan");
        this.configOption(LCConfig.SERVER.claimingAllowClaimPurchase,"Can Purchase Claims","Whether the `/lcclaims buy claim` command will be accessible to players.");
        this.configOption(LCConfig.SERVER.claimingClaimPrice,"Claim Price","The price per claim chunk purchased.");
        this.configOption(LCConfig.SERVER.claimingMaxClaimCount,"Max Bonus Claims",
                "The maximum number of extra claim chunks allowed to be purchased with this command.",
                "Note: This count includes extra claim chunks given to the player/team via normal FTB Chunks methods as well (if applicable).");
        this.configOption(LCConfig.SERVER.claimingAllowForceloadPurchase,"Can Purchase Forceloads","Whether the `/lcclaims buy forceload` command will be accessible to players.");
        this.configOption(LCConfig.SERVER.claimingForceloadPrice,"Forceload Price","The price per forceload chunk purchased.");
        this.configOption(LCConfig.SERVER.claimingMaxForceloadCount,"Max Bonus Forceloads",
                "The maximum number of extra forceload chunks allowed to be purchased with this command.",
                "Note: This count includes extra forceload chunks given to the player/team via normal FTB Chunks methods as well (if applicable).");
        //Compat -> Claim Purchasing -> Flan
        this.configSection(LCConfig.SERVER,"compat.claim_purchasing.flan","Flan Settings");
        this.configOption(LCConfig.SERVER.flanClaimingBlocksPerChunk,"Blocks per 'Chunk'","Blocks that will be added with each 'claim' purchased");
        //1.20.1 exclusive here perhaps?


    }

    protected String getColorName(VanillaColor color) { return EnumHelper.prettyName(color); }
    protected String getWoodName(WoodType wood) { return wood.displayName; }

    protected final void item(ItemLike item, String translation) { this.add(item.asItem(),translation); }
    protected final void itemPlural(ItemLike item,String translation) { this.add(item.asItem().getDescriptionId() + ".plural",translation); }
    protected final void itemInitial(ItemLike item,String translation) { this.add(item.asItem().getDescriptionId() + ".initial",translation); }
    protected final void itemWithPlural(ItemLike item,String name) { this.itemWithPlural(item,name,this.makePlural(name)); }
    protected final void itemWithPlural(ItemLike item,String name,String plural) { this.item(item,name); this.itemPlural(item,plural); }
    protected final void itemWithInitial(ItemLike item,String name) { this.itemWithInitial(item,name,this.makeInitial(name)); }
    protected final void itemWithInitial(ItemLike item,String name,String initial) { this.item(item,name); this.itemInitial(item,initial); }
    protected final void itemWithPluralAndInitial(ItemLike item,String name) { this.itemWithPluralAndInitial(item,name,this.makePlural(name),this.makeInitial(name)); }
    protected final void itemWithPluralAndInitial(ItemLike item,String name,String plural,String initial) { this.item(item,name); this.itemPlural(item,plural); this.itemInitial(item,initial); }

    protected final void block(Supplier<? extends Block> block, String translation) { this.add(block.get(),translation); }
    protected final void blockPlural(Supplier<? extends Block> block, String translation) { this.add(block.get().getDescriptionId() + ".plural",translation); }
    protected final void blockInitial(Supplier<? extends Block> block, String translation) { this.add(block.get().getDescriptionId() + ".initial",translation); }
    protected final void blockWithPlural(Supplier<? extends Block> block,String name) { this.blockWithPlural(block,name,this.makePlural(name)); }
    protected final void blockWithPlural(Supplier<? extends Block> block,String name,String plural) { this.block(block,name); this.blockPlural(block,plural); }
    protected final void blockWithInitial(Supplier<? extends Block> block,String name) { this.blockWithInitial(block,name,this.makeInitial(name)); }
    protected final void blockWithInitial(Supplier<? extends Block> block,String name,String initial) { this.block(block,name); this.blockInitial(block,initial); }
    protected final void blockWithPluralAndInitial(Supplier<? extends Block> block,String name) { this.blockWithPluralAndInitial(block,name,this.makePlural(name),this.makeInitial(name));}
    protected final void blockWithPluralAndInitial(Supplier<? extends Block> block,String name,String plural,String initial) { this.block(block,name); this.blockPlural(block,plural); this.blockInitial(block,initial); }

    protected String makePlural(String name) { return name + "s"; }
    protected String makeInitial(String name) { return name.substring(0,1).toLowerCase(); }

    protected final void coloredBlock(DeferredHolderBundle<VanillaColor,Block,? extends Block> bundle, Function<String,String> translation) {
        bundle.forEach((color,block) ->
            this.add(block,translation.apply(this.getColorName(color))));
    }

    protected final void woodenBlock(DeferredHolderBundle2<WoodType,?,Block,? extends Block> bundle,Function<String,String> translation) {
        Set<WoodType> translated = new HashSet<>();
        bundle.forEach((type,x,block) -> {
            if(!translated.contains(type))
            {
                translated.add(type);
                this.add(block,translation.apply(this.getWoodName(type)));
            }
        });
    }

    protected final void woodenBlock(DeferredHolderBundle<WoodType,Block,? extends Block> bundle,Function<String,String> translation) {
        bundle.forEach((type,block) ->
                this.add(block,translation.apply(this.getWoodName(type))));
    }

    protected final void enchantment(ResourceKey<Enchantment> enchantment,String translation,String description) { this.enchantment(enchantment.identifier(),translation,description); }
    protected final void enchantment(Identifier enchantment,String translation,String description) {
        String key = Util.makeDescriptionId("enchantment",enchantment);
        this.add(key,translation);
        this.add(key + ".description",description);
    }

    protected final void tradeRule(TradeRuleType<?> type,String translation) { this.tradeRule(type.getKey(),translation); }
    protected final void tradeRule(Identifier type,String translation) { this.add(TradeRule.translationKeyOfType(type),translation); }

    protected final void permission(Permission<?> permission,String translation,String tooltip) {
        this.add(permission.getDescriptionID(),translation);
        this.add(permission.getTooltipID(),tooltip);
    }
    protected final <T extends Enum<T> & StringRepresentable> void permissionEntry(Permission<T> permission,T key,String translation) {
        this.add(EnumPermissionType.getEntryKey(permission,key),translation);
    }

    protected final void text(SingletonNotificationCategory category,String translation) { this.text(category.text,translation); }
    protected final void text(TextEntry text,String translation) { this.add(text.getKey(),translation); }
    protected final void text(DualTextEntry text,String firstTranslation,String secondTranslation) {
        this.text(text.first,firstTranslation);
        this.text(text.second,secondTranslation);
    }
    protected final void text(MultiLineTextEntry text,String... translations) {
        int index = 0;
        for(String line : translations)
            this.add(text.getKey(index++),line);
    }

    protected final void statKeyName(StatKey<?,?> key,String name) {
        this.add(StatKey.getDescriptionID(key.key()),name);
    }
    protected final void statKeyTooltip(StatKey<?,?> key,String tooltip) {
        this.add(StatKey.getTooltipID(key.key()),tooltip);
    }
    protected final void statKey(StatKey<?,?> key,String name,String tooltip) {
        this.statKeyName(key,name);
        this.statKeyTooltip(key,tooltip);
    }

    protected final void timeText(TimeUnitTextEntry entry,String fullName,String pluralName,String initial) {
        this.text(entry.fullText,fullName);
        this.text(entry.pluralText,pluralName);
        this.text(entry.shortText,initial);
    }

    protected final void configName(ConfigFile file,String translation) {
        this.add(ConfigFile.translationForFile(file.getFileID()),translation);
    }

    protected final void configSection(ConfigFile file,String section,String name,String... tooltip) {
        this.add(ConfigFile.translationForSection(file.getFileID(),section),name);
        this.configComment(file,section,tooltip);
    }

    protected final void configOption(ConfigOption<?> option,String name,String... comments) {
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalStateException("Cannot translate the config option as its config file has not been initialized!");
        String optionKey = option.getFullName();
        this.add(ConfigFile.translationForOption(file.getFileID(),optionKey),name);
        this.configComment(file,optionKey,comments);
    }

    protected final void configComment(ConfigFile file,String section,String... comments) {
        this.text(new MultiLineTextEntry(ConfigFile.translationForComment(file.getFileID(),section)),comments);
    }

}