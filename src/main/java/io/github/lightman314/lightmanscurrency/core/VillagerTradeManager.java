package io.github.lightman314.lightmanscurrency.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.IItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class VillagerTradeManager {

	
	
	private static final List<ITrade> GENERIC_TRADES_WANDERER = ImmutableList.of(
			//Machines
			new LazyTrade(ModItems.COIN_GOLD, 1, ModBlocks.MACHINE_ATM.item),
			new LazyTrade(ModItems.COIN_IRON, 5, ModBlocks.CASH_REGISTER.item),
			new LazyTrade(ModItems.COIN_IRON, 5, ModBlocks.TERMINAL.item)
			);
	private static final List<ITrade> RARE_TRADES_WANDERER = ImmutableList.of(
			//Traders
			new LazyTrade(ModItems.COIN_GOLD, 2, ModItems.COIN_IRON, 4, ModBlocks.DISPLAY_CASE),
			new LazyTrade(ModItems.COIN_GOLD, 4, ModBlocks.ARMOR_DISPLAY.item)
			);
	
	//Bankers sell miscellaneous trade-related stuff
	//Can also trade raw materials for coins to allow bypassing of the coin-mint
	private static final Map<Integer,List<ITrade>> TRADES_BANKER = ImmutableMap.of(
			1,
			ImmutableList.of(
					//Sell Coin Mint
					new LazyTrade(2, ModItems.COIN_IRON, 5, ModBlocks.MACHINE_MINT),
					//Sell ATM
					new LazyTrade(2, ModItems.COIN_GOLD, 1, ModBlocks.MACHINE_ATM),
					//Sell Cash Register
					new LazyTrade(1, ModItems.COIN_IRON, 5, ModBlocks.CASH_REGISTER),
					//Sell Trading Core
					new LazyTrade(1, ModItems.COIN_IRON, 4, ModItems.COIN_COPPER, 8, ModItems.TRADING_CORE)
					//Coin for ingot & ingot for coin trades (copper level)
					//(TBD 1.17)
					//(TBD 1.17)
					),
			2,
			ImmutableList.of(
					//Sell first 4 shelves
					new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 6), new IItemProvider[] {ModBlocks.SHELF.get(WoodType.OAK), ModBlocks.SHELF.get(WoodType.BIRCH), ModBlocks.SHELF.get(WoodType.SPRUCE), ModBlocks.SHELF.get(WoodType.JUNGLE)}, 12, 5, 0.05f),
					//Sell 4 "rare" shelves
					new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 6), new IItemProvider[] {ModBlocks.SHELF.get(WoodType.ACACIA), ModBlocks.SHELF.get(WoodType.DARK_OAK), ModBlocks.SHELF.get(WoodType.WARPED), ModBlocks.SHELF.get(WoodType.CRIMSON)}, 12, 5, 0.05f),
					//Sell display case
					new LazyTrade(5, ModItems.COIN_IRON, 10, ModBlocks.DISPLAY_CASE)
					//Coin for ingot & ingot for coin trades (iron level)
					//new SetTrade(5, Items.IRON_INGOT, 10, ModItems.COIN_IRON, 9),
					//new SetTrade(5, ModItems.COIN_IRON, 10, Items.IRON_INGOT, 9)
					),
			3,
			ImmutableList.of(
					//Sell first 4 card displays
					new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 15), new IItemProvider[] {ModBlocks.CARD_DISPLAY.get(WoodType.OAK), ModBlocks.CARD_DISPLAY.get(WoodType.BIRCH), ModBlocks.CARD_DISPLAY.get(WoodType.SPRUCE), ModBlocks.CARD_DISPLAY.get(WoodType.JUNGLE) }, 12, 10, 0.05f),
					//Sell second 4 card displays
					new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 15), new IItemProvider[] {ModBlocks.CARD_DISPLAY.get(WoodType.ACACIA), ModBlocks.CARD_DISPLAY.get(WoodType.DARK_OAK), ModBlocks.CARD_DISPLAY.get(WoodType.CRIMSON), ModBlocks.CARD_DISPLAY.get(WoodType.WARPED) }, 12, 10, 0.05f),
					//Sell armor display
					new LazyTrade(10, ModItems.COIN_IRON, 20, ModBlocks.ARMOR_DISPLAY),
					//Sell small trader server
					new LazyTrade(10, ModItems.COIN_IRON, 15, ModBlocks.ITEM_TRADER_SERVER_SMALL),
					//Sell Terminal
					new LazyTrade(10, ModItems.COIN_IRON, 10, ModBlocks.TERMINAL)
					//Coin for ingot & ingot for coin trades (gold level)
					//new SetTrade(10, Items.GOLD_INGOT, 10, ModItems.COIN_GOLD, 9),
					//new SetTrade(10, ModItems.COIN_GOLD, 10, Items.GOLD_INGOT, 9)
					),
			4,
			ImmutableList.of(
					//Sell Vending Machines
					new RandomItemSetForItemTrade(new ItemStack(ModItems.COIN_IRON, 25), ModBlocks.VENDING_MACHINE1, 12, 15, 0.05f),
					//Sell medium trader server
					new LazyTrade(15, ModItems.COIN_IRON, 30, ModBlocks.ITEM_TRADER_SERVER_MEDIUM),
					//Sell Freezer
					new LazyTrade(20, ModItems.COIN_IRON, 30, ModBlocks.FREEZER)
					//Coin for ingot & ingot for coin trades (emerald & diamond level)
					//new SetTrade(15, Items.EMERALD, 10, ModItems.COIN_EMERALD, 9),
					//new SetTrade(20, Items.DIAMOND, 10, ModItems.COIN_DIAMOND, 9),
					//new SetTrade(15, ModItems.COIN_EMERALD, 10, Items.EMERALD, 9),
					//new SetTrade(20, ModItems.COIN_DIAMOND, 10, Items.DIAMOND, 9)
					),
			5,
			ImmutableList.of(
					//Sell Large Vending Machines
					new RandomItemSetForItemTrade(new ItemStack(ModItems.COIN_IRON, 25), ModBlocks.VENDING_MACHINE2, 12, 30, 0.05f),
					//Sell large trader server
					new LazyTrade(30, ModItems.COIN_GOLD, 6, ModBlocks.ITEM_TRADER_SERVER_LARGE),
					//Sell extra-large trader server
					new LazyTrade(30, ModItems.COIN_GOLD, 10, ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE)
					//Coin for ingot & ingot for coin trades (netherite level)
					//new SetTrade(25, Items.NETHERITE_INGOT, 10, ModItems.COIN_NETHERITE,9),
					//new SetTrade(25, ModItems.COIN_NETHERITE, 10, Items.NETHERITE_INGOT, 9)
					)
			);
	
	private static final float ENCHANTMENT_PRICE_MODIFIER = 0.25f;
	
	//Cashiers are a mashup of every vanilla trade where the player buys items from the trader, however the payment is in coins instead of emeralds.
	//Will not buy items and give coins, it will only sell items for coins
	private static final Map<Integer,List<ITrade>> TRADES_CASHIER = ImmutableMap.of(
			1,
			ImmutableList.of(
					//Farmer
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.BREAD, 6), 16, 1, 0.05f),
					//Fisherman
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(Items.COD_BUCKET), 16, 1, 0.05f),
					//Shepherd
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(Items.SHEARS), 12, 1, 0.05f),
					//Fletcher
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 8), new ItemStack(Items.ARROW, 16), 12, 1, 0.05f),
					//Librarian
					new EnchantedBookForCoinsTrade(1),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Blocks.BOOKSHELF), 12, 1, 0.05f),
					//Cartographer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.MAP), 12, 1, 0.05f),
					//Cleric
					new BasicTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.REDSTONE), 12, 1, 0.05f),
					//Armorer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.IRON_LEGGINGS), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.IRON_BOOTS), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.IRON_HELMET), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.IRON_CHESTPLATE), 12, 1, 0.05f),
					//Weaponsmith
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.IRON_AXE), 12, 1, 0.05f),
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 3, Items.IRON_SWORD, 12, 1, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Toolsmith
					new BasicTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_AXE), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_SHOVEL), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_PICKAXE), 12, 1, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_HOE), 12, 1, 0.05f),
					//Butcher
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.RABBIT_STEW), 12, 1, 0.05f),
					//Leatherworker (dyed armor only)
					//Mason
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.BRICK, 10), 16, 1, 0.05f)
					),
			2,
			ImmutableList.of(
					//Farmer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.PUMPKIN_PIE, 4), 12, 5, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.APPLE, 4), 16, 5, 0.05f),
					//Fisherman
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 3), new ItemStack(Items.COD, 15), 16, 10, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.CAMPFIRE), 12, 5, 0.05f),
					//Shepherd
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.WHITE_WOOL), 16, 5, 0.05f),
					//Fletcher
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(Items.BOW), 12, 5, 0.05f),
					//Librarian
					new EnchantedBookForCoinsTrade(5),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.LANTERN), 12, 5, 0.05f),
					//Cartographer
					new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD, 3), Structure.MONUMENT, MapDecoration.Type.MONUMENT, 12, 5),
					//Cleric
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.LAPIS_LAZULI), 12, 5, 0.05f),
					//Armorer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 4), new ItemStack(Blocks.BELL), 12, 5, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 9), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.CHAINMAIL_LEGGINGS), 12, 5, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(ModItems.COIN_COPPER, 3), new ItemStack(Items.CHAINMAIL_BOOTS), 12, 5, 0.05f),
					//Weaponsmith (bell trade duplicate)
					//Toolsmith (bell trade duplicate)
					//Butcher
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.PORKCHOP, 6), 16, 5, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.COOKED_CHICKEN, 8), 16, 5, 0.05f),
					//Leatherworker (dyed armor only)
					//Mason
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.CHISELED_STONE_BRICKS, 4), 16, 5, 0.05f)
					),
			3,
			ImmutableList.of(
					//Farmer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.COOKIE, 18), 18, 10, 0.05f),
					//Fisherman
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 2, Items.FISHING_ROD, 3, 10, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Shepherd (none)
					//Fletcher
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(Items.CROSSBOW), 12, 10, 0.05f),
					//Librarian
					new EnchantedBookForCoinsTrade(10),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Blocks.GLASS,4), 12, 10, 0.05f),
					//Cartographer
					new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD, 4), Structure.WOODLAND_MANSION, MapDecoration.Type.MANSION, 12, 10),
					//Cleric
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Blocks.GLOWSTONE), 12, 10, 0.05f),
					//Armorer
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(ModItems.COIN_COPPER, 4), new ItemStack(Items.CHAINMAIL_HELMET), 12, 10, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 10), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 12, 10, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.SHIELD), 12, 10, 0.05f),
					//Weaponsmith (none)
					//Toolsmith
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 5, Items.IRON_AXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 4, Items.IRON_SHOVEL, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 6, Items.IRON_PICKAXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new BasicTrade(new ItemStack(ModItems.COIN_DIAMOND, 2), new ItemStack(ModItems.COIN_IRON, 1), new ItemStack(Items.DIAMOND_HOE), 3, 10, 0.05f),
					//Butcher (none)
					//Leatherworker (dyed armor only)
					//Mason
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_ANDESITE, 4), 16, 10, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_DIORITE, 4), 16, 10, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_GRANITE, 4), 16, 10, 0.05f)
					),
			4,
			ImmutableList.of(
					//Farmer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Blocks.CAKE), 12, 15, 0.05f),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.NIGHT_VISION, 100, 15),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.JUMP_BOOST, 160, 15),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.WEAKNESS, 100, 15),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.BLINDNESS, 120, 15),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.POISON, 100, 15),
					new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), Effects.SATURATION, 7, 15),
					//Fisherman (none)
					//Shepherd (none)
					//Fletcher
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 5, Items.BOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Librarian
					new EnchantedBookForCoinsTrade(15),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 5), new ItemStack(Items.CLOCK), 12, 15, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.COMPASS), 12, 15, 0.05f),
					//Cartographer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Items.ITEM_FRAME), 12, 15, 0.05f),
					//Cleric
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Items.ENDER_PEARL), 12, 15, 0.05f),
					//Armorer
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 7, Items.DIAMOND_LEGGINGS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 5, Items.DIAMOND_BOOTS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Weaponsmith
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Toolsmith
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 3, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 2, Items.DIAMOND_SHOVEL, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Butcher (none)
					//Leatherworker (dyed horse armor only)
					//Mason
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.TERRACOTTA, 1), 16, 15, 0.05f)
					),
			5,
			ImmutableList.of(
					//Farmer
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 15), new ItemStack(Items.GOLDEN_CARROT), 12, 30, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.GLISTERING_MELON_SLICE), 12, 30, 0.05f),
					//Fisherman (none)
					//Shepherd
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.PAINTING), 12, 30, 0.05f),
					//Fletcher
					new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 10, Items.CROSSBOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Librarian
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.NAME_TAG), 12, 30, 0.05f),
					//Cartographer
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.GLOBE_BANNER_PATTERN), 12, 30, 0.05f),
					//Cleric
					new BasicTrade(new ItemStack(ModItems.COIN_EMERALD, 1), new ItemStack(Blocks.NETHER_WART, 12), 12, 30, 0.05f),
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.EXPERIENCE_BOTTLE), 12, 30, 0.05f),
					//Armorer
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 8, Items.DIAMOND_CHESTPLATE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 6, Items.DIAMOND_HELMET, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Weaponsmith
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_SWORD, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Toolsmith
					new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_PICKAXE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
					//Butcher (none)
					//Leatherworker (dyed armor)
					new BasicTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.SADDLE), 12, 30, 0.05f),
					//Mason
					new BasicTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.QUARTZ_BLOCK), 12, 30, 0.05f)
					)
			);
	
	@SubscribeEvent
	public static void OnVillagerTradeSetup(VillagerTradesEvent event)
	{
		if(event.getType() == CustomProfessions.BANKER && Config.COMMON.addBankerVillager.get())
		{
			
			LightmansCurrency.LogInfo("Registering banker trades.");
			
			for(int i = 1; i <= 5; i++)
			{
				List<ITrade> currentTrades = event.getTrades().get(i);
				List<ITrade> newTrades = TRADES_BANKER.get(i);
				newTrades.forEach(trade -> currentTrades.add(trade));
			}
			
		}
		else if(event.getType() == CustomProfessions.CASHIER && Config.COMMON.addCashierVillager.get())
		{
			
			LightmansCurrency.LogInfo("Registering cashier trades.");
			
			for(int i = 1; i <= 5; i++)
			{
				List<ITrade> currentTrades = event.getTrades().get(i);
				List<ITrade> newTrades = TRADES_CASHIER.get(i);
				newTrades.forEach(trade -> currentTrades.add(trade));
			}
		}
	}
	
	@SubscribeEvent
	public static void OnWandererTradeSetup(WandererTradesEvent event)
	{
		
		if(!Config.COMMON.addCustomWanderingTrades.get())
			return;
		
		List<ITrade> genericTrades = event.getGenericTrades();
		List<ITrade> rareTrades = event.getRareTrades();
		
		GENERIC_TRADES_WANDERER.forEach(trade -> genericTrades.add(trade));
		RARE_TRADES_WANDERER.forEach(trade -> rareTrades.add(trade));
		
	}
	
	public static class LazyTrade extends BasicTrade
	{
		
		private static final int MAX_COUNT = 12;
		private static final float PRICE_MULT = 0.05f;
		
		public LazyTrade(IItemProvider priceItem, int priceCount, IItemProvider forsaleItem)
		{
			this(1, priceItem, priceCount, forsaleItem);
		}
		
		public LazyTrade(IItemProvider priceItem, int priceCount, IItemProvider forsaleItem, int forsaleCount)
		{
			this(1, priceItem, priceCount, forsaleItem, forsaleCount);
		}
		
		public LazyTrade(int xpValue, IItemProvider priceItem, int priceCount, IItemProvider forsaleItem)
		{
			this(xpValue, priceItem, priceCount, forsaleItem, 1);
		}
		
		public LazyTrade(int xpValue, IItemProvider priceItem, int priceCount, IItemProvider forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem, priceCount), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
		public LazyTrade(IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
		}
		
		public LazyTrade(IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem, int forsaleCount)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
		}
		
		public LazyTrade(int xpValue, IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem)
		{
			this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
		}
		
		public LazyTrade(int xpValue, IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
	}
	
	public static class SetTrade extends BasicTrade
	{
		
		private static final int MAX_COUNT = 12;
		private static final float PRICE_MULT = 0.05f;
		
		public SetTrade(IItemProvider priceItem, int priceCount, IItemProvider forsaleItem)
		{
			this(1, priceItem, priceCount, forsaleItem);
		}
		
		public SetTrade(IItemProvider priceItem, int priceCount, IItemProvider forsaleItem, int forsaleCount)
		{
			this(1, priceItem, priceCount, forsaleItem, forsaleCount);
		}
		
		public SetTrade(int xpValue, IItemProvider priceItem, int priceCount, IItemProvider forsaleItem)
		{
			this(xpValue, priceItem, priceCount, forsaleItem, 1);
		}
		
		public SetTrade(int xpValue, IItemProvider priceItem, int priceCount, IItemProvider forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem, priceCount), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
		public SetTrade(IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
		}
		
		public SetTrade(IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem, int forsaleCount)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
		}
		
		public SetTrade(int xpValue, IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem)
		{
			this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
		}
		
		public SetTrade(int xpValue, IItemProvider priceItem1, int priceCount1, IItemProvider priceItem2, int priceCount2, IItemProvider forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
	}
	
	private static class SuspiciousStewForItemTrade implements ITrade
	{
		
		private final ItemStack price1;
		private final ItemStack price2;
		private final Effect effect;
		private final int duration;
		private final int xpValue;
		
		private SuspiciousStewForItemTrade(ItemStack price, Effect effect, int duration, int xpValue)
		{
			this(price, ItemStack.EMPTY, effect, duration, xpValue);
		}
		
		private SuspiciousStewForItemTrade(ItemStack price1, ItemStack price2, Effect effect, int duration, int xpValue)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.effect = effect;
			this.duration = duration;
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			SuspiciousStewItem.addEffect(itemstack, this.effect, this.duration);
			return new MerchantOffer(this.price1, this.price2, itemstack, 12, this.xpValue, 0.05f);
		}
		
		
		
	}
	
	private static class EnchantedItemForCoinsTrade implements ITrade
	{
		
		private final Item baseCoin;
		private final int baseCoinCount;
		private final Item sellItem;
		private final int maxUses;
		private final int xpValue;
		private final float priceMultiplier;
		private final double basePriceModifier;
		
		private EnchantedItemForCoinsTrade(IItemProvider baseCoin, int baseCoinCount, IItemProvider sellItem, int maxUses, int xpValue, float priceMultiplier, double basePriceModifier)
		{
			this.baseCoin = baseCoin.asItem();
			this.baseCoinCount = baseCoinCount;
			this.sellItem = sellItem.asItem();
			this.maxUses = maxUses;
			this.xpValue = xpValue;
			this.priceMultiplier = priceMultiplier;
			this.basePriceModifier = basePriceModifier;
		}
		
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			int i = 5 + rand.nextInt(15);
			ItemStack itemstack = EnchantmentHelper.addRandomEnchantment(rand, new ItemStack(sellItem), i, false);
			
			long coinValue = MoneyUtil.getValue(this.baseCoin);
			long baseValue = coinValue * this.baseCoinCount;
			long priceValue = baseValue + (long)(coinValue * i * this.basePriceModifier);
			
			ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
			List<ItemStack> priceStacks = MoneyUtil.getCoinsOfValue(priceValue);
			if(priceStacks.size() > 0)
				price1 = priceStacks.get(0);
			if(priceStacks.size() > 1)
				price2 = priceStacks.get(1);
			
			LightmansCurrency.LogInfo("EnchantedItemForCoinsTrade.getOffer() -> \n" +
			"i=" + i +
			"\ncoinValue=" + coinValue +
			"\nbaseValue=" + baseValue +
			"\npriceValue=" + priceValue +
			"\nprice1=" + price1.getCount() + "x" + price1.getItem().getRegistryName() +
			"\nprice2=" + price2.getCount() + "x" + price2.getItem().getRegistryName()
			);
			
			return new MerchantOffer(price1, price2, itemstack, this.maxUses, this.xpValue, this.priceMultiplier);
		}
		
	}
	
	private static class EnchantedBookForCoinsTrade implements ITrade
	{
		
		private static final Item baseCoin = ModItems.COIN_GOLD;
		private static final int baseCoinAmount = 5;
		
		private final int xpValue;
		
		public EnchantedBookForCoinsTrade(int xpValue)
		{
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			
			List<Enchantment> list = GameRegistry.findRegistry(Enchantment.class).getValues().stream().filter(Enchantment::canVillagerTrade).collect(Collectors.toList());
			Enchantment enchantment = list.get(rand.nextInt(list.size()));
			
			int level = MathHelper.nextInt(rand, enchantment.getMinLevel(), enchantment.getMaxLevel());
			ItemStack itemstack = EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchantment, level));
			
			long coinValue = MoneyUtil.getValue(baseCoin);
			long baseValue = coinValue * baseCoinAmount;
			
			int valueRandom = rand.nextInt(5 + level * 10);
			long value = baseValue + coinValue * (level + valueRandom);
			if (enchantment.isTreasureEnchantment()) {
				value *= 2;
			}

			List<ItemStack> coins = MoneyUtil.getCoinsOfValue(value);
			ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
			if(coins.size() > 0)
				price1 = coins.get(0);
			if(coins.size() > 1)
				price2 = coins.get(1);
			
			LightmansCurrency.LogInfo("EnchantedBookForCoinsTrade.getOffer() -> \n" +
			"baseValue=" + baseValue +
			"\ncoinValue=" + coinValue +
			"\nlevel=" + level +
			"\nvalueRandom=" + valueRandom +
			"\nvalue=" + value +
			"\nprice1=" + price1.getCount() + "x" + price1.getItem().getRegistryName() +
			"\nprice2=" + price2.getCount() + "x" + price2.getItem().getRegistryName()
			);
			
     		return new MerchantOffer(price1, price2, itemstack, 12, this.xpValue, 0.05f);
     		
		}
		
	}
	
	private static class ItemsForMapTrade implements ITrade
	{
		
		private final ItemStack price1;
		private final ItemStack price2;
		private final Structure<?> structureName;
		private final MapDecoration.Type mapDecorationType;
		private final int maxUses;
		private final int xpValue;
		
		public ItemsForMapTrade(ItemStack price, Structure<?> structureName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
		{
			this(price, ItemStack.EMPTY, structureName, mapDecorationType, maxUses, xpValue);
		}
		
		public ItemsForMapTrade(ItemStack price1, ItemStack price2, Structure<?> structureName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.structureName = structureName;
			this.mapDecorationType = mapDecorationType;
			this.maxUses = maxUses;
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			
			if(!(trader.world instanceof ServerWorld))
				return null;
			else
			{
				ServerWorld serverworld = (ServerWorld)trader.world;
				BlockPos blockPos = serverworld.func_241117_a_(this.structureName, trader.getPosition(), 100, true);
				if(blockPos != null)
				{
					ItemStack itemstack = FilledMapItem.setupNewMap(serverworld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
					FilledMapItem.func_226642_a_(serverworld, itemstack);
					MapData.addTargetDecoration(itemstack, blockPos, "+", this.mapDecorationType);
					itemstack.setDisplayName(new TranslationTextComponent("filled_map." + this.structureName.getStructureName().toLowerCase(Locale.ROOT)));
					return new MerchantOffer(this.price1, this.price2, itemstack, this.maxUses, this.xpValue, 0.05f);
				}
				else
					return null;
			}
		}
		
	}
	
	public static class RandomItemSetForItemTrade implements ITrade
	{
		private final ItemStack price1;
		private final ItemStack price2;
		private final IItemSet<?> sellItemOptions;
		private final int maxTrades;
		private final int xpValue;
		private final float priceMult;
		
		public RandomItemSetForItemTrade(ItemStack price, IItemSet<?> sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
		}
		
		public RandomItemSetForItemTrade(ItemStack price1, ItemStack price2, IItemSet<?> sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.sellItemOptions = sellItemOptions;
			this.maxTrades = maxTrades;
			this.xpValue = xpValue;
			this.priceMult = priceMult;
		}
		
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			
			List<Item> items = this.sellItemOptions.getAllItems();
			int index = rand.nextInt(items.size());
			ItemStack sellItem = new ItemStack(items.get(index));
			
			return new MerchantOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xpValue, this.priceMult);
		}
	}
	
	public static class RandomItemForItemTrade implements ITrade
	{

		private final ItemStack price1;
		private final ItemStack price2;
		private final IItemProvider[] sellItemOptions;
		private final int maxTrades;
		private final int xpValue;
		private final float priceMult;
		
		public RandomItemForItemTrade(ItemStack price, IItemProvider[] sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
		}
		
		public RandomItemForItemTrade(ItemStack price1, ItemStack price2, IItemProvider[] sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.sellItemOptions = sellItemOptions;
			this.maxTrades = maxTrades;
			this.xpValue = xpValue;
			this.priceMult = priceMult;
		}
		
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand) {
			
			int index = rand.nextInt(this.sellItemOptions.length);
			ItemStack sellItem = new ItemStack(sellItemOptions[index]);
			
			return new MerchantOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xpValue, this.priceMult);
		}
		
		
		
	}
	
}
