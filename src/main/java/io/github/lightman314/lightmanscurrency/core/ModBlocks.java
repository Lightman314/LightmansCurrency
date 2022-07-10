package io.github.lightman314.lightmanscurrency.core;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.blocks.*;
import io.github.lightman314.lightmanscurrency.blocks.tradeinterface.ItemTraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.items.CashRegisterItem;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.items.CoinJarItem;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference;
import io.github.lightman314.lightmanscurrency.Reference.Color;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;

@ObjectHolder(LightmansCurrency.MODID)
public class ModBlocks {

	private static BiFunction<Block,CreativeModeTab,Item> getDefaultGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new BlockItem(block, properties);
		};
	}
	private static BiFunction<Block,CreativeModeTab,Item> getCoinGenerator(boolean fireResistant) {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			if(fireResistant)
				properties.fireResistant();
			return new CoinBlockItem(block, properties);
		};
	}
	private static BiFunction<Block,CreativeModeTab,Item> getCoinJarGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new CoinJarItem(block, properties);
		};
	}
	
	public static void init()
	{
		//Coin Piles
		register("coinpile_copper", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_COPPER
				)
		);
		register("coinpile_iron", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_IRON
				)
		);
		register("coinpile_gold", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_GOLD
				)
		);
		register("coinpile_emerald", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_EMERALD
				)
		);
		register("coinpile_diamond", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_DIAMOND
				)
		);
		register("coinpile_netherite", LightmansCurrency.COIN_GROUP, getCoinGenerator(true), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_NETHERITE
				)
		);
		
		//Coin Blocks
		register("coinblock_copper", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_COPPER
				)
		);
		register("coinblock_iron", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_IRON
				)
		);
		register("coinblock_gold", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_GOLD
				)
		);
		register("coinblock_emerald", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_EMERALD
				)
		);
		register("coinblock_diamond", LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_DIAMOND
				)
		);
		register("coinblock_netherite", LightmansCurrency.COIN_GROUP, getCoinGenerator(true), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				() -> ModItems.COIN_NETHERITE
				)
		);
		
		//Machines
		register("atm", LightmansCurrency.MACHINE_GROUP, () -> new ATMBlock(
		Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL)
			)
		);
		register("coinmint", LightmansCurrency.MACHINE_GROUP, () -> new CoinMintBlock(
			Block.Properties.of(Material.GLASS)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
			)
		);
		register("cash_register", LightmansCurrency.MACHINE_GROUP, (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new CashRegisterItem(block, properties);
		},
				() -> new CashRegisterBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,10d,15d)
				)
		);
		
		//Item Traders
		//Display Case
		register("display_case", LightmansCurrency.TRADING_GROUP, () -> new DisplayCaseBlock(
			Block.Properties.of(Material.GLASS)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
			)
		);
		
		//Vending Machine
		registerColored("vending_machine", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			),
			Color.WHITE
		);
		
		//Large Vending Machine
		registerColored("vending_machine_large", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineLargeBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			),
			Color.WHITE
		);
		
		//Shelves
		registerWooden("shelf", LightmansCurrency.TRADING_GROUP, () -> new ShelfBlock(
				Block.Properties.of(Material.WOOD)
					.strength(2.0f, Float.POSITIVE_INFINITY)
				)
		);
		
		//Card Display
		registerWooden("card_display", LightmansCurrency.TRADING_GROUP, () -> new CardDisplayBlock(
				Block.Properties.of(Material.WOOD)
					.strength(2.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD)
				)
		);
		
		//Freezer
		register("freezer", LightmansCurrency.TRADING_GROUP, () -> new FreezerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Armor Display
		register("armor_display", LightmansCurrency.TRADING_GROUP, () -> new ArmorDisplayBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Kiosk
		register("ticket_kiosk",LightmansCurrency.TRADING_GROUP, () -> new TicketKioskBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		
		
		//Network Traders
		register("item_trader_server_sml", LightmansCurrency.TRADING_GROUP, () -> new ItemTraderServerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					ItemTraderServerBlock.SMALL_SERVER_COUNT
				)
		);
		register("item_trader_server_med", LightmansCurrency.TRADING_GROUP, () -> new ItemTraderServerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					ItemTraderServerBlock.MEDIUM_SERVER_COUNT
				)
		);
		register("item_trader_server_lrg", LightmansCurrency.TRADING_GROUP, () -> new ItemTraderServerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					ItemTraderServerBlock.LARGE_SERVER_COUNT
				)
		);
		register("item_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, () -> new ItemTraderServerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					ItemTraderServerBlock.EXTRA_LARGE_SERVER_COUNT
				)
		);
		
		//Trader Interface
		register("item_trader_interface", LightmansCurrency.MACHINE_GROUP, () -> new ItemTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Terminal
		register("terminal", LightmansCurrency.MACHINE_GROUP, () -> new TerminalBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,15d,15d)
				)
		);
		
		//Paygate
		register("paygate", LightmansCurrency.MACHINE_GROUP, () -> new PaygateBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Machine
		register("ticket_machine", LightmansCurrency.MACHINE_GROUP, () -> new TicketMachineBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		
		//Coin Jars
		register("piggy_bank", CreativeModeTab.TAB_DECORATIONS, getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of(Material.STONE)
					.strength(0.1f, 2.0f)
					.sound(SoundType.STONE),
					Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		register("coinjar_blue", CreativeModeTab.TAB_DECORATIONS, getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of(Material.STONE)
				.strength(0.1f, 2.0f)
				.sound(SoundType.STONE),
				Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		
	}
	
	//Coin piles
	public static final Block COINPILE_COPPER = null;
	public static final Block COINPILE_IRON = null;
	public static final Block COINPILE_GOLD = null;
	public static final Block COINPILE_DIAMOND = null;
	public static final Block COINPILE_EMERALD = null;
	public static final Block COINPILE_NETHERITE = null;
	
	//Coin blocks
	public static final Block COINBLOCK_COPPER = null;
	public static final Block COINBLOCK_IRON = null;
	public static final Block COINBLOCK_GOLD = null;
	public static final Block COINBLOCK_EMERALD = null;
	public static final Block COINBLOCK_DIAMOND = null;
	public static final Block COINBLOCK_NETHERITE = null;
	
	//Machines
	//Misc Machines
	@ObjectHolder("atm")
	public static final Block MACHINE_ATM = null;
	@ObjectHolder("coinmint")
	public static final Block MACHINE_MINT = null;
	
	//Display Case
	public static final Block DISPLAY_CASE = null;
	
	//Vending Machines
	public static final Block VENDING_MACHINE = null;
	public static final Block VENDING_MACHINE_ORANGE = null;
	public static final Block VENDING_MACHINE_MAGENTA = null;
	public static final Block VENDING_MACHINE_LIGHTBLUE = null;
	public static final Block VENDING_MACHINE_YELLOW = null;
	public static final Block VENDING_MACHINE_LIME = null;
	public static final Block VENDING_MACHINE_PINK = null;
	public static final Block VENDING_MACHINE_GRAY = null;
	public static final Block VENDING_MACHINE_LIGHTGRAY = null;
	public static final Block VENDING_MACHINE_CYAN = null;
	public static final Block VENDING_MACHINE_PURPLE = null;
	public static final Block VENDING_MACHINE_BLUE = null;
	public static final Block VENDING_MACHINE_BROWN = null;
	public static final Block VENDING_MACHINE_GREEN = null;
	public static final Block VENDING_MACHINE_RED = null;
	public static final Block VENDING_MACHINE_BLACK = null;
	
	//Large Vending Machines
	public static final Block VENDING_MACHINE_LARGE = null;
	public static final Block VENDING_MACHINE_LARGE_ORANGE = null;
	public static final Block VENDING_MACHINE_LARGE_MAGENTA = null;
	public static final Block VENDING_MACHINE_LARGE_LIGHTBLUE = null;
	public static final Block VENDING_MACHINE_LARGE_YELLOW = null;
	public static final Block VENDING_MACHINE_LARGE_LIME = null;
	public static final Block VENDING_MACHINE_LARGE_PINK = null;
	public static final Block VENDING_MACHINE_LARGE_GRAY = null;
	public static final Block VENDING_MACHINE_LARGE_LIGHTGRAY = null;
	public static final Block VENDING_MACHINE_LARGE_CYAN = null;
	public static final Block VENDING_MACHINE_LARGE_PURPLE = null;
	public static final Block VENDING_MACHINE_LARGE_BLUE = null;
	public static final Block VENDING_MACHINE_LARGE_BROWN = null;
	public static final Block VENDING_MACHINE_LARGE_GREEN = null;
	public static final Block VENDING_MACHINE_LARGE_RED = null;
	public static final Block VENDING_MACHINE_LARGE_BLACK = null;
	
	//Wooden Shelves
	public static final Block SHELF_OAK = null;
	public static final Block SHELF_BIRCH = null;
	public static final Block SHELF_SPRUCE = null;
	public static final Block SHELF_JUNGLE = null;
	public static final Block SHELF_ACACIA = null;
	public static final Block SHELF_DARK_OAK = null;
	public static final Block SHELF_CRIMSON = null;
	public static final Block SHELF_WARPED = null;
	
	//Card Shelves
	public static final Block CARD_DISPLAY_OAK = null;
	public static final Block CARD_DISPLAY_BIRCH = null;
	public static final Block CARD_DISPLAY_SPRUCE = null;
	public static final Block CARD_DISPLAY_JUNGLE = null;
	public static final Block CARD_DISPLAY_ACACIA = null;
	public static final Block CARD_DISPLAY_DARK_OAK = null;
	public static final Block CARD_DISPLAY_CRIMSON = null;
	public static final Block CARD_DISPLAY_WARPED = null;
	
	//Armor Display
	public static final Block ARMOR_DISPLAY = null;
	
	//Freezer
	public static final Block FREEZER = null;
	
	
	//Network Traders
	@ObjectHolder("item_trader_server_sml")
	public static final Block ITEM_TRADER_SERVER_SMALL = null;
	@ObjectHolder("item_trader_server_med")
	public static final Block ITEM_TRADER_SERVER_MEDIUM = null;
	@ObjectHolder("item_trader_server_lrg")
	public static final Block ITEM_TRADER_SERVER_LARGE = null;
	@ObjectHolder("item_trader_server_xlrg")
	public static final Block ITEM_TRADER_SERVER_EXTRA_LARGE = null;
	
	//Trader Interface
	public static final Block ITEM_TRADER_INTERFACE = null;
	
	//Cash Register
	public static final Block CASH_REGISTER = null;
	
	//Terminal
	public static final Block TERMINAL = null;
	
	//Paygate
	public static final Block PAYGATE = null;
	
	//Ticket Kiosk
	public static final Block TICKET_KIOSK = null;
	
	//Ticket Machine
	public static final Block TICKET_MACHINE = null;
	
	//Coin Jars
	public static final Block PIGGY_BANK = null;
	public static final Block COINJAR_BLUE = null;
	
	
	/**
	* Block Registration Code
	*/
	private static void register(String name, CreativeModeTab itemGroup, Supplier<Block> sup)
	{
		register(name, itemGroup, getDefaultGenerator(), sup);
	}
	
	private static void register(String name, CreativeModeTab itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Supplier<Block> sup)
	{
		RegistryObject<Block> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get(), itemGroup));
	}
	
	/**
	 * Colored block registration code
	 */
	private static void registerColored(String name, CreativeModeTab itemGroup, Supplier<Block> block, @Nullable Color dontNameThisColor)
	{
		registerColored(name, itemGroup, getDefaultGenerator(), block, dontNameThisColor);
	}
	
	private static void registerColored(String name, CreativeModeTab itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Supplier<Block> block, @Nullable Color dontNameThisColor)
	{
		for(Reference.Color color : Reference.Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.toString().toLowerCase();
			//Register the block normally
			register(thisName, itemGroup, itemGenerator, block);
		}
	}
	
	/**
	 * Wooden block registration code
	 */
	private static void registerWooden(String name, CreativeModeTab itemGroup, Supplier<Block> block)
	{
		registerWooden(name, itemGroup, getDefaultGenerator(), block);
	}
	
	private static void registerWooden(String name, CreativeModeTab itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Supplier<Block> block)
	{
		for(WoodType woodType : WoodType.values())
		{
			String thisName = name + "_" + woodType.toString().toLowerCase();
			//Register the block normally
			register(thisName, itemGroup, itemGenerator, block);
		}
	}
	
	public static class Groups
	{
		public static ItemLike[] getVendingMachineList() {
			return new ItemLike[] { VENDING_MACHINE, VENDING_MACHINE_ORANGE, VENDING_MACHINE_MAGENTA, VENDING_MACHINE_LIGHTBLUE, VENDING_MACHINE_YELLOW, VENDING_MACHINE_LIME, VENDING_MACHINE_PINK, VENDING_MACHINE_GRAY, VENDING_MACHINE_LIGHTGRAY, VENDING_MACHINE_CYAN, VENDING_MACHINE_PURPLE, VENDING_MACHINE_BLUE, VENDING_MACHINE_BROWN, VENDING_MACHINE_GREEN, VENDING_MACHINE_RED, VENDING_MACHINE_BLACK };
		}
		public static ItemLike[] getLargeVendingMachineList() {
			return new ItemLike[] { VENDING_MACHINE_LARGE, VENDING_MACHINE_LARGE_ORANGE, VENDING_MACHINE_LARGE_MAGENTA, VENDING_MACHINE_LARGE_LIGHTBLUE, VENDING_MACHINE_LARGE_YELLOW, VENDING_MACHINE_LARGE_LIME, VENDING_MACHINE_LARGE_PINK, VENDING_MACHINE_LARGE_GRAY, VENDING_MACHINE_LARGE_LIGHTGRAY, VENDING_MACHINE_LARGE_CYAN, VENDING_MACHINE_LARGE_PURPLE, VENDING_MACHINE_LARGE_BLUE, VENDING_MACHINE_LARGE_BROWN, VENDING_MACHINE_LARGE_GREEN, VENDING_MACHINE_LARGE_RED, VENDING_MACHINE_LARGE_BLACK };
		}
	}
	
}
