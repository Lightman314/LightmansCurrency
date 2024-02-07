package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import io.github.lightman314.lightmanscurrency.common.blocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.ItemTraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference.AuctionStandBlock;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.*;
import io.github.lightman314.lightmanscurrency.common.items.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.CustomBlockModelItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }

	private static Item.Properties PropertiesForTab(CreativeModeTab tab) {
		Item.Properties properties = new Item.Properties();
		if(tab != null)
			properties.tab(tab);
		return properties;
	}

	private static Item.Properties FireResistant(Item.Properties properties, boolean fireResistant) { if(fireResistant) return properties.fireResistant(); return properties; }

	private static BiFunction<Block,CreativeModeTab,Item> getDefaultGenerator() { return (block, tab) ->  new BlockItem(block, PropertiesForTab(tab)); }
	private static BiFunction<Block,CreativeModeTab,Item> getCoinGenerator(boolean fireResistant) { return (block, tab) ->  new BlockItem(block, FireResistant(PropertiesForTab(tab), fireResistant)); }
	private static BiFunction<Block,CreativeModeTab,Item> getCoinJarGenerator() { return (block, tab) ->  new CoinJarItem(block, PropertiesForTab(tab)); }
	private static BiFunction<Block,CreativeModeTab,Item> getColoredCoinJarGenerator() { return (block, tab) ->  new CoinJarItem.Colored(block, PropertiesForTab(tab)); }
	private static BiFunction<Block,CreativeModeTab,Item> getCustomRendererGenerator() { return (block,tab) ->  new CustomBlockModelItem(block, PropertiesForTab(tab)); }


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
	public static final RegistryObject<Block> ATM;
	public static final RegistryObject<Block> COIN_MINT;

	//Display Case
	public static final RegistryObject<Block> DISPLAY_CASE;

	//Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE;

	//Large Vending Machines
	public static final RegistryObjectBundle<Block,Color> VENDING_MACHINE_LARGE;

	//Wooden Shelves
	public static final RegistryObjectBundle<Block, WoodType> SHELF;
	public static final RegistryObjectBundle<Block, WoodType> SHELF_2x2;

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

	public static final RegistryObject<Block> SLOT_MACHINE;

	//Ticket Machine
	public static final RegistryObject<Block> TICKET_STATION;

	//Coin Chest
	public static final RegistryObject<Block> COIN_CHEST;

	//Tax Block
	public static final RegistryObject<TaxCollectorBlock> TAX_COLLECTOR;

	//Coin Jars
	public static final RegistryObject<Block> PIGGY_BANK;
	public static final RegistryObject<Block> COINJAR_BLUE;
	public static final RegistryObject<Block> SUS_JAR;

	//Auciton Stands
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND;

	static {

		LightmansCurrency.LogDebug("Loading the ModBlocks class!");

		//Coin Piles
		COINPILE_COPPER = register("coinpile_copper", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_ORANGE)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_COPPER
				)
		);
		COINPILE_IRON = register("coinpile_iron", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_IRON
				)
		);
		COINPILE_GOLD = register("coinpile_gold", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.GOLD)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_GOLD
				)
		);
		COINPILE_EMERALD = register("coinpile_emerald", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.EMERALD)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_EMERALD
				)
		);
		COINPILE_DIAMOND = register("coinpile_diamond", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.DIAMOND)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_DIAMOND
				)
		);
		COINPILE_NETHERITE = register("coinpile_netherite", ModCreativeGroups::getCoinGroup, getCoinGenerator(true), () -> new CoinpileBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_BLACK)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_NETHERITE
				)
		);

		//Coin Blocks
		COINBLOCK_COPPER = register("coinblock_copper", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_ORANGE)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_COPPER
				)
		);
		COINBLOCK_IRON = register("coinblock_iron", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_IRON
				)
		);
		COINBLOCK_GOLD = register("coinblock_gold", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.GOLD)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_GOLD
				)
		);
		COINBLOCK_EMERALD = register("coinblock_emerald", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.EMERALD)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_EMERALD
				)
		);
		COINBLOCK_DIAMOND = register("coinblock_diamond", ModCreativeGroups::getCoinGroup, getCoinGenerator(false), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.DIAMOND)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_DIAMOND
				)
		);
		COINBLOCK_NETHERITE = register("coinblock_netherite", ModCreativeGroups::getCoinGroup, getCoinGenerator(true), () -> new CoinBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_BLACK)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
				ModItems.COIN_NETHERITE
				)
		);

		//Machines
		ATM = register("atm", ModCreativeGroups::getMachineGroup, () -> new ATMBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_GRAY)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL)
				)
		);
		COIN_MINT = register("coinmint", ModCreativeGroups::getMachineGroup, () -> new CoinMintBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_LIGHT_BLUE)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);
		CASH_REGISTER = register("cash_register", ModCreativeGroups::getMachineGroup, (block, tab) -> {
					Item.Properties properties = new Item.Properties();
					if(tab != null)
						properties.tab(tab);
					return new CashRegisterItem(block, properties);
				},
				() -> new CashRegisterBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_GRAY)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						Block.box(1d,0d,1d,15d,10d,15d)
				)
		);

		//Item Traders
		//Display Case
		DISPLAY_CASE = register("display_case", ModCreativeGroups::getTradingGroup, () -> new DisplayCaseBlock(
						Block.Properties.of(Material.GLASS)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.GLASS)
				)
		);

		//Vending Machine
		VENDING_MACHINE = registerColored("vending_machine", ModCreativeGroups::getTradingGroup, c -> new VendingMachineBlock(
						Block.Properties.of(Material.METAL)
								.color(c.mapColor)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				),
				Color.WHITE
		);

		//Large Vending Machine
		VENDING_MACHINE_LARGE = registerColored("vending_machine_large", ModCreativeGroups::getTradingGroup, c -> new VendingMachineLargeBlock(
						Block.Properties.of(Material.METAL)
								.color(c.mapColor)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				),
				Color.WHITE
		);

		//Shelves
		SHELF = registerWooden("shelf", WoodType.Attributes.needsSlab, ModCreativeGroups.getExtraOr(ModCreativeGroups::getTradingGroup), w -> new ShelfBlock(
						Block.Properties.of(Material.WOOD)
								.color(w.mapColor)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD)
				)
		);
		SHELF_2x2 = registerWooden("shelf_2x2", WoodType.Attributes.needsSlab, ModCreativeGroups.getExtraOr(ModCreativeGroups::getTradingGroup), w -> new ShelfBlock(
						Block.Properties.of(Material.WOOD)
								.color(w.mapColor)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD),
						4
				)
		);

		//Card Display
		CARD_DISPLAY = registerWoodenAndColored("card_display", WoodType.Attributes.needsLog, ModCreativeGroups.getExtraOr(ModCreativeGroups::getTradingGroup), (w,c) -> new CardDisplayBlock(
						Block.Properties.of(Material.WOOD)
								.color(w.mapColor)
								.strength(2.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD),
						w.generateID("block.lightmanscurrency.card_display"),
						c
				),
				Color.RED
		);

		//Freezer
		FREEZER = registerColored("freezer", ModCreativeGroups::getTradingGroup, c -> new FreezerBlock(
						Block.Properties.of(Material.METAL)
								.color(c.mapColor)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						FreezerBlock.GenerateDoorModel(c)
				),
				Color.BLACK
		);

		//Armor Display
		ARMOR_DISPLAY = register("armor_display", ModCreativeGroups::getTradingGroup, () -> new ArmorDisplayBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Ticket Kiosk
		TICKET_KIOSK = register("ticket_kiosk", ModCreativeGroups::getTradingGroup, () -> new TicketKioskBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Bookshelf Traders
		BOOKSHELF_TRADER = registerWooden("bookshelf_trader", WoodType.Attributes.needsPlanksAndSlab, ModCreativeGroups.getExtraOr(ModCreativeGroups::getTradingGroup), w -> new BookTraderBlock(
						Block.Properties.of(Material.WOOD)
								.color(w.mapColor)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD)
				)
		);

		//Slot Machine Trader
		SLOT_MACHINE = register("slot_machine", ModCreativeGroups::getTradingGroup, () -> new SlotMachineBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.COLOR_YELLOW)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Network Traders
		ITEM_NETWORK_TRADER_1 = register("item_trader_server_sml", ModCreativeGroups::getTradingGroup, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_SMALL
				)
		);
		ITEM_NETWORK_TRADER_2 = register("item_trader_server_med", ModCreativeGroups::getTradingGroup, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_MEDIUM
				)
		);
		ITEM_NETWORK_TRADER_3 = register("item_trader_server_lrg", ModCreativeGroups::getTradingGroup, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_LARGE
				)
		);
		ITEM_NETWORK_TRADER_4 = register("item_trader_server_xlrg", ModCreativeGroups::getTradingGroup, () -> new NetworkItemTraderBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL),
						NetworkItemTraderBlock.TRADER_COUNT_XLARGE
				)
		);

		//Trader Interface
		ITEM_TRADER_INTERFACE = register("item_trader_interface", ModCreativeGroups::getMachineGroup, () -> new ItemTraderInterfaceBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(5.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Terminal
		TERMINAL = register("terminal", ModCreativeGroups::getMachineGroup, () -> new TerminalBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL),
						Block.box(1d,0d,1d,15d,15d,15d)
				)
		);

		//Gem Terminal
		GEM_TERMINAL = register("gem_terminal", ModCreativeGroups::getMachineGroup, () -> new TerminalBlock(
						Block.Properties.of(Material.AMETHYST)
								.color(MaterialColor.COLOR_PURPLE)
								.strength(3.0f, 6.0f)
								.sound(SoundType.AMETHYST_CLUSTER),
						Block.box(2d, 0d, 2d, 14d, 12d, 14d)
				)
		);

		//Paygate
		PAYGATE = register("paygate", ModCreativeGroups::getTradingGroup, () -> new PaygateBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Ticket Machine
		TICKET_STATION = register("ticket_machine", ModCreativeGroups::getMachineGroup, () -> new TicketStationBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.METAL)
								.strength(3.0f, 6.0f)
								.sound(SoundType.METAL)
				)
		);

		//Coin Chest
		COIN_CHEST = register("coin_chest", ModCreativeGroups::getMachineGroup, getCustomRendererGenerator(), () -> new CoinChestBlock(
						Block.Properties.of(Material.WOOD)
								.color(MaterialColor.WOOD)
								.strength(2.5f, Float.POSITIVE_INFINITY)
								.sound(SoundType.WOOD)
				)
		);

		//Tax Block
		TAX_COLLECTOR = register("tax_block", ModCreativeGroups::getMachineGroup, () -> new TaxCollectorBlock(
						Block.Properties.of(Material.METAL)
								.color(MaterialColor.GOLD)
								.strength(3f, Float.POSITIVE_INFINITY)
								.sound(SoundType.METAL)
				)
		);

		//Coin Jars
		PIGGY_BANK = register("piggy_bank", ModCreativeGroups::getMachineGroup, getCoinJarGenerator(), () -> new CoinJarBlock(
						Block.Properties.of(Material.STONE)
								.color(MaterialColor.COLOR_PINK)
								.strength(0.1f, 2.0f)
								.sound(SoundType.STONE),
						Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		COINJAR_BLUE = register("coinjar_blue", ModCreativeGroups::getMachineGroup, getCoinJarGenerator(), () -> new CoinJarBlock(
						Block.Properties.of(Material.STONE)
								.color(MaterialColor.COLOR_BLUE)
								.strength(0.1f, 2.0f)
								.sound(SoundType.STONE),
						Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);

		SUS_JAR = register("sus_jar", ModCreativeGroups::getMachineGroup, getColoredCoinJarGenerator(), () -> new CoinJarBlock(
						Block.Properties.of(Material.STONE)
								.color(MaterialColor.SNOW)
								.strength(0.1f, 2.0f)
								.sound(SoundType.STONE),
						Block.box(4, 0d, 4d, 12d, 8d, 12d)
				)
		);

		//Auction Stand
		AUCTION_STAND = registerWooden("auction_stand", WoodType.Attributes.needsLog, ModCreativeGroups.getExtraOr(ModCreativeGroups::getMachineGroup), w ->
				new AuctionStandBlock(Block.Properties.of(Material.WOOD).color(w.mapColor).strength(2.0f))
		);


	}


	/**
	 * Block Registration Code
	 */
	private static <T extends Block> RegistryObject<T>  register(String name, NonNullSupplier<CreativeModeTab> itemGroup, Supplier<T> sup)
	{
		return register(name, itemGroup, getDefaultGenerator(), sup);
	}

	private static<T extends Block> RegistryObject<T> register(String name, NonNullSupplier<CreativeModeTab> itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Supplier<T> sup)
	{
		RegistryObject<T> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get(), itemGroup != null ? itemGroup.get() : null));
		return block;
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, NonNullSupplier<CreativeModeTab> itemGroup, Function<Color,T> block, @Nullable Color dontNameThisColor)
	{
		return registerColored(name, itemGroup, getDefaultGenerator(), block, dontNameThisColor);
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, NonNullSupplier<CreativeModeTab> itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Function<Color,T> block, @Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getResourceSafeName();
			//Register the block normally
			bundle.put(color, register(thisName, itemGroup, itemGenerator, () -> block.apply(color)));
		}
		return bundle.lock();
	}

	/**
	 * Wooden block registration code
	 */
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Predicate<WoodType.Attributes> check, NonNullFunction<WoodType,CreativeModeTab> itemGroup, Function<WoodType,T> block)
	{
		return registerWooden(name, check, itemGroup, getDefaultGenerator(), block);
	}

	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Predicate<WoodType.Attributes> check, NonNullFunction<WoodType,CreativeModeTab> itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Function<WoodType,T> block)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			if(!check.test(woodType.attributes))
				continue;
			String thisName = woodType.generateID(name);
			//Register the block normally
			bundle.put(woodType, register(thisName, () -> itemGroup.apply(woodType), itemGenerator, () -> block.apply(woodType)));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, NonNullFunction<WoodType,CreativeModeTab> itemGroup, BiFunction<WoodType,Color,T> block) { return registerWoodenAndColored(name, check, itemGroup, block, null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, NonNullFunction<WoodType,CreativeModeTab> itemGroup, BiFunction<WoodType,Color,T> block, @Nullable Color ignoreColor) { return registerWoodenAndColored(name, check, getDefaultGenerator(), itemGroup, block, ignoreColor); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, BiFunction<Block,CreativeModeTab,Item> itemGenerator, NonNullFunction<WoodType,CreativeModeTab> itemGroup, BiFunction<WoodType,Color,T> block) { return registerWoodenAndColored(name, check, itemGenerator, itemGroup, block, null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Predicate<WoodType.Attributes> check, BiFunction<Block,CreativeModeTab,Item> itemGenerator, NonNullFunction<WoodType,CreativeModeTab> itemGroup, BiFunction<WoodType,Color,T> block, @Nullable Color ignoreColor) {
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType : WoodType.validValues())
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
					bundle.put(woodType, color, register(thisName, () -> itemGroup.apply(woodType), itemGenerator, () -> block.apply(woodType, color)));
				}
			}
		}
		return bundle.lock();
	}

	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, NonNullSupplier<CreativeModeTab> itemGroup, Function<Color,FreezerBlock> block) {
		return registerFreezer(name, itemGroup, getDefaultGenerator(), block);
	}
	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, NonNullSupplier<CreativeModeTab> itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Function<Color,FreezerBlock> block) {
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