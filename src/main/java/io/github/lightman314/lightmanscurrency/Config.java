package io.github.lightman314.lightmanscurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.events.DroplistConfigGenerator;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.*;
import io.github.lightman314.lightmanscurrency.util.config.MoneyValueConfig;
import io.github.lightman314.lightmanscurrency.util.config.ItemValueConfig;
import io.github.lightman314.lightmanscurrency.util.config.ScreenPositionConfig;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public class Config {

	public static boolean canMint(Item item)
	{
		if(item == ModItems.COIN_COPPER.get())
			return SERVER.mintCopper.get();
		else if(item == ModItems.COIN_IRON.get())
			return SERVER.mintIron.get();
		else if(item == ModItems.COIN_GOLD.get())
			return SERVER.mintGold.get();
		else if(item == ModItems.COIN_EMERALD.get())
			return SERVER.mintEmerald.get();
		else if(item == ModItems.COIN_DIAMOND.get())
			return SERVER.mintDiamond.get();
		else if(item == ModItems.COIN_NETHERITE.get())
			return SERVER.mintNetherite.get();

		//If no rule is against it, allow the minting
		return true;
	}

	public static boolean canMelt(Item item)
	{
		if(item == ModItems.COIN_COPPER.get())
			return SERVER.meltCopper.get();
		else if(item == ModItems.COIN_IRON.get())
			return SERVER.meltIron.get();
		else if(item == ModItems.COIN_GOLD.get())
			return SERVER.meltGold.get();
		else if(item == ModItems.COIN_EMERALD.get())
			return SERVER.meltEmerald.get();
		else if(item == ModItems.COIN_DIAMOND.get())
			return SERVER.meltDiamond.get();
		else if(item == ModItems.COIN_NETHERITE.get())
			return SERVER.meltNetherite.get();

		//If no rule is against it, allow the minting
		return true;
	}

	private static Map<String,Item> traderOverrides = new HashMap<>();

	public static void reloadVillagerOverrides() {
		traderOverrides = new HashMap<>();
		List<? extends String> overrides = COMMON.traderOverrides.get();
		for(int i = 0; i < overrides.size(); ++i)
		{
			try {
				String override = overrides.get(i);
				if(!override.contains("-"))
					throw new RuntimeException("Input doesn't have a '-' splitter.");
				String[] split = override.split("-");
				if(split.length != 2)
					throw new RuntimeException("Input has more than 1 '-' splitter.");

				ResourceLocation villagerType;
				try {
					villagerType = new ResourceLocation(split[0]);
				} catch(Throwable t) { throw new RuntimeException("Villager type is not a valid resource location.", t); }
				ResourceLocation itemType;
				try {
					itemType = new ResourceLocation(split[1]);
				} catch(Throwable t) { throw new RuntimeException("Item is not a valid resource location.", t); }

				Item item = ForgeRegistries.ITEMS.getValue(itemType);
				if(item == null)
					throw new RuntimeException("Item '" + itemType + "' is not a registered item.");

				if(traderOverrides.containsKey(villagerType.toString()))
					throw new RuntimeException("Villager Type '" + villagerType + "' already has an override. Cannot override it twice!");

				traderOverrides.put(villagerType.toString(), item);
				LightmansCurrency.LogInfo("Trader Override loaded: " + villagerType + " -> " + itemType);

			} catch(Throwable t) { LightmansCurrency.LogError("Error parsing trader override input #" + (i + 1) + ".", t); }
		}
	}

	public static Item getEmeraldReplacementItem(String trader) {

		if(traderOverrides.containsKey(trader))
			return traderOverrides.get(trader);

		return Config.COMMON.defaultTraderCoin.get();

	}

	@Deprecated
	public static Item getDefaultEmeraldReplacementItem() {
		return Config.COMMON.defaultTraderCoin.get();
	}

	public static class Client
	{

		private static final Supplier<ForgeConfigSpec> SPEC_SUPPLIER = () -> Config.clientSpec;

		//Render Options
		public final ForgeConfigSpec.IntValue itemRenderLimit;

		//Timestamp Formatting Options
		public final ForgeConfigSpec.ConfigValue<String> timeFormat;

		//Wallet Button Options
		public final ScreenPositionConfig walletSlot;
		public final ScreenPositionConfig walletSlotCreative;
		public final ScreenPositionConfig walletButtonOffset;

		//Wallet Overlay Options
		public final ForgeConfigSpec.BooleanValue walletOverlayEnabled;
		public final ForgeConfigSpec.EnumValue<ScreenCorner> walletOverlayCorner;
		public final ScreenPositionConfig walletOverlayPosition;
		public final ForgeConfigSpec.EnumValue<WalletDisplayOverlay.DisplayType> walletOverlayType;

		//Notification Options
		public final ForgeConfigSpec.BooleanValue pushNotificationsToChat;

		//Inventory Button Options
		public final ScreenPositionConfig notificationAndTeamButtonPosition;
		public final ScreenPositionConfig notificationAndTeamButtonCreativePosition;

		//Chest Button Options
		public final ForgeConfigSpec.BooleanValue chestButtonVisible;
		public final ForgeConfigSpec.BooleanValue chestButtonAllowHidden;

		//Slot Machine Options
		public final ForgeConfigSpec.IntValue slotMachineAnimationTime;
		public final ForgeConfigSpec.IntValue slotMachineAnimationRestTime;

		//Sound Options
		public final ForgeConfigSpec.BooleanValue moneyMendingClink;


		Client(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Client configuration settings").push("client");

			builder.comment("Quality Settings").push("quality");

			this.itemRenderLimit = builder
					.comment("Maximum number of items each Item Trader can renderBG (per-trade) as stock. Lower to decrease client-lag in trader-rich areas.",
							"Setting to 0 will disable item rendering entirely, so use with caution.")
					.defineInRange("itemTraderRenderLimit", Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

			builder.pop();

			builder.comment("Time Formatting Settings").push("time");

			this.timeFormat = builder
					.comment("How Notification Timestamps are displayed.","Follows SimpleDateFormat formatting: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html")
					.define("timeFormatting","MM/dd/yy hh:mmaa");

			builder.pop();

			builder.comment("Wallet Slot Settings").push("wallet_slot");

			this.walletSlot = ScreenPositionConfig.define(builder
					.comment("The position that the wallet slot will be placed at in the players inventory.")
					,"slot", ScreenPosition.of(76, 43), SPEC_SUPPLIER);

			this.walletSlotCreative = ScreenPositionConfig.define(builder
					.comment("The position that the wallet slot will be placed at in the players creative inventory."),
					"creativeSlot", ScreenPosition.of(126,19), SPEC_SUPPLIER);

			this.walletButtonOffset = ScreenPositionConfig.define(builder
					.comment("The offset that the wallet button should be placed at relative to the wallet slot position."),
					"button", ScreenPosition.of(8,-10), SPEC_SUPPLIER);

			builder.pop();

			builder.comment("Wallet Overlay Settings").push("wallet_hud");

			this.walletOverlayEnabled = builder
					.comment("Whether an overlay should be drawn on your HUD displaying your wallets current money amount.")
					.define("enabled", true);

			this.walletOverlayCorner = builder
					.comment("The corner of the screen that the overlay should be drawn on.")
					.defineEnum("displayCorner", ScreenCorner.BOTTOM_LEFT);

			this.walletOverlayPosition = ScreenPositionConfig.define(
					builder.comment("The position offset from the defined corner."),
					"displayOffset", ScreenPosition.of(5,-5), SPEC_SUPPLIER);

			this.walletOverlayType = builder
					.comment("Whether the wallets contents should be displayed as a coin item, or as value text.")
					.defineEnum("displayType", WalletDisplayOverlay.DisplayType.ITEMS_WIDE);

			builder.pop();

			builder.comment("Inventory Button Settings").push("inventory_buttons");

			this.notificationAndTeamButtonPosition = ScreenPositionConfig.define(builder
					.comment("The position that the notification & team manager buttons will be placed at in the players inventory."),
					"button", ScreenPosition.of(152,3), SPEC_SUPPLIER);

			this.notificationAndTeamButtonCreativePosition = ScreenPositionConfig.define(builder
					.comment("The position that the notification & team manager buttons will be placed at in the players creative inventory."),
					"buttonCreative", ScreenPosition.of(171,3), SPEC_SUPPLIER);

			builder.pop();

			builder.comment("Chest Button Settings").push("chest_buttons");

			this.chestButtonVisible = builder
					.comment("Whether the 'Move Coins into Wallet' button will appear in the top-right corner of the Chest Screen if there are coins in the chest that can be collected.")
					.define("enabled", true);

			this.chestButtonAllowHidden = builder
					.comment("Whether the 'Move Coins into Wallet' button should collect coins flagged as 'hidde'",
							"By default these would be the coin pile and coin block variants of the coins.")
					.define("allowHiddenCollection", false);

			builder.pop();

			builder.comment("Notification Settings").push("notification");

			this.pushNotificationsToChat = builder
					.comment("Whether notifications should be posted in your chat when you receive them.")
					.define("notificationsInChat", true);

			builder.pop();

			builder.comment("Slot Machine Animation Settings").push("slot_machine");

			this.slotMachineAnimationTime = builder.comment("The number of Minecraft ticks the slot machine animation will last.",
					"Note: 20 ticks = 1 second",
					"Must be at least 20 ticks (1s) for coding reasons.")
					.defineInRange("animationDuration", 100, 20, 1200);
			this.slotMachineAnimationRestTime = builder.comment("The number of Minecraft ticks the slot machine will pause before repeating the animation.")
					.defineInRange("animationRestDuration", 20, 0, 1200);

			builder.pop();

			builder.comment("Sound Settings").push("sounds");

			this.moneyMendingClink = builder
					.comment("Whether Money Mending should make a noise when triggered.")
					.define("moneyMendingClink", true);

			builder.pop();

		}

	}

	public static class Common
	{

		private static final Supplier<ForgeConfigSpec> SPEC_SUPPLIER = () -> Config.commonSpec;

		//Crafting Options
		public final ForgeConfigSpec.BooleanValue canCraftNetworkTraders;
		public final ForgeConfigSpec.BooleanValue canCraftTraderInterfaces;
		public final ForgeConfigSpec.BooleanValue canCraftAuctionStands;
		public final ForgeConfigSpec.BooleanValue canCraftCoinChest;
		public final ForgeConfigSpec.BooleanValue canCraftCoinChestUpgradeExchange;
		public final ForgeConfigSpec.BooleanValue canCraftCoinChestUpgradeBank;
		public final ForgeConfigSpec.BooleanValue canCraftCoinChestUpgradeMagnet;
		public final ForgeConfigSpec.BooleanValue canCraftCoinChestUpgradeSecurity;
		public final ForgeConfigSpec.BooleanValue canCraftTaxBlock;

		//Custom trades
		public final ForgeConfigSpec.BooleanValue addCustomWanderingTrades;
		public final ForgeConfigSpec.BooleanValue addBankerVillager;
		public final ForgeConfigSpec.BooleanValue addCashierVillager;
		public final ForgeConfigSpec.BooleanValue changeVanillaTrades;
		public final ForgeConfigSpec.BooleanValue changeModdedTrades;
		public final ForgeConfigSpec.BooleanValue changeWanderingTrades;
		public final ItemValueConfig defaultTraderCoin;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> traderOverrides;

		//Debug
		public final ForgeConfigSpec.IntValue debugLevel;

		//Loot Coin Entries
		public final ItemValueConfig lootItem1;
		public final ItemValueConfig lootItem2;
		public final ItemValueConfig lootItem3;
		public final ItemValueConfig lootItem4;
		public final ItemValueConfig lootItem5;
		public final ItemValueConfig lootItem6;

		//Entity Loot
		public final ForgeConfigSpec.BooleanValue enableEntityDrops;
		public final ForgeConfigSpec.BooleanValue enableSpawnerEntityDrops;
		public final ForgeConfigSpec.BooleanValue allowFakePlayerCoinDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT1;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT2;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT3;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT4;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT5;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> entityDropsT6;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT1;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT2;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT3;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT4;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT5;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEntityDropsT6;

		//Chest Loot
		public final ForgeConfigSpec.BooleanValue enableChestLoot;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT1;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT2;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT3;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT4;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT5;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> chestDropsT6;

		Common(ForgeConfigSpec.Builder builder)
		{

			builder.comment("Common configuration settings").push("common");

			builder.comment("Crafting Settings").push("crafting");

			this.canCraftNetworkTraders = builder.comment("Whether Network Traders can be crafted.",
							"Disabling will not remove any existing Network Traders from the world, nor prevent their use.",
							"Disabling does NOT disable the recipes of Network Upgrades or the Trading Terminals.",
							"/reload required for changes to take effect.")
					.define("allowNetworkTraderCrafting", true);

			this.canCraftTraderInterfaces = builder.comment("Whether Trader Interface blocks can be crafted.",
							"Disabling will not remove any existing Trader Interfaces from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowTraderInterfaceCrafting", true);

			this.canCraftAuctionStands = builder.comment("Whether Auction Stand blocks can be crafted.",
							"Disabling will not remove any existing Auction Stands from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowAuctionStandCrafting", true);

			this.canCraftTaxBlock = builder.comment("Whether Tax Blocks can be crafted.",
							"Disabling will not remove any existing Tax Blocks from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowTaxBlockCrafting", true);

			builder.comment("Coin Chest Related Crafting Settings").push("coin_chest");

			this.canCraftCoinChest = builder.comment("Whether the Coin Chest can be crafted.",
							"Disabling will not remove any existing Coin Chests from the world, nor prevent their use.",
							"Disabling does NOT disable the recipes of Coin Chest Upgrades.",
							"/reload required for changes to take effect.")
						.define("allowChestCrafting", true);

			this.canCraftCoinChestUpgradeExchange = builder.comment("Whether the Coin Chest Exchange Upgrade can be crafted.",
							"Disabling will not remove any existing Coin Chest Exchange Upgrades from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowExchangeUpgradeCrafting", true);

			//Temporary assignment as I don't want to make a config option for a feature that isn't present yet
			this.canCraftCoinChestUpgradeBank = this.canCraftCoinChest;

			this.canCraftCoinChestUpgradeMagnet = builder.comment("Whether the Coin Chest Magnet Upgrades can be crafted.",
							"Disabling will not remove any existing Coin Chest Magnet Upgrades from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowMagnetUpgradeCrafting", true);

			this.canCraftCoinChestUpgradeSecurity = builder.comment("Whether the Coin Chest Security Upgrades can be crafted.",
							"Disabling will not remove any existing Coin Chest Security Upgrades from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.define("allowSecurityUpgradeCrafting", true);

			builder.pop();

			builder.pop();

			builder.comment("Villager Related Settings","Note: Any changes to villagers requires a full reboot to be applied due to how Minecraft/Forge registers trades.").push("villagers");

			this.addCustomWanderingTrades = builder
					.comment("Whether the wandering trader will have additional trades that allow you to buy misc items with money.")
					.define("addCustomWanderingTrades", true);

			this.addBankerVillager = builder
					.comment("Whether the banker villager profession will have any registered trades. The banker sells Lightman's Currency items for coins.")
					.define("addBanker", true);
			this.addCashierVillager = builder
					.comment("Whether the cashier villager profession will have any registered trades.. The cashier sells an amalgamation of vanilla traders products for coins.")
					.define("addCashier", true);

			builder.comment("Settings Related to other Villagers").push("other_traders");

			this.changeVanillaTrades = builder
					.comment("Whether vanilla villagers should have the Emeralds from their trades replaced with coins.")
					.define("changeVanillaTrades", false);

			this.changeModdedTrades = builder
					.comment("Whether villagers added by other mods should have the Emeralds from their trades replaced with coins.")
					.define("changeModdedTrades", false);

			this.changeWanderingTrades = builder
					.comment("Whether the wandering trader should have the emeralds from their trades replaced with the default trader coin.")
					.define("changeWanderingTrades", false);

			this.defaultTraderCoin = ItemValueConfig.define(builder
							.comment("The default coin to replace a traders emeralds with.")
					,"defaultTraderCoin", new ResourceLocation("lightmanscurrency","coin_emerald"),
					SPEC_SUPPLIER);

			this.traderOverrides = builder
					.comment("List of trader coin overrides.",
							"Each entry must be formatted as follows: \"mod:some_trader_type-lightmanscurrency:some_coin\"",
							"Every trader not on this list will use the default trader coin defined above.")
					.define("traderOverrides", Lists.newArrayList(
							"minecraft:butcher-lightmanscurrency:coin_iron",
							"minecraft:cartographer-lightmanscurrency:coin_iron",
							"minecraft:farmer-lightmanscurrency:coin_iron",
							"minecraft:fisherman-lightmanscurrency:coin_iron",
							"minecraft:fletcher-lightmanscurrency:coin_copper",
							"minecraft:leatherworker-lightmanscurrency:coin_iron",
							"minecraft:mason-lightmanscurrency:coin_iron",
							"minecraft:shepherd-lightmanscurrency:coin_iron"
					));

			builder.pop();

			builder.pop();

			this.debugLevel = builder
					.comment("Level of debug messages to be shown in the logs.","0-All debug messages. 1-Warnings/Errors only. 2-Errors only. 3-No debug messages.","Note: All debug messages will still be sent debug.log regardless of settings.")
					.defineInRange("debugLevel", 0, 0, 3);

			builder.comment("Loot item customization. Accepts item ids (i.e. lightmanscurrency:coin_copper).",
					"Leave blank (lootItemT? = \"\") to not spawn loot of that tier (so that you can use higher-tier spawn rates without the presence of lower-tier loot).").push("loot_customization");

			this.lootItem1 = ItemValueConfig.define(builder.comment("T1 loot item. Used for T1-T6 entity & chest loot drops."),
					"lootItemT1", "lightmanscurrency:coin_copper",SPEC_SUPPLIER);
			this.lootItem2 = ItemValueConfig.define(builder.comment("T2 loot item. Used for T2-T6 entity & chest loot drops."),
					"lootItemT2", "lightmanscurrency:coin_iron",SPEC_SUPPLIER);
			this.lootItem3 = ItemValueConfig.define(builder.comment("T3 loot item. Used for T3-T6 entity & chest loot drops."),
					"lootItemT3", "lightmanscurrency:coin_gold",SPEC_SUPPLIER);
			this.lootItem4 = ItemValueConfig.define(builder.comment("T4 loot item. Used for T4-T6 entity & chest loot drops."),
					"lootItemT4", "lightmanscurrency:coin_emerald",SPEC_SUPPLIER);
			this.lootItem5 = ItemValueConfig.define(builder.comment("T5 loot item. Used for T5-T6 entity & chest loot drops."),
					"lootItemT5", "lightmanscurrency:coin_diamond",SPEC_SUPPLIER);
			this.lootItem6 = ItemValueConfig.define(builder.comment("T6 loot item. Used for T6 entity & chest loot drops."),
					"lootItemT6", "lightmanscurrency:coin_netherite",SPEC_SUPPLIER);

			builder.pop();


			//Entity loot modification
			builder.comment("Entity loot settings. Accepts entity ids (i.e. minecraft:zombie)").push("entity_loot");

			this.enableEntityDrops = builder
					.comment("Whether coins can be dropped by entities. Does not effect chest loot generation.")
					.define("enableEntityDrops", true);
			//Entity spawned loot drops
			this.enableSpawnerEntityDrops = builder
					.comment("Whether coins can be dropped by entities that were spawned by the vanilla spawner.")
					.define("enableSpawnerEntityDrops", false);
			//Fake Player loot drops
			this.allowFakePlayerCoinDrops = builder
					.comment("Whether modded machines that emulate player behaviour can trigger coin drops from entities.",
							"Set to false to help prevent coin farming.")
					.define("allowFakePlayerTrigger", true);

			//Copper
			this.entityDropsT1 = builder
					.comment("Entities that will occasionally drop T1 loot.")
					.defineList("entityListT1", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T1), o -> o instanceof String);
			//Iron
			this.entityDropsT2 = builder
					.comment("Entities that will occasionally drop T1 -> T2 loot.")
					.defineList("entityListT2", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T2), o -> o instanceof String);
			//Gold
			this.entityDropsT3 = builder
					.comment("Entities that will occasionally drop T1 -> T3 loot.")
					.defineList("entityListT3", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T3), o -> o instanceof String);
			//Emerald
			this.entityDropsT4 = builder
					.comment("Entities that will occasionally drop T1 -> T4 loot.")
					.defineList("entityListT4", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T4), o -> o instanceof String);
			//Diamond
			this.entityDropsT5 = builder
					.comment("Entities that will occasionally drop T1 -> T5 loot.")
					.defineList("entityListT5", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T5), o -> o instanceof String);
			//Netherite
			this.entityDropsT6 = builder
					.comment("Entities that will occasionally drop T1 -> T6 loot.")
					.defineList("entityListT6", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.T6), o -> o instanceof String);

			//Boss
			this.bossEntityDropsT1 = builder
					.comment("Entities that will drop a large amount of T1 loot.")
					.defineList("bossEntityListT1", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T1), o -> o instanceof String);
			this.bossEntityDropsT2 = builder
					.comment("Entities that will drop a large amount of T1 -> T2 loot.")
					.defineList("bossEntityListT2", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T2), o -> o instanceof String);
			this.bossEntityDropsT3 = builder
					.comment("Entities that will drop a large amount of T1 -> T3 loot.")
					.defineList("bossEntityListT3", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T3), o -> o instanceof String);
			this.bossEntityDropsT4 = builder
					.comment("Entities that will drop a large amount of T1 -> T4 loot.")
					.defineList("bossEntityListT4", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T4), o -> o instanceof String);
			this.bossEntityDropsT5 = builder
					.comment("Entities that will drop a large amount of T1 -> T5 loot.")
					.defineList("bossEntityListT5", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T5), o -> o instanceof String);
			this.bossEntityDropsT6 = builder
					.comment("Entities that will drop a large amount of T1 -> T6 loot.")
					.defineList("bossEntityListT6", DroplistConfigGenerator.CollectDefaultEntityDrops(EntityPoolLevel.BOSS_T6), o -> o instanceof String);

			builder.pop();

			builder.comment("Chest loot settings.").push("chest_loot");
			this.enableChestLoot = builder
					.comment("Whether coins can spawn in chests Does not effect entity loot drops.")
					.define("enableChestLoot", true);
			this.chestDropsT1 = builder
					.comment("Chests that will occasionally spawn T1 loot.")
					.defineList("chestListT1", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T1), o -> o instanceof String);
			this.chestDropsT2 = builder
					.comment("Chests that will occasionally spawn T1 -> T2 loot.")
					.defineList("chestListT2", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T2), o -> o instanceof String);
			this.chestDropsT3 = builder
					.comment("Chests that will occasionally spawn T1 -> T3 loot.")
					.defineList("chestListT3", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T3), o -> o instanceof String);
			this.chestDropsT4 = builder
					.comment("Chests that will occasionally spawn T1 -> T4 loot.")
					.defineList("chestListT4", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T4), o -> o instanceof String);
			this.chestDropsT5 = builder
					.comment("Chests that will occasionally spawn T1 -> T5 loot.")
					.defineList("chestListT5", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T5), o -> o instanceof String);
			this.chestDropsT6 = builder
					.comment("Chests that will occasionally spawn T1 -> T6 loot.")
					.defineList("chestListT6", DroplistConfigGenerator.CollectDefaultChestDrops(ChestPoolLevel.T6), o -> o instanceof String);

			builder.pop();


		}

	}

	public static class Server
	{

		private static final Supplier<ForgeConfigSpec> SPEC_SUPPLIER = () -> Config.serverSpec;

		//Log Limit Option
		public final ForgeConfigSpec.IntValue notificationLimit;

		//Ejection Options
		public final ForgeConfigSpec.BooleanValue safelyEjectIllegalBreaks;

		//Melt/Mint Options
		public final ForgeConfigSpec.BooleanValue allowCoinMinting;
		public final ForgeConfigSpec.BooleanValue allowCoinMelting;
		public final ForgeConfigSpec.IntValue defaultMintDuration;

		//Specific Melt/Mint Options
		public final ForgeConfigSpec.BooleanValue mintCopper;
		public final ForgeConfigSpec.BooleanValue mintIron;
		public final ForgeConfigSpec.BooleanValue mintGold;
		public final ForgeConfigSpec.BooleanValue mintEmerald;
		public final ForgeConfigSpec.BooleanValue mintDiamond;
		public final ForgeConfigSpec.BooleanValue mintNetherite;

		public final ForgeConfigSpec.BooleanValue meltCopper;
		public final ForgeConfigSpec.BooleanValue meltIron;
		public final ForgeConfigSpec.BooleanValue meltGold;
		public final ForgeConfigSpec.BooleanValue meltEmerald;
		public final ForgeConfigSpec.BooleanValue meltDiamond;
		public final ForgeConfigSpec.BooleanValue meltNetherite;

		//Wallet Options
		public final ForgeConfigSpec.IntValue walletExchangeLevel;
		public final ForgeConfigSpec.IntValue walletPickupLevel;
		public final ForgeConfigSpec.IntValue walletBankLevel;

		//Capacity Upgrade Options
		public final ForgeConfigSpec.IntValue itemUpgradeCapacity1;
		public final ForgeConfigSpec.IntValue itemUpgradeCapacity2;
		public final ForgeConfigSpec.IntValue itemUpgradeCapacity3;

		//Enchantment Options
		public final MoneyValueConfig moneyMendingCoinCost;
		public final ForgeConfigSpec.IntValue coinMagnetRangeBase;
		public final ForgeConfigSpec.IntValue coinMagnetRangeLevel;

		//Auction House Options
		public final ForgeConfigSpec.BooleanValue enableAuctionHouse;
		public final ForgeConfigSpec.BooleanValue auctionHouseOnTerminal;
		public final ForgeConfigSpec.IntValue maxAuctionDuration;
		public final ForgeConfigSpec.IntValue minAuctionDuration;

		//Terminal Options
		public final ForgeConfigSpec.BooleanValue moveUnnamedTradersToBottom;

		//Money Chest Upgrade Options
		public final ForgeConfigSpec.IntValue coinChestMagnetRange1;
		public final ForgeConfigSpec.IntValue coinChestMagnetRange2;
		public final ForgeConfigSpec.IntValue coinChestMagnetRange3;
		public final ForgeConfigSpec.IntValue coinChestMagnetRange4;

		//Player Trade Options
		public final ForgeConfigSpec.DoubleValue maxPlayerTradingRange;

		//Discord Bot Options
		public final ForgeConfigSpec.ConfigValue<String> currencyChannel;
		public final ForgeConfigSpec.ConfigValue<String> currencyCommandPrefix;
		public final ForgeConfigSpec.BooleanValue limitSearchToNetworkTraders;

		//Player Tax Options
		public final ForgeConfigSpec.BooleanValue taxMachinesAdminOnly;
		public final ForgeConfigSpec.IntValue taxMachineMaxRate;
		public final ForgeConfigSpec.IntValue taxMachineMaxRadius;
		public final ForgeConfigSpec.IntValue taxMachineMaxHeight;
		public final ForgeConfigSpec.IntValue taxMachineMaxVertOffset;

		//Event Options
		public final ForgeConfigSpec.BooleanValue chocolateEventCoins;
		public final ForgeConfigSpec.BooleanValue chocolateCoinEffects;
		public final ForgeConfigSpec.DoubleValue chocolateCoinDropRate;

		//FTB Chunks & Cadmus Claim Purchase Options
		public final ForgeConfigSpec.BooleanValue claimingAllowClaimPurchase;
		public final MoneyValueConfig claimingClaimPrice;
		public final ForgeConfigSpec.IntValue claimingMaxClaimCount;
		public final ForgeConfigSpec.BooleanValue claimingAllowForceloadPurchase;
		public final MoneyValueConfig claimingForceloadPrice;
		public final ForgeConfigSpec.IntValue claimingMaxForceloadCount;

		//Discord Bot Notification Options
		public final ForgeConfigSpec.BooleanValue traderCreationNotifications;
		public final ForgeConfigSpec.BooleanValue auctionHouseCreateNotifications;
		public final ForgeConfigSpec.BooleanValue auctionHouseCreatePersistentNotifications;
		public final ForgeConfigSpec.BooleanValue auctionHouseCancelNotifications;
		public final ForgeConfigSpec.BooleanValue auctionHouseWinNotifications;


		Server(ForgeConfigSpec.Builder builder)
		{

			builder.comment("Server Config Settings").push("server");

			this.notificationLimit = builder
					.comment("The maximum number of notifications each player can have before old entries are deleted.",
							"Lower if you encounter packet size problems.")
					.defineInRange("notificationLimit", 500, 0, Integer.MAX_VALUE);

			this.safelyEjectIllegalBreaks = builder
					.comment("Whether illegally broken traders (such as being replaced with /setblock, or modded machines that break blocks) will safely eject their block/contents into a temporary storage area for the owner to collect safely.",
							"If disabled, illegally broken traders will throw their items on the ground, and can thus be griefed by modded machines.")
					.define("ejectIllegalBreaks", true);

			this.allowCoinMinting = builder
					.comment("Determines whether or not coins should be craftable via the Coin Minting Machine.")
					.translation("lightmanscurrency.configgui.canMintCoins")
					.define("canMintCoins", true);
			this.allowCoinMelting = builder
					.comment("Determines whether or not coins can be melted back into their source material in the Coin Minting Machine.")
					.translation("lightmanscurrency.configgui.canMeltCoins")
					.define("canMeltCoins", false);
			this.defaultMintDuration = builder
					.comment("Default number of ticks it takes to mint a coin via the Coin Minting Machine.")
					.defineInRange("defaultMintDuration", 100, 1, 72000);

			builder.comment("Specific Coin Minting Settings.").push("coin_minting");
			this.mintCopper = builder.comment("Whether copper coins can be minted.")
					.define("canMintCopper", true);
			this.mintIron = builder.comment("Whether iron coins can be minted.")
					.define("canMintIron", true);
			this.mintGold = builder.comment("Whether gold coins can be minted.")
					.define("canMintGold", true);
			this.mintEmerald = builder.comment("Whether emerald coins can be minted.")
					.define("canMintEmerald", true);
			this.mintDiamond = builder.comment("Whether diamond coins can be minted.")
					.define("canMintDiamond", true);
			this.mintNetherite = builder.comment("Whether netherite coins can be minted.")
					.define("canMintNetherite", true);
			builder.pop();

			builder.comment("Specific Coin Melting Settings.").push("coin_melting");
			this.meltCopper = builder.comment("Whether copper coins can be melted.")
					.define("canMeltCopper", true);
			this.meltIron = builder.comment("Whether iron coins can be melted.")
					.define("canMeltIron", true);
			this.meltGold = builder.comment("Whether gold coins can be melted.")
					.define("canMeltGold", true);
			this.meltEmerald = builder.comment("Whether emerald coins can be melted.")
					.define("canMeltEmerald", true);
			this.meltDiamond = builder.comment("Whether diamond coins can be melted.")
					.define("canMeltDiamond", true);
			this.meltNetherite = builder.comment("Whether netherite coins can be melted.")
					.define("canMeltNetherite", true);
			builder.pop();

			builder.comment("Wallet Settings.").push("wallet");

			this.walletExchangeLevel = builder.comment("The lowest level wallet capable of exchanging coins in the UI.",
							"0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
					.defineInRange("convertLevel", 1, 0, 6);

			this.walletPickupLevel = builder.comment("The lowest level wallet capable of automatically collecting coins while equipped.",
							"0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
					.defineInRange("pickupLevel", 2, 0, 6);

			this.walletBankLevel = builder.comment("The lowest level wallet capable of allowing transfers to/from your bank account.",
							"0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
					.defineInRange("bankLevel", 5, 0, 6);

			builder.pop();

			builder.comment("Item Capacity Upgrade Settings").push("upgrades");

			this.itemUpgradeCapacity1 = builder.comment("The amount of item storage added by the first Item Capacity upgrade (Iron).")
					.defineInRange("upgradeCapacity1", 3 * 64, 1, 1728);
			this.itemUpgradeCapacity2 = builder.comment("The amount of item storage added by the second Item Capacity upgrade (Gold).")
					.defineInRange("upgradeCapacity2", 6 * 64, 1, 1728);
			this.itemUpgradeCapacity3 = builder.comment("The amount of item storage added by the third Item Capacity upgrade (Diamond).")
					.defineInRange("upgradeCapacity3", 9 * 64, 1, 1728);

			builder.pop();

			builder.comment("Money Chest Upgrade Settings").push("money_chest_upgrades");

			this.coinChestMagnetRange1 = builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Copper)'s coin collection.")
					.defineInRange("magnetRange1", 4, 1, 50);
			this.coinChestMagnetRange2 = builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Iron)'s coin collection.")
					.defineInRange("magnetRange2", 6, 1, 50);
			this.coinChestMagnetRange3 = builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Gold)'s coin collection.")
					.defineInRange("magnetRange3", 8, 1, 50);
			this.coinChestMagnetRange4 = builder.comment("The radius (in meters) of the Money Chest Magnet Upgrade (Emerald)'s coin collection.")
					.defineInRange("magnetRange4", 10, 1, 50);

			builder.pop();

			builder.comment("Enchantment Settings").push("enchantments");

			this.moneyMendingCoinCost = MoneyValueConfig.define(builder.comment("The cost required to repair a single item durability point with the Money Mending enchantment.")
					,"moneyMendingCoinCost", "coin;1-lightmanscurrency:coin_copper", () -> CoinValue.fromNumber("main", 1), SPEC_SUPPLIER);

			this.coinMagnetRangeBase = builder.comment("The base radius around the player that the Coin Magnet enchantment will collect coins from.")
					.defineInRange("coinMagnetRangeBase", 5, 0, 50);
			this.coinMagnetRangeLevel = builder.comment("The increase in collection radius added by each additional level of the enchantment.")
					.defineInRange("coinMagnetRangeLevel", 2, 0, 50);


			builder.pop();

			builder.comment("Auction House Settings").push("auction_house");

			this.enableAuctionHouse = builder.comment("Whether the Auction House will be automatically generated and accessible.",
							"If disabled after players have interacted with it, items & money in the auction house cannot be accessed until re-enabled.",
							"If disabled, it is highly recommended that you also disable the 'crafting.allowAuctionStandCrafting' option in the common config.")
					.define("enabled", true);

			this.auctionHouseOnTerminal = builder.comment("Whether the Auction House will appear in the trading terminal.",
							"If false, you will only be able to access the Auction House from an Auction Stand.")
					.define("visibleOnTerminal", true);

			this.minAuctionDuration = builder.comment("The minimum number of days an auction can be carried out.")
					.defineInRange("minDuration", 0, 0, Integer.MAX_VALUE);

			this.maxAuctionDuration = builder.comment("The maximum number of days an auction can be carried out.")
					.defineInRange("maxDuration", 30, 1, Integer.MAX_VALUE);

			builder.pop();

			builder.comment("Network Terminal Settings").push("terminal");

			this.moveUnnamedTradersToBottom = builder.comment("Whether Traders with no defined Custom Name will be sorted to the bottom of the Trader list on the Network Terminal.")
					.define("sortUnnamedToBottom", false);

			builder.pop();

			builder.comment("Player Trading Options").push("player_trading");

			this.maxPlayerTradingRange = builder.comment("The maximum distance allowed between players in order for a player trade to persist.",
							"-1 will always allow trading regardless of dimension.",
							"0 will allow infinite distance but require that both players be in the same dimension.")
					.defineInRange("maxPlayerDistance", -1d, -1d, Double.MAX_VALUE);

			builder.pop();

			builder.comment("Tax Block").push("taxes");

			this.taxMachinesAdminOnly = builder.comment("Whether Tax Blocks can only be activated by an Admin in LC Admin Mode.",
							"Will not prevent players from crafting or placing/configuring Tax Blocks.")
					.define("adminOnlyActivation", false);

			this.taxMachineMaxRate = builder.comment("The maximum tax rate (in %) a Tax Block is allowed to enforce.")
					.defineInRange("maxTaxRate", 25, 1, 99);

			this.taxMachineMaxRadius = builder.comment("The maximum radius of a Tax Block's area in meters.")
					.defineInRange("maxRadius", 256, 16, Integer.MAX_VALUE);
			this.taxMachineMaxHeight = builder.comment("The maximum height of a Tax Block's area in meters.")
					.defineInRange("maxHeight", 64, 8, Integer.MAX_VALUE);
			this.taxMachineMaxVertOffset = builder.comment("The maximum vertical offset of a Tax Block's vertical offset in meters.",
							"Note: Vertical offset can be negative, so this will also enforce the lowest value.")
					.defineInRange("maxVertOffset", 32, 4, Integer.MAX_VALUE);

			builder.pop();

			builder.comment("Event Coins").push("events");

			this.chocolateEventCoins = builder.comment("Whether the Chocolate Event Coins will be added to the coin data.",
							"Requires /lcreload for changes to take effect.",
							"Note: Disabling will not remove any Chocolate Coin items that already exist.")
					.define("chocolate", true);

			this.chocolateCoinDropRate = builder.comment("The percentage of Chocolate Coins being dropped instead of normal coins while an event is active.",
							"Note: Will only replace the built-in coins, so if you've modified the loot items to not include the default copper -> netherite coins, the chocolate coins will not drop.")
							.defineInRange("chocolateRate", 0.1, 0d,1d);

			this.chocolateCoinEffects = builder.comment("Whether the Chocolate Coins will give players custom potion and/or healing effects on consumption.")
							.define("chocolateEffects", true);

			builder.pop();

			builder.comment("Claim purchasing settings for FTB Chunks & Cadmus. Requires FTB Chunks or Cadmus to apply!").push("claim_purchasing");

			//Claim Purchase Settings
			this.claimingAllowClaimPurchase = builder.comment("Whether the '/lcclaims buy claim' command will be accessible to players.")
					.define("allowClaimPurchase", false);
			this.claimingClaimPrice = MoneyValueConfig.define(builder.comment("The price per claim chunk purchased."),
					"claimPrice", "coin;1-lightmanscurrency:coin_gold", () -> CoinValue.fromItemOrValue(ModItems.COIN_GOLD.get(), 100), SPEC_SUPPLIER);
			this.claimingMaxClaimCount = builder.comment("The maximum number of extra claim chunks allowed to be purchased with this command.",
						"Note: This count includes extra claim chunks given to the player/team via normal FTB Chunks methods as well (if applicable).")
					.defineInRange("maxClaimCount", 1000000, 1, Integer.MAX_VALUE);

			//Forceload Purchase Settings
			this.claimingAllowForceloadPurchase = builder.comment("Whether the `/lcclaims buy forceload` or `/lccadmus buy forceload` command will be accessible to players.")
					.define("allowForceloadPurchase", false);
			this.claimingForceloadPrice = MoneyValueConfig.define(builder.comment("The price per forceload chunk purchased."),
					"forceloadPrice", "coin;10-lightmanscurrency:coin_netherite", () -> CoinValue.fromItemOrValue(ModItems.COIN_NETHERITE.get(), 10, 1000000), SPEC_SUPPLIER);
			this.claimingMaxForceloadCount = builder.comment("The maximum number of extra forceload chunks allowed to be purchased with this command.",
						"Note: This count includes extra forceload chunks given to the player/team via normal FTB Chunks methods as well.")
					.defineInRange("maxForceloadCount", 100, 1, Integer.MAX_VALUE);

			builder.pop();

			builder.comment("Discord bot settings. Requires lightmansdiscord v0.0.3.0+ to use.").push("discord");

			this.currencyChannel = builder
					.comment("The channel where users can run the currency commands and where currency related announcements will be made.")
					.define("channel", "000000000000000000");
			this.currencyCommandPrefix = builder
					.comment("Prefix for currency commands.")
					.define("prefix", "!");
			this.limitSearchToNetworkTraders = builder
					.comment("Whether the !search command should limit its search results to only Network Traders, or if it should list all traders.")
					.define("limitSearchToNetwork", true);


			builder.comment("Bot notification options.").push("notifications");

			this.traderCreationNotifications = builder
					.comment("Whether a notification will appear in the currency bot channel when a Network Trader is created.",
							"Notification will have a 60 second delay to allow them time to customize the traders name, etc.")
					.define("networkTraderBuilt", true);

			this.auctionHouseCreateNotifications = builder
					.comment("Whether a notification will appear in the currency bot channel when an Auction is created in the Auction House.")
					.define("auctionHouseCreated", true);

			this.auctionHouseCreatePersistentNotifications = builder
					.comment("Whether a notification will appear in the currency bot channel when a Persistent Auction is created automatically.",
							"Requires that auction house creation notifications also be enabled.")
					.define("auctionHousePersistentCreations", true);

			this.auctionHouseCancelNotifications = builder
					.comment("Whether a notification will appear in the currency bot channel when an Auction is cancelled in the Auction House.")
					.define("auctionHouseCancelled", false);

			this.auctionHouseWinNotifications = builder
					.comment("Whether a notification will appear in the currency bot channel when an Auction is completed and had a bidder.")
					.define("auctionHouseWon", true);

			builder.pop();

			builder.pop();

		}

	}

	public static final ForgeConfigSpec clientSpec;
	public static final Config.Client CLIENT;
	public static final ForgeConfigSpec commonSpec;
	public static final Config.Common COMMON;
	public static final ForgeConfigSpec serverSpec;
	public static final Config.Server SERVER;

	static
	{
		//Client
		final Pair<Client,ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Config.Client::new);
		clientSpec = clientPair.getRight();
		CLIENT = clientPair.getLeft();
		//Common
		final Pair<Common,ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Config.Common::new);
		commonSpec = commonPair.getRight();
		COMMON = commonPair.getLeft();
		//Server
		final Pair<Server,ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Config.Server::new);
		serverSpec = serverPair.getRight();
		SERVER = serverPair.getLeft();
	}

}