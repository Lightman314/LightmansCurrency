package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.blocks.*;
import io.github.lightman314.lightmanscurrency.items.CashRegisterItem;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.BlockItemPair;
import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference;
import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {

	private enum BlockItemType { DEFAULT, COIN, CASH_REGISTER };
	
	
	private static final List<Block> BLOCKS = new ArrayList<>();
	private static final List<Item> ITEMS = new ArrayList<>();
	
	//Coin piles
	public static final BlockItemPair COINPILE_COPPER = register("coinpile_copper", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_COPPER
			)
	);
	public static final BlockItemPair COINPILE_IRON = register("coinpile_iron", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_IRON
			)
	);
	public static final BlockItemPair COINPILE_GOLD = register("coinpile_gold", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_GOLD
			)
	);
	public static final BlockItemPair COINPILE_DIAMOND = register("coinpile_diamond", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_DIAMOND
			)
	);
	public static final BlockItemPair COINPILE_EMERALD = register("coinpile_emerald", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_EMERALD
			)
	);
	public static final BlockItemPair COINPILE_NETHERITE = register("coinpile_netherite", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinpileBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_NETHERITE
			)
	);
	
	//Coin blocks
	public static final BlockItemPair COINBLOCK_COPPER = register("coinblock_copper", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_COPPER
			)
			
	);
	public static final BlockItemPair COINBLOCK_IRON = register("coinblock_iron", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_IRON
			)
	);
	public static final BlockItemPair COINBLOCK_GOLD = register("coinblock_gold", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_GOLD
			)
	);
	public static final BlockItemPair COINBLOCK_EMERALD = register("coinblock_emerald", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_EMERALD
			)
	);
	public static final BlockItemPair COINBLOCK_DIAMOND = register("coinblock_diamond", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_DIAMOND
			)
	);
	public static final BlockItemPair COINBLOCK_NETHERITE = register("coinblock_netherite", LightmansCurrency.COIN_GROUP, BlockItemType.COIN, new CoinBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			ModItems.COIN_NETHERITE
			)
	);
	
	//Machines
	//Misc Machines
	public static final BlockItemPair MACHINE_ATM = register("atm", LightmansCurrency.MACHINE_GROUP, new ATMBlock(
		Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE)
			)
	);
	public static final BlockItemPair MACHINE_MINT = register("coinmint", LightmansCurrency.MACHINE_GROUP, new CoinMintBlock(
		Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE)
			)
	);
	
	//Trading Machines
	//Display Case
	public static final BlockItemPair DISPLAY_CASE = register("display_case", LightmansCurrency.TRADING_GROUP, BlockItemType.DEFAULT, new DisplayCaseBlock(
			Block.Properties.create(Material.GLASS)
				.notSolid()
				.hardnessAndResistance(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
				.harvestTool(ToolType.PICKAXE)
				)
	);
	
	//Vending Machine 1 (Normal Sized)
	public static final BlockItemSet<Colors> VENDING_MACHINE1 = registerColored("vending_machine", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE)
		), false);
	
	//Vending Machine 2 (Large)
	public static final BlockItemSet<Colors> VENDING_MACHINE2 = registerColored("vending_machine_large", LightmansCurrency.TRADING_GROUP, () -> new VendingMachineLargeBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE)
			), false);
	
	//Wooden Shelves
	public static final BlockItemSet<WoodType> SHELF = registerWooden("shelf", LightmansCurrency.TRADING_GROUP, () -> new ShelfBlock(
			Block.Properties.create(Material.WOOD)
				.notSolid()
				.hardnessAndResistance(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				));
	
	//Card Shelves
	public static final BlockItemSet<WoodType> CARD_DISPLAY = registerWooden("card_display", LightmansCurrency.TRADING_GROUP, () -> new CardDisplayBlock(
			Block.Properties.create(Material.WOOD)
				.notSolid()
				.hardnessAndResistance(2.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				));
	
	//Armor Display
	public static final BlockItemPair ARMOR_DISPLAY = register("armor_display", LightmansCurrency.TRADING_GROUP, new ArmorDisplayBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE)
			)
	);
	
	//Freezer
	public static final BlockItemPair FREEZER = register("freezer", LightmansCurrency.TRADING_GROUP, new FreezerBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE)
			)
	);
	
	//Small Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_SMALL = register("item_trader_server_sml", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE),
				3
			)
	);
	//Medium Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_MEDIUM = register("item_trader_server_med", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE),
				6
			)
	);
	//Large Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_LARGE = register("item_trader_server_lrg", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE),
				12
			)
	);
	//X-Large Network Trader
	public static final BlockItemPair ITEM_TRADER_SERVER_EXTRA_LARGE = register("item_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, new ItemTraderServerBlock(
			Block.Properties.create(Material.IRON)
				.notSolid()
				.hardnessAndResistance(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE),
				16
			)
	);
	
	//Cash Register
	public static final BlockItemPair CASH_REGISTER = register("cash_register", LightmansCurrency.MACHINE_GROUP, BlockItemType.CASH_REGISTER, new CashRegisterBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			Block.makeCuboidShape(1d,0d,1d,15d,10d,15d)
		)
	);
	
	//Terminal
	public static final BlockItemPair TERMINAL = register("terminal", LightmansCurrency.MACHINE_GROUP, new TerminalBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE),
			Block.makeCuboidShape(1d,0d,1d,15d,15d,15d)
		)
	);
	
	//Paygate
	public static final BlockItemPair PAYGATE = register("paygate", LightmansCurrency.MACHINE_GROUP, new PaygateBlock(
			Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE)
		)
	);
	
	//Ticket Machine
	public static final BlockItemPair TICKET_MACHINE = register("ticket_machine", LightmansCurrency.MACHINE_GROUP, new TicketMachineBlock(
			Block.Properties.create(Material.IRON)
			.notSolid()
			.hardnessAndResistance(3.0f, 6.0f)
			.sound(SoundType.METAL)
			.harvestLevel(0)
			.harvestTool(ToolType.PICKAXE)
			)
	);
	
	/*
	* Block Registration Code
	*/
	private static BlockItemPair register(String name, ItemGroup itemGroup, Block block)
	{
		return register(name, itemGroup, BlockItemType.DEFAULT, block);
	}
	
	private static BlockItemPair register(String name, ItemGroup itemGroup, BlockItemType type, Block block)
	{
		block.setRegistryName(name);
		BLOCKS.add(block);
		if(block.getRegistryName() != null)
		{
			Item item = null;
			if(type == BlockItemType.DEFAULT)
				item = new BlockItem(block, new Item.Properties().group(itemGroup));
			else if(type == BlockItemType.CASH_REGISTER)
				item = new CashRegisterItem(block, new Item.Properties().group(itemGroup).maxStackSize(1));
			else if(type == BlockItemType.COIN)
				item = new CoinBlockItem(block, new Item.Properties().group(itemGroup));
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
	/*private static BlockItemSet<Reference.Colors> registerColored(String name, ItemGroup itemGroup, Supplier<Block> block)
	{
		return registerColored(name, itemGroup, BlockItemType.DEFAULT, block);
	}
	
	private static BlockItemSet<Colors> registerColored(String name, ItemGroup itemGroup, BlockItemType type, Supplier<Block> block)
	{
		return registerColored(name, itemGroup, type, block, true);
	}*/
	
	private static BlockItemSet<Colors> registerColored(String name, ItemGroup itemGroup, Supplier<Block> block, boolean whiteNamed)
	{
		return registerColored(name, itemGroup, BlockItemType.DEFAULT, block, whiteNamed);
	}
	
	private static BlockItemSet<Colors> registerColored(String name, ItemGroup itemGroup, BlockItemType type, Supplier<Block> block, boolean whiteNamed)
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
	private static BlockItemSet<WoodType> registerWooden(String name, ItemGroup itemGroup, Supplier<Block> block)
	{
		return registerWooden(name, itemGroup, BlockItemType.DEFAULT, block);
	}
	
	private static BlockItemSet<WoodType> registerWooden(String name, ItemGroup itemGroup, BlockItemType type, Supplier<Block> block)
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
