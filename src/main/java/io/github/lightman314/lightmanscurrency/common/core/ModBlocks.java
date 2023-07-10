package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.BiFunction;
import java.util.function.Function;

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
			return new CoinBlockItem(block, properties);
		};
	}
	private static Function<Block,Item> getCoinJarGenerator() {
		return block ->  new CoinJarItem(block, new Item.Properties());
	}
	private static Function<Block,Item> getCustomRendererGenerator() {
		return block -> new CustomBlockModelItem(block, new Item.Properties());
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
	public static final RegistryObjectBundle<Block, Color> VENDING_MACHINE;
	public static final RegistryObjectBundle<Block, Color> VENDING_MACHINE_OLDCOLORS;

	//Large Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE_LARGE;
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE_LARGE_OLDCOLORS;

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

	//Bookshelf Traders
	public static final RegistryObjectBundle<Block,WoodType> BOOKSHELF_TRADER;

	public static final RegistryObject<Block> SLOT_MACHINE;

	//Ticket Machine
	public static final RegistryObject<Block> TICKET_STATION;

	//Coin Chest
	public static final RegistryObject<Block> COIN_CHEST;

	//Coin Jars
	public static final RegistryObject<Block> PIGGY_BANK;
	public static final RegistryObject<Block> COINJAR_BLUE;

	//Auciton Stands
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND;

	
	static {

		//Coin Piles
		COINPILE_COPPER = register("coinpile_copper", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_COPPER
				)
		);
		COINPILE_IRON = register("coinpile_iron", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_IRON
				)
		);
		COINPILE_GOLD = register("coinpile_gold", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.GOLD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_GOLD
				)
		);
		COINPILE_EMERALD = register("coinpile_emerald", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.EMERALD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_EMERALD
				)
		);
		COINPILE_DIAMOND = register("coinpile_diamond", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.DIAMOND)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_DIAMOND
				)
		);
		COINPILE_NETHERITE = register("coinpile_netherite", getCoinGenerator(true), () -> new CoinpileBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_NETHERITE
				)
		);

		//Coin Blocks
		COINBLOCK_COPPER = register("coinblock_copper", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_COPPER
				)
		);

		COINBLOCK_IRON = register("coinblock_iron", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_IRON
				)
		);
		COINBLOCK_GOLD = register("coinblock_gold", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.GOLD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_GOLD
				)
		);
		COINBLOCK_EMERALD = register("coinblock_emerald", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.EMERALD)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_EMERALD
				)
		);
		COINBLOCK_DIAMOND = register("coinblock_diamond", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.DIAMOND)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_DIAMOND
				)
		);
		COINBLOCK_NETHERITE = register("coinblock_netherite", getCoinGenerator(true), () -> new CoinBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					ModItems.COIN_NETHERITE
				)
		);
		
		//Machines
		MACHINE_ATM = register("atm", () -> new ATMBlock(
				Block.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		MACHINE_MINT = register("coinmint", () -> new CoinMintBlock(
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
		VENDING_MACHINE_OLDCOLORS = registerDeprecatedColored("vending_machine", VENDING_MACHINE, (c,b) -> new VendingMachineBlock.ReplaceMe(
			Block.Properties.of()
					.mapColor(c.mapColor)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				,b
			)
		);
		
		//Large Vending Machine
		VENDING_MACHINE_LARGE = registerColored("vending_machine_large", c -> new VendingMachineLargeBlock(
			Block.Properties.of()
				.mapColor(c.mapColor)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			), Color.WHITE
		);
		VENDING_MACHINE_LARGE_OLDCOLORS = registerDeprecatedColored("vending_machine_large", VENDING_MACHINE_LARGE, (c,b) -> new VendingMachineLargeBlock.ReplaceMe(
			Block.Properties.of()
					.mapColor(c.mapColor)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				,b
			)
		);
		
		//Shelves
		SHELF = registerWooden("shelf", w -> new ShelfBlock(
				Block.Properties.of()
					.mapColor(w.mapColor)
					.strength(2.0f, Float.POSITIVE_INFINITY)
				)
		);
		
		//Card Display
		CARD_DISPLAY = registerWooden("card_display", w -> new CardDisplayBlock(
				Block.Properties.of()
					.mapColor(w.mapColor)
					.strength(2.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD)
				)
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
		BOOKSHELF_TRADER = registerWooden("bookshelf_trader", w -> new BookTraderBlock(
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
		
		//Ticket Machine
		TICKET_STATION = register("ticket_machine", () -> new TicketMachineBlock(
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

		//Auction Stand
		AUCTION_STAND = registerWooden("auction_stand", w ->
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

	private static <T extends Block> RegistryObjectBundle<T,Color> registerDeprecatedColored(String name, RegistryObjectBundle<T,Color> replacementSource, BiFunction<Color,Supplier<Block>,T> block) {
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.deprecatedValues())
		{
			String thisName = name + "_" + color.getDeprecatedName();
			bundle.put(color, register(thisName, (b) -> new DeprecatedBlockItem(b), () -> block.apply(color, () -> replacementSource.get(color))));
		}
		return bundle.lock();
	}

	// Colored block registration code
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
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Function<WoodType,T> block)
	{
		return registerWooden(name, getDefaultGenerator(), block);
	}
	
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Function<Block,Item> itemGenerator, Function<WoodType,T> block)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			String thisName = name + "_" + woodType.name;
			//Register the block normally
			bundle.put(woodType, register(thisName, itemGenerator, () -> block.apply(woodType)));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, BiFunction<Color,WoodType,T> block)
	{
		return registerWoodenAndColored(name, getDefaultGenerator(), block);
	}

	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Function<Block,Item> itemGenerator, BiFunction<Color,WoodType,T> block)
	{
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType: WoodType.validValues())
		{
			for(Color color : Color.values())
			{
				String thisName = name + "_" + woodType.name + "_" + color.getResourceSafeName();
				//Register the block normally
				bundle.put(woodType, color, register(thisName, itemGenerator, () -> block.apply(color, woodType)));
			}
		}
		return bundle.lock();
	}

}
