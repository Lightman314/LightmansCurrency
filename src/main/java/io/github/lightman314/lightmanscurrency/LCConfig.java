package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.config.*;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.*;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.*;
import io.github.lightman314.lightmanscurrency.api.events.DroplistConfigGenerator;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.common.config.VillagerTradeModsOption;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.ChestPoolLevel;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.EntityPoolLevel;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMod;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMods;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Supplier;

public final class LCConfig {

    private LCConfig() {}

    public static final Client CLIENT = new Client();
    public static final Common COMMON = new Common();
    public static final Server SERVER = new Server();

    public static void init() {
        CLIENT.confirmSetup();
        COMMON.confirmSetup();
        SERVER.confirmSetup();
    }

    public static final class Client extends ClientConfigFile
    {
        private Client() { super("lightmanscurrency-client"); }

        public final IntOption itemRenderLimit = IntOption.create(Integer.MAX_VALUE, 0);

        public final StringOption timeFormat = StringOption.create("MM/dd/yy hh:mmaa");

        public final ScreenPositionOption walletSlot = ScreenPositionOption.create(76, 43);
        public final ScreenPositionOption walletSlotCreative = ScreenPositionOption.create(126,19);
        public final ScreenPositionOption walletButtonOffset = ScreenPositionOption.create(8,-10);

        public final BooleanOption walletOverlayEnabled = BooleanOption.createTrue();
        public final EnumOption<ScreenCorner> walletOverlayCorner = EnumOption.create(ScreenCorner.BOTTOM_LEFT);
        public final ScreenPositionOption walletOverlayPosition = ScreenPositionOption.create(5,-5);
        public final EnumOption<WalletDisplayOverlay.DisplayType> walletOverlayType = EnumOption.create(WalletDisplayOverlay.DisplayType.ITEMS_WIDE);

        public final ScreenPositionOption notificationAndTeamButtonPosition = ScreenPositionOption.create(152,3);
        public final ScreenPositionOption notificationAndTeamButtonCreativePosition = ScreenPositionOption.create(171,3);

        public final BooleanOption chestButtonVisible = BooleanOption.createTrue();
        public final BooleanOption chestButtonAllowSideChains = BooleanOption.createFalse();

        public final BooleanOption pushNotificationsToChat = BooleanOption.createTrue();

        public final IntOption slotMachineAnimationTime = IntOption.create(100, 20, 1200);
        public final IntOption slotMachineAnimationRestTime = IntOption.create(20, 0, 1200);

        public final BooleanOption moneyMendingClink = BooleanOption.createTrue();

        public final IntOption terminalColumnLimit = IntOption.create(4,2,100);
        public final IntOption terminalRowLimit = IntOption.create(16,4,100);

        @Override
        protected void setup(@Nonnull ConfigBuilder builder) {


            builder.comment("Quality Settings").push("quality");

            builder.comment("Maximum number of items each Item Trader can renderBG (per-trade) as stock. Lower to decrease client-lag in trader-rich areas.",
                            "Setting to 0 will disable item rendering entirely, so use with caution.")
                    .add("itemTraderRenderLimit", this.itemRenderLimit);

            builder.pop();

            builder.comment("Time Formatting Settings").push("time");

            builder.comment("How Notification Timestamps are displayed.","Follows SimpleDateFormat formatting: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html")
                    .add("timeFormatting", this.timeFormat);

            builder.pop();

            builder.comment("Wallet Slot Settings").push("wallet_slot");

            builder.comment("The position that the wallet slot will be placed at in the players inventory.")
                    .add("slot", this.walletSlot);

            builder.comment("The position that the wallet slot will be placed at in the players creative inventory.")
                    .add("creativeSlot", this.walletSlotCreative);

            builder.comment("The offset that the wallet button should be placed at relative to the wallet slot position.")
                    .add("button", this.walletButtonOffset);

            builder.pop();

            builder.comment("Wallet Overlay Settings").push("wallet_hud");

            builder.comment("Whether an overlay should be drawn on your HUD displaying your wallets current money amount.")
                    .add("enabled", this.walletOverlayEnabled);

            builder.comment("The corner of the screen that the overlay should be drawn on.")
                    .add("displayCorner", this.walletOverlayCorner);

            builder.comment("The position offset from the defined corner.")
                    .add("displayOffset", this.walletOverlayPosition);

            builder.comment("Whether the wallets contents should be displayed as a coin item, or as value text.")
                    .add("displayType", this.walletOverlayType);

            builder.pop();

            builder.comment("Network Terminal Settings").push("network_terminal");

            builder.comment("The maximum number of columns the Network Terminal is allowed to display")
                            .add("columnLimit", this.terminalColumnLimit);
            builder.comment("The maximum number of rows the Network Terminal is allowed to display")
                    .add("rowLimit", this.terminalRowLimit);

            builder.pop();

            builder.comment("Inventory Button Settings").push("inventory_buttons");

            builder.comment("The position that the notification & team manager buttons will be placed at in the players inventory.")
                    .add("button", this.notificationAndTeamButtonPosition);

            builder.comment("The position that the notification & team manager buttons will be placed at in the players creative inventory.")
                    .add("buttonCreative", this.notificationAndTeamButtonCreativePosition);

            builder.pop();

            builder.comment("Chest Button Settings").push("chest_buttons");

            builder.comment("Whether the 'Move Coins into Wallet' button will appear in the top-right corner of the Chest Screen if there are coins in the chest that can be collected.")
                    .add("enabled", this.chestButtonVisible);

            builder.comment("Whether the 'Move Coins into Wallet' button should collect coins from a side-chain.",
                            "By default these would be the coin pile and coin block variants of the coins.")
                    .add("allowSideChainCollection", this.chestButtonAllowSideChains);

            builder.pop();

            builder.comment("Notification Settings").push("notification");

            builder.comment("Whether notifications should be posted in your chat when you receive them.")
                    .add("notificationsInChat", this.pushNotificationsToChat);

            builder.pop();

            builder.comment("Slot Machine Animation Settings").push("slot_machine");

            builder.comment("The number of Minecraft ticks the slot machine animation will last.",
                            "Note: 20 ticks = 1 second",
                            "Must be at least 20 ticks (1s) for coding reasons.")
                    .add("animationDuration", this.slotMachineAnimationTime);

            builder.comment("The number of Minecraft ticks the slot machine will pause before repeating the animation.")
                    .add("animationRestDuration", this.slotMachineAnimationRestTime);

            builder.pop();

            builder.comment("Sound Settings").push("sounds");

            builder.comment("Whether Money Mending should make a noise when triggered.")
                    .add("moneyMendingClink", this.moneyMendingClink);

            builder.pop();

        }
    }

