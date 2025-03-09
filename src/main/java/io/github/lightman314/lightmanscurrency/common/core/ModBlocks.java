package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.common.blocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference.*;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	private static Function<Block,Item> getDefaultGenerator() { return block -> new BlockItem(block, new Item.Properties()); }
	private static Function<Block,Item> getCoinGenerator(final boolean fireResistant) {
		return block -> {
			Item.Properties properties = new Item.Properties();
			if(fireResistant)
				properties.fireResistant();
			return new BlockItem(block, properties);
		};
	}
	private static Function<Block,Item> getCoinJarGenerator() { return block ->  new CoinJarItem(block, new Item.Properties()); }
	private static Function<Block,Item> getColoredCoinJarGenerator() { return block ->  new CoinJarItem.Colored(block, new Item.Properties()); }
	private static Function<Block,Item> getCustomRendererGenerator() { return block -> new CustomBlockModelItem(block, new Item.Properties()); }

	private static final SoundType CHOCOLATE_SOUND = SoundType.MUD_BRICKS;

	//Coin piles
	public static final RegistryObject<Block> COINPILE_COPPER;
	public static final RegistryObject<Block> COINPILE_IRON;
	public static final RegistryObject<Block> COINPILE_GOLD;
	public static final RegistryObject<Block> COINPILE_DIAMOND;
	public static final RegistryObject<Block> COINPILE_EMERALD;
	public static final RegistryObject<Block> COINPILE_NETHERITE;

	public static final RegistryObject<Block> COINPILE_CHOCOLATE_COPPER;
	public static final RegistryObject<Block> COINPILE_CHOCOLATE_IRON;
	public static final RegistryObject<Block> COINPILE_CHOCOLATE_GOLD;
	public static final RegistryObject<Block> COINPILE_CHOCOLATE_DIAMOND;
	public static final RegistryObject<Block> COINPILE_CHOCOLATE_EMERALD;
	public static final RegistryObject<Block> COINPILE_CHOCOLATE_NETHERITE;

	//Coin blocks
	public static final RegistryObject<Block> COINBLOCK_COPPER;
	public static final RegistryObject<Block> COINBLOCK_IRON;
	public static final RegistryObject<Block> COINBLOCK_GOLD;
	public static final RegistryObject<Block> COINBLOCK_EMERALD;
	public static final RegistryObject<Block> COINBLOCK_DIAMOND;
	public static final RegistryObject<Block> COINBLOCK_NETHERITE;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_COPPER;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_IRON;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_GOLD;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_EMERALD;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_DIAMOND;
	public static final RegistryObject<Block> COINBLOCK_CHOCOLATE_NETHERITE;

	//Machines
	//Misc Machines
	public static final RegistryObject<Block> ATM;
	public static final RegistryObject<Block> COIN_MINT;

	//Display Case
	public static final RegistryObject<Block> DISPLAY_CASE;

	//Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE;

	//Large Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE_LARGE;

	//Wooden Shelves
	public static final RegistryObjectBundle<Block,WoodType> SHELF;
	public static final RegistryObjectBundle<Block,WoodType> SHELF_2x2;

	//Card Shelves
	public static final RegistryObjectBiBundle<Block,WoodType,Color> CARD_DISPLAY;

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

	//Bookshelf Traders
	public static final RegistryObjectBundle<Block,WoodType> BOOKSHELF_TRADER;

	//Slot Machine
	public static final RegistryObject<Block> SLOT_MACHINE;
	public static final RegistryObjectBundle<Block,Color> GACHA_MACHINE;

	//Command Trader
	public static final RegistryObject<Block> COMMAND_TRADER;

	//Ticket Machine
	public static final RegistryObject<Block> TICKET_STATION;

	//Coin Chest
	public static final RegistryObject<Block> COIN_CHEST;

	//Tax Block
	public static final RegistryObject<Block> TAX_COLLECTOR;

	//Coin Jars
	public static final RegistryObject<Block> PIGGY_BANK;
	public static final RegistryObject<Block> COINJAR_BLUE;
	public static final RegistryObject<Block> SUS_JAR;

	//Auciton Stands
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND;

	
	static {

		//Coin Piles
		COINPILE_COPPER = register("coinpile_copper", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_IRON = register("coinpile_iron", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_GOLD = register("coinpile_gold", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.GOLD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_EMERALD = register("coinpile_emerald", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.EMERALD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_DIAMOND = register("coinpile_diamond", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.DIAMOND)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_NETHERITE = register("coinpile_netherite", getCoinGenerator(true), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINPILE_CHOCOLATE_COPPER = register("coinpile_chocolate_copper", getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.COLOR_ORANGE)
								.strength(3.0f, 6.0f)
								.sound(SoundType.MUD)
				)
		);
		COINPILE_CHOCOLATE_IRON = register("coinpile_chocolate_iron", getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINPILE_CHOCOLATE_GOLD = register("coinpile_chocolate_gold", getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.GOLD)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINPILE_CHOCOLATE_EMERALD = register("coinpile_chocolate_emerald", getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.EMERALD)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINPILE_CHOCOLATE_DIAMOND = register("coinpile_chocolate_diamond", getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.DIAMOND)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINPILE_CHOCOLATE_NETHERITE = register("coinpile_chocolate_netherite", getCoinGenerator(true), () -> new CoinpileBlock(
						Block.Properties.of()
								.mapColor(MapColor.COLOR_BLACK)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);

		//Coin Blocks
		COINBLOCK_COPPER = register("coinblock_copper", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);

		COINBLOCK_IRON = register("coinblock_iron", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINBLOCK_GOLD = register("coinblock_gold", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.GOLD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINBLOCK_EMERALD = register("coinblock_emerald", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.EMERALD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINBLOCK_DIAMOND = register("coinblock_diamond", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.DIAMOND)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COINBLOCK_NETHERITE = register("coinblock_netherite", getCoinGenerator(true), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);

		COINBLOCK_CHOCOLATE_COPPER = register("coinblock_chocolate_copper", getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.COLOR_ORANGE)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);

		COINBLOCK_CHOCOLATE_IRON = register("coinblock_chocolate_iron", getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINBLOCK_CHOCOLATE_GOLD = register("coinblock_chocolate_gold", getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.GOLD)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINBLOCK_CHOCOLATE_EMERALD = register("coinblock_chocolate_emerald", getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.EMERALD)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINBLOCK_CHOCOLATE_DIAMOND = register("coinblock_chocolate_diamond", getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.DIAMOND)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		COINBLOCK_CHOCOLATE_NETHERITE = register("coinblock_chocolate_netherite", getCoinGenerator(true), () -> new CoinBlock(
						Block.Properties.of()
								.mapColor(MapColor.COLOR_BLACK)
								.strength(3.0f, 6.0f)
								.sound(CHOCOLATE_SOUND)
				)
		);
		
		//Machines
		ATM = register("atm", () -> new ATMBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		COIN_MINT = register("coinmint", () -> new CoinMintBlock(
			Block.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_BLUE)
					.strength(2.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		CASH_REGISTER = register("cash_register", block -> new CashRegisterItem(block, new Item.Properties()), () -> new CashRegisterBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,10d,15d)
				)
		);

		//Item Traders
		//Display Case
		DISPLAY_CASE = register("display_case", () -> new DisplayCaseBlock(
			Block.Properties.of()
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
			)
		);
		
		//Vending Machine
		VENDING_MACHINE = registerColored("vending_machine", c -> new VendingMachineBlock(
			Block.Properties.of()
				.mapColor(c.mapColor)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			), Color.WHITE
		);
		
		//Large Vending Machine
		VENDING_MACHINE_LARGE = registerColored("vending_machine_large", c -> new VendingMachineLargeBlock(
			Block.Properties.of()
				.mapColor(c.mapColor)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			), Color.WHITE
		);
		
		//Shelves
		SHELF = registerWooden("shelf", WoodType.Attributes.needsSlab, w -> new ShelfBlock(
				Block.Properties.of()
					.mapColor(w.mapColor)
					.strength(2.0f, Float.POSITIVE_INFINITY)
				)
		);

		SHELF_2x2 = registerWooden("shelf_2x2", WoodType.Attributes.needsSlab, w -> new ShelfBlock(
				BlockBehaviour.Properties.of()
					.mapColor(w.mapColor)
					.strength(2.0f, Float.POSITIVE_INFINITY),
				4
				)
		);
		
		//Card Display
		CARD_DISPLAY = registerWoodenAndColored("card_display", WoodType.Attributes.needsLog, (w,c) -> new CardDisplayBlock(
				Block.Properties.of()
					.mapColor(w.mapColor)
					.strength(2.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD),
					w.generateID("block.lightmanscurrency.card_display"),
					c
				),
				Color.RED
		);

		//Freezer
		FREEZER = registerColored("freezer", c -> new FreezerBlock(
				Block.Properties.of()
					.mapColor(c.mapColor)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					FreezerBlock.GenerateDoorModel(c)
				),
				Color.BLACK
		);
		
		//Armor Display
		ARMOR_DISPLAY = register("armor_display", () -> new ArmorDisplayBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Kiosk
		TICKET_KIOSK = register("ticket_kiosk", () -> new TicketKioskBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);

		//Bookshelf Traders
		BOOKSHELF_TRADER = registerWooden("bookshelf_trader", WoodType.Attributes.needsPlanksAndSlab, w -> new BookTraderBlock(
				Block.Properties.of()
					.mapColor(w.mapColor)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD)
				)
		);

		//Slot Machine Trader
		SLOT_MACHINE = register("slot_machine", () -> new SlotMachineBlock(
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.COLOR_YELLOW)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				)
		);

		//Gacha Machine
		GACHA_MACHINE = registerColored("gacha_machine",c -> new GachaMachineBlock(
					BlockBehaviour.Properties.of()
							.mapColor(c.mapColor)
							.strength(3.0f,Float.POSITIVE_INFINITY)
							.sound(SoundType.METAL)
							,c
				)
		);
		
		
		//Network Traders
		ITEM_NETWORK_TRADER_1 = register("item_trader_server_sml", () -> new NetworkItemTraderBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_SMALL
				)
		);
		ITEM_NETWORK_TRADER_2 = register("item_trader_server_med", () -> new NetworkItemTraderBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_MEDIUM
				)
		);
		ITEM_NETWORK_TRADER_3 = register("item_trader_server_lrg", () -> new NetworkItemTraderBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_LARGE
				)
		);
		ITEM_NETWORK_TRADER_4 = register("item_trader_server_xlrg", () -> new NetworkItemTraderBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_XLARGE
				)
		);
		
		//Trader Interface
		ITEM_TRADER_INTERFACE = register("item_trader_interface", () -> new ItemTraderInterfaceBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Terminal
		TERMINAL = register("terminal", () -> new TerminalBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,15d,15d)
				)
		);

		//Gem Terminal
		GEM_TERMINAL = register("gem_terminal", () -> new TerminalBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.strength(3.0f, 6.0f)
					.sound(SoundType.AMETHYST_CLUSTER),
					Block.box(2d, 0d, 2d, 14d, 12d, 14d)
				)
		);
		
		//Paygate
		PAYGATE = register("paygate", () -> new PaygateBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);

		//Command Trader
		COMMAND_TRADER = register("command_trader", () -> new CommandTraderBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f,Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Machine
		TICKET_STATION = register("ticket_machine", () -> new TicketStationBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);

		//Coin Chest
		COIN_CHEST = register("coin_chest", getCustomRendererGenerator(), () -> new CoinChestBlock(
				Block.Properties.of()
					.mapColor(MapColor.WOOD)
					.strength(2.5f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD)
				)
		);

		//Tax Block
		TAX_COLLECTOR = register("tax_block", () -> new TaxCollectorBlock(
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.METAL)
						.strength(3f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				)
		);

		//Coin Jars
		PIGGY_BANK = register("piggy_bank", getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_PINK)
					.strength(0.1f, 2.0f)
					.sound(SoundType.STONE),
					Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		COINJAR_BLUE = register("coinjar_blue", getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_BLUE)
					.strength(0.1f, 2.0f)
					.sound(SoundType.STONE),
					Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		SUS_JAR = register("sus_jar", getColoredCoinJarGenerator(), () -> new CoinJarBlock(
				BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.strength(0.1f, 2.0f)
					.sound(SoundType.STONE),
					Block.box(4, 0d, 4d, 12d, 8d, 12d)
				)
		);

		//Auction Stand
		AUCTION_STAND = registerWooden("auction_stand", WoodType.Attributes.needsLog, w ->
			new AuctionStandBlock(BlockBehaviour.Properties.of().mapColor(w.mapColor).strength(2.0f))
		);
		
	}
	
	
	/**
	* Block Registration Code
	*/
	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> sup)
	{
		return register(name, getDefaultGenerator(), sup);
	}
	
	private static <T extends Block> RegistryObject<T> register(String name, Function<Block,Item> itemGenerator, Supplier<T> sup)
	{
		RegistryObject<T> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get()));
		return block;
	}

	// Colored block registration code
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<Color,T> block) { return registerColored(name,block,null); }
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<Color,T> block, @Nullable Color dontNameThisColor) {
		return registerColored(name, getDefaultGenerator(), block, dontNameThisColor);
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<Block,Item> itemGenerator, Function<Color,T> block, @Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getResourceSafeName();
			//Register the block normally
			bundle.put(color, register(thisName, itemGenerator, () -> block.apply(color)));
		}
		return bundle.lock();
	}
	
	/**
	 * Wooden block registration code
	 */
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Predicate<WoodType.Attributes> check, Function<WoodType,T> block)
	{
		return registerWooden(name,check, getDefaultGenerator(), block);
	}
	
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Predicate<WoodType.Attributes> check, Function<Block,Item> itemGenerator, Function<WoodType,T> block)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			if(!check.test(woodType.attributes))
				continue;
			String thisName = woodType.generateID(name);
			//Register the block normally
			bundle.put(woodType, register(thisName, itemGenerator, () -> block.apply(woodType)));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, BiFunction<WoodType,Color,T> block) { return registerWoodenAndColored(name, check, block, null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, BiFunction<WoodType,Color,T> block, @Nullable Color ignoreColor) { return registerWoodenAndColored(name, check, getDefaultGenerator(), block, ignoreColor); }

	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, Function<Block,Item> itemGenerator, BiFunction<WoodType,Color,T> block) { return registerWoodenAndColored(name, check, itemGenerator, block, null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, Function<Block,Item> itemGenerator, BiFunction<WoodType,Color,T> block, @Nullable Color ignoreColor)
	{
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType: WoodType.validValues())
		{
			if(check.test(woodType.attributes))
			{
				for(Color color : Color.values())
				{
					String thisName;
					if(color == ignoreColor)
						thisName = woodType.generateID(name);
					else
						thisName = woodType.generateID(name) + "_" + color.getResourceSafeName();
					//Register the block normally
					bundle.put(woodType, color, register(thisName, itemGenerator, () -> block.apply(woodType, color)));
				}
			}
		}
		return bundle.lock();
	}

}
