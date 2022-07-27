package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class VillagerTradeManager {

	public static List<ItemListing> getGenericWandererTrades() {
		return ImmutableList.of(
				//Machines
				new LazyTrade(ModItems.COIN_GOLD.get(), 1, ModBlocks.MACHINE_ATM.get()),
				new LazyTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
				new LazyTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.TERMINAL.get())
				);
	}
	public static List<ItemListing> getRareWandererTrades() {
		return ImmutableList.of(
				//Traders
				new LazyTrade(ModItems.COIN_GOLD.get(), 2, ModItems.COIN_IRON.get(), 4, ModBlocks.DISPLAY_CASE.get()),
				new LazyTrade(ModItems.COIN_GOLD.get(), 4, ModBlocks.ARMOR_DISPLAY.get())
				);
	}
	
	//Bankers sell miscellaneous trade-related stuff
	//Can also trade raw materials for coins to allow bypassing of the coin-mint
	public static Map<Integer,List<ItemListing>> getBankerTrades() {
		return ImmutableMap.of(
				1,
				ImmutableList.of(
						//Sell Coin Mint
						new LazyTrade(2, ModItems.COIN_IRON.get(), 5, ModBlocks.MACHINE_MINT.get()),
						//Sell ATM
						new LazyTrade(2, ModItems.COIN_GOLD.get(), 1, ModBlocks.MACHINE_ATM.get()),
						//Sell Cash Register
						new LazyTrade(1, ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
						//Sell Trading Core
						new LazyTrade(1, ModItems.COIN_IRON.get(), 4, ModItems.COIN_COPPER.get(), 8, ModItems.TRADING_CORE.get())
						),
				2,
				ImmutableList.of(
						//Sell first 4 shelves
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemLike[] {ModBlocks.SHELF.get(WoodType.OAK), ModBlocks.SHELF.get(WoodType.BIRCH), ModBlocks.SHELF.get(WoodType.SPRUCE), ModBlocks.SHELF.get(WoodType.JUNGLE) }, 12, 5, 0.05f),
						//Sell 4 "rare" shelves
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemLike[] {ModBlocks.SHELF.get(WoodType.ACACIA), ModBlocks.SHELF.get(WoodType.DARK_OAK), ModBlocks.SHELF.get(WoodType.CRIMSON), ModBlocks.SHELF.get(WoodType.WARPED) }, 12, 5, 0.05f),
						//Sell display case
						new LazyTrade(5, ModItems.COIN_IRON.get(), 10, ModBlocks.DISPLAY_CASE.get())
						),
				3,
				ImmutableList.of(
						//Sell first 4 card displays
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 15), new ItemLike[] {ModBlocks.CARD_DISPLAY.get(WoodType.OAK), ModBlocks.CARD_DISPLAY.get(WoodType.BIRCH), ModBlocks.CARD_DISPLAY.get(WoodType.SPRUCE), ModBlocks.CARD_DISPLAY.get(WoodType.JUNGLE) }, 12, 10, 0.05f),
						//Sell second 4 card displays
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 15), new ItemLike[] {ModBlocks.CARD_DISPLAY.get(WoodType.ACACIA), ModBlocks.CARD_DISPLAY.get(WoodType.DARK_OAK), ModBlocks.CARD_DISPLAY.get(WoodType.CRIMSON), ModBlocks.CARD_DISPLAY.get(WoodType.WARPED) }, 12, 10, 0.05f),
						//Sell armor display
						new LazyTrade(10, ModItems.COIN_IRON.get(), 20, ModBlocks.ARMOR_DISPLAY.get()),
						//Sell small trader server
						new LazyTrade(10, ModItems.COIN_IRON.get(), 15, ModBlocks.ITEM_TRADER_SERVER_SMALL.get()),
						//Sell Terminal
						new LazyTrade(10, ModItems.COIN_IRON.get(), 10, ModBlocks.TERMINAL.get())
						),
				4,
				ImmutableList.of(
						//Sell Vending Machines
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 25), ModBlocks.VENDING_MACHINE.getAll().toArray(new Block[0]), 12, 15, 0.05f),
						//Sell medium trader server
						new LazyTrade(15, ModItems.COIN_IRON.get(), 30, ModBlocks.ITEM_TRADER_SERVER_MEDIUM.get()),
						//Sell Freezer
						new LazyTrade(20, ModItems.COIN_IRON.get(), 30, ModBlocks.FREEZER.get()),
						//Sell Money Mending book
						new LazyTrade(20, ModItems.COIN_DIAMOND.get(), 15, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.MONEY_MENDING.get(), 1)))
						),
				5,
				ImmutableList.of(
						//Sell Large Vending Machines
						new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON.get(), 25), ModBlocks.VENDING_MACHINE_LARGE.getAll().toArray(new Block[0]), 12, 30, 0.05f),
						//Sell large trader server
						new LazyTrade(30, ModItems.COIN_GOLD.get(), 6, ModBlocks.ITEM_TRADER_SERVER_LARGE.get()),
						//Sell extra-large trader server
						new LazyTrade(30, ModItems.COIN_GOLD.get(), 10, ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE.get()),
						//Sell Money Mending book
						new LazyTrade(30, ModItems.COIN_DIAMOND.get(), 10, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.MONEY_MENDING.get(), 1)))
						)
				);
	}
	
	private static final float ENCHANTMENT_PRICE_MODIFIER = 0.25f;
	
	//Cashiers are a mashup of every vanilla trade where the player buys items from the trader, however the payment is in coins instead of emeralds.
	//Will not buy items and give coins, it will only sell items for coins
	public static Map<Integer,List<ItemListing>> getCashierTrades() {
		return ImmutableMap.of(
				1,
				ImmutableList.of(
						//Farmer
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.BREAD, 6), 16, 1, 0.05f),
						//Fisherman
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(Items.COD_BUCKET), 16, 1, 0.05f),
						//Shepherd
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(Items.SHEARS), 12, 1, 0.05f),
						//Fletcher
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 8), new ItemStack(Items.ARROW, 16), 12, 1, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(1),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Blocks.BOOKSHELF), 12, 1, 0.05f),
						//Cartographer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.MAP), 12, 1, 0.05f),
						//Cleric
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.REDSTONE), 12, 1, 0.05f),
						//Armorer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.IRON_LEGGINGS), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.IRON_BOOTS), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.IRON_HELMET), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.IRON_CHESTPLATE), 12, 1, 0.05f),
						//Weaponsmith
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.IRON_AXE), 12, 1, 0.05f),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 3, Items.IRON_SWORD, 12, 1, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Toolsmith
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_AXE), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_SHOVEL), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_PICKAXE), 12, 1, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_HOE), 12, 1, 0.05f),
						//Butcher
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.RABBIT_STEW), 12, 1, 0.05f),
						//Leatherworker (dyed armor only)
						//Mason
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.BRICK, 10), 16, 1, 0.05f)
						),
				2,
				ImmutableList.of(
						//Farmer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.PUMPKIN_PIE, 4), 12, 5, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.APPLE, 4), 16, 5, 0.05f),
						//Fisherman
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 3), new ItemStack(Items.COD, 15), 16, 10, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.CAMPFIRE), 12, 5, 0.05f),
						//Shepherd
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.WHITE_WOOL), 16, 5, 0.05f),
						//Fletcher
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(Items.BOW), 12, 5, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(5),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.LANTERN), 12, 5, 0.05f),
						//Cartographer
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 3), StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecoration.Type.MONUMENT, 12, 5),
						//Cleric
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.LAPIS_LAZULI), 12, 5, 0.05f),
						//Armorer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 4), new ItemStack(Blocks.BELL), 12, 5, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 9), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.CHAINMAIL_LEGGINGS), 12, 5, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(ModItems.COIN_COPPER.get(), 3), new ItemStack(Items.CHAINMAIL_BOOTS), 12, 5, 0.05f),
						//Weaponsmith (bell trade duplicate)
						//Toolsmith (bell trade duplicate)
						//Butcher
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.PORKCHOP, 6), 16, 5, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.COOKED_CHICKEN, 8), 16, 5, 0.05f),
						//Leatherworker (dyed armor only)
						//Mason
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.CHISELED_STONE_BRICKS, 4), 16, 5, 0.05f)
						),
				3,
				ImmutableList.of(
						//Farmer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.COOKIE, 18), 18, 10, 0.05f),
						//Fisherman
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 2, Items.FISHING_ROD, 3, 10, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Shepherd (none)
						//Fletcher
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(Items.CROSSBOW), 12, 10, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(10),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Blocks.GLASS,4), 12, 10, 0.05f),
						//Cartographer
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 4), StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecoration.Type.MANSION, 12, 10),
						//Cleric
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Blocks.GLOWSTONE), 12, 10, 0.05f),
						//Armorer
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(ModItems.COIN_COPPER.get(), 4), new ItemStack(Items.CHAINMAIL_HELMET), 12, 10, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 10), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 12, 10, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.SHIELD), 12, 10, 0.05f),
						//Weaponsmith (none)
						//Toolsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 5, Items.IRON_AXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 4, Items.IRON_SHOVEL, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 6, Items.IRON_PICKAXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new BasicItemListing(new ItemStack(ModItems.COIN_DIAMOND.get(), 2), new ItemStack(ModItems.COIN_IRON.get(), 1), new ItemStack(Items.DIAMOND_HOE), 3, 10, 0.05f),
						//Butcher (none)
						//Leatherworker (dyed armor only)
						//Mason
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_ANDESITE, 4), 16, 10, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_DIORITE, 4), 16, 10, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_GRANITE, 4), 16, 10, 0.05f)
						),
				4,
				ImmutableList.of(
						//Farmer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Blocks.CAKE), 12, 15, 0.05f),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.NIGHT_VISION, 100, 15),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.JUMP, 160, 15),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.WEAKNESS, 100, 15),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.BLINDNESS, 120, 15),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.POISON, 100, 15),
						new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), MobEffects.SATURATION, 7, 15),
						//Fisherman (none)
						//Shepherd (none)
						//Fletcher
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 5, Items.BOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Librarian
						new EnchantedBookForCoinsTrade(15),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 5), new ItemStack(Items.CLOCK), 12, 15, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.COMPASS), 12, 15, 0.05f),
						//Cartographer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Items.ITEM_FRAME), 12, 15, 0.05f),
						//Cleric
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Items.ENDER_PEARL), 12, 15, 0.05f),
						//Armorer
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 7, Items.DIAMOND_LEGGINGS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 5, Items.DIAMOND_BOOTS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Weaponsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 4, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Toolsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 3, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 2, Items.DIAMOND_SHOVEL, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Butcher (none)
						//Leatherworker (dyed horse armor only)
						//Mason
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.TERRACOTTA, 1), 16, 15, 0.05f)
						),
				5,
				ImmutableList.of(
						//Farmer
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 15), new ItemStack(Items.GOLDEN_CARROT), 12, 30, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.GLISTERING_MELON_SLICE), 12, 30, 0.05f),
						//Fisherman (none)
						//Shepherd
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.PAINTING), 12, 30, 0.05f),
						//Fletcher
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 10, Items.CROSSBOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Librarian
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.NAME_TAG), 12, 30, 0.05f),
						//Cartographer
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.GLOBE_BANNER_PATTERN), 12, 30, 0.05f),
						//Cleric
						new BasicItemListing(new ItemStack(ModItems.COIN_EMERALD.get(), 1), new ItemStack(Blocks.NETHER_WART, 12), 12, 30, 0.05f),
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.EXPERIENCE_BOTTLE), 12, 30, 0.05f),
						//Armorer
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 8, Items.DIAMOND_CHESTPLATE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 6, Items.DIAMOND_HELMET, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Weaponsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 4, Items.DIAMOND_SWORD, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Toolsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 4, Items.DIAMOND_PICKAXE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Butcher (none)
						//Leatherworker (dyed armor)
						new BasicItemListing(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.SADDLE), 12, 30, 0.05f),
						//Mason
						new BasicItemListing(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.QUARTZ_BLOCK), 12, 30, 0.05f)
						)
				);
	}
	
	//Be lowest priority so that we can modify trades added to a vanilla villager by another mod
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnVillagerTradeSetup(VillagerTradesEvent event)
	{
		
		if(event.getType() == CustomProfessions.BANKER.get())
		{
			
			if(!Config.COMMON.addBankerVillager.get())
				return;
			
			LightmansCurrency.LogInfo("Registering banker trades.");
			
			Map<Integer,List<ItemListing>> bankerTrades = getBankerTrades();
			for(int i = 1; i <= 5; i++)
			{
				List<ItemListing> currentTrades = event.getTrades().get(i);
				List<ItemListing> newTrades = bankerTrades.get(i);
				newTrades.forEach(trade -> currentTrades.add(trade));
			}
			
		}
		else if(event.getType() == CustomProfessions.CASHIER.get())
		{
			
			if(!Config.COMMON.addCashierVillager.get())
				return;
			
			LightmansCurrency.LogInfo("Registering cashier trades.");
			
			Map<Integer,List<ItemListing>> cashierTrades = getCashierTrades();
			for(int i = 1; i <= 5; i++)
			{
				List<ItemListing> currentTrades = event.getTrades().get(i);
				List<ItemListing> newTrades = cashierTrades.get(i);
				newTrades.forEach(trade -> currentTrades.add(trade));
			}
		}
		else
		{
			
			ResourceLocation type = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(event.getType());
			
			if(type.getNamespace().equals("minecraft"))
			{
				if(!Config.COMMON.changeVanillaTrades.get())
					return;
				LightmansCurrency.LogInfo("Replacing Emeralds for villager type '" + type + "'.");
				replaceExistingTrades(type.toString(), event.getTrades());
			}
			else if(Config.COMMON.changeModdedTrades.get())
			{
				LightmansCurrency.LogInfo("Replacing Emeralds for villager type '" + type + "'.");
				replaceExistingTrades(type.toString(), event.getTrades());
			}
				
			
		}
	}
	
	private static void replaceExistingTrades(String trader, Int2ObjectMap<List<ItemListing>> trades) {
		
		Item replacementItem = Config.getEmeraldReplacementItem(trader);
		
		for(int i = 1; i <= 5; ++i)
		{
			List<ItemListing> tradeList = trades.get(i);
			
			List<ItemListing> newList = new ArrayList<>();
			
			for(ItemListing trade : tradeList)
				newList.add(new ConvertedTrade(trade, Items.EMERALD, replacementItem));
			
			trades.put(i, newList);
			
		}
		
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnWandererTradeSetup(WandererTradesEvent event)
	{
		
		if(Config.COMMON.addCustomWanderingTrades.get())
		{
			List<ItemListing> genericTrades = event.getGenericTrades();
			List<ItemListing> rareTrades = event.getRareTrades();
			
			getGenericWandererTrades().forEach(trade -> genericTrades.add(trade));
			getRareWandererTrades().forEach(trade -> rareTrades.add(trade));
		}
		
		if(Config.COMMON.changeWanderingTrades.get())
		{
			
			replaceExistingTrades(event.getGenericTrades());
			replaceExistingTrades(event.getRareTrades());
			
		}
		
	}
	
	private static void replaceExistingTrades(List<ItemListing> tradeList) {
		
		Item replacementItem = Config.getDefaultEmeraldReplacementItem();
		
		for(int i = 0; i < tradeList.size(); ++i)
			tradeList.set(i, new ConvertedTrade(tradeList.get(i), Items.EMERALD, replacementItem));
		
	}
	
	public static class LazyTrade extends BasicItemListing
	{
		
		private static final int MAX_COUNT = 12;
		private static final float PRICE_MULT = 0.05f;
		
		public LazyTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem)
		{
			this(1, priceItem, priceCount, forsaleItem);
		}
		
		public LazyTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
		{
			this(1, priceItem, priceCount, forsaleItem, forsaleCount);
		}
		
		public LazyTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem)
		{
			this(xpValue, priceItem, priceCount, forsaleItem, 1);
		}
		
		public LazyTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem, priceCount), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
		public LazyTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
		}
		
		public LazyTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
		}
		
		public LazyTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
		{
			this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
		}
		
		public LazyTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
		public LazyTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemStack forSaleItem)
		{
			super(new ItemStack(priceItem1, priceCount1), ItemStack.EMPTY, forSaleItem, MAX_COUNT, xpValue, PRICE_MULT);
		}

		public LazyTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemStack forSaleItem)
		{
			super(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), forSaleItem, MAX_COUNT, xpValue, PRICE_MULT);
		}
		
	}
	
	public static class SetTrade extends BasicItemListing
	{
		
		private static final int MAX_COUNT = 12;
		private static final float PRICE_MULT = 0.05f;
		
		public SetTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem)
		{
			this(1, priceItem, priceCount, forsaleItem);
		}
		
		public SetTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
		{
			this(1, priceItem, priceCount, forsaleItem, forsaleCount);
		}
		
		public SetTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem)
		{
			this(xpValue, priceItem, priceCount, forsaleItem, 1);
		}
		
		public SetTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem, priceCount), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
		public SetTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
		}
		
		public SetTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
		{
			this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
		}
		
		public SetTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
		{
			this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
		}
		
		public SetTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
		{
			super(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
		}
		
	}
	
	private static class SuspiciousStewForItemTrade implements ItemListing
	{
		
		private final ItemStack price1;
		private final ItemStack price2;
		private final MobEffect effect;
		private final int duration;
		private final int xpValue;
		
		private SuspiciousStewForItemTrade(ItemStack price, MobEffect effect, int duration, int xpValue)
		{
			this(price, ItemStack.EMPTY, effect, duration, xpValue);
		}
		
		private SuspiciousStewForItemTrade(ItemStack price1, ItemStack price2, MobEffect effect, int duration, int xpValue)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.effect = effect;
			this.duration = duration;
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			SuspiciousStewItem.saveMobEffect(itemstack, this.effect, this.duration);
			return new MerchantOffer(this.price1, this.price2, itemstack, 12, this.xpValue, 0.05f);
		}
		
		
		
	}
	
	private static class EnchantedItemForCoinsTrade implements ItemListing
	{
		
		private final Item baseCoin;
		private final int baseCoinCount;
		private final Item sellItem;
		private final int maxUses;
		private final int xpValue;
		private final float priceMultiplier;
		private final double basePriceModifier;
		
		private EnchantedItemForCoinsTrade(ItemLike baseCoin, int baseCoinCount, ItemLike sellItem, int maxUses, int xpValue, float priceMultiplier, double basePriceModifier)
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
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			int i = 5 + rand.nextInt(15);
			ItemStack itemstack = EnchantmentHelper.enchantItem(rand, new ItemStack(sellItem), i, false);
			
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
			"\nprice1=" + price1.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price1.getItem()) +
			"\nprice2=" + price2.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price2.getItem())
			);
			
			return new MerchantOffer(price1, price2, itemstack, this.maxUses, this.xpValue, this.priceMultiplier);
		}
		
	}
	
	private static class EnchantedBookForCoinsTrade implements ItemListing
	{
		
		private static final Item baseCoin = ModItems.COIN_GOLD.get();
		private static final int baseCoinAmount = 5;
		
		private final int xpValue;
		
		public EnchantedBookForCoinsTrade(int xpValue)
		{
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			
			List<Enchantment> list = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(Enchantment::isTradeable).collect(Collectors.toList());
			Enchantment enchantment = list.get(rand.nextInt(list.size()));
			
			int level = 1;
			if(enchantment.getMaxLevel() > 0)
				level = rand.nextInt(enchantment.getMaxLevel()) + 1;
			else
				LightmansCurrency.LogError("Enchantment of type '" + ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString() + "' has a max enchantment level of " + enchantment.getMaxLevel() + ". Unable to properly randomize the enchantment level for a villager trade. Will default to a level 1 enchantment.");
			ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
			
			long coinValue = MoneyUtil.getValue(baseCoin);
			long baseValue = coinValue * baseCoinAmount;
			
			int valueRandom = rand.nextInt(5 + level * 10);
			long value = baseValue + coinValue * (level + valueRandom);
			if (enchantment.isTreasureOnly())
				value *= 2;

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
			"\nprice1=" + price1.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price1.getItem()) +
			"\nprice2=" + price2.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price2.getItem())
			);
			
     		return new MerchantOffer(price1, price2, itemstack, 12, this.xpValue, 0.05f);
     		
		}
		
	}
	
	private static class ItemsForMapTrade implements ItemListing
	{
		
		private final ItemStack price1;
		private final ItemStack price2;
		private final TagKey<Structure> destination;
		private final String displayName;
		private final MapDecoration.Type mapDecorationType;
		private final int maxUses;
		private final int xpValue;
		
		public ItemsForMapTrade(ItemStack price, TagKey<Structure> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
		{
			this(price, ItemStack.EMPTY, destination, displayName, mapDecorationType, maxUses, xpValue);
		}
		
		public ItemsForMapTrade(ItemStack price1, ItemStack price2, TagKey<Structure> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.destination = destination;
			this.displayName = displayName;
			this.mapDecorationType = mapDecorationType;
			this.maxUses = maxUses;
			this.xpValue = xpValue;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			
			if(!(trader.level instanceof ServerLevel))
				return null;
			else
			{
				ServerLevel serverworld = (ServerLevel)trader.level;
				BlockPos blockPos = serverworld.findNearestMapStructure(this.destination, trader.blockPosition(), 100, true);
				if(blockPos != null)
				{
					ItemStack itemstack = MapItem.create(serverworld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
					MapItem.lockMap(serverworld, itemstack);
					MapItemSavedData.addTargetDecoration(itemstack, blockPos, "+", this.mapDecorationType);
					itemstack.setHoverName(Component.translatable(this.displayName));
					return new MerchantOffer(this.price1, this.price2, itemstack, this.maxUses, this.xpValue, 0.05f);
				}
				else
					return null;
			}
		}
		
	}
	
	public static class RandomItemForItemTrade implements ItemListing
	{

		private final ItemStack price1;
		private final ItemStack price2;
		private final ItemLike[] sellItemOptions;
		private final int maxTrades;
		private final int xpValue;
		private final float priceMult;
		
		public RandomItemForItemTrade(ItemStack price, ItemLike[] sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
		}
		
		public RandomItemForItemTrade(ItemStack price1, ItemStack price2, ItemLike[] sellItemOptions, int maxTrades, int xpValue, float priceMult)
		{
			this.price1 = price1;
			this.price2 = price2;
			this.sellItemOptions = sellItemOptions;
			this.maxTrades = maxTrades;
			this.xpValue = xpValue;
			this.priceMult = priceMult;
		}
		
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource rand) {
			
			int index = rand.nextInt(this.sellItemOptions.length);
			ItemStack sellItem = new ItemStack(sellItemOptions[index]);
			
			return new MerchantOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xpValue, this.priceMult);
		}
		
		
		
	}
	
	public static class ConvertedTrade implements ItemListing
	{

		final ItemListing tradeSource;
		final ItemLike oldItem;
		final ItemLike newItem;
		
		/**
		 * A modified Item Listing that takes an existing trade/listing and converts a given item into another item.
		 * Warning: Replaced items do not keep any NBT data, so this should not be used for items that can be enchanted.
		 * Used by LC to replace Emeralds with Emerald Coins.
		 * @param tradeSource The Item Listing to modify.
		 * @param oldItem The Item to replace.
		 * @param newItem The Item to replace the oldItem with.
		 */
		public ConvertedTrade(ItemListing tradeSource, ItemLike oldItem, ItemLike newItem) {
			this.tradeSource = tradeSource;
			this.oldItem = oldItem;
			this.newItem = newItem;
		}
		
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource random) {
			MerchantOffer offer = this.tradeSource.getOffer(trader, random);
			ItemStack itemA = offer.getBaseCostA();
			ItemStack itemB = offer.getCostB();
			ItemStack itemC = offer.getResult();
			if(itemA.getItem() == this.oldItem)
				itemA = new ItemStack(this.newItem, itemA.getCount());
			if(itemB.getItem() == this.oldItem)
				itemB = new ItemStack(this.newItem, itemB.getCount());
			if(itemC.getItem() == this.oldItem)
				itemC = new ItemStack(this.newItem, itemC.getCount());
			
			
			return new MerchantOffer(itemA, itemB, itemC, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
		}
		
	}
	
}