    public static final class Common extends ConfigFile
    {
        private Common() { super("lightmanscurrency-common", LoadPhase.SETUP); }

        //Debug Level (in root)
        public final IntOption debugLevel = IntOption.create(0,0,3);

        //Crafting Options
        public final BooleanOption canCraftNetworkTraders = BooleanOption.createTrue();
        public final BooleanOption canCraftTraderInterfaces = BooleanOption.createTrue();
        public final BooleanOption canCraftAuctionStands = BooleanOption.createTrue();
        public final BooleanOption canCraftCoinChest = BooleanOption.createTrue();
        public final BooleanOption canCraftCoinChestUpgradeExchange = BooleanOption.createTrue();
        public final BooleanOption canCraftCoinChestUpgradeMagnet = BooleanOption.createTrue();
        public final BooleanOption canCraftCoinChestUpgradeBank = BooleanOption.createTrue();
        public final BooleanOption canCraftCoinChestUpgradeSecurity = BooleanOption.createTrue();
        public final BooleanOption canCraftTaxBlock = BooleanOption.createTrue();

        //Custom Trades
        public final BooleanOption addCustomWanderingTrades = BooleanOption.createTrue();
        public final BooleanOption addBankerVillager = BooleanOption.createTrue();
        public final BooleanOption addCashierVillager = BooleanOption.createTrue();
        public final BooleanOption changeVanillaTrades = BooleanOption.createFalse();
        public final BooleanOption changeModdedTrades = BooleanOption.createFalse();
        public final BooleanOption changeWanderingTrades = BooleanOption.createFalse();
        public final ConfiguredTradeModOption defaultEmeraldReplacementMod =
                ConfiguredTradeMod.builder()
                        .defaults(ModItems.COIN_EMERALD)
                        .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_EMERALD)
                        .buildOption();
        public final VillagerTradeModsOption professionEmeraldReplacementOverrides = VillagerTradeMods.builder()
                .forProfession(VillagerProfession.BUTCHER)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .forProfession(VillagerProfession.CARTOGRAPHER)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .forProfession(VillagerProfession.FARMER)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .forProfession(VillagerProfession.FISHERMAN)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                //Fletcher will cost iron, but pay copper because stick trades are OP
                .forProfession(VillagerProfession.FLETCHER)
                    .defaultCost(ModItems.COIN_IRON)
                    .defaultResult(ModItems.COIN_COPPER)
                    .costForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON)
                    .resultForRegion(VillagerType.SNOW, ModItems.COIN_CHOCOLATE_COPPER).back()
                .forProfession(VillagerProfession.LEATHERWORKER)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .forProfession(VillagerProfession.MASON)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .forProfession(VillagerProfession.SHEPHERD)
                    .defaults(ModItems.COIN_IRON)
                    .bothForRegion(VillagerType.SNOW,ModItems.COIN_CHOCOLATE_IRON).back()
                .buildOption();

        //Loot Items
        public final ItemOption lootItem1 = ItemOption.create(ModItems.COIN_COPPER);
        public final ItemOption lootItem2 = ItemOption.create(ModItems.COIN_IRON);
        public final ItemOption lootItem3 = ItemOption.create(ModItems.COIN_GOLD);
        public final ItemOption lootItem4 = ItemOption.create(ModItems.COIN_EMERALD);
        public final ItemOption lootItem5 = ItemOption.create(ModItems.COIN_DIAMOND);
        public final ItemOption lootItem6 = ItemOption.create(ModItems.COIN_NETHERITE);

        //Entity Loot
        public final BooleanOption enableEntityDrops = BooleanOption.createTrue();
        public final BooleanOption allowSpawnerEntityDrops = BooleanOption.createFalse();
        public final BooleanOption allowFakePlayerCoinDrops = BooleanOption.createTrue();

        public final StringListOption entityDropsT1 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T1));
        public final StringListOption entityDropsT2 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T2));
        public final StringListOption entityDropsT3 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T3));
        public final StringListOption entityDropsT4 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T4));
        public final StringListOption entityDropsT5 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T5));
        public final StringListOption entityDropsT6 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T6));

        public final StringListOption bossEntityDropsT1 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T1));
        public final StringListOption bossEntityDropsT2 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T2));
        public final StringListOption bossEntityDropsT3 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T3));
        public final StringListOption bossEntityDropsT4 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T4));
        public final StringListOption bossEntityDropsT5 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T5));
        public final StringListOption bossEntityDropsT6 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T6));

        //Chest Loot
        public final BooleanOption enableChestLoot = BooleanOption.createTrue();

        public final StringListOption chestDropsT1 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T1));
        public final StringListOption chestDropsT2 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T2));
        public final StringListOption chestDropsT3 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T3));
        public final StringListOption chestDropsT4 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T4));
        public final StringListOption chestDropsT5 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T5));
        public final StringListOption chestDropsT6 = StringListOption.create(() -> DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T6));

        //Event Options
        public final BooleanOption chocolateEventCoins = BooleanOption.createTrue();
        public final BooleanOption chocolateEventCoinLootDrops = BooleanOption.createTrue();
        public final DoubleOption chocolateCoinDropRate = DoubleOption.create(0.1d,0d,1d);
        public final BooleanOption eventAdvancementRewards = BooleanOption.createTrue();

        @Override
        protected void setup(@Nonnull ConfigBuilder builder) {

            builder.comment("Level of debug messages to be shown in the logs.","0-All debug messages. 1-Warnings/Errors only. 2-Errors only. 3-No debug messages.","Note: All debug messages will still be sent debug.log regardless of settings.")
                    .add("debugLevel", this.debugLevel);

            builder.comment("Crafting Settings","/reload required for any changes made to take effect.").push("crafting");

            builder.comment("Whether Network Traders can be crafted.",
                            "Disabling will not remove any existing Network Traders from the world, nor prevent their use.",
                            "Disabling does NOT disable the recipes of Network Upgrades or the Trading Terminals.")
                    .add("canCraftNetworkTrader", this.canCraftNetworkTraders);

            builder.comment("Whether Trader Interface blocks can be crafted.",
                            "Disabling will not remove any existing Trader Interfaces from the world, nor prevent their use.")
                    .add("canCraftTraderInterface", this.canCraftTraderInterfaces);

            builder.comment("Whether Auction Stand blocks can be crafted.",
                            "Disabling will not remove any existing Auction Stands from the world, nor prevent their use.")
                    .add("canCraftAuctionStand", this.canCraftAuctionStands);

            builder.comment("Whether Tax Blocks can be crafted.",
                            "Disabling will not remove any existing Tax Blocks from the world, nor prevent their use.")
                    .add("canCraftTaxCollector", this.canCraftTaxBlock);

            builder.comment("Money Chest Crafting").push("money_chest");

            builder.comment("Whether the Money Chest can be crafted.",
                            "Disabling will not remove any existing Money Chests from the world, nor prevent their use.",
                            "Disabling does NOT disable the recipes of Money Chest Upgrades.")
                    .add("canCraftCoinChest", this.canCraftCoinChest);

            builder.comment("Whether the Money Chest Exchange Upgrade can be crafted.",
                            "Disabling will not remove any existing Money Chest Exchange Upgrades from the world, nor prevent their use.")
                    .add("canCraftExchangeUpgrade", this.canCraftCoinChestUpgradeExchange);

            builder.comment("Whether the Money Chest Magnet Upgrades can be crafted.",
                            "Disabling will not remove any existing Money Chest Magnet Upgrades from the world, nor prevent their use.")
                    .add("canCraftMagnetUpgrade", this.canCraftCoinChestUpgradeMagnet);

            builder.comment("Whether the Money Chest Bank Upgrade can be crafted.",
                            "Disabling will not remove any existing Money Chest Bank Upgrades from the world, nor prevent their use.")
                    .add("canCraftBankUpgrade", this.canCraftCoinChestUpgradeBank);

            builder.comment("Whether the Money Chest Security Upgrades can be crafted.",
                            "Disabling will not remove any existing Money Chest Security Upgrades from the world, nor prevent their use.")
                    .add("canCraftSecurityUpgrade", this.canCraftCoinChestUpgradeSecurity);

            //Pop money_chest -> crafting
            builder.pop().pop();

            builder.comment("Event Settings").push("events");

            builder.comment("Whether advancements will give players chocolate coins as a reward for playing during the event.",
                            "Note: Disabling will disable the entire `/lcadmin events reward` command used by the functions to give the reward.")
                    .add("advancementRewards", this.eventAdvancementRewards);

            builder.comment("Whether the Chocolate Event Coins will be added to the coin data.",
                            "Note: Disabling will not remove any Chocolate Coin items that already exist.")
                    .add("chocolate", this.chocolateEventCoins);

            builder.comment("Whether the Chocolate Event Coins will replace a small portion of the default coin loot drops during the event.",
                            "See \"chocolateRate\" to customize the replacement rate")
                    .add("chocolateDrops", this.chocolateEventCoinLootDrops);

            builder.comment("The percentage of Chocolate Coins being dropped instead of normal coins while an event is active.")
                    .add("chocolateRate", this.chocolateCoinDropRate);

            builder.pop();

            builder.comment("Villager Related Settings","Note: Any changes to villagers requires a full reboot to be applied due to how Minecraft/Forge registers trades.").push("villagers");

            builder.comment("Whether the wandering trader will have additional trades that allow you to buy misc items with money.")
                    .add("addCustomWanderingTrades", this.addCustomWanderingTrades);

            builder.comment("Whether the banker villager profession will have any registered trades. The banker sells Lightman's Currency items for coins.")
                    .add("addBanker", this.addBankerVillager);

            builder.comment("Whether the cashier villager profession will have any registered trades.. The cashier sells an amalgamation of vanilla traders products for coins.")
                    .add("addCashier", this.addCashierVillager);

            builder.comment("Villager Trade Modification","Note: Changes made only apply to newly generated trades. Villagers with trades already defined will not be changed.").push("modification");

            builder.comment("Whether vanilla villagers should have the Emeralds from their trades replaced with coins.")
                    .add("changeVanillaTrades", this.changeVanillaTrades);

            builder.comment("Whether villagers added by other mods should have the Emeralds from their trades replaced with coins.")
                    .add("changeModdedTrades", this.changeModdedTrades);

            builder.comment("Whether the wandering trader should have the emeralds from their trades replaced with the default replacement coin.")
                    .add("changeWanderingTrades", this.changeWanderingTrades);

            builder.comment("The default coin to replace a trades emeralds with.",
                            "May seperate and define villager type specific entries by adding multiple items seperated by '-' with region")
                    .add("defaultEmeraldReplacementItem", this.defaultEmeraldReplacementMod);

            builder.comment("List of replacement coin overrides.",
                            "Each entry must be formatted as follows: \"mod:some_profession_type-SUB_ENTRY-SUB_ENTRY-...\"",
                            "You may use \"minecraft:wandering_trader\" as a profession id to override the vanilla Wandering Trader",
                            "",
                            "Each sub-entry must be formatted as either of the following: \"r;minecraft:villager_type;ITEM_ENTRY\" to define an entry specific to an in-game region (villagers from `mincraft:snow` or `minecraft:desert` regions, etc.)",
                            "with the exception of a single 'default' entry with no defined villager type/region \"...-ITEM_ENTRY-...\"",
                            "",
                            "Each item-entry is either 1 or 2 item ids (e.g. \"mod:coin_item\" or \"mod:coin_item_1;mod:coin_item_2\".",
                            "When two are given, the first will replace the cost items (items the player must pay the villager) and the second will replace the result (items the player will be paid by the villager)",
                            "Every trader not on this list will use the default trader coin defined above.")
                    .add("professionEmeraldReplacementOverrides", this.professionEmeraldReplacementOverrides);

            builder.pop().pop();

            builder.comment("Loot Options").push("loot");

            builder.comment("T1 loot item.","Leave blank (\"\") to not spawn T1 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":1, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT1", this.lootItem1);
            builder.comment("T2 loot item.","Leave blank (\"\") to not spawn T2 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":2, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT2", this.lootItem2);
            builder.comment("T3 loot item.","Leave blank (\"\") to not spawn T3 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":3, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT3", this.lootItem3);
            builder.comment("T4 loot item.","Leave blank (\"\") to not spawn T4 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":4, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT4", this.lootItem4);
            builder.comment("T5 loot item.","Leave blank (\"\") to not spawn T5 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":5, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT5", this.lootItem5);
            builder.comment("T6 loot item.","Leave blank (\"\") to not spawn T6 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":6, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT6", this.lootItem6);

            builder.comment("Entity Loot Settings.").push("entities");

            builder.comment("Whether coins can be dropped by entities.")
                    .add("enabled", this.enableEntityDrops);

            builder.comment("Whether coins can be dropped by entities that were spawned by the vanilla spawner.")
                    .add("allowSpawnedDrops", this.allowSpawnerEntityDrops);

            builder.comment("Whether modded machines that emulate player behaviour can trigger coin drops from entities.",
                            "Set to false to help prevent autmated coin farming.")
                    .add("allowFakePlayerDrops", this.allowFakePlayerCoinDrops);

            builder.comment("Entity Drop Lists. Accepts the following inputs:",
                            "Entity IDs. e.g. \"minecraft:cow\"",
                            "Entity Tags. e.g. \"#minecraft:skeletons\"",
                            "Every entity provided by a mod. e.g. \"minecraft:*\"",
                            "Note: If an entity meets multiple criteria, it will drop the lowest tier loot that matches (starting with normal T1 -> T6 then boss T1 -> T6)")
                    .push("lists");

            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier1\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T1", this.entityDropsT1);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier2\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T2", this.entityDropsT2);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier3\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T3", this.entityDropsT3);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier4\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T4", this.entityDropsT4);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier5\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T5", this.entityDropsT5);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/entity/tier6\" loot table.","Requires a player kill to trigger coin drops.")
                    .add("T6", this.entityDropsT6);

            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier1\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT1", this.bossEntityDropsT1);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier2\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT2", this.bossEntityDropsT2);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier3\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT3", this.bossEntityDropsT3);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier4\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT4", this.bossEntityDropsT4);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier5\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT5", this.bossEntityDropsT5);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier6\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT6", this.bossEntityDropsT6);

            //Pop lists -> entities
            builder.pop().pop();

            builder.comment("Chest Loot Settings").push("chests");

            builder.comment("Whether coins can spawn in chests.")
                    .add("enabled", this.enableChestLoot);

            builder.comment("Chest Spawn Lists").push("lists");

            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier1\" loot table.")
                    .add("T1", this.chestDropsT1);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier2\" loot table.")
                    .add("T2", this.chestDropsT2);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier3\" loot table.")
                    .add("T3", this.chestDropsT3);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier4\" loot table.")
                    .add("T4", this.chestDropsT4);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier5\" loot table.")
                    .add("T5", this.chestDropsT5);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier6\" loot table.")
                    .add("T6", this.chestDropsT6);

            //Pop lists -> chests -> loot
            builder.pop().pop().pop();

        }

        @Nonnull
        public Supplier<VillagerTradeMod> getVillagerMod(@Nonnull String trader) { return () -> this.professionEmeraldReplacementOverrides.get().getModFor(trader); }

    }

    public static final class Server extends SyncedConfigFile
    {
        private Server() { super("lightmanscurrency-server", new ResourceLocation(LightmansCurrency.MODID,"server")); }

        //Notification Limit
        public final IntOption notificationLimit = IntOption.create(500, 0);

        public final BooleanOption safelyEjectMachineContents = BooleanOption.createTrue();
        public final BooleanOption anarchyMode = BooleanOption.createFalse();

        //Coin Minting/Melting
        public final BooleanOption coinMintCanMint = BooleanOption.createTrue();
        public final BooleanOption coinMintCanMelt = BooleanOption.createFalse();
        public final IntOption coinMintDefaultDuration = IntOption.create(100,1,72000);

        //Mint Specific Options
        public final BooleanOption coinMintMintableCopper = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableIron = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableGold = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableEmerald = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableDiamond = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableNetherite = BooleanOption.createTrue();

        //Melt Specific Options
        public final BooleanOption coinMintMeltableCopper = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableIron = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableGold = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableEmerald = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableDiamond = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableNetherite = BooleanOption.createTrue();

        //Wallet Settings
        public final IntOption walletExchangeLevel = IntOption.create(1,0,6);
        public final IntOption walletPickupLevel = IntOption.create(2,0,6);
        public final IntOption walletBankLevel = IntOption.create(5,0,6);

        //Item Capacity Upgrade Settings
        public final IntOption itemCapacityUpgrade1 = IntOption.create(3*64, 1, 100*64);
        public final IntOption itemCapacityUpgrade2 = IntOption.create(6*64, 2, 100*64);
        public final IntOption itemCapacityUpgrade3 = IntOption.create(9*64, 3, 100*64);
        public final IntOption itemCapacityUpgrade4 = IntOption.create(18*64, 4, 100*64);

        //Money Chest Upgrades
        public final IntOption coinChestMagnetRange1 = IntOption.create(4,1,50);
        public final IntOption coinChestMagnetRange2 = IntOption.create(6,2,50);
        public final IntOption coinChestMagnetRange3 = IntOption.create(8,3,50);
        public final IntOption coinChestMagnetRange4 = IntOption.create(10,4,50);

        //Enchantment Settings
        public final IntOption enchantmentTickDelay = IntOption.create(20, 1);
        public final MoneyValueOption moneyMendingRepairCost = MoneyValueOption.createNonEmpty(() -> CoinValue.fromNumber("main", 1));
        public final MoneyValueOption moneyMendingInfinityCost = MoneyValueOption.create(() -> CoinValue.fromNumber("main", 4), v -> v.sameType(this.moneyMendingRepairCost.get()));
        public final IntOption coinMagnetBaseRange = IntOption.create(5,1,50);
        public final IntOption coinMagnetLeveledRange = IntOption.create(2,1,50);
        public final IntOption coinMagnetCalculationCap = IntOption.create(10,3, Integer.MAX_VALUE);

        //Auction House Settings
        public final BooleanOption auctionHouseEnabled = BooleanOption.createTrue();
        public final BooleanOption auctionHouseOnTerminal = BooleanOption.createTrue();
        public final IntOption auctionHouseDurationMin = IntOption.create(0,0);
        public final IntOption auctionHouseDurationMax = IntOption.create(30,1);

        //Bank Account Settings
        public final DoubleOption bankAccountInterestRate = DoubleOption.create(0d,0d,1d);
        public final BooleanOption bankAccountForceInterest = BooleanOption.createTrue();
        public final BooleanOption bankAccountInterestNotification = BooleanOption.createTrue();
        public final IntOption bankAccountInterestTime = IntOption.create(1728000, 1200, 630720000);
        public final MoneyValueListOption bankAccountInterestLimits = MoneyValueListOption.createNonEmpty(ArrayList::new);

        //Terminal Options
        public final BooleanOption moveUnnamedTradersToBottom = BooleanOption.createFalse();

        //Paygate Options
        public final IntOption paygateMaxDuration = IntOption.create(1200, 0);

        //Player Trading Options
        public final DoubleOption playerTradingRange = DoubleOption.create(-1d,-1d);

        //Tax Collector Options
        public final BooleanOption taxCollectorAdminOnly = BooleanOption.createFalse();
        public final IntOption taxCollectorMaxRate = IntOption.create(25,1,99);
        public final IntOption taxCollectorMaxRadius = IntOption.create(256,16);
        public final IntOption taxCollectorMaxHeight = IntOption.create(64,8);
        public final IntOption taxCollectorMaxVertOffset = IntOption.create(32,4);

        //Chocolate Coin Options
        public final BooleanOption chocolateCoinEffects = BooleanOption.createTrue();

        //Claim Purchasing
        public final BooleanOption claimingAllowClaimPurchase = BooleanOption.createFalse();
        public final MoneyValueOption claimingClaimPrice = MoneyValueOption.createNonEmpty(() -> CoinValue.fromItemOrValue(ModItems.COIN_GOLD.get(), 100));
        public final IntOption claimingMaxClaimCount = IntOption.create(1000000,1);

        public final BooleanOption claimingAllowForceloadPurchase = BooleanOption.createFalse();
        public final MoneyValueOption claimingForceloadPrice = MoneyValueOption.createNonEmpty(() -> CoinValue.fromItemOrValue(ModItems.COIN_NETHERITE.get(), 1000000));
        public final IntOption claimingMaxForceloadCount = IntOption.create(100,1);
        public final IntOption flanClaimingBlocksPerChunk = IntOption.create(64, 1, 64);

        //LDI Settings
        public final StringOption ldiCurrencyChannel = StringOption.create("000000000000000000");
        public final StringOption ldiCurrencyCommandPrefix = StringOption.create("!");
        public final BooleanOption ldiLimitSearchToNetworkTraders = BooleanOption.createTrue();

        public final BooleanOption ldiNetworkTraderNotification = BooleanOption.createTrue();
        public final BooleanOption ldiAuctionCreateNotification = BooleanOption.createTrue();
        public final BooleanOption ldiAuctionPersistentCreateNotification = BooleanOption.createTrue();
        public final BooleanOption ldiAuctionCancelNotification = BooleanOption.createFalse();
        public final BooleanOption ldiAuctionWinNotification = BooleanOption.createTrue();

        @Override
        protected void setup(@Nonnull ConfigBuilder builder) {

            builder.comment("Notification Settings").push("notifications")
                    .comment("The maximum number of notifications each player and/or machine can have before old entries are deleted.",
                            "Lower if you encounter packet size problems.")
                    .add("limit", this.notificationLimit)
                    .pop();

            builder.comment("Machine Protection Settings").push("machine_protection")
                    .comment("Whether illegally broken traders (such as being replaced with /setblock, or modded machines that break blocks) will safely eject their block/contents into a temporary storage area for the owner to collect safely.",
                            "If disabled, illegally broken traders will throw their items on the ground, and can thus be griefed by modded machines.",
                            "Value ignored if anarchyMode is enabled!")
                    .add("safeEjection", this.safelyEjectMachineContents);


            builder.comment("Whether block break protection will be disabled completely.",
                    "Enable with caution as this will allow players to grief and rob other players shops and otherwise protected machinery.")
                            .add("anarchyMode", this.anarchyMode);

            builder.pop();

            builder.comment("Coin Mint Settings").push("coin_mint");

            builder.comment("Whether or not Coin Mint recipes of mintType \"MINT\" will function.",
                            "Defaults to the built-in recipes that turn resources into coins.")
                    .add("canMint", this.coinMintCanMint);

            builder.comment("Whether or not Coin Mint recipes of mintType \"MELT\" will function.",
                            "Defaults to the built-in recipes that turn coins back into resources.")
                    .add("canMelt", this.coinMintCanMelt);

            builder.comment("Default number of ticks it takes to process a Coin Mint recipe.",
                            "Does not apply to Coin Mint recipes with a defined \"duration\" input.")
                    .add("defaultMintDuration", this.coinMintDefaultDuration);

            builder.comment("Default Recipes").push("recipes").comment("Minting").push("mint");

            builder.comment("Whether recipes of mintType \"MINT\" with an output of copper coins will function.")
                    .add("copper", this.coinMintMintableCopper);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of iron coins will function.")
                    .add("iron", this.coinMintMintableIron);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of gold coins will function.")
                    .add("gold", this.coinMintMintableGold);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of emerald coins will function.")
                    .add("emerald", this.coinMintMintableEmerald);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of diamond coins will function.")
                    .add("diamond", this.coinMintMintableDiamond);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of netherite coins will function.")
                    .add("netherite", this.coinMintMintableNetherite);

            builder.pop().comment("Melting").push("melt");

            builder.comment("Whether recipes of mintType \"MELT\" with an output of copper ingots will function.")
                    .add("copper", this.coinMintMeltableCopper);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of iron ingots will function.")
                    .add("iron", this.coinMintMeltableIron);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of gold ingots will function.")
                    .add("gold", this.coinMintMeltableGold);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of emeralds will function.")
                    .add("emerald", this.coinMintMeltableEmerald);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of diamonds will function.")
                    .add("diamond", this.coinMintMeltableDiamond);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of netherite ingots will function.")
                    .add("netherite", this.coinMintMeltableNetherite);

            //Pop melt -> recipes -> coin_mint
            builder.pop().pop().pop();

            builder.comment("Wallet Settings").push("wallet");

            builder.comment("The lowest level wallet capable of exchanging coins.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("exchangeLevel", this.walletExchangeLevel);

            builder.comment("The lowest level wallet capable of automatically collecting coins while equipped.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("pickupLevel", this.walletPickupLevel);

            builder.comment("The lowest level wallet capable of allowing transfers to/from your bank account.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("bankLevel", this.walletBankLevel);

            builder.pop();

            builder.comment("Upgrade Settings").push("upgrades").comment("Item Capacity Upgrade").push("item_capacity");

            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Iron)")
                    .add("itemCapacity1", this.itemCapacityUpgrade1);
            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Gold)")
                    .add("itemCapacity2", this.itemCapacityUpgrade2);
            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Diamond)")
                    .add("itemCapacity3", this.itemCapacityUpgrade3);
            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Netherite)")
                    .add("itemCapacity4", this.itemCapacityUpgrade4);

            builder.pop().comment("Money Chest Magnet Upgrade").push("money_chest_magnet");

            builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Copper)'s coin collection.")
                    .add("radius1", this.coinChestMagnetRange1);
            builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Iron)'s coin collection.")
                    .add("radius2", this.coinChestMagnetRange2);
            builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Gold)'s coin collection.")
                    .add("radius3", this.coinChestMagnetRange3);
            builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Emerald)'s coin collection.")
                    .add("radius4", this.coinChestMagnetRange4);

            //Pop money_chest_magnet -> upgrades
            builder.pop().pop();

            builder.comment("Enchantment Settings").push("enchantments");

            builder.comment("The desired delay (in ticks) between Money Mending & Coin Magnet ticks. This value will be ignored in favor of 'maxTickDelay' if the server is overloaded and/or falling behind.",
                            "Increase if my enchantments are causing extreme lag.",
                            "Note: 20 ticks = 1s")
                    .add("tickDelay", this.enchantmentTickDelay);

            builder.comment("The cost required to repair a single item durability point with the Money Mending enchantment.")
                    .add("moneyMendingRepairCost", this.moneyMendingRepairCost);

            builder.comment("The additional cost to repair an item with Infinity applied to it.")
                    .add("moneyMendingInfinityCost", this.moneyMendingInfinityCost);

            builder.comment("The coin collection radius of the Coin Magnet I enchantment.")
                    .add("coinMagnetBaseRange", this.coinMagnetBaseRange);
            builder.comment("The increase in the coin collection radius added by each additional level of the Coin Magnet enchantment.")
                    .add("coinMagnetLeveledRange", this.coinMagnetLeveledRange);
            builder.comment("The final level of Coin Magnet that will result in increased range calculations.",
                            "Increase if you have another mod that increases the max level of the Coin Magnet enchantment",
                            "and you wish for those levels to actually apply an effect.")
                    .add("coinMagnetCalculationLevelCap", this.coinMagnetCalculationCap);

            builder.pop();

            builder.comment("Auction House Settings").push("auction_house");

            builder.comment("Whether the Auction House will be automatically generated and accessible.",
                            "If disabled after players have interacted with it, items & money in the auction house cannot be accessed until re-enabled.",
                            "If disabled, it is highly recommended that you also disable the 'crafting.allowAuctionStandCrafting' option in the common config.")
                    .add("enabled", this.auctionHouseEnabled);

            builder.comment("Whether the Auction House will appear in the trading terminal.",
                            "If false, you will only be able to access the Auction House from an Auction Stand.")
                    .add("visibleOnTerminal", this.auctionHouseOnTerminal);

            builder.comment("The minimum number of days an auction can have its duration set to.",
                            "If given a 0 day minimum, the minimum auction duration will be 1 hour.")
                    .add("minimumDuration", this.auctionHouseDurationMin);

            builder.comment("The maxumim number of day an auction can have its duration set to.")
                    .add("maximumDuration", this.auctionHouseDurationMax);

            builder.pop();

            builder.comment("Bank Account Settings").push("bank_accounts");

            builder.comment("The interest rate that bank accounts will earn just by existing.",
                        "Setting to 0 will disable interesting and all interest-related ticks from happening.",
                        "Note: Rate of 1.0 will result in doubling the accounts money each interest tick.",
                        "Rate of 0.01 is equal to a 1% interest rate.")
                    .add("interest", this.bankAccountInterestRate);

            builder.comment("Whether interest applied to small amounts of money are guaranteed to give at least *some* money as long as there's money in the account.",
                            "Example 1% interest applied to a bank account with only 1 copper coin will always give *at least* 1 copper coin.")
                    .add("forceInterest", this.bankAccountForceInterest);

            builder.comment("Whether players will receive a personal notification whenever their bank account collects interest.",
                            "Regardless of this value, the bank accounts logs will always display the interest interaction.")
                    .add("interestNotification", this.bankAccountInterestNotification);

            builder.comment("The number of minecraft ticks that will pass before interest is applied.",
                            "Helpful Notes:",
                            "1s = 20 ticks",
                            "1m = 1200 ticks",
                            "1h = 72000 ticks",
                            "1 day = 1728000 ticks",
                            "1 week = 12096000 ticks",
                            "30 days = 51840000 ticks",
                            "365 days = 630720000 ticks")
                    .add("interestDelay", this.bankAccountInterestTime);

            builder.comment("A list of upper interest limits.",
                            "Example:",
                            "Adding \"coin;1-lightmanscurrency:coin_netherite\" to this list will make it so that players will get no more than 1 netherite coin worth of interest even if they would normally get more.")
                    .add("interestUpperLimits", this.bankAccountInterestLimits);

            builder.pop();

            builder.comment("Network Terminal Settings").push("terminal");

            builder.comment("Whether Traders with no defined Custom Name will be sorted to the bottom of the Trader list on the Network Terminal.")
                    .add("sortUnnamedTradersToBottom", this.moveUnnamedTradersToBottom);

            builder.pop();

            builder.comment("Paygate Settings").push("paygate");

            builder.comment("The maximum number of ticks that a paygate can be set to output a redstone signal for.",
                            "Note: 20t = 1s")
                    .add("maxRedstoneDuration", this.paygateMaxDuration);

            builder.pop();

            builder.comment("Player <-> Player Trading Options").push("player_trading");

            builder.comment("The maximum distance allowed between players in order for a player trade to persist.",
                            "-1 will always allow trading regardless of dimension.",
                            "0 will allow infinite distance but require that both players be in the same dimension.")
                    .add("maxPlayerDistance", this.playerTradingRange);

            builder.pop();

            builder.comment("Tax Settings").push("taxes");

            builder.comment("Whether Tax Collectors can only be activated by an Admin in LC Admin Mode.",
                            "Will not prevent players from crafting, placing, or configuring Tax Collectors.")
                    .add("adminOnlyActivation", this.taxCollectorAdminOnly);

            builder.comment("The maximum tax rate (in %) a Tax Collector is allowed to enforce.",
                            "Note: The sum of multiple tax collectors rates can still exceed this number.",
                            "If a machine reaches a total tax rate of 100% it will forcible prevent all monetary interactions until this is resolved.")
                    .add("maxTaxRate", this.taxCollectorMaxRate);

            builder.comment("The maximum radius of a Tax Collectors area in meters.")
                    .add("maxRadius", this.taxCollectorMaxRadius);
            builder.comment("The maximum height of a Tax Collectors area in meters.")
                    .add("maxHeight", this.taxCollectorMaxHeight);
            builder.comment("The maximum vertical offset of a Tax Collectors area in meters.",
                            "Note: Vertical offset can be negative, so this will also enforce the lowest value.")
                    .add("maxVertOffset", this.taxCollectorMaxVertOffset);

            builder.pop();

            builder.comment("Chocolate Coin Settings").push("chocolate_coins");

            builder.comment("Whether the Chocolate Coins will give players custom potion and/or healing effects on consumption.")
                    .add("chocolateEffects", this.chocolateCoinEffects);

            builder.pop();

            builder.comment("Mod Compat Options").push("compat");

            builder.comment("Claim Purchasing Settings for FTB Chunks, Cadmus, & Flan").push("claim_purchasing");

            builder.comment("Whether the '/lcclaims buy claim' command will be accessible to players.")
                    .add("allowClaimPurchase", this.claimingAllowClaimPurchase);

            builder.comment("The price per claim chunk purchased.")
                    .add("claimPrice", this.claimingClaimPrice);

            builder.comment("The maximum number of extra claim chunks allowed to be purchased with this command.",
                            "Note: This count includes extra claim chunks given to the player/team via normal FTB Chunks methods as well (if applicable).")
                    .add("maxClaimCount", this.claimingMaxClaimCount);

            builder.comment("Whether the `/lcclaims buy forceload` command will be accessible to players.")
                    .add("allowForceloadPurchase", this.claimingAllowForceloadPurchase);

            builder.comment("The price per forceload chunk purchased.")
                    .add("forceloadPrice", this.claimingForceloadPrice);

            builder.comment("The maximum number of extra forceload chunks allowed to be purchased with this command.",
                            "Note: This count includes extra forceload chunks given to the player/team via normal FTB Chunks methods as well (if applicable).")
                    .add("maxForceloadCount", this.claimingMaxForceloadCount);

            builder.comment("Flan Settings").push("flan");

            builder.comment("Blocks that will be added with each 'claim' purchased")
                    .add("blocksPerChunk", this.flanClaimingBlocksPerChunk);

            //Pop flan -> claim_purchasing
            builder.pop().pop();

            builder.comment("Lightman's Discord Compat Settings.").push("ldi");

            builder.comment("The channel where users can run the currency commands and where currency related announcements will be made.")
                    .add("channel", this.ldiCurrencyChannel);
            builder.comment("Prefix for currency commands.")
                    .add("prefix", this.ldiCurrencyCommandPrefix);
            builder.comment("Whether the !search command should limit its search results to only Network Traders, or if it should list all traders.")
                    .add("limitSearchToNetwork", this.ldiLimitSearchToNetworkTraders);

            builder.comment("Currency Bot Notification Options").push("notifications");

            builder.comment("Whether a notification will appear in the currency bot channel when a Network Trader is created.",
                            "Notification will have a 60 second delay to allow them time to customize the traders name, etc.")
                    .add("networkTraderBuilt", this.ldiNetworkTraderNotification);

            builder.comment("Whether a notification will appear in the currency bot channel when a player starts an auction.")
                    .add("auctionCreated", this.ldiAuctionCreateNotification);

            builder.comment("Whether a notification will appear in the currency bot channel when a Persistent Auction is created automatically.",
                            "Requires that auction house creation notifications also be enabled.")
                    .add("auctionPersistentCreations", this.ldiAuctionPersistentCreateNotification);

            builder.comment("Whether a notification will appear in the currency bot channel when an Auction is cancelled in the Auction House.")
                    .add("auctionCancelled", this.ldiAuctionCancelNotification);

            builder.comment("Whether a notification will appear in the currency bot channel when an Auction is completed and had a bidder.")
                    .add("auctionWon", this.ldiAuctionWinNotification);

            //Pop notifications -> ldi
            builder.pop().pop();

            //Pop compat section
            builder.pop();

        }

        public boolean allowCoinMintRecipe(@Nonnull CoinMintRecipe recipe)
        {
            switch (recipe.getMintType())
            {
                case OTHER: return true;
                case MINT: {
                    if(!this.coinMintCanMint.get())
                        return false;
                    Item resultItem = recipe.getOutputItem().getItem();
                    if(resultItem == ModItems.COIN_COPPER.get())
                        return this.coinMintMintableCopper.get();
                    if(resultItem == ModItems.COIN_IRON.get())
                        return this.coinMintMintableIron.get();
                    if(resultItem == ModItems.COIN_GOLD.get())
                        return this.coinMintMintableGold.get();
                    if(resultItem == ModItems.COIN_EMERALD.get())
                        return this.coinMintMintableEmerald.get();
                    if(resultItem == ModItems.COIN_DIAMOND.get())
                        return this.coinMintMintableDiamond.get();
                    if(resultItem == ModItems.COIN_NETHERITE.get())
                        return this.coinMintMintableNetherite.get();
                }
                case MELT: {
                    if(!this.coinMintCanMelt.get())
                        return false;
                    Item resultItem = recipe.getOutputItem().getItem();
                    if(resultItem == Items.COPPER_INGOT)
                        return this.coinMintMeltableCopper.get();
                    if(resultItem == Items.IRON_INGOT)
                        return this.coinMintMeltableIron.get();
                    if(resultItem == Items.GOLD_INGOT)
                        return this.coinMintMeltableGold.get();
                    if(resultItem == Items.EMERALD)
                        return this.coinMintMeltableEmerald.get();
                    if(resultItem == Items.DIAMOND)
                        return this.coinMintMeltableDiamond.get();
                    if(resultItem == Items.NETHERITE_INGOT)
                        return this.coinMintMeltableNetherite.get();
                }
            }
            return true;
        }

    }

}