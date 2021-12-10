package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.blocks.*;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.ArmorDisplayBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.CardDisplayBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.DisplayCaseBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.ShelfBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.TicketKioskBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.VendingMachineBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.VendingMachineLargeBlock;
import io.github.lightman314.lightmanscurrency.items.CashRegisterItem;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.items.CoinJarItem;
import io.github.lightman314.lightmanscurrency.BlockItemPair;
import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference;
import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {

	private enum BlockItemType { DEFAULT, COIN, NETHERITE_COIN, CASH_REGISTER, COIN_JAR };
	
	
	private static final List<Block> BLOCKS = new ArrayList<>();
	private static final List<Item> ITEMS = new ArrayList<>();
	
	//Coin piles
	public static final BlockItemPair COINPILE_COPPER = register("coinpile_copper", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_COPPER
			)
	);
	public static final BlockItemPair COINPILE_IRON = register("coinpile_iron", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_IRON
			)
	);
	public static final BlockItemPair COINPILE_GOLD = register("coinpile_gold", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_GOLD
			)
	);
	public static final BlockItemPair COINPILE_DIAMOND = register("coinpile_diamond", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_DIAMOND
			)
	);
	public static final BlockItemPair COINPILE_EMERALD = register("coinpile_emerald", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_EMERALD
			)
	);
	public static final BlockItemPair COINPILE_NETHERITE = register("coinpile_netherite", LightmansCurrency.COIN_GROUP, BlockItemType.NETHERITE_COIN, new CoinpileBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_NETHERITE
			)
	);
	
	//Coin blocks
	public static final BlockItemPair COINBLOCK_COPPER = register("coinblock_copper", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_COPPER
			)
			
	);
	public static final BlockItemPair COINBLOCK_IRON = register("coinblock_iron", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_IRON
			)
	);
	public static final BlockItemPair COINBLOCK_GOLD = register("coinblock_gold", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_GOLD
			)
	);
	public static final BlockItemPair COINBLOCK_EMERALD = register("coinblock_emerald", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_EMERALD
			)
	);
	public static final BlockItemPair COINBLOCK_DIAMOND = register("coinblock_diamond", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_DIAMOND
			)
	);
	public static final BlockItemPair COINBLOCK_NETHERITE = register("coinblock_netherite", LightmansCurrency.COIN_GROUP, BlockItemType.NETHERITE_COIN, new CoinBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			ModItems.COIN_NETHERITE
			)
	);
	
	//Machines
	//Misc Machines
	public static final BlockItemPair MACHINE_ATM = register("atm", LightmansCurrency.MACHINE_GROUP, new ATMBlock(
		Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL)
			)
	);
	public static final BlockItemPair MACHINE_MINT = register("coinmint", LightmansCurrency.MACHINE_GROUP, new CoinMintBlock(
		Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL)
			)
	);
	
	//Trading Machines
	//Display Case
	public static final BlockItemPair DISPLAY_CASE = register("display_case", LightmansCurrency.TRADING_GROUP, BlockItemType.DEFAULT, new DisplayCaseBlock(
			Block.Properties.of(Material.GLASS)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
				)
	);
	
	//Vending Machine 1 (Normal Sized)
	public static final BlockItemSet<Colors> VENDING_MACHINE1 = registerColored("vending_machine", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineBlock(
			Block.Properties.of(Material.METAL)
			.strength(5.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
		), false);
	
	//Vending Machine 2 (Large)
	public static final BlockItemSet<Colors> VENDING_MACHINE2 = registerColored("vending_machine_large", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineLargeBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			), false);
	
	//Wooden Shelves
	public static final BlockItemSet<WoodType> SHELF = registerWooden("shelf", LightmansCurrency.TRADING_GROUP, () -> new ShelfBlock(
			Block.Properties.of(Material.WOOD)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				));
	
	//Card Shelves
	public static final BlockItemSet<WoodType> CARD_DISPLAY = registerWooden("card_display", LightmansCurrency.TRADING_GROUP, () -> new CardDisplayBlock(
			Block.Properties.of(Material.WOOD)
				.strength(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.WOOD)
				));
	
	//Armor Display
	public static final BlockItemPair ARMOR_DISPLAY = register("armor_display", LightmansCurrency.TRADING_GROUP, new ArmorDisplayBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			)
	);
	
	//Freezer
	public static final BlockItemPair FREEZER = register("freezer", LightmansCurrency.TRADING_GROUP, new FreezerBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
			)
	);
	
	
	
	//Small Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_SMALL = register("item_trader_server_sml", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL),
				3
			)
	);
	//Medium Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_MEDIUM = register("item_trader_server_med", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL),
				6
			)
	);
	//Large Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_LARGE = register("item_trader_server_lrg", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL),
				12
			)
	);
	//X-Large Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_EXTRA_LARGE = register("item_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL),
				16
			)
	);
	
	//Cash Register
	public static final BlockItemPair CASH_REGISTER = register("cash_register", LightmansCurrency.MACHINE_GROUP, BlockItemType.CASH_REGISTER, new CashRegisterBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			Block.box(1d,0d,1d,15d,10d,15d)
		)
	);
	
	//Terminal
	public static final BlockItemPair TERMINAL = register("terminal", LightmansCurrency.MACHINE_GROUP, new TerminalBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL),
			Block.box(1d,0d,1d,15d,15d,15d)
		)
	);
	
	//Paygate
	public static final BlockItemPair PAYGATE = register("paygate", LightmansCurrency.MACHINE_GROUP, new PaygateBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
		)
	);
	
	//Ticket Kiosk
	public static final BlockItemPair TICKET_KIOSK = register("ticket_kiosk",LightmansCurrency.TRADING_GROUP, new TicketKioskBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
		)
	);
	
	//Ticket Machine
	public static final BlockItemPair TICKET_MACHINE = register("ticket_machine", LightmansCurrency.MACHINE_GROUP, new TicketMachineBlock(
			Block.Properties.of(Material.METAL)
			.strength(3.0f, 6.0f)
			.sound(SoundType.METAL)
			)
	);
	
	//Coin Jars
	public static final BlockItemPair PIGGY_BANK = register("piggy_bank", CreativeModeTab.TAB_DECORATIONS, BlockItemType.COIN_JAR, new CoinJarBlock(
			Block.Properties.of(Material.STONE)
			.strength(0.1f, 2.0f)
			.sound(SoundType.STONE),
			Block.box(4d, 0d, 4d, 12d, 8d, 12d)
			)
	);
	
	//Jar Blue
	public static final BlockItemPair COINJAR_BLUE = register("coinjar_blue", CreativeModeTab.TAB_DECORATIONS, BlockItemType.COIN_JAR, new CoinJarBlock(
			Block.Properties.of(Material.STONE)
			.strength(0.1f, 2.0f)
			.sound(SoundType.STONE),
			Block.box(4d, 0d, 4d, 12d, 8d, 12d)
			)
	);
	
	
	/*
	* Block Registration Code
	*/
	private static BlockItemPair register(String name, CreativeModeTab itemGroup, Block block)
	{
		return register(name, itemGroup, BlockItemType.DEFAULT, block);
	}
	
	private static BlockItemPair register(String name, CreativeModeTab itemGroup, BlockItemType type, Block block)
	{
		block.setRegistryName(name);
		BLOCKS.add(block);
		if(block.getRegistryName() != null)
		{
			Item item = null;
			switch(type)
			{
			case CASH_REGISTER:
				item = new CashRegisterItem(block, new Item.Properties().tab(itemGroup).stacksTo(1));
				break;
			case COIN:
				item = new CoinBlockItem(block, new Item.Properties().tab(itemGroup));
				break;
			case NETHERITE_COIN:
				item = new CoinBlockItem(block, new Item.Properties().tab(itemGroup).fireResistant());
				break;
			case COIN_JAR:
				item = new CoinJarItem(block, new Item.Properties().tab(itemGroup));
				break;
			default:
				item = new BlockItem(block, new Item.Properties().tab(itemGroup));
			}
			if(item != null)
			{
				item.setRegistryName(name);
				ITEMS.add(item);
			}
			return new BlockItemPair(block,item);
		}
		return new BlockItemPair(block,null);
	}
	
	/*
	 * Colored block registration code
	 */
	private static BlockItemSet<Colors> registerColored(String name, CreativeModeTab itemGroup, Supplier<Block> block, boolean whiteNamed)
	{
		return registerColored(name, itemGroup, BlockItemType.DEFAULT, block, whiteNamed);
	}
	
	private static BlockItemSet<Colors> registerColored(String name, CreativeModeTab itemGroup, BlockItemType type, Supplier<Block> block, boolean whiteNamed)
	{
		BlockItemSet<Reference.Colors> set = new BlockItemSet<Reference.Colors>();
		for(Reference.Colors color : Reference.Colors.values())
		{
			String thisName = name;
			if(color != Reference.Colors.WHITE || whiteNamed) //Add the color name to the end unless this is white and white is not flagged to be named
				thisName += "_" + color.toString().toLowerCase();
			//Register the block normally
			BlockItemPair thisBlock = register(thisName, itemGroup, type, block.get());
			//Add to the set
			set.add(color, thisBlock);
		}
		//Return the set
		return set;
	}
	
	/*
	 * Wooden block registration code
	 */
	private static BlockItemSet<WoodType> registerWooden(String name, CreativeModeTab itemGroup, Supplier<Block> block)
	{
		return registerWooden(name, itemGroup, BlockItemType.DEFAULT, block);
	}
	
	private static BlockItemSet<WoodType> registerWooden(String name, CreativeModeTab itemGroup, BlockItemType type, Supplier<Block> block)
	{
		BlockItemSet<Reference.WoodType> set = new BlockItemSet<Reference.WoodType>();
		for(WoodType woodType : WoodType.values())
		{
			String thisName = name + "_" + woodType.toString().toLowerCase();
			//Register the block normally
			BlockItemPair thisBlock = register(thisName, itemGroup, type, block.get());
			//Add to the set
			set.add(woodType, thisBlock);
		}
		//Return the set
		return set;
	}
	
	//Registration Events
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event)
	{
		BLOCKS.forEach(block -> event.getRegistry().register(block));
		BLOCKS.clear();
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
