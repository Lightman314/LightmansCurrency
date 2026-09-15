package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInteractionNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInterestNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.CoinDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValueParser;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.impl.EmptyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.notifications.category.SingletonNotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.api.text.*;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.CopiedTrader;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NotificationNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
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
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.InfoTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.MoneyStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.client.features.resources.BuiltInResourcePacks;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.CoinExchangeTab;
import io.github.lightman314.lightmanscurrency.features.chocolate_coins.ChocolateCoinItem;
import io.github.lightman314.lightmanscurrency.features.colors.ColorDisplay;
import io.github.lightman314.lightmanscurrency.features.commands.LCAdminCommand;
import io.github.lightman314.lightmanscurrency.features.commands.LCConfigCommand;
import io.github.lightman314.lightmanscurrency.features.enchantments.LCEnchantments;
import io.github.lightman314.lightmanscurrency.features.enchantments.MoneyMendingEnchantmentHelper;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.notifications.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.AbstractWalletMenu;
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

        //Display Cases
        this.coloredBlock(LCBlocks.DISPLAY_CASE,color -> color + " Display Case");
        this.woodenBlock(LCBlocks.CARD_DISPLAY,wood -> wood + " Card Display");

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
        //Owner Settings
        this.text(OwnerNode.NAME,"Trader Ownership");
        this.text(OwnerNode.VALUE_OWNER,"Trader Owner");

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

        //Info
        this.text(InfoTab.TOOLTIP,"Trader Info");

        this.text(NotificationNode.TOOLTIP_TRADER_LOGS,"Trader Logs");
        this.text(NotificationNode.TOOLTIP_TRADER_LOGS_SETTINGS,"Settings Logs");


        this.text(ItemTradesNode.SECTION_TITLE,"Item Trades");
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
        this.permission(BuiltInPermissions.OPEN_STORAGE,"Open Storage","Allows player to access the Storage Menu and add or remove stock from the traders storage.");
        this.permission(BuiltInPermissions.EDIT_DISPLAY,"Display Settings","Allows player to change the traders display name & custom icon.");
        this.permission(BuiltInPermissions.EDIT_TRADES,"Modify Trades","Allows the player to create, remove, and modify the trades.");
        this.permission(BuiltInPermissions.EDIT_TRADE_RULES,"Edit Trade Rules","Allows the player to activate, deactivate, and otherwise edit the Trade Rules attached to this trader or its trades. Modify Trade Permissions are still required to edit Trade-Specific rules.");
        this.permission(BuiltInPermissions.COLLECT_MONEY,"Collect Money","Allows the player to collect money from this traders internal money storage.");
        this.permission(BuiltInPermissions.STORE_MONEY,"Store Money","Allows the player to deposit money into this traders internal money storage.");
        this.permission(BuiltInPermissions.EDIT_SETTINGS,"General Settings","Allows the player to access the traders settings tab, and modify any setting not explicitly locked behind a different permission.");
        this.permission(BuiltInPermissions.ADD_REMOVE_ALLIES,"Add/Remove Allies","Allows the player to add or remove other players (or themselves) from the list of Allies.");
        this.permission(BuiltInPermissions.EDIT_ALLY_PERMS,"Edit Permissions","Allows the player to edit the permissions that allies have access to (exactly as you are doing now).");
        this.permission(BuiltInPermissions.VIEW_LOGS,"Manage Logs","Allows the player to view this machines interaction logs. Higher levels allow deletion of undesired logs as well.");
        this.permissionEntry(BuiltInPermissions.VIEW_LOGS,TriStatePermission.NONE,"None");
        this.permissionEntry(BuiltInPermissions.VIEW_LOGS,TriStatePermission.LOW,"View");
        this.permissionEntry(BuiltInPermissions.VIEW_LOGS,TriStatePermission.HIGH,"View & Delete");
        this.permission(BuiltInPermissions.BREAK_TRADER,"Break or Move Machine","Allows the player to move this machines block. 'Delete' access required to fully delete and destroy the trader.");
        this.permissionEntry(BuiltInPermissions.BREAK_TRADER, TriStatePermission.NONE,"None");
        this.permissionEntry(BuiltInPermissions.BREAK_TRADER, TriStatePermission.LOW,"Move");
        this.permissionEntry(BuiltInPermissions.BREAK_TRADER, TriStatePermission.HIGH,"Move & Delete");
        this.permission(BuiltInPermissions.TRANSFER_OWNERSHIP,"Transfer Ownership","Allows the player to change the owner of this machine.");
        //TODO Trader Interface Permission
        this.permission(BuiltInPermissions.EXTERNAL_ACCESS_SETTINGS,"External Access Settings","Allows the player to change with physical sides of the machine can have product inserted or extracted through automation.");
        //TODO CC Authorization Permission

        //Notifications
        this.text(ItemHelper.NOTIFICATION_ITEM_FORMAT,"%1$sx %2$s");
        this.text(OutOfStockNotification.TEXT,"Trade #%2$s is out of stock");
        this.text(OutOfStockNotification.TEXT_INDEXLESS,"Trader is out of stock");
        this.text(ItemTradeNotification.TEXT,"%1$s %2$s %3$s for %4$s");

        this.text(LowBalanceNotification.TEXT,"Bank Account is below %s");
        this.text(BankInteractionNotification.TEXT_WITHDRAW,"%1$s withdrew %2$s");
        this.text(BankInteractionNotification.TEXT_DEPOSIT,"%1$s deposited %2$s");
        this.text(BankInteractionNotification.TEXT_INTERACTION_SERVER,"An admin");
        this.text(BankInterestNotification.TEXT,"Gained %s in interest");

        this.text(TaxesPaidNotification.TEXT,"%s was paid in taxes");

        this.text(ChangeSettingNotification.Dumb.TEXT,"%1$s changed %2$s");
        this.text(ChangeSettingNotification.Simple.TEXT,"%1$s changed %2$s to %3$s");
        this.text(ChangeSettingNotification.Advanced.TEXT,"%1$s changed %2$s from %3$s to %4$s");

        //Commands
        this.text(CoinValueParser.ARGUMENT_MONEY_VALUE_NOT_A_COIN,"'%s' is not a valid coin!");
        this.text(CoinValueParser.ARGUMENT_MONEY_VALUE_DIFFERENT_CHAIN,"'%1$s' is not on the same coin chain (was '%2$s', expected '%3$s'!");
        this.text(MoneyValueParser.ARGUMENT_MONEY_VALUE_NO_VALUE,"Value parsed had no value!");
        this.text(TraderArgument.ARGUMENT_TRADER_NOT_FOUND,"Could not find a trader with the given ID");
        this.text(TraderArgument.ARGUMENT_TRADER_NOT_RECOVERABLE,"Trader is not in a state that would require recovery");

        this.text(LCAdminCommand.TOGGLE_ADMIN,"LC Admin Mode is now %s");
        this.text(LCAdminCommand.TOGGLE_ADMIN_ENABLED,"ENABLED");
        this.text(LCAdminCommand.TOGGLE_ADMIN_DISABLED,"DISABLED");

        this.text(LCConfigCommand.RELOAD,"Reloaded %s Config Files");
        this.text(LCConfigCommand.RELOAD_FILE,"Reloaded %s");

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

    protected final void timeText(TimeUnitTextEntry entry,String fullName,String pluralName,String initial) {
        this.text(entry.fullText,fullName);
        this.text(entry.pluralText,pluralName);
        this.text(entry.shortText,initial);
    }

}