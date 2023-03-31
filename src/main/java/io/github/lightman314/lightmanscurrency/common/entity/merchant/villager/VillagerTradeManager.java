package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.listings.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
public class VillagerTradeManager {

	public static final ResourceLocation BANKER_ID = new ResourceLocation(LightmansCurrency.MODID, "banker");
	public static final ResourceLocation CASHIER_ID = new ResourceLocation(LightmansCurrency.MODID, "cashier");

	public static List<ItemListing> getGenericWandererTrades() {
		return ImmutableList.of(
				//Machines
				new SimpleTrade(ModItems.COIN_GOLD.get(), 1, ModBlocks.MACHINE_ATM.get()),
				new SimpleTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
				new SimpleTrade(ModItems.COIN_IRON.get(), 5, ModBlocks.TERMINAL.get())
				);
	}
	public static List<ItemListing> getRareWandererTrades() {
		return ImmutableList.of(
				//Traders
				new SimpleTrade(ModItems.COIN_GOLD.get(), 2, ModItems.COIN_IRON.get(), 4, ModBlocks.DISPLAY_CASE.get()),
				new SimpleTrade(ModItems.COIN_GOLD.get(), 4, ModBlocks.ARMOR_DISPLAY.get())
				);
	}

	//Bankers sell miscellaneous trade-related stuff
	//Can also trade raw materials for coins to allow bypassing of the coin-mint
	public static void registerDefaultTrades() {
		CustomVillagerTradeData.registerDefaultFile(BANKER_ID, ImmutableMap.of(
				1,
				ImmutableList.of(
						//Sell Coin Mint
						new SimpleTrade(2, ModItems.COIN_IRON.get(), 5, ModBlocks.MACHINE_MINT.get()),
						//Sell ATM
						new SimpleTrade(2, ModItems.COIN_GOLD.get(), 1, ModBlocks.MACHINE_ATM.get()),
						//Sell Cash Register
						new SimpleTrade(1, ModItems.COIN_IRON.get(), 5, ModBlocks.CASH_REGISTER.get()),
						//Sell Trading Core
						new SimpleTrade(1, ModItems.COIN_IRON.get(), 4, ModItems.COIN_COPPER.get(), 8, ModItems.TRADING_CORE.get())
						),
				2,
				ImmutableList.of(
						//Sell first 4 shelves
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 6), ModBlocks.SHELF.getSome(WoodType.OAK, WoodType.BIRCH, WoodType.SPRUCE, WoodType.JUNGLE), 12, 5, 0.05f),
						//Sell 4 "rare" shelves
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 6), ModBlocks.SHELF.getSome(WoodType.ACACIA, WoodType.DARK_OAK, WoodType.CRIMSON, WoodType.WARPED), 12, 5, 0.05f),
						//Sell display case
						new SimpleTrade(5, ModItems.COIN_IRON.get(), 10, ModBlocks.DISPLAY_CASE.get())
						),
				3,
				ImmutableList.of(
						//Sell first 4 card displays
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 15), ModBlocks.CARD_DISPLAY.getSome(WoodType.OAK, WoodType.BIRCH, WoodType.SPRUCE, WoodType.JUNGLE), 12, 10, 0.05f),
						//Sell second 4 card displays
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 15), ModBlocks.CARD_DISPLAY.getSome(WoodType.ACACIA, WoodType.DARK_OAK, WoodType.CRIMSON, WoodType.WARPED), 12, 10, 0.05f),
						//Sell armor display
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 20, ModBlocks.ARMOR_DISPLAY.get()),
						//Sell small trader server
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 15, ModBlocks.ITEM_NETWORK_TRADER_1.get()),
						//Sell Terminal
						new SimpleTrade(10, ModItems.COIN_IRON.get(), 10, ModBlocks.TERMINAL.get())
						),
				4,
				ImmutableList.of(
						//Sell Vending Machines
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 25), ModBlocks.VENDING_MACHINE.getAll(), 12, 15, 0.05f),
						//Sell medium trader server
						new SimpleTrade(15, ModItems.COIN_IRON.get(), 30, ModBlocks.ITEM_NETWORK_TRADER_2.get()),
						//Sell Freezer
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 30), ModBlocks.FREEZER.getAll(), 12, 20, 0.05f),
						//Sell Money Mending book
						new SimpleTrade(20, ModItems.COIN_DIAMOND.get(), 15, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.MONEY_MENDING.get(),1)))
						),
				5,
				ImmutableList.of(
						//Sell Large Vending Machines
						RandomTrade.build(new ItemStack(ModItems.COIN_IRON.get(), 25), ModBlocks.VENDING_MACHINE_LARGE.getAll(), 12, 30, 0.05f),
						//Sell large trader server
						new SimpleTrade(30, ModItems.COIN_GOLD.get(), 6, ModBlocks.ITEM_NETWORK_TRADER_3.get()),
						//Sell extra-large trader server
						new SimpleTrade(30, ModItems.COIN_GOLD.get(), 10, ModBlocks.ITEM_NETWORK_TRADER_4.get()),
						//Sell Money Mending book
						new SimpleTrade(30, ModItems.COIN_DIAMOND.get(), 10, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.MONEY_MENDING.get(),1)))
						)
				));

		CustomVillagerTradeData.registerDefaultFile(CASHIER_ID, ImmutableMap.of(
				1,
				ImmutableList.of(
						//Farmer
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.BREAD, 6), 16, 1, 0.05f),
						//Fisherman
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(Items.COD_BUCKET), 16, 1, 0.05f),
						//Shepherd
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(Items.SHEARS), 12, 1, 0.05f),
						//Fletcher
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 8), new ItemStack(Items.ARROW, 16), 12, 1, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(1),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Blocks.BOOKSHELF), 12, 1, 0.05f),
						//Cartographer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.MAP), 12, 1, 0.05f),
						//Cleric
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.REDSTONE), 12, 1, 0.05f),
						//Armorer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.IRON_LEGGINGS), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.IRON_BOOTS), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.IRON_HELMET), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.IRON_CHESTPLATE), 12, 1, 0.05f),
						//Weaponsmith
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.IRON_AXE), 12, 1, 0.05f),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 3, Items.IRON_SWORD, 12, 1, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Toolsmith
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_AXE), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_SHOVEL), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_PICKAXE), 12, 1, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get()), new ItemStack(Items.STONE_HOE), 12, 1, 0.05f),
						//Butcher
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.RABBIT_STEW), 12, 1, 0.05f),
						//Leatherworker (dyed armor only)
						//Mason
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.BRICK, 10), 16, 1, 0.05f)
						),
				2,
				ImmutableList.of(
						//Farmer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.PUMPKIN_PIE, 4), 12, 5, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.APPLE, 4), 16, 5, 0.05f),
						//Fisherman
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 3), new ItemStack(Items.COD, 15), 16, 10, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.CAMPFIRE), 12, 5, 0.05f),
						//Shepherd
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.WHITE_WOOL), 16, 5, 0.05f),
						//Fletcher
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 4), new ItemStack(Items.BOW), 12, 5, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(5),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.LANTERN), 12, 5, 0.05f),
						//Cartographer
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 3), ConfiguredStructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecoration.Type.MONUMENT, 12, 5),
						//Cleric
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Items.LAPIS_LAZULI), 12, 5, 0.05f),
						//Armorer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 4), new ItemStack(Blocks.BELL), 12, 5, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 9), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.CHAINMAIL_LEGGINGS), 12, 5, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(ModItems.COIN_COPPER.get(), 3), new ItemStack(Items.CHAINMAIL_BOOTS), 12, 5, 0.05f),
						//Weaponsmith (bell trade duplicate)
						//Toolsmith (bell trade duplicate)
						//Butcher
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.PORKCHOP, 6), 16, 5, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Items.COOKED_CHICKEN, 8), 16, 5, 0.05f),
						//Leatherworker (dyed armor only)
						//Mason
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.CHISELED_STONE_BRICKS, 4), 16, 5, 0.05f)
						),
				3,
				ImmutableList.of(
						//Farmer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.COOKIE, 18), 18, 10, 0.05f),
						//Fisherman
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 2, Items.FISHING_ROD, 3, 10, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Shepherd (none)
						//Fletcher
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(Items.CROSSBOW), 12, 10, 0.05f),
						//Librarian
						new EnchantedBookForCoinsTrade(10),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 3), new ItemStack(Blocks.GLASS,4), 12, 10, 0.05f),
						//Cartographer
						new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD.get(), 4), ConfiguredStructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecoration.Type.MANSION, 12, 10),
						//Cleric
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Blocks.GLOWSTONE), 12, 10, 0.05f),
						//Armorer
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 7), new ItemStack(ModItems.COIN_COPPER.get(), 4), new ItemStack(Items.CHAINMAIL_HELMET), 12, 10, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 10), new ItemStack(ModItems.COIN_COPPER.get(), 5), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 12, 10, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 6), new ItemStack(Items.SHIELD), 12, 10, 0.05f),
						//Weaponsmith (none)
						//Toolsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 5, Items.IRON_AXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 4, Items.IRON_SHOVEL, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 6, Items.IRON_PICKAXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new SimpleTrade(new ItemStack(ModItems.COIN_DIAMOND.get(), 2), new ItemStack(ModItems.COIN_IRON.get(), 1), new ItemStack(Items.DIAMOND_HOE), 3, 10, 0.05f),
						//Butcher (none)
						//Leatherworker (dyed armor only)
						//Mason
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_ANDESITE, 4), 16, 10, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_DIORITE, 4), 16, 10, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.POLISHED_GRANITE, 4), 16, 10, 0.05f)
						),
				4,
				ImmutableList.of(
						//Farmer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Blocks.CAKE), 12, 15, 0.05f),
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
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 5), new ItemStack(Items.CLOCK), 12, 15, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.COMPASS), 12, 15, 0.05f),
						//Cartographer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Items.ITEM_FRAME), 12, 15, 0.05f),
						//Cleric
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get()), new ItemStack(Items.ENDER_PEARL), 12, 15, 0.05f),
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
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.TERRACOTTA, 1), 16, 15, 0.05f)
						),
				5,
				ImmutableList.of(
						//Farmer
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 15), new ItemStack(Items.GOLDEN_CARROT), 12, 30, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 2), new ItemStack(Items.GLISTERING_MELON_SLICE), 12, 30, 0.05f),
						//Fisherman (none)
						//Shepherd
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.PAINTING), 12, 30, 0.05f),
						//Fletcher
						new EnchantedItemForCoinsTrade(ModItems.COIN_IRON.get(), 10, Items.CROSSBOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Librarian
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.NAME_TAG), 12, 30, 0.05f),
						//Cartographer
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.GLOBE_BANNER_PATTERN), 12, 30, 0.05f),
						//Cleric
						new SimpleTrade(new ItemStack(ModItems.COIN_EMERALD.get(), 1), new ItemStack(Blocks.NETHER_WART, 12), 12, 30, 0.05f),
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 5), new ItemStack(Items.EXPERIENCE_BOTTLE), 12, 30, 0.05f),
						//Armorer
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 8, Items.DIAMOND_CHESTPLATE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 6, Items.DIAMOND_HELMET, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Weaponsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 4, Items.DIAMOND_SWORD, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Toolsmith
						new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND.get(), 4, Items.DIAMOND_PICKAXE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
						//Butcher (none)
						//Leatherworker (dyed armor)
						new SimpleTrade(new ItemStack(ModItems.COIN_GOLD.get(), 1), new ItemStack(Items.SADDLE), 12, 30, 0.05f),
						//Mason
						new SimpleTrade(new ItemStack(ModItems.COIN_IRON.get(), 2), new ItemStack(Blocks.QUARTZ_BLOCK), 12, 30, 0.05f)
						)
				));
	}

	private static final float ENCHANTMENT_PRICE_MODIFIER = 0.25f;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnVillagerTradeSetup(VillagerTradesEvent event)
	{

		if(event.getType() == CustomProfessions.BANKER)
		{

			if(!Config.COMMON.addBankerVillager.get())
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
		else if(event.getType() == CustomProfessions.CASHIER)
		{

			if(!Config.COMMON.addBankerVillager.get())
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
			ResourceLocation type = event.getType().getRegistryName();

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

		Supplier<Item> replacementSupplier = () -> Config.getEmeraldReplacementItem(trader);

		for(int i = 1; i <= 5; ++i)
		{
			List<ItemListing> tradeList = trades.get(i);

			List<ItemListing> newList = new ArrayList<>();

			for(ItemListing trade : tradeList)
			{
				if(trade != null)
					newList.add(new ConvertedTrade(trade, Items.EMERALD, replacementSupplier));
			}

			trades.put(i, newList);

		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnWandererTradeSetup(WandererTradesEvent event)
	{

		if(Config.COMMON.changeWanderingTrades.get())
		{
			replaceExistingTrades(event.getGenericTrades());
			replaceExistingTrades(event.getRareTrades());
		}

		//Add my own custom trades
		if(Config.COMMON.addCustomWanderingTrades.get())
		{
			event.getGenericTrades().addAll(getGenericWandererTrades());
			event.getRareTrades().addAll(getRareWandererTrades());
		}

	}

	private static void replaceExistingTrades(List<ItemListing> tradeList) {

		for(int i = 0; i < tradeList.size(); ++i)
		{
			if(tradeList.get(i) != null)
				tradeList.set(i, new ConvertedTrade(tradeList.get(i), Items.EMERALD, Config.COMMON.defaultTraderCoin));
		}

	}

	public static class ConvertedTrade implements ItemListing
	{

		final ItemListing tradeSource;
		final ItemLike oldItem;
		final Supplier<Item> newItem;

		/**
		 * A modified Item Listing that takes an existing trade/listing and converts a given item into another item.
		 * Warning: Replaced items do not keep any NBT data, so this should not be used for items that can be enchanted.
		 * Used by LC to replace Emeralds with Emerald Coins.
		 * @param tradeSource The Item Listing to modify.
		 * @param oldItem The Item to replace.
		 * @param newItem The Item to replace the oldItem with.
		 */
		public ConvertedTrade(ItemListing tradeSource, ItemLike oldItem, Supplier<Item> newItem) {
			this.tradeSource = tradeSource;
			this.oldItem = oldItem;
			this.newItem = newItem;
		}

		@Override
		public MerchantOffer getOffer(@NotNull Entity trader, @NotNull Random random) {
			try {
				int attempts = 0;
				MerchantOffer offer;
				do
				{
					offer = this.tradeSource.getOffer(trader, random);
				} while(offer == null && attempts++ < 100);


				if(attempts > 1)
				{
					if(offer == null)
					{
						LightmansCurrency.LogError("Original Item Listing Class: " + this.tradeSource.getClass().getName());
						throw new NullPointerException("The original Item Listing of the converted trade returned a null trade offer " + attempts + " times!");
					}
					else
					{
						LightmansCurrency.LogWarning("Original Item Listing Class: " + this.tradeSource.getClass().getName());
						LightmansCurrency.LogWarning("Converted Trade took " + attempts + " attempts to receive a non-null trade offer from the original Item Listing!");
					}
				}

				ItemStack itemA = offer.getBaseCostA();
				ItemStack itemB = offer.getCostB();
				ItemStack itemC = offer.getResult();
				if(itemA.getItem() == this.oldItem)
					itemA = new ItemStack(this.newItem.get(), itemA.getCount());
				if(itemB.getItem() == this.oldItem)
					itemB = new ItemStack(this.newItem.get(), itemB.getCount());
				if(itemC.getItem() == this.oldItem)
					itemC = new ItemStack(this.newItem.get(), itemC.getCount());

				return new MerchantOffer(itemA, itemB, itemC, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
			} catch(Throwable t) {
				LightmansCurrency.LogDebug("Error converting trade:", t);
				return null;
			}
		}

	}

}
