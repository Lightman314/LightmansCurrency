package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.common.blocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.ItemTraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference.AuctionStandBlock;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.*;
import io.github.lightman314.lightmanscurrency.common.items.CashRegisterItem;
import io.github.lightman314.lightmanscurrency.common.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.common.items.CoinJarItem;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.RegistryObject;

public class ModBlocks {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }

	private static BiFunction<Block,ItemGroup, Item> getDefaultGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			else
				LightmansCurrency.LogWarning("Block item for block '" + block.getName().getString() + "' does not have a creative mode tab!");
			return new BlockItem(block, properties);
		};
	}
	private static BiFunction<Block,ItemGroup,Item> getCoinGenerator(boolean fireResistant) {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			else
				LightmansCurrency.LogWarning("Block item for block '" + block.getName().getString() + "' does not have a creative mode tab!");
			if(fireResistant)
				properties.fireResistant();
			return new CoinBlockItem(block, properties);
		};
	}
	private static BiFunction<Block,ItemGroup,Item> getCoinJarGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			else
				LightmansCurrency.LogWarning("Block item for block '" + block.getName().getString() + "' does not have a creative mode tab!");
			return new CoinJarItem(block, properties);
		};
	}

	static {
		//Coin Piles
		COINPILE_COPPER = register("coinpile_copper", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_COPPER::get
				)
		);
		COINPILE_IRON = register("coinpile_iron", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_IRON::get
				)
		);
		COINPILE_GOLD = register("coinpile_gold", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_GOLD::get
				)
		);
		COINPILE_EMERALD = register("coinpile_emerald", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_EMERALD::get
				)
		);
		COINPILE_DIAMOND = register("coinpile_diamond", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_DIAMOND::get
				)
		);
		COINPILE_NETHERITE = register("coinpile_netherite", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(true), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_NETHERITE::get
				)
		);

		//Coin Blocks
		COINBLOCK_COPPER = register("coinblock_copper", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_COPPER::get
				)
		);
		COINBLOCK_IRON = register("coinblock_iron", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_IRON::get
				)
		);
		COINBLOCK_GOLD = register("coinblock_gold", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_GOLD::get
				)
		);
		COINBLOCK_EMERALD = register("coinblock_emerald", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_EMERALD::get
				)
		);
		COINBLOCK_DIAMOND = register("coinblock_diamond", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_DIAMOND::get
				)
		);
		COINBLOCK_NETHERITE = register("coinblock_netherite", () -> LightmansCurrency.COIN_GROUP, getCoinGenerator(true), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						ModItems.COIN_NETHERITE::get
				)
		);

		//Machines
		MACHINE_ATM = register("atm", () -> LightmansCurrency.MACHINE_GROUP, () -> new ATMBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL)
				)
		);
		MACHINE_MINT = register("coinmint", () -> LightmansCurrency.MACHINE_GROUP, () -> new CoinMintBlock(
						Block.Properties.of(Material.METAL)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);
		CASH_REGISTER = register("cash_register", () -> LightmansCurrency.MACHINE_GROUP, (block, tab) -> {
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
		DISPLAY_CASE = register("display_case", () -> LightmansCurrency.TRADING_GROUP, () -> new DisplayCaseBlock(
						Block.Properties.of(Material.GLASS)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.GLASS)
				)
		);

		//Vending Machine
		VENDING_MACHINE = registerColoredOldNames("vending_machine", () -> LightmansCurrency.TRADING_GROUP, () -> new VendingMachineBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				),
				Color.WHITE
		);

		//Large Vending Machine
		VENDING_MACHINE_LARGE = registerColoredOldNames("vending_machine_large", () -> LightmansCurrency.TRADING_GROUP, () -> new VendingMachineLargeBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				),
				Color.WHITE
		);

		//Shelves
		SHELF = registerWooden("shelf", () -> LightmansCurrency.TRADING_GROUP, () -> new ShelfBlock(
						Block.Properties.of(Material.WOOD)
								.strength(2.0f, Float.POSITIVE_INFINITY)
				)
		);

		//Card Display
		CARD_DISPLAY = registerWooden("card_display", () -> LightmansCurrency.TRADING_GROUP, () -> new CardDisplayBlock(
						Block.Properties.of(Material.WOOD)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD)
				)
		);

		//Freezer
		FREEZER = registerFreezer("freezer", () -> LightmansCurrency.TRADING_GROUP, (c) -> new FreezerBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
								.sound(SoundType.METAL),
						FreezerBlock.GenerateDoorModel(c)
				)
		);

		//Armor Display
		ARMOR_DISPLAY = register("armor_display", () -> LightmansCurrency.TRADING_GROUP, () -> new ArmorDisplayBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Ticket Kiosk
		TICKET_KIOSK = register("ticket_kiosk", () -> LightmansCurrency.TRADING_GROUP, () -> new TicketKioskBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);



		//Network Traders
		ITEM_NETWORK_TRADER_1 = register("item_trader_server_sml", () -> LightmansCurrency.TRADING_GROUP, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_SMALL
				)
		);
		ITEM_NETWORK_TRADER_2 = register("item_trader_server_med", () -> LightmansCurrency.TRADING_GROUP, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_MEDIUM
				)
		);
		ITEM_NETWORK_TRADER_3 = register("item_trader_server_lrg", () -> LightmansCurrency.TRADING_GROUP, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_LARGE
				)
		);
		ITEM_NETWORK_TRADER_4 = register("item_trader_server_xlrg", () -> LightmansCurrency.TRADING_GROUP, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_XLARGE
				)
		);

		//Trader Interface
		ITEM_TRADER_INTERFACE = register("item_trader_interface", () -> LightmansCurrency.MACHINE_GROUP, () -> new ItemTraderInterfaceBlock(
						Block.Properties.of(Material.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Terminal
		TERMINAL = register("terminal", () -> LightmansCurrency.MACHINE_GROUP, () -> new TerminalBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						Block.box(1d,0d,1d,15d,15d,15d)
				)
		);

		//Gem Terminal
		GEM_TERMINAL = register("gem_terminal", () -> LightmansCurrency.MACHINE_GROUP, () -> new TerminalBlock(
						Block.Properties.of(Material.GLASS)
								.strength(3.0f, 6.0f)
								.sound(SoundType.GLASS),
						Block.box(2d, 0d, 2d, 14d, 12d, 14d)
				)
		);

		//Paygate
		PAYGATE = register("paygate", () -> LightmansCurrency.TRADING_GROUP, () -> new PaygateBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Ticket Machine
		TICKET_STATION = register("ticket_machine", () -> LightmansCurrency.MACHINE_GROUP, () -> new TicketMachineBlock(
						Block.Properties.of(Material.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL)
				)
		);

		//Coin Jars
		PIGGY_BANK = register("piggy_bank", () -> LightmansCurrency.MACHINE_GROUP, getCoinJarGenerator(), () -> new CoinJarBlock(
						Block.Properties.of(Material.STONE)
								.strength(0.1f, 2.0f)
								.sound(SoundType.STONE),
						Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		COINJAR_BLUE = register("coinjar_blue", () -> LightmansCurrency.MACHINE_GROUP, getCoinJarGenerator(), () -> new CoinJarBlock(
						Block.Properties.of(Material.STONE)
								.strength(0.1f, 2.0f)
								.sound(SoundType.STONE),
						Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);

		//Auction Stand
		AUCTION_STAND = registerWooden("auction_stand", () -> LightmansCurrency.MACHINE_GROUP, () ->
				new AuctionStandBlock(Block.Properties.of(Material.WOOD).strength(2.0f))
		);

	}

	//Coin piles
	public static final RegistryObject<Block> COINPILE_COPPER;
	public static final RegistryObject<Block> COINPILE_IRON;
	public static final RegistryObject<Block> COINPILE_GOLD;
	public static final RegistryObject<Block> COINPILE_DIAMOND;
	public static final RegistryObject<Block> COINPILE_EMERALD;
	public static final RegistryObject<Block> COINPILE_NETHERITE;

	//Coin blocks
	public static final RegistryObject<Block> COINBLOCK_COPPER;
	public static final RegistryObject<Block> COINBLOCK_IRON;
	public static final RegistryObject<Block> COINBLOCK_GOLD;
	public static final RegistryObject<Block> COINBLOCK_EMERALD;
	public static final RegistryObject<Block> COINBLOCK_DIAMOND;
	public static final RegistryObject<Block> COINBLOCK_NETHERITE;

	//Machines
	//Misc Machines
	public static final RegistryObject<Block> MACHINE_ATM;
	public static final RegistryObject<Block> MACHINE_MINT;

	//Display Case
	public static final RegistryObject<Block> DISPLAY_CASE;

	//Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE;

	//Large Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE_LARGE;

	//Wooden Shelves
	public static final RegistryObjectBundle<Block, WoodType> SHELF;

	//Card Shelves
	public static final RegistryObjectBundle<Block,WoodType> CARD_DISPLAY;

	//Armor Display
	public static final RegistryObject<Block> ARMOR_DISPLAY;

	//Freezer
	public static final RegistryObjectBundle<FreezerBlock,Color> FREEZER;


	//Network Traders
	public static final RegistryObject<Block> ITEM_NETWORK_TRADER_1;
	public static final RegistryObject<Block> ITEM_NETWORK_TRADER_2;
	public static final RegistryObject<Block> ITEM_NETWORK_TRADER_3;
	public static final RegistryObject<Block> ITEM_NETWORK_TRADER_4;

	//Trader Interface
	public static final RegistryObject<Block> ITEM_TRADER_INTERFACE;

	//Cash Register
	public static final RegistryObject<Block> CASH_REGISTER;

	//Terminal
	public static final RegistryObject<Block> TERMINAL;
	public static final RegistryObject<Block> GEM_TERMINAL;

	//Paygate
	public static final RegistryObject<Block> PAYGATE;

	//Ticket Kiosk
	public static final RegistryObject<Block> TICKET_KIOSK;

	//Ticket Machine
	public static final RegistryObject<Block> TICKET_STATION;

	//Coin Jars
	public static final RegistryObject<Block> PIGGY_BANK;
	public static final RegistryObject<Block> COINJAR_BLUE;

	//Auciton Stands
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND;


	/**
	 * Block Registration Code
	 */
	private static <T extends Block> RegistryObject<T>  register(String name, NonNullSupplier<ItemGroup> itemGroup, Supplier<T> sup)
	{
		return register(name, itemGroup, getDefaultGenerator(), sup);
	}

	private static<T extends Block> RegistryObject<T> register(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<T> sup)
	{
		RegistryObject<T> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get(), itemGroup != null ? itemGroup.get() : null));
		return block;
	}

	// Colored block registration code
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColoredOldNames(String name, NonNullSupplier<ItemGroup> itemGroup, Supplier<T> block, @Nullable Color dontNameThisColor)
	{
		return registerColoredOldNames(name, itemGroup, getDefaultGenerator(), block, dontNameThisColor);
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColoredOldNames(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<T> block, @Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getResourceSafeOldName();
			//Register the block normally
			bundle.put(color, register(thisName, itemGroup, itemGenerator, block));
		}
		return bundle.lock();
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, NonNullSupplier<ItemGroup> itemGroup, Supplier<T> block, @Nullable Color dontNameThisColor)
	{
		return registerColored(name, itemGroup, getDefaultGenerator(), block, dontNameThisColor);
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<T> block, @Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getResourceSafeName();
			//Register the block normally
			bundle.put(color, register(thisName, itemGroup, itemGenerator, block));
		}
		return bundle.lock();
	}

	/**
	 * Wooden block registration code
	 */
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, NonNullSupplier<ItemGroup> itemGroup, Supplier<T> block)
	{
		return registerWooden(name, itemGroup, getDefaultGenerator(), block);
	}

	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<T> block)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			String thisName = name + "_" + woodType.name;
			//Register the block normally
			bundle.put(woodType, register(thisName, itemGroup, itemGenerator, block));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, NonNullSupplier<ItemGroup> itemGroup, Supplier<T> block)
	{
		return registerWoodenAndColored(name, itemGroup, getDefaultGenerator(), block);
	}

	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<T> block)
	{
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType: WoodType.validValues())
		{
			for(Color color : Color.values())
			{
				String thisName = name + "_" + woodType.name + "_" + color.getResourceSafeName();
				//Register the block normally
				bundle.put(woodType, color, register(thisName, itemGroup, itemGenerator, block));
			}
		}
		return bundle.lock();
	}

	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, NonNullSupplier<ItemGroup> itemGroup, Function<Color,FreezerBlock> block) {
		return registerFreezer(name, itemGroup, getDefaultGenerator(), block);
	}
	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, NonNullSupplier<ItemGroup> itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Function<Color,FreezerBlock> block) {
		RegistryObjectBundle<FreezerBlock,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = color == Color.BLACK ? name : name + "_" + color.getResourceSafeName();
			//Register the block normally
			bundle.put(color, register(thisName, itemGroup, itemGenerator, () -> block.apply(color)));
		}
		return bundle.lock();
	}



}