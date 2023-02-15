package io.github.lightman314.lightmanscurrency.core;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.blocks.*;
import io.github.lightman314.lightmanscurrency.blocks.tradeinterface.ItemTraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.reference.AuctionStandBlock;
import io.github.lightman314.lightmanscurrency.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.core.variants.Color;
import io.github.lightman314.lightmanscurrency.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.items.CashRegisterItem;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.items.CoinJarItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	private static Function<Block,Item> getDefaultGenerator() {
		return block -> {
			Item.Properties properties = new Item.Properties();
			return new BlockItem(block, properties);
		};
	}
	private static Function<Block,Item> getCoinGenerator(boolean fireResistant) {
		return block -> {
			Item.Properties properties = new Item.Properties();
			if(fireResistant)
				properties.fireResistant();
			return new CoinBlockItem(block, properties);
		};
	}
	private static Function<Block,Item> getCoinJarGenerator() {
		return block -> {
			Item.Properties properties = new Item.Properties();
			return new CoinJarItem(block, properties);
		};
	}
	
	static {
		//Coin Piles
		COINPILE_COPPER = register("coinpile_copper", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_COPPER
				)
		);
		COINPILE_IRON = register("coinpile_iron", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_IRON
				)
		);
		COINPILE_GOLD = register("coinpile_gold", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_GOLD
				)
		);
		COINPILE_EMERALD = register("coinpile_emerald", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_EMERALD
				)
		);
		COINPILE_DIAMOND = register("coinpile_diamond", getCoinGenerator(false), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_DIAMOND
				)
		);
		COINPILE_NETHERITE = register("coinpile_netherite", getCoinGenerator(true), () -> new CoinpileBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_NETHERITE
				)
		);
		
		//Coin Blocks
		COINBLOCK_COPPER = register("coinblock_copper", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_COPPER
				)
		);
		COINBLOCK_IRON = register("coinblock_iron", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_IRON
				)
		);
		COINBLOCK_GOLD = register("coinblock_gold", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_GOLD
				)
		);
		COINBLOCK_EMERALD = register("coinblock_emerald", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_EMERALD
				)
		);
		COINBLOCK_DIAMOND = register("coinblock_diamond", getCoinGenerator(false), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_DIAMOND
				)
		);
		COINBLOCK_NETHERITE = register("coinblock_netherite", getCoinGenerator(true), () -> new CoinBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL),
				ModItems.COIN_NETHERITE
				)
		);
		
		//Machines
		MACHINE_ATM = register("atm", () -> new ATMBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, 6.0f)
				.sound(SoundType.METAL)
				)
		);
		MACHINE_MINT = register("coinmint", () -> new CoinMintBlock(
			Block.Properties.of(Material.METAL)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			)
		);
		CASH_REGISTER = register("cash_register", block -> new CashRegisterItem(block, new Item.Properties()),
				() -> new CashRegisterBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,10d,15d)
				)
		);
		
		//Item Traders
		//Display Case
		DISPLAY_CASE = register("display_case", () -> new DisplayCaseBlock(
			Block.Properties.of(Material.GLASS)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
			)
		);
		
		//Vending Machine
		VENDING_MACHINE = registerColoredOldNames("vending_machine", () -> new VendingMachineBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			)
		);
		
		//Large Vending Machine
		VENDING_MACHINE_LARGE = registerColoredOldNames("vending_machine_large", () -> new VendingMachineLargeBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			)
		);
		
		//Shelves
		SHELF = registerWooden("shelf", () -> new ShelfBlock(
				Block.Properties.of(Material.WOOD)
					.strength(2.0f, Float.POSITIVE_INFINITY)
				)
		);
		
		//Card Display
		CARD_DISPLAY = registerWooden("card_display", () -> new CardDisplayBlock(
				Block.Properties.of(Material.WOOD)
					.strength(2.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.WOOD)
				)
		);
		
		//Freezer
		FREEZER = registerFreezer("freezer", (c) -> new FreezerBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
				FreezerBlock.GenerateDoorModel(c)
				)
		);
		
		//Armor Display
		ARMOR_DISPLAY = register("armor_display", () -> new ArmorDisplayBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Kiosk
		TICKET_KIOSK = register("ticket_kiosk", () -> new TicketKioskBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		
		
		//Network Traders
		ITEM_NETWORK_TRADER_1 = register("item_trader_server_sml", () -> new NetworkItemTraderBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_SMALL
				)
		);
		ITEM_NETWORK_TRADER_2 = register("item_trader_server_med", () -> new NetworkItemTraderBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_MEDIUM
				)
		);
		ITEM_NETWORK_TRADER_3 = register("item_trader_server_lrg", () -> new NetworkItemTraderBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_LARGE
				)
		);
		ITEM_NETWORK_TRADER_4 = register("item_trader_server_xlrg", () -> new NetworkItemTraderBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL),
					NetworkItemTraderBlock.TRADER_COUNT_XLARGE
				)
		);
		
		//Trader Interface
		ITEM_TRADER_INTERFACE = register("item_trader_interface", () -> new ItemTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
					.strength(5.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Terminal
		TERMINAL = register("terminal", () -> new TerminalBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL),
					Block.box(1d,0d,1d,15d,15d,15d)
				)
		);
		
		//Gem Terminal
		GEM_TERMINAL = register("gem_terminal", () -> new TerminalBlock(
				Block.Properties.of(Material.AMETHYST)
				.strength(3.0f, 6.0f)
				.sound(SoundType.AMETHYST_CLUSTER),
				Block.box(2d, 0d, 2d, 14d, 12d, 14d)
				)
		);
		
		//Paygate
		PAYGATE = register("paygate", () -> new PaygateBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, Float.POSITIVE_INFINITY)
					.sound(SoundType.METAL)
				)
		);
		
		//Ticket Machine
		TICKET_STATION = register("ticket_machine", () -> new TicketMachineBlock(
				Block.Properties.of(Material.METAL)
					.strength(3.0f, 6.0f)
					.sound(SoundType.METAL)
				)
		);
		
		//Coin Jars
		PIGGY_BANK = register("piggy_bank", getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of(Material.STONE)
					.strength(0.1f, 2.0f)
					.sound(SoundType.STONE),
					Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);
		COINJAR_BLUE = register("coinjar_blue", getCoinJarGenerator(), () -> new CoinJarBlock(
				Block.Properties.of(Material.STONE)
				.strength(0.1f, 2.0f)
				.sound(SoundType.STONE),
				Block.box(4d, 0d, 4d, 12d, 8d, 12d)
				)
		);

		//Auction Stand
		AUCTION_STAND = registerWooden("auction_stand", () ->
			new AuctionStandBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.0f))
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
	public static final RegistryObjectBundle<Block,WoodType> SHELF;
	
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

	//Auciton Stants
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND;
	
	
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
	/**
	 * Will be removed in 1.20, and colored block ids will be renamed to include underscores for light_gray/light_blue
	 */
	@Deprecated(forRemoval = true, since = "1.19.3")
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColoredOldNames(String name, Supplier<T> block)
	{
		return registerColoredOldNames(name, getDefaultGenerator(), block);
	}

	/**
	 * Will be removed in 1.20, and colored block ids will be renamed to include underscores for light_gray/light_blue
	 */
	@Deprecated(forRemoval = true, since = "1.19.3")
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColoredOldNames(String name, Function<Block,Item> itemGenerator, Supplier<T> block)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != Color.WHITE) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getOldName().toLowerCase();
			//Register the block normally
			bundle.put(color, register(thisName, itemGenerator, block));
		}
		return bundle.lock();
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Supplier<T> block, @Nullable Color dontNameThisColor) {
		return registerColored(name, getDefaultGenerator(), block, dontNameThisColor);
	}

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<Block,Item> itemGenerator, Supplier<T> block, @Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.toString().toLowerCase();
			//Register the block normally
			bundle.put(color, register(thisName, itemGenerator, block));
		}
		return bundle.lock();
	}
	
	/**
	 * Wooden block registration code
	 */
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Supplier<T> block)
	{
		return registerWooden(name, getDefaultGenerator(), block);
	}
	
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name, Function<Block,Item> itemGenerator, Supplier<T> block)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			String thisName = name + "_" + woodType.toString().toLowerCase();
			//Register the block normally
			bundle.put(woodType, register(thisName, itemGenerator, block));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block>  RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Supplier<T> block)
	{
		return registerWoodenAndColored(name, getDefaultGenerator(), block);
	}

	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name, Function<Block,Item> itemGenerator, Supplier<T> block)
	{
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType: WoodType.validValues())
		{
			for(Color color : Color.values())
			{
				String thisName = name + "_" + woodType.toString().toLowerCase() + "_" + color.toString().toLowerCase();
				//Register the block normally
				bundle.put(woodType, color, register(thisName, itemGenerator, block));
			}
		}
		return bundle.lock();
	}

	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, Function<Color,FreezerBlock> block) {
		return registerFreezer(name, getDefaultGenerator(), block);
	}
	public static RegistryObjectBundle<FreezerBlock,Color> registerFreezer(String name, Function<Block,Item> itemGenerator, Function<Color,FreezerBlock> block) {
		RegistryObjectBundle<FreezerBlock,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = color == Color.BLACK ? name : name + "_" + color.toString().toLowerCase();
			//Register the block normally
			bundle.put(color, register(thisName, itemGenerator, () -> block.apply(color)));
		}
		return bundle.lock();
	}

}
