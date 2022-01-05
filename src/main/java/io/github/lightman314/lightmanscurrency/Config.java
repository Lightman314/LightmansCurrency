package io.github.lightman314.lightmanscurrency;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.core.LootManager;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	
	
	
	public static final List<String> CLIENT_DEFAULT_RENDER_AS_BLOCK = ImmutableList.of(
			"minecraft:oak_sapling", "minecraft:birch_sapling", "minecraft:spruce_sapling", "minecraft:jungle_sapling",
			"minecraft:acacia_sapling", "minecraft:dark_oak_sapling", "minecraft:cobweb", "minecraft:grass",
			"minecraft:dead_bush", "minecraft:fern", "minecraft:seagrass", "minecraft:sea_pickle", "minecraft:dandelion",
			"minecraft:poppy", "minecraft:blue_orchid", "minecraft:allium", "minecraft:azure_bluet", "minecraft:red_tulip",
			"minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip", "minecraft:oxeye_daisy",
			"minecraft:cornflower", "minecraft:lily_of_the_valley", "minecraft:wither_rose", "minecraft:brown_mushroom",
			"minecraft:red_mushroom", "minecraft:crimson_fungus", "minecraft:warped_fungus", "minecraft:crimson_roots",
			"minecraft:warped_roots", "minecraft:nether_sprouts", "minecraft:weeping_vines", "minecraft:twisting_vines",
			"minecraft:sugar_cane", "minecraft:kelp", "minecraft:bamboo", "minecraft:torch", "minecraft:end_rod",
			"minecraft:soul_torch", "minecraft:chain", "minecraft:vine", "minecraft:lily_pad", "minecraft:flower_pot",
			"minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony", "minecraft:tall_grass",
			"minecraft:large_fern", "minecraft:glass_pane", "minecraft:white_stained_glass_pane",
			"minecraft:orange_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane",
			"minecraft:yellow_stained_glass_pane", "minecraft:lime_stained_glass_pane", "minecraft:pink_stained_glass_pane",
			"minecraft:gray_stained_glass_pane", "minecraft:light_gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane",
			"minecraft:purple_stained_glass_pane", "minecraft:blue_stained_glass_pane", "minecraft:brown_stained_glass_pane",
			"minecraft:green_stained_glass_pane", "minecraft:red_stained_glass_pane", "minecraft:black_stained_glass_pane",
			"minecraft:tube_coral", "minecraft:brain_coral", "minecraft:bubble_coral", "minecraft:fire_coral",
			"minecraft:horn_coral", "minecraft:dead_brain_coral", "minecraft:dead_bubble_coral", "minecraft:dead_horn_coral",
			"minecraft:dead_tube_coral", "minecraft:tube_coral_fan", "minecraft:brain_coral_fan", "minecraft:bubble_coral_fan",
			"minecraft:fire_coral_fan", "minecraft:horn_coral_fan", "minecraft:dead_tube_coral_fan", "minecraft:dead_brain_coral_fan",
			"minecraft:dead_bubble_coral_fan", "minecraft:dead_fire_coral_fan", "minecraft:dead_horn_coral_fan", "minecraft:oak_sign",
			"minecraft:spruce_sign", "minecraft:birch_sign", "minecraft:jungle_sign", "minecraft:acacia_sign",
			"minecraft:dark_oak_sign", "minecraft:crimson_sign", "minecraft:warped_sign", "minecraft:campfire", "minecraft:lantern",
			"minecraft:soul_lantern", "minecraft:bell", "minecraft:soul_campfire", "minecraft:redstone_torch", "minecraft:lever",
			"minecraft:tripwire_hook", "minecraft:string", "minecraft:hopper", "minecraft:iron_door", "minecraft:oak_door",
			"minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door",
			"minecraft:dark_oak_door", "minecraft:crimson_door", "minecraft:warped_door", "minecraft:repeater", "minecraft:comparator",
			"minecraft:redstone", "minecraft:rail", "minecraft:powered_rail", "minecraft:detector_rail", "minecraft:activator_rail",
			"minecraft:cake", "lightmanscurrency:coinpile_copper", "lightmanscurrency:coinpile_iron", "lightmanscurrency:coinpile_gold",
			"lightmanscurrency:coinpile_emerald", "lightmanscurrency:coinpile_diamond", "lightmanscurrency:coinpile_netherite"
			);
	
	private static boolean canMint = false;
	public static boolean canMint() { return canMint; }
	public static boolean localCanMint() { return COMMON.allowCoinMinting.get(); }
	private static boolean canMelt = false;
	public static boolean canMelt() { return canMelt; }
	public static boolean localCanMelt() { return COMMON.allowCoinMelting.get(); }
	
	private static boolean mintCopper = false;
	private static boolean mintIron = false;
	private static boolean mintGold = false;
	private static boolean mintEmerald = false;
	private static boolean mintDiamond = false;
	private static boolean mintNetherite = false;
	public static boolean canMint(Item item)
	{
		if(item == ModItems.COIN_COPPER)
			return mintCopper;
		else if(item == ModItems.COIN_IRON)
			return mintIron;
		else if(item == ModItems.COIN_GOLD)
			return mintGold;
		else if(item == ModItems.COIN_EMERALD)
			return mintEmerald;
		else if(item == ModItems.COIN_DIAMOND)
			return mintDiamond;
		else if(item == ModItems.COIN_NETHERITE)
			return mintNetherite;
		
		//If no rule is against it, allow the minting
		return true;
	}
	
	private static boolean meltCopper = false;
	private static boolean meltIron = false;
	private static boolean meltGold = false;
	private static boolean meltEmerald = false;
	private static boolean meltDiamond = false;
	private static boolean meltNetherite = false;
	public static boolean canMelt(Item item)
	{
		if(item == ModItems.COIN_COPPER)
			return meltCopper;
		else if(item == ModItems.COIN_IRON)
			return meltIron;
		else if(item == ModItems.COIN_GOLD)
			return meltGold;
		else if(item == ModItems.COIN_EMERALD)
			return meltEmerald;
		else if(item == ModItems.COIN_DIAMOND)
			return meltDiamond;
		else if(item == ModItems.COIN_NETHERITE)
			return meltNetherite;
		
		//If no rule is against it, allow the minting
		return true;
	}
	
	public static class Client
	{
		
		public enum TraderRenderType { FULL, PARTIAL, NONE }
		
		//Render options
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> renderBlocksAsItems;
		public final ForgeConfigSpec.EnumValue<TraderRenderType> traderRenderType;
		
		//Wallet Button options
		public final ForgeConfigSpec.BooleanValue renderWalletButton;
		public final ForgeConfigSpec.IntValue walletButtonX;
		public final ForgeConfigSpec.IntValue walletButtonY;
		public final ForgeConfigSpec.IntValue walletButtonCreativeX;
		public final ForgeConfigSpec.IntValue walletButtonCreativeY;
		
		Client(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Client configuration settings").push("client");
			
			this.renderBlocksAsItems = builder
					.comment("BlockItems that should be spaced out as though they were normal items.")
					.defineList("renderBlocksAsItems", CLIENT_DEFAULT_RENDER_AS_BLOCK, o -> o instanceof String);
			
			builder.comment("Quality Settings").push("settings");
			this.traderRenderType = builder
					.comment("How many items the traders should render as stock. Useful to avoid lag in trader-rich areas.",
							"FULL: Renders all items based on stock as intended.",
							"PARTIAL: Renders only 1 item per trade slot regardless of stock.",
							"NONE: Traders do not render items.")
					.defineEnum("traderRenderType", TraderRenderType.FULL);
			builder.pop();
			
			builder.comment("Wallet Button Settings").push("wallet_button");
			this.renderWalletButton = builder
					.comment("Whether a wallet button should appear on the inventory screen, allowing you to open the wallet screen without use of the keybind.")
					.define("renderButton", true);
			this.walletButtonX = builder
					.comment("The x position that the button should be placed at.")
					.defineInRange("buttonX", 176, -50, 255);
			this.walletButtonY = builder
					.comment("The y position that the button should be placed at.")
					.defineInRange("buttonY", 0, -50, 255);
			this.walletButtonCreativeX = builder
					.comment("The x position that the button should be placed at in the creative screen.")
					.defineInRange("buttonCreativeX", 195, -50, 255);
			this.walletButtonCreativeY = builder
					.comment("The y position that the button should be placed at.")
					.defineInRange("buttonCreativeY", 0, -50, 255);
			
		}
		
	}
	
	public static class Common
	{
		
		//Melt/Mint Options
		private final ForgeConfigSpec.BooleanValue allowCoinMinting;
		private final ForgeConfigSpec.BooleanValue allowCoinMelting;
		
		//Specific Melt/Mint Options
		private final ForgeConfigSpec.BooleanValue mintCopper;
		private final ForgeConfigSpec.BooleanValue mintIron;
		private final ForgeConfigSpec.BooleanValue mintGold;
		private final ForgeConfigSpec.BooleanValue mintEmerald;
		private final ForgeConfigSpec.BooleanValue mintDiamond;
		private final ForgeConfigSpec.BooleanValue mintNetherite;
		
		private final ForgeConfigSpec.BooleanValue meltCopper;
		private final ForgeConfigSpec.BooleanValue meltIron;
		private final ForgeConfigSpec.BooleanValue meltGold;
		private final ForgeConfigSpec.BooleanValue meltEmerald;
		private final ForgeConfigSpec.BooleanValue meltDiamond;
		private final ForgeConfigSpec.BooleanValue meltNetherite;
		
		//Custom trades
		public final ForgeConfigSpec.BooleanValue addCustomWanderingTrades;
		public final ForgeConfigSpec.BooleanValue addBankerVillager;
		public final ForgeConfigSpec.BooleanValue addCashierVillager;
		
		//Debug
		public final ForgeConfigSpec.IntValue debugLevel;
		
		//Entity Loot
		public final ForgeConfigSpec.BooleanValue enableEntityDrops;
		public final ForgeConfigSpec.BooleanValue enableSpawnerEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> copperEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> ironEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> goldEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> emeraldEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> diamondEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> netheriteEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossCopperEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossIronEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossGoldEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossEmeraldEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossDiamondEntityDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> bossNetheriteEntityDrops;
		
		//Chest Loot
		public final ForgeConfigSpec.BooleanValue enableChestLoot;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> copperChestDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> ironChestDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> goldChestDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> emeraldChestDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> diamondChestDrops;
		public final ForgeConfigSpec.ConfigValue<List <? extends String>> netheriteChestDrops;
		
		//Coin Values
		public final ForgeConfigSpec.IntValue ironCoinWorth;
		public final ForgeConfigSpec.IntValue goldCoinWorth;
		public final ForgeConfigSpec.IntValue emeraldCoinWorth;
		public final ForgeConfigSpec.IntValue diamondCoinWorth;
		public final ForgeConfigSpec.IntValue netheriteCoinWorth;
		
		//Coin Pile Values
		public final ForgeConfigSpec.IntValue coinpileCopperWorth;
		public final ForgeConfigSpec.IntValue coinpileIronWorth;
		public final ForgeConfigSpec.IntValue coinpileGoldWorth;
		public final ForgeConfigSpec.IntValue coinpileEmeraldWorth;
		public final ForgeConfigSpec.IntValue coinpileDiamondWorth;
		public final ForgeConfigSpec.IntValue coinpileNetheriteWorth;
		
		//Coin Block Values
		public final ForgeConfigSpec.IntValue coinBlockCopperWorth;
		public final ForgeConfigSpec.IntValue coinBlockIronWorth;
		public final ForgeConfigSpec.IntValue coinBlockGoldWorth;
		public final ForgeConfigSpec.IntValue coinBlockEmeraldWorth;
		public final ForgeConfigSpec.IntValue coinBlockDiamondWorth;
		public final ForgeConfigSpec.IntValue coinBlockNetheriteWorth;
		
		
		
		
		Common(ForgeConfigSpec.Builder builder)
		{
			
			builder.comment("Common configuration settings").push("common");
			
			this.allowCoinMinting = builder
					.comment("Determines whether or not coins should be craftable via the Coin Minting Machine.")
					.translation("lightmanscurrency.configgui.canMintCoins")
					.define("canMintCoins", true);
			this.allowCoinMelting = builder
					.comment("Determines whether or not coins can be melted back into their source material in the Coin Minting Machine.")
					.translation("lightmanscurrency.configgui.canMeltCoins")
					.define("canMeltCoins", false);
			
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
			
			builder.comment("Villager Related Settings.").push("villagers");
			
			this.addCustomWanderingTrades = builder
					.comment("Whether the wandering trader will have additional trades that allow you to buy misc items with money.")
					.define("addCustomWanderingTrades", true);
			
			this.addBankerVillager = builder
					.comment("Whether the banker villager profession will have any registered trades. The banker sells Lightman's Currency items for coins.")
					.define("addBanker", true);
			this.addCashierVillager = builder
					.comment("Whether the cashier villager profession will have any registered trades.. The cashier sells an amalgamation of vanilla traders products for coins.")
					.define("addCashier", true);
			
			builder.pop();
			
			this.debugLevel = builder
					.comment("Level of debug messages to be shown in the logs.","0-All debug messages. 1-Warnings/Errors only. 2-Errors only. 3-No debug messages.","Note: All debug messages will still be sent debug.log regardless of settings.")
					.defineInRange("debugLevel", 0, 0, 3);
			
			//Entity loot modification
			builder.comment("Entity loot settings. Inputs can be either loot-table id ('minecraft:entities/zombie'), or the LivingEntities id ('minecraft:zombie').").push("entity_loot");
			
			this.enableEntityDrops = builder
					.comment("Whether coins can be dropped by entities. Does not effect chest loot generation.")
					.define("enableEntityDrops", true);
			//Entity spawned loot drops
			this.enableSpawnerEntityDrops = builder
					.comment("Whether coins can be dropped by entities that were spawned by the vanilla spawner.")
					.define("enableSpawnerEntityDrops", false);
			
			//Copper
			this.copperEntityDrops = builder
					.comment("Entities that will occasionally drop copper coins.")
					.defineList("copper", LootManager.ENTITY_COPPER_DROPLIST, o -> o instanceof String);
			//Iron
			this.ironEntityDrops = builder
					.comment("Entities that will occasionally drop copper -> iron coins.")
					.defineList("iron", LootManager.ENTITY_IRON_DROPLIST, o -> o instanceof String);
			//Gold
			this.goldEntityDrops = builder
					.comment("Entities that will occasionally drop copper -> gold coins.")
					.defineList("gold", LootManager.ENTITY_GOLD_DROPLIST, o -> o instanceof String);
			//Emerald
			this.emeraldEntityDrops = builder
					.comment("Entities that will occasionally drop copper -> emerald coins.")
					.defineList("emerald", LootManager.ENTITY_EMERALD_DROPLIST, o -> o instanceof String);
			//Diamond
			this.diamondEntityDrops = builder
					.comment("Entities that will occasionally drop copper -> diamond coins.")
					.defineList("diamond", LootManager.ENTITY_DIAMOND_DROPLIST, o -> o instanceof String);
			//Netherite
			this.netheriteEntityDrops = builder
					.comment("Entities that will occasionally drop copper -> netherite coins.")
					.defineList("netherite", LootManager.ENTITY_NETHERITE_DROPLIST, o -> o instanceof String);
			
			//Boss
			this.bossCopperEntityDrops = builder
					.comment("Entities that will drop a large amount of copper coins.")
					.defineList("boss_copper", LootManager.ENTITY_BOSS_COPPER_DROPLIST, o -> o instanceof String);
			this.bossIronEntityDrops = builder
					.comment("Entities that will drop a large amount of copper -> iron coins.")
					.defineList("boss_iron", LootManager.ENTITY_BOSS_IRON_DROPLIST, o -> o instanceof String);
			this.bossGoldEntityDrops = builder
					.comment("Entities that will drop a large amount of copper -> gold coins.")
					.defineList("boss_gold", LootManager.ENTITY_BOSS_GOLD_DROPLIST, o -> o instanceof String);
			this.bossEmeraldEntityDrops = builder
					.comment("Entities that will drop a large amount of copper -> emerald coins.")
					.defineList("boss_emerald", LootManager.ENTITY_BOSS_EMERALD_DROPLIST, o -> o instanceof String);
			this.bossDiamondEntityDrops = builder
					.comment("Entities that will drop a large amount of copper -> diamond coins.")
					.defineList("boss_diamond", LootManager.ENTITY_BOSS_DIAMOND_DROPLIST, o -> o instanceof String);
			this.bossNetheriteEntityDrops = builder
					.comment("Entities that will drop a large amount of copper -> netherite coins.")
					.defineList("boss_netherite", LootManager.ENTITY_BOSS_NETHERITE_DROPLIST, o -> o instanceof String);
			
			builder.pop();
			
			builder.comment("Chest loot settings.").push("chest_loot");
			this.enableChestLoot = builder
					.comment("Whether coins can spawn in chests Does not effect entity loot drops.")
					.define("enableChestLoot", true);
			this.copperChestDrops = builder
					.comment("Chests that will occasionally spawn copper coins.")
					.defineList("copper", LootManager.CHEST_COPPER_DROPLIST, o -> o instanceof String);
			this.ironChestDrops = builder
					.comment("Chests that will occasionally spawn copper -> iron coins.")
					.defineList("iron", LootManager.CHEST_IRON_DROPLIST, o -> o instanceof String);
			this.goldChestDrops = builder
					.comment("Chests that will occasionally spawn copper -> gold coins.")
					.defineList("gold", LootManager.CHEST_GOLD_DROPLIST, o -> o instanceof String);
			this.emeraldChestDrops = builder
					.comment("Chests that will occasionally spawn copper -> emerald coins.")
					.defineList("emerald", LootManager.CHEST_EMERALD_DROPLIST, o -> o instanceof String);
			this.diamondChestDrops = builder
					.comment("Chests that will occasionally spawn copper -> diamond coins.")
					.defineList("diamond", LootManager.CHEST_DIAMOND_DROPLIST, o -> o instanceof String);
			this.netheriteChestDrops = builder
					.comment("Chests that will occasionally spawn copper -> netherite coins.")
					.defineList("netherite", LootManager.CHEST_NETHERITE_DROPLIST, o -> o instanceof String);
			
			builder.pop();
			
			builder.comment("Coin value settings.").push("coin_value");
			
			this.ironCoinWorth = builder
					.comment("How many copper coins are required to make 1 iron coin.")
					.defineInRange("coinValueIron", 10, 2, 64);
			this.goldCoinWorth = builder
					.comment("How many iron coins are required to make 1 gold coin.")
					.defineInRange("coinValueGold", 10, 2, 64);
			this.emeraldCoinWorth = builder
					.comment("How many gold coins are required to make 1 emerald coin.")
					.defineInRange("coinValueEmerald", 10, 2, 64);
			this.diamondCoinWorth = builder
					.comment("How many emerald coins are required to make 1 diamond coin.")
					.defineInRange("coinValueDiamond", 10, 2, 64);
			this.netheriteCoinWorth = builder
					.comment("How many diamond coins are required to make 1 netherite coin.")
					.defineInRange("coinValueNetherite", 10, 2, 64);
			
			builder.comment("Coinpile values (leave as default unless a datapack/mod changes the crafting recipes)").push("coinpile");
			
			this.coinpileCopperWorth = builder
					.comment("How many copper coins are used to craft a copper coinpile.")
					.defineInRange("coinpileValueCopper", 9, 2, 9);
			this.coinpileIronWorth = builder
					.comment("How many iron coins are used to craft an iron coinpile.")
					.defineInRange("coinpileValueIron", 9, 2, 9);
			this.coinpileGoldWorth = builder
					.comment("How many gold coins are used to craft a gold coinpile.")
					.defineInRange("coinpileValueGold", 9, 2, 9);
			this.coinpileEmeraldWorth = builder
					.comment("How many emerald coins are used to craft an emerald coinpile.")
					.defineInRange("coinpileValueEmerald", 9, 2, 9);
			this.coinpileDiamondWorth = builder
					.comment("How many diamond coins are used to craft a diamond coinpile.")
					.defineInRange("coinpileValueDiamond", 9, 2, 9);
			this.coinpileNetheriteWorth = builder
					.comment("How many netherite coins are used to craft a netherite coinpile.")
					.defineInRange("coinpileValueNetherite", 9, 2, 9);
			
			builder.pop();
			
			builder.comment("Coin block values.", "Leave at default values unless a datapack/mod changes the crafting recipes!").push("coinblock");
			
			this.coinBlockCopperWorth = builder
					.comment("How many copper coinpiles are used to craft a copper coin block.")
					.defineInRange("coinBlockValueCopper", 4, 2, 9);
			this.coinBlockIronWorth = builder
					.comment("How many iron coinpiles are used to craft an iron coin block.")
					.defineInRange("coinBlockValueIron", 4, 2, 9);
			this.coinBlockGoldWorth = builder
					.comment("How many gold coinpiles are used to craft a gold coin block.")
					.defineInRange("coinBlockValueGold", 4, 2, 9);
			this.coinBlockEmeraldWorth = builder
					.comment("How many emerald coinpiles are used to craft an emerald coin block.")
					.defineInRange("coinBlockValueEmerald", 4, 2, 9);
			this.coinBlockDiamondWorth = builder
					.comment("How many diamond coinpiles are used to craft a diamond coin block.")
					.defineInRange("coinBlockValueDiamond", 4, 2, 9);
			this.coinBlockNetheriteWorth = builder
					.comment("How many netherite coinpiles are used to craft a netherite coin block.")
					.defineInRange("coinBlockValueNetherite", 4, 2, 9);
			
			builder.pop();
			
			builder.pop();
			
			builder.pop();
		}
		
	}
	
	public static class Server
	{
		
		public final ForgeConfigSpec.IntValue logLimit;
		
		Server(ForgeConfigSpec.Builder builder)
		{
			
			builder.comment("Server Config Settings").push("server");
			
			this.logLimit = builder.comment("The maximum amount of entries allowed in a text log.")
					.defineInRange("logLimit", 100, 1, Integer.MAX_VALUE);
			
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
	
	public static CompoundNBT getSyncData()
	{
		CompoundNBT data = new CompoundNBT();
		
		//Put mint/melt data
		data.putBoolean("canMint", COMMON.allowCoinMinting.get());
		data.putBoolean("canMelt", COMMON.allowCoinMelting.get());
		
		//Put coin-specific mint/melt data
		data.putBoolean("mintCopper", COMMON.mintCopper.get());
		data.putBoolean("mintIron", COMMON.mintIron.get());
		data.putBoolean("mintGold", COMMON.mintGold.get());
		data.putBoolean("mintEmerald", COMMON.mintEmerald.get());
		data.putBoolean("mintDiamond", COMMON.mintDiamond.get());
		data.putBoolean("mintNetherite", COMMON.mintNetherite.get());
		
		data.putBoolean("meltCopper", COMMON.meltCopper.get());
		data.putBoolean("meltIron", COMMON.meltIron.get());
		data.putBoolean("meltGold", COMMON.meltGold.get());
		data.putBoolean("meltEmerald", COMMON.meltEmerald.get());
		data.putBoolean("meltDiamond", COMMON.meltDiamond.get());
		data.putBoolean("meltNetherite", COMMON.meltNetherite.get());
		
		//Coin worth
		CompoundNBT coinValues = new CompoundNBT();
		coinValues.putInt("iron", COMMON.ironCoinWorth.get());
		coinValues.putInt("gold", COMMON.goldCoinWorth.get());
		coinValues.putInt("emerald", COMMON.emeraldCoinWorth.get());
		coinValues.putInt("diamond", COMMON.diamondCoinWorth.get());
		coinValues.putInt("netherite", COMMON.netheriteCoinWorth.get());
		data.put("coinValues", coinValues);
		//Coinpile worth
		CompoundNBT coinpileValues = new CompoundNBT();
		coinpileValues.putInt("copper", COMMON.coinpileCopperWorth.get());
		coinpileValues.putInt("iron", COMMON.coinpileIronWorth.get());
		coinpileValues.putInt("gold", COMMON.coinpileGoldWorth.get());
		coinpileValues.putInt("emerald", COMMON.coinpileEmeraldWorth.get());
		coinpileValues.putInt("diamond", COMMON.coinpileDiamondWorth.get());
		coinpileValues.putInt("netherite", COMMON.coinpileNetheriteWorth.get());
		data.put("coinpileValues", coinpileValues);
		//Coinblock worth
		CompoundNBT coinblockValues = new CompoundNBT();
		coinblockValues.putInt("copper", COMMON.coinBlockCopperWorth.get());
		coinblockValues.putInt("iron", COMMON.coinBlockIronWorth.get());
		coinblockValues.putInt("gold", COMMON.coinBlockGoldWorth.get());
		coinblockValues.putInt("emerald", COMMON.coinBlockEmeraldWorth.get());
		coinblockValues.putInt("diamond", COMMON.coinBlockDiamondWorth.get());
		coinblockValues.putInt("netherite", COMMON.coinBlockNetheriteWorth.get());
		data.put("coinblockValues", coinblockValues);
		
		return data;
	}
	
	public static void syncConfig()
	{
		syncConfig(getSyncData());
	}
	
	public static void syncConfig(CompoundNBT data)
	{
		//Can Mint/Melt
		canMint = data.getBoolean("canMint");
		canMelt = data.getBoolean("canMelt");
		
		mintCopper = data.getBoolean("mintCopper");
		mintIron = data.getBoolean("mintIron");
		mintGold = data.getBoolean("mintGold");
		mintEmerald = data.getBoolean("mintEmerald");
		mintDiamond = data.getBoolean("mintDiamond");
		mintNetherite = data.getBoolean("mintNetherite");
		
		meltCopper = data.getBoolean("meltCopper");
		meltIron = data.getBoolean("meltIron");
		meltGold = data.getBoolean("meltGold");
		meltEmerald = data.getBoolean("meltEmerald");
		meltDiamond = data.getBoolean("meltDiamond");
		meltNetherite = data.getBoolean("meltNetherite");
		
		//Coin worth
		CompoundNBT coinValues = data.getCompound("coinValues");
		MoneyUtil.changeCoinConversion(ModItems.COIN_IRON, ModItems.COIN_COPPER, coinValues.getInt("iron"));
		MoneyUtil.changeCoinConversion(ModItems.COIN_GOLD, ModItems.COIN_IRON, coinValues.getInt("gold"));
		MoneyUtil.changeCoinConversion(ModItems.COIN_EMERALD, ModItems.COIN_GOLD, coinValues.getInt("emerald"));
		MoneyUtil.changeCoinConversion(ModItems.COIN_DIAMOND, ModItems.COIN_EMERALD, coinValues.getInt("diamond"));
		MoneyUtil.changeCoinConversion(ModItems.COIN_NETHERITE, ModItems.COIN_DIAMOND, coinValues.getInt("netherite"));
		
		//Coinpile worth
		CompoundNBT coinpileValues = data.getCompound("coinpileValues");
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_COPPER.item, ModItems.COIN_COPPER, coinpileValues.getInt("copper"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_IRON.item, ModItems.COIN_IRON, coinpileValues.getInt("iron"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_GOLD.item, ModItems.COIN_GOLD, coinpileValues.getInt("gold"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_EMERALD.item, ModItems.COIN_EMERALD, coinpileValues.getInt("emerald"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_DIAMOND.item, ModItems.COIN_DIAMOND, coinpileValues.getInt("diamond"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINPILE_NETHERITE.item, ModItems.COIN_NETHERITE, coinpileValues.getInt("netherite"));
		
		//Coinblock worth
		CompoundNBT coinblockValues = data.getCompound("coinblockValues");
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_COPPER.item, ModBlocks.COINPILE_COPPER.item, coinblockValues.getInt("copper"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_IRON.item, ModBlocks.COINPILE_IRON.item, coinblockValues.getInt("iron"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_GOLD.item, ModBlocks.COINPILE_GOLD.item, coinblockValues.getInt("gold"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_EMERALD.item, ModBlocks.COINPILE_EMERALD.item, coinblockValues.getInt("emerald"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_DIAMOND.item, ModBlocks.COINPILE_DIAMOND.item, coinblockValues.getInt("diamond"));
		MoneyUtil.changeCoinConversion(ModBlocks.COINBLOCK_NETHERITE.item, ModBlocks.COINPILE_NETHERITE.item, coinblockValues.getInt("netherite"));
		
	}
	
}
