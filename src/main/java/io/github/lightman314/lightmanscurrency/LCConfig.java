package io.github.lightman314.lightmanscurrency;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.*;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.*;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.*;
import io.github.lightman314.lightmanscurrency.api.events.DroplistConfigGenerator;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleConfigOption;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleData;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.common.config.VillagerTradeModsOption;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.ChestPoolLevel;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.EntityPoolLevel;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMod;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMods;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

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
        private Client() { super(VersionUtil.lcResource("client"),"lightmanscurrency-client"); }

        public final IntOption itemRenderLimit = IntOption.create(Integer.MAX_VALUE, 0);
        public final CustomItemScaleConfigOption itemScaleOverrides = CustomItemScaleConfigOption.create(new CustomItemScaleData(Lists.newArrayList(Pair.of(CustomItemScaleData.create(LCTags.Items.DRAW_HALF_SIZE),0.5f))));
        public final BooleanOption drawGachaBallItem = BooleanOption.createTrue();
        public final BooleanOption gachaMachineFancyGraphics = BooleanOption.createTrue();

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
        public final StringOption terminalBonusFilters = StringOption.create("ready:true");

        public final BooleanOption debugScreens = BooleanOption.createFalse();

        @Override
        protected void setup(@Nonnull ConfigBuilder builder) {

            builder.comment("Quality Settings").push("quality");

            builder.comment("Maximum number of items each Item Trader can renderBG (per-trade) as stock. Lower to decrease client-lag in trader-rich areas.",
                            "Setting to 0 will disable item rendering entirely, so use with caution.")
                    .add("itemTraderRenderLimit", this.itemRenderLimit);

            builder.comment("A list of item ids or item tags that should be rendered by Item Traders at a different scale.")
                    .add("itemScaleOverrides",this.itemScaleOverrides);

            builder.comment("Whether the Gacha Ball should render the item inside",
                            "Enabling will double the number of items being rendered, and can cause FPS issues near Gacha Machines if their fancy graphics are enabled")
                    .add("gachaBallFullRender",this.drawGachaBallItem);

            builder.comment("Whether the Gacha Machine will render each Gacha Ball individually",
                            "Disable if you're having FPS issues near the Gacha Machine, this will make the machine render a far more simplisitic representation of its contents.")
                    .add("gachaMachineFancyGraphics",this.gachaMachineFancyGraphics);

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

            builder.comment("A default search filter that will be automatically added to the search parameters")
                    .add("searchFilter",this.terminalBonusFilters);

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

            builder.comment("Debug Settings").push("debug");

            builder.comment("Whether LC Screens should render a white background for easier debugging & screenshots")
                    .add("debugScreenBG", this.debugScreens);

            /* Wallet Position Debug
            builder.comment("Wallet Offset X").add("offX", this.xOff);
            builder.comment("Wallet Offset Y").add("offY", this.yOff);
            builder.comment("Wallet Offset Z").add("offZ", this.zOff);
            //*/

            builder.pop();

        }

        /* Wallet Position Debug
        public final DoubleOption xOff = DoubleOption.create(2f/16f);
        public final DoubleOption yOff = DoubleOption.create(-7.5f/16f);
        public final DoubleOption zOff = DoubleOption.create(6f/16f);
        //*/

    }

    public static final class Common extends ConfigFile
    {
        private Common() { super(VersionUtil.lcResource("common"),"lightmanscurrency-common", LoadPhase.SETUP); }

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
        public final BooleanOption canCraftATMCard = BooleanOption.createFalse();
        //Coin Mint Crafting Options
        public final BooleanOption canCraftCoinMint = BooleanOption.createTrue();
        public final BooleanOption coinMintCanMint = BooleanOption.createTrue();
        public final BooleanOption coinMintCanMelt = BooleanOption.createFalse();
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
        public final BooleanOption eventStartingRewards = BooleanOption.createTrue();
        public final BooleanOption eventLootReplacements = BooleanOption.createTrue();

        //Structure Options
        public final BooleanOption structureVillageHouses = BooleanOption.createTrue();
        public final BooleanOption structureAncientCity = BooleanOption.createTrue();
        //public final BooleanOption structureIDAS = BooleanOption.createTrue();

        //Early Load Compat Options
        public final BooleanOption compatImpactor = BooleanOption.createTrue();

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

            builder.comment("Whether ATM Cards can be crafted.",
                            "Disabling will not remove any existing ATM Cards from the world, nor prevent their use.")
                    .add("canCraftATMCard", this.canCraftATMCard);

            builder.comment("Coin Mint Crafting").push("coin_mint");

            builder.comment("Whether the Coin Mint machine can be crafted.",
                            "Disabling will not remove and exist Coin Mints from the world, nor prevent their use.")
                    .add("canCraftCoinMint",this.canCraftCoinMint);

            builder.comment("Whether or not built-in coin mint recipes that turn resources into coins will be loaded.")
                    .add("canMint", this.coinMintCanMint);

            builder.comment("Whether or not built-in coin mint recipes that turn coins back into resources will be loaded.")
                    .add("canMelt", this.coinMintCanMelt);

            builder.comment("Specific Minting Options",
                            "Does nothing if 'canMint' is already false/disabled.")
                    .push("mint");

            builder.comment("Whether the default mint recipe to mint copper coins from copper ingots will be loaded.")
                    .add("copper", this.coinMintMintableCopper);
            builder.comment("Whether the default mint recipe to mint iron coins from iron ingots will be loaded.")
                    .add("iron", this.coinMintMintableIron);
            builder.comment("Whether the default mint recipe to mint gold coins from gold ingots will be loaded.")
                    .add("gold", this.coinMintMintableGold);
            builder.comment("Whether the default mint recipe to mint emerald coins from emeralds will be loaded.")
                    .add("emerald", this.coinMintMintableEmerald);
            builder.comment("Whether the default mint recipe to mint diamond coins from diamonds will be loaded.")
                    .add("diamond", this.coinMintMintableDiamond);
            builder.comment("Whether the default mint recipe to mint netherite coins from netherite ingots will be loaded.")
                    .add("netherite", this.coinMintMintableNetherite);

            builder.pop().comment("Specific Melting Options",
                            "Does nothing if 'canMelt' is already false/disabled.")
                    .push("melt");

            builder.comment("Whether the default mint recipe to melt copper coins back into copper ingots will be loaded.")
                    .add("copper", this.coinMintMeltableCopper);
            builder.comment("Whether the default mint recipe to melt iron coins back into iron ingots will be loaded.")
                    .add("iron", this.coinMintMeltableIron);
            builder.comment("Whether the default mint recipe to melt gold coins back into gold ingots will be loaded.")
                    .add("gold", this.coinMintMeltableGold);
            builder.comment("Whether the default mint recipe to melt emerald coins back into emeralds will be loaded.")
                    .add("emerald", this.coinMintMeltableEmerald);
            builder.comment("Whether the default mint recipe to melt diamond coins back into diamonds will be loaded.")
                    .add("diamond", this.coinMintMeltableDiamond);
            builder.comment("Whether the default mint recipe to melt netherite coins back into netherite ingots will be loaded.")
                    .add("netherite", this.coinMintMeltableNetherite);

            //Pop melt -> coin_mint
            builder.pop().pop();

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

            builder.comment("Whether the Chocolate Event Coins will be added to the coin data.",
                            "Note: Disabling will not remove any Chocolate Coin items that already exist, this simply makes them no longer function as money")
                    .add("chocolate", this.chocolateEventCoins);

            builder.comment("Whether custom events defined in the 'SeasonalEvents.json' config can give players the one-time reward for logging in during the event.")
                    .add("startingRewards", this.eventStartingRewards);

            builder.comment("Whether custom events can replace a portion (or all) of the default loot with custom event loot.")
                    .add("lootReplacements",this.eventLootReplacements);

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

            builder.comment("Structure Settings","Requires a /reload command to be applied correctly").push("structures");

            builder.comment("Whether new village structures will have a chance to spawn in vanilla villages")
                    .add("villageHouses",this.structureVillageHouses);

            builder.comment("Whether new structures will have a chance to spawn in ancient cities")
                    .add("ancientCity",this.structureAncientCity);

            /*builder.comment("Whether new special structures designed for Integrated Dungeons and Structures compatibility can spawn",
                            "Does nothing if IDAS is not installed")
                    .add("idasStructures",this.structureIDAS);
            //*/

            builder.pop();

            builder.push("compat");

            builder.comment("Whether the Impactor compat will be initialized.",
                            "Requires a full server reboot for changes to be applied!")
                    .add("impactorModule",this.compatImpactor);


            builder.pop();

        }

        @Nonnull
        public Supplier<VillagerTradeMod> getVillagerMod(@Nonnull String trader) { return () -> this.professionEmeraldReplacementOverrides.get().getModFor(trader); }

    }

    public static final class Server extends SyncedConfigFile {
        private Server() {
            super("lightmanscurrency-server", VersionUtil.lcResource("server"));
        }

        //Notification Limit
        public final IntOption notificationLimit = IntOption.create(500, 0);

        public final BooleanOption safelyEjectMachineContents = BooleanOption.createTrue();
        public final BooleanOption anarchyMode = BooleanOption.createFalse();
        public final ResourceListOption quarantinedDimensions = ResourceListOption.create(new ArrayList<>());

        //Coin Mint
        public final IntOption coinMintDefaultDuration = IntOption.create(100, 1, 72000);
        public final FloatOption coinMintSoundVolume = FloatOption.create(0.5f, 0f, 1f);

        //Wallet Settings
        public final IntOption walletExchangeLevel = IntOption.create(1, 0, WalletItem.CONFIG_LIMIT);
        public final IntOption walletPickupLevel = IntOption.create(2, 0, WalletItem.CONFIG_LIMIT);
        public final IntOption walletBankLevel = IntOption.create(5, 0, WalletItem.CONFIG_LIMIT);
        public final BooleanOption walletCapacityUpgradeable = BooleanOption.createTrue();
        public final BooleanOption walletDropsManualSpawn = BooleanOption.createFalse();

        //Money Bag Settings
        public final FloatOption moneyBagBaseAttack = FloatOption.create(1f,0f,Float.MAX_VALUE);
        public final FloatOption moneyBagAttackPerSize = FloatOption.create(3f,0f,Float.MAX_VALUE);
        public final FloatOption moneyBagBaseAtkSpeed = FloatOption.create(-2f,-4f,0f);
        public final FloatOption moneyBagAtkSpeedPerSize = FloatOption.create(-0.5f,-4f,0f);
        public final FloatOption moneyBagBaseFallDamage = FloatOption.create(0.5f,0f,Float.MAX_VALUE);
        public final FloatOption moneyBagFallDamagerPerSize = FloatOption.create(1.0f,0f,Float.MAX_VALUE);
        public final IntOption moneyBagMaxFallDamageBase = IntOption.create(30,0,Integer.MAX_VALUE);
        public final IntOption moneyBagMaxFallDamagePerSize = IntOption.create(10,0,Integer.MAX_VALUE);
        public final DoubleOption moneyBagCoinLossChance = DoubleOption.create(0d,0d,1d);
        public final IntOption moneyBagCoinLossFallDistance = IntOption.create(2,0,Integer.MAX_VALUE);

        //Item Capacity Upgrade Settings
        public final IntOption itemCapacityUpgrade1 = IntOption.create(3 * 64, 1, 100 * 64);
        public final IntOption itemCapacityUpgrade2 = IntOption.create(6 * 64, 2, 100 * 64);
        public final IntOption itemCapacityUpgrade3 = IntOption.create(9 * 64, 3, 100 * 64);
        public final IntOption itemCapacityUpgrade4 = IntOption.create(18 * 64, 4, 100 * 64);

        //Interaction Upgrade Settings
        public final IntOption interactionUpgrade1 = IntOption.create(5, 1, 100);
        public final IntOption interactionUpgrade2 = IntOption.create(10, 1, 100);
        public final IntOption interactionUpgrade3 = IntOption.create(15, 1, 100);

        //Money Chest Upgrades
        public final IntOption coinChestMagnetRange1 = IntOption.create(4, 1, 50);
        public final IntOption coinChestMagnetRange2 = IntOption.create(6, 2, 50);
        public final IntOption coinChestMagnetRange3 = IntOption.create(8, 3, 50);
        public final IntOption coinChestMagnetRange4 = IntOption.create(10, 4, 50);

        //Enchantment Settings
        public final IntOption enchantmentTickDelay = IntOption.create(20, 1);
        public final MoneyValueOption moneyMendingRepairCost = MoneyValueOption.createNonEmpty(() -> CoinValue.fromNumber("main", 1));
        public final MoneyValueOption moneyMendingInfinityCost = MoneyValueOption.create(() -> CoinValue.fromNumber("main", 4), v -> v.sameType(this.moneyMendingRepairCost.get()));
        public final IntOption coinMagnetBaseRange = IntOption.create(5, 1, 50);
        public final IntOption coinMagnetLeveledRange = IntOption.create(2, 1, 50);
        public final IntOption coinMagnetCalculationCap = IntOption.create(10, 3, Integer.MAX_VALUE);

        //Auction House Settings
        public final BooleanOption auctionHouseEnabled = BooleanOption.createTrue();
        public final BooleanOption auctionHouseOnTerminal = BooleanOption.createTrue();
        public final IntOption auctionHouseDurationMin = IntOption.create(0, 0);
        public final IntOption auctionHouseDurationMax = IntOption.create(30, 1);

        //Bank Account Settings
        public final DoubleOption bankAccountInterestRate = DoubleOption.create(0d, 0d, 1d);
        public final BooleanOption bankAccountForceInterest = BooleanOption.createTrue();
        public final BooleanOption bankAccountInterestNotification = BooleanOption.createTrue();
        public final IntOption bankAccountInterestTime = IntOption.create(1728000, 1200, 630720000);
        public final MoneyValueListOption bankAccountInterestLimits = MoneyValueListOption.createNonEmpty(ArrayList::new);
        public final StringListOption bankAccountInterestBlacklist = StringListOption.create(ArrayList::new);

        //Terminal Options
        public final BooleanOption openTerminalCommand = BooleanOption.createFalse();
        public final BooleanOption moveUnnamedTradersToBottom = BooleanOption.createFalse();

        //Paygate Options
        public final IntOption paygateMaxDuration = IntOption.create(1200, 0);
        //Command Trader Settings
        public final IntOption commandTraderMaxPermissionLevel = IntOption.create(4, 0, 4);

        //Player Trading Options
        public final DoubleOption playerTradingRange = DoubleOption.create(-1d, -1d);

        //Tax Collector Options
        public final BooleanOption taxCollectorAdminOnly = BooleanOption.createFalse();
        public final IntOption taxCollectorMaxRate = IntOption.create(25, 1, 99);
        public final IntOption taxCollectorMaxRadius = IntOption.create(256, 16);
        public final IntOption taxCollectorMaxHeight = IntOption.create(64, 8);
        public final IntOption taxCollectorMaxVertOffset = IntOption.create(32, 4);

        //Chocolate Coin Options
        public final BooleanOption chocolateCoinEffects = BooleanOption.createTrue();

        //Variant
        public final ResourceListOption variantBlacklist = ResourceListOption.create(ArrayList::new);

        //Claim Purchasing
        public final BooleanOption claimingAllowClaimPurchase = BooleanOption.createFalse();
        public final MoneyValueOption claimingClaimPrice = MoneyValueOption.createNonEmpty(() -> CoinValue.fromItemOrValue(ModItems.COIN_GOLD.get(), 100));
        public final IntOption claimingMaxClaimCount = IntOption.create(1000000, 1);

        public final BooleanOption claimingAllowForceloadPurchase = BooleanOption.createFalse();
        public final MoneyValueOption claimingForceloadPrice = MoneyValueOption.createNonEmpty(() -> CoinValue.fromItemOrValue(ModItems.COIN_NETHERITE.get(), 1000000));
        public final IntOption claimingMaxForceloadCount = IntOption.create(100, 1);
        public final IntOption flanClaimingBlocksPerChunk = IntOption.create(256, 1, 256);

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

            builder.comment("A list of dimension ids that are quarantined from all cross-dimensional interactions.",
                            "This includes disabling Trader Interfaces, Network Traders & Terminals (personal trader interactions & cash registers will still function), and all Bank Account access.",
                            "Mostly intended to be used to allow the existence of 'Creative Dimensions' where money can be cheated in by your average player, but should not affect a players inventory/bank balance in the 'normal' dimensions.")
                    .add("quarantinedDimensions", this.quarantinedDimensions);

            builder.pop();

            builder.comment("Coin Mint Settings").push("coin_mint");

            builder.comment("Default number of ticks it takes to process a Coin Mint recipe.",
                            "Does not apply to Coin Mint recipes with a defined \"duration\" input.")
                    .add("defaultMintDuration", this.coinMintDefaultDuration);

            builder.comment("The volume of the noise played whenever the Coin Mint finishes the crafting process.")
                    .add("soundVolume", this.coinMintSoundVolume);

            builder.pop();

            builder.comment("Wallet Settings").push("wallet");

            final String walletLevelDescription = "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-Nether Star Wallet; 7-No Wallet";

            builder.comment("The lowest level wallet capable of exchanging coins.",
                            walletLevelDescription)
                    .add("exchangeLevel", this.walletExchangeLevel);

            builder.comment("The lowest level wallet capable of automatically collecting coins while equipped.",
                            walletLevelDescription)
                    .add("pickupLevel", this.walletPickupLevel);

            builder.comment("The lowest level wallet capable of allowing transfers to/from your bank account.",
                            walletLevelDescription)
                    .add("bankLevel", this.walletBankLevel);

            builder.comment("Whether wallets can have additional slots added by using an upgrade item on them from their inventory",
                            "By default diamonds are the only valid upgrade item, but this can be changed by a datapack")
                    .add("allowCapacityUpgrade", this.walletCapacityUpgradeable);

            builder.comment("Whether Wallet Drops should be manually spawned into the world instead of the default behaviour of being passed to the PlayerDropsEvent",
                            "Wallet Drops will be either the Wallet itself, or the coins dropped when the `coinDropPercent` game rule is greater than 0.")
                    .add("manualDropOverride", this.walletDropsManualSpawn);

            builder.pop();

            //Money Bag Settings
            builder.comment("Money Bag Settings",
                            "Important Note: Money Bag Attributes are only validated when the item is first created or loaded from file, so config changes may not be reflected immediately.")
                    .push("money_bag");

            builder.comment("The base Attack Damage that an empty Money Bag will have (not counting the base 1 attack damage the player has)")
                    .add("baseAttack",this.moneyBagBaseAttack);

            builder.comment("The additional Attack Damage added by each additional size (up to a size of 3)")
                    .add("attackPerBagSize",this.moneyBagAttackPerSize);

            builder.comment("The base Attack Speed that an empty Money Bag will have (not counting the base 4 attack speed the player has)",
                            "Is negative because you typically want to make weapons such as these attack slower (vanilla sword attack speed is 1.5, which can be obtained with a value of -2.5)")
                    .add("baseAttackSpeed",this.moneyBagBaseAtkSpeed);

            builder.comment("The additional Attack Speed added by each additional size (up to a size of 3)",
                            "Is negative because you typically want to make weapons such as these attack slower",
                            "Note: If the total attack speed additions are more than -4.0, the player will be unable to get a full-strength attack with that size of Money Bag.")
                    .add("attackSpeedPerBagSize",this.moneyBagAtkSpeedPerSize);

            builder.comment("The base fall damage per distance an empty Money Bag will have")
                    .add("baseFallDamage",this.moneyBagBaseFallDamage);

            builder.comment("The additional fall damage per distance added by each additional size (up to a size of 3)")
                    .add("fallDamagePerBagSize",this.moneyBagFallDamagerPerSize);

            builder.comment("The base fall damage limit an empty Money Bag will have")
                    .add("baseFallDamageLimit",this.moneyBagMaxFallDamageBase);

            builder.comment("The additional fall damage limit added by each additional size (up to a size of 3)")
                    .add("fallDamageLimitPerBagSize",this.moneyBagMaxFallDamagePerSize);

            builder.comment("The chance of the Money Bag dropping a random coin when it's used to attack another entity or when it falls a significant distance",
                            "0.0 is a 0% chance, and 1.0 is a 100% chance")
                    .add("coinLossChance",this.moneyBagCoinLossChance);

            builder.comment("The minimum distance a Money Bag must fall before it has a chance to drop coins when it lands")
                    .add("coinLossFallDistance",this.moneyBagCoinLossFallDistance);

            builder.pop();

            builder.comment("Upgrade Settings").push("upgrades").comment("Item Capacity Upgrade").push("item_capacity");

            builder.comment("The amount of item storage added by the Item Capacity Upgrade (Iron)")
                    .add("itemCapacity1", this.itemCapacityUpgrade1);
            builder.comment("The amount of item storage added by the Item Capacity Upgrade (Gold)")
                    .add("itemCapacity2", this.itemCapacityUpgrade2);
            builder.comment("The amount of item storage added by the Item Capacity Upgrade (Diamond)")
                    .add("itemCapacity3", this.itemCapacityUpgrade3);
            builder.comment("The amount of item storage added by the Item Capacity Upgrade (Netherite)")
                    .add("itemCapacity4", this.itemCapacityUpgrade4);

            builder.pop().comment("Interaction Upgrade").push("interaction_upgrade");

            builder.comment("The amount of bonus selections added by the Interaction Upgrade (Emerald)")
                    .add("interactionCount1", this.interactionUpgrade1);
            builder.comment("The amount of bonus selections added by the Interaction Upgrade (Diamond)")
                    .add("interactionCount2", this.interactionUpgrade2);
            builder.comment("The amount of bonus selections added by the Interaction Upgrade (Netherite)")
                    .add("interactionCount3", this.interactionUpgrade3);

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

            builder.comment("The delay (in ticks) between Money Mending & Coin Magnet ticks.",
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

            builder.comment("A list of Money Value unique ids that should not have interest applied to them.",
                            "Example:",
                            "Adding \"lightmanscurrency:coins!chocolate_coins\" will prevent chocolate coins from getting interest,",
                            "Adding \"lightmanscurrency:coins!*\" will prevent all built-in money types from getting interest")
                    .add("interestBlacklist",this.bankAccountInterestBlacklist);

            builder.pop();

            builder.comment("Network Terminal Settings").push("terminal");

            builder.comment("Whether the /lcterminal command will exist allowing players to access the Trading Terminal without the physical item/block")
                    .add("lcterminalCommand", this.openTerminalCommand);

            builder.comment("Whether Traders with no defined Custom Name will be sorted to the bottom of the Trader list on the Network Terminal.")
                    .add("sortUnnamedTradersToBottom", this.moveUnnamedTradersToBottom);

            builder.pop();

            builder.comment("Paygate Settings").push("paygate");

            builder.comment("The maximum number of ticks that a paygate can be set to output a redstone signal for",
                            "Note: 20t = 1s")
                    .add("maxRedstoneDuration", this.paygateMaxDuration);

            builder.pop();

            builder.comment("Command Trader Settings").push("command_trader");

            builder.comment("The maximum permission level that can set and used by a command trader")
                    .add("maxPermissionLevel", this.commandTraderMaxPermissionLevel);

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

            builder.comment("Model Variant Settings").push("model_variants");

            builder.comment("A list of Model Variant ids that will be hidden from the Variant Select Menu on the client,",
                            "and cannot be selected in said menu.")
                    .add("variantBlacklist",this.variantBlacklist);

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

            //Pop compat section
            builder.pop();

        }
    }

}