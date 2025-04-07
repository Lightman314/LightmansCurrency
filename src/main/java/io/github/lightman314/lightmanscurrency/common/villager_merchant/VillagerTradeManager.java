package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;

import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.*;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredItemListing;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber
public class VillagerTradeManager {

	public static final ResourceLocation BANKER_ID = VersionUtil.lcResource("banker");
	public static final ResourceLocation CASHIER_ID = VersionUtil.lcResource("cashier");

	public static final String WANDERING_TRADER_ID = "minecraft:wandering_trader";

	public static void registerDefaultTrades() {
		CustomVillagerTradeData.registerDefaultFile(BANKER_ID, ImmutableMap.of(
				1,
				ImmutableList.of(
						//Sell Coin Mint
						new SimpleTrade(2, ModItems.COIN_IRON.get(), 5, ModBlocks.COIN_MINT.get()),
						//Sell Terminal
						RandomTrade.build(new ItemStack(ModItems.COIN_GOLD.get()), LCTags.Items.NETWORK_TERMINAL, 12, 1, 0.05f),
						//Sell ATM
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 8), LCTags.Items.ATM, 12, 1, 0.05f),
						//Sell Cash Register
						new SimpleTrade(1, ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
						//Sell Trading Core
						new SimpleTrade(1, ModItems.COIN_IRON.get(), 4, ModItems.COIN_COPPER.get(), 8, ModItems.TRADING_CORE.get())
				),
				2,
				ImmutableList.of(
						//Sell shelves
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 6), LCTags.Items.TRADER_SHELF, 12, 5, 0.05f),
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 14), LCTags.Items.TRADER_SHELF_2x2, 12, 5, 0.05f),
						//Sell Coin Chest
						new SimpleTrade(5, ModItems.COIN_IRON.get(), 15, ModBlocks.COIN_CHEST.get()),
						//Sell display case
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 10), LCTags.Items.TRADER_DISPLAY_CASE, 12, 5, 0.05f)
				),
				3,
				ImmutableList.of(
						//Sell card displays
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 15), LCTags.Items.TRADER_CARD_DISPLAY, 12, 10, 0.05f),
						//Sell armor display
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 20, ModBlocks.ARMOR_DISPLAY.get()),
						//Sell ticket kiosk
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 20, ModBlocks.TICKET_KIOSK.get()),
						//Sell small trader server
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 15, ModBlocks.ITEM_NETWORK_TRADER_1.get()),
						//Sell Terminal
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 10, ModBlocks.TERMINAL.get())
				),
				4,
				ImmutableList.of(
						//Sell Vending Machines
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 25), LCTags.Items.TRADER_VENDING_MACHINE, 12, 15, 0.05f),
						//Sell medium trader server
						new SimpleTrade(15, ModItems.COIN_IRON.get(), 30, ModBlocks.ITEM_NETWORK_TRADER_2.get()),
						//Sell Freezer
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 30), LCTags.Items.TRADER_FREEZER, 12, 20, 0.05f),
						//Sell Bookshelf trader
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 30), LCTags.Items.TRADER_SPECIALTY_BOOKSHELF, 12, 20, 0.05f),
						//Sell Money Mending book
						new EnchantedBookForCoinsTrade(ModItems.COIN_DIAMOND.get(), 1, LCTags.Enchantments.MONEY_MENDING, SimpleTrade.MAX_TRADES, 20, SimpleTrade.PRICE_MULT)
				),
				5,
				ImmutableList.of(
						//Sell Large Vending Machines
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 25), LCTags.Items.TRADER_LARGE_VENDING_MACHINE, 12, 30, 0.05f),
						//Sell large trader server
						new SimpleTrade(30, ModItems.COIN_GOLD.get(), 6, ModBlocks.ITEM_NETWORK_TRADER_3.get()),
						//Sell extra-large trader server
						new SimpleTrade(30, ModItems.COIN_GOLD.get(), 10, ModBlocks.ITEM_NETWORK_TRADER_4.get()),
						//Sell slot machine
						new SimpleTrade(30, ModItems.COIN_GOLD.get(), 10, ModBlocks.SLOT_MACHINE.get()),
						//Sell trader interface
						RandomTrade.build(new ItemStack(ModItems.COIN_EMERALD.get(), 5), LCTags.Items.TRADER_INTERFACE, 12, 30, 0.05f),
						//Sell Money Mending book
						new EnchantedBookForCoinsTrade(ModItems.COIN_DIAMOND.get(), 1, LCTags.Enchantments.MONEY_MENDING, SimpleTrade.MAX_TRADES, 30, SimpleTrade.PRICE_MULT)
				)
		));

		CustomVillagerTradeData.registerDefaultFile(CASHIER_ID, ImmutableMap.of(
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
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 3), StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 5),
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
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 4), StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 12, 10),
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
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.NIGHT_VISION, 100), 15),
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.JUMP, 160), 15),
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.WEAKNESS, 100), 15),
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.BLINDNESS, 120), 15),
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.POISON, 100), 15),
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), SimpleTrade.createSuspiciousStew(MobEffects.SATURATION, 7), 15),
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
		));

		//Wandering Trader Trades
		CustomVillagerTradeData.registerDefaultWanderingTrades(
				//Generic Trades
				ImmutableList.of(
						//Machines
						new SimpleTrade(ModItems.COIN_GOLD.get(), 1, ModBlocks.ATM.get()),
						new SimpleTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
						new SimpleTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.TERMINAL.get())
				),
				//Rare Trades
				ImmutableList.of(
						//Traders
						RandomTrade.build(new ItemStack(ModItems.COIN_GOLD.get(),2), new ItemStack(ModItems.COIN_IRON.get(),4), LCTags.Items.TRADER_DISPLAY_CASE, 12, 1, 0.05f),
						new SimpleTrade(ModItems.COIN_GOLD.get(), 4, ModBlocks.ARMOR_DISPLAY.get())
				));
	}

	private static final float ENCHANTMENT_PRICE_MODIFIER = 0.25f;

	//Be the lowest priority so that we can modify trades added to a vanilla villager by another mod
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnVillagerTradeSetup(VillagerTradesEvent event)
	{
		if(event.getType() == CustomProfessions.BANKER.get())
		{
			
			if(!LCConfig.COMMON.addBankerVillager.get())
				return;
			
			LightmansCurrency.LogInfo("Registering banker trades.");
			
			Map<Integer,List<ItemListing>> bankerTrades = CustomVillagerTradeData.getVillagerData(BANKER_ID);
			for(int i = 1; i <= 5; i++)
			{
				List<ItemListing> currentTrades = event.getTrades().get(i);
				List<ItemListing> newTrades = bankerTrades.get(i);
				if(newTrades != null)
					currentTrades.addAll(newTrades);
				else
					LightmansCurrency.LogWarning("Banker Trades have no listings for trade level " + i);
			}
			
		}
		else if(event.getType() == CustomProfessions.CASHIER.get())
		{
			
			if(!LCConfig.COMMON.addCashierVillager.get())
				return;
			
			LightmansCurrency.LogInfo("Registering cashier trades.");
			
			Map<Integer,List<ItemListing>> cashierTrades = CustomVillagerTradeData.getVillagerData(CASHIER_ID);
			for(int i = 1; i <= 5; i++)
			{
				List<ItemListing> currentTrades = event.getTrades().get(i);
				List<ItemListing> newTrades = cashierTrades.get(i);
				if(newTrades != null)
					currentTrades.addAll(newTrades);
				else
					LightmansCurrency.LogWarning("Cashier Trades have no listings for trade level " + i);
			}
		}
		else
		{
			
			ResourceLocation type = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());

			assert type != null;
			if(type.getNamespace().equals("minecraft"))
			{
				if(!LCConfig.COMMON.changeVanillaTrades.get())
					return;
				LightmansCurrency.LogInfo("Replacing Emeralds for villager type '" + type + "'.");
				replaceExistingTrades(type.toString(), event.getTrades());
			}
			else if(LCConfig.COMMON.changeModdedTrades.get())
			{
				LightmansCurrency.LogInfo("Replacing Emeralds for villager type '" + type + "'.");
				replaceExistingTrades(type.toString(), event.getTrades());
			}
				
			
		}
	}

	/**
	 * Used to apply configured trade changes as defined by {@link LCConfig.Common#getVillagerMod(String)}
	 * Should check {@link LCConfig.Common#changeVanillaTrades} or {@link LCConfig.Common#changeModdedTrades} first before applying.
	 */
	public static void replaceExistingTrades(String trader, Int2ObjectMap<List<ItemListing>> trades) {
		trades.forEach((i,list) -> replaceExistingTrades(trader, list));
	}

	/**
	 * Used to apply configured trade changes as defined by {@link LCConfig.Common#getVillagerMod(String)}
	 * Should check {@link LCConfig.Common#changeVanillaTrades} or {@link LCConfig.Common#changeModdedTrades} first before applying.
	 */
	public static void replaceExistingTrades(String trader, List<ItemListing> trades) {
		trades.replaceAll(t -> {
			if(t instanceof ConfiguredItemListing)
				return t;
			return new ConfiguredItemListing(t, LCConfig.COMMON.getVillagerMod(trader));
		});
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnWandererTradeSetup(WandererTradesEvent event)
	{
		
		//Replace the existing trades before adding my own custom ones
		if(LCConfig.COMMON.changeWanderingTrades.get())
		{
			
			replaceExistingTrades(WANDERING_TRADER_ID,event.getGenericTrades());
			replaceExistingTrades(WANDERING_TRADER_ID,event.getRareTrades());
			
		}
		
		//Add my own custom trades
		if(LCConfig.COMMON.addCustomWanderingTrades.get())
		{
			var pair = CustomVillagerTradeData.getWanderingTraderData();
			event.getGenericTrades().addAll(pair.getFirst());
			event.getRareTrades().addAll(pair.getSecond());
		}
		
	}
	

	
}
