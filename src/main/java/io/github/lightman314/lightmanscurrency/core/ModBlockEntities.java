package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.Reference.Color;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("trader", () -> BlockEntityType.Builder.of(ItemTraderBlockEntity::new,
				//Display Case
				ModBlocks.DISPLAY_CASE.get(),
				//Vending Machine 1
				ModBlocks.VENDING_MACHINE.get(Color.WHITE),
				ModBlocks.VENDING_MACHINE.get(Color.ORANGE),
				ModBlocks.VENDING_MACHINE.get(Color.MAGENTA),
				ModBlocks.VENDING_MACHINE.get(Color.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE.get(Color.YELLOW),
				ModBlocks.VENDING_MACHINE.get(Color.LIME),
				ModBlocks.VENDING_MACHINE.get(Color.PINK),
				ModBlocks.VENDING_MACHINE.get(Color.GRAY),
				ModBlocks.VENDING_MACHINE.get(Color.LIGHTGRAY),
				ModBlocks.VENDING_MACHINE.get(Color.CYAN),
				ModBlocks.VENDING_MACHINE.get(Color.PURPLE),
				ModBlocks.VENDING_MACHINE.get(Color.BLUE),
				ModBlocks.VENDING_MACHINE.get(Color.BROWN),
				ModBlocks.VENDING_MACHINE.get(Color.GREEN),
				ModBlocks.VENDING_MACHINE.get(Color.RED),
				ModBlocks.VENDING_MACHINE.get(Color.BLACK),
				//Vending Machine 2
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.WHITE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.ORANGE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.MAGENTA),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.YELLOW),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIME),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.PINK),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.GRAY),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIGHTGRAY),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.CYAN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.PURPLE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BLUE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BROWN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.GREEN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.RED),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BLACK),
				//Wooden Shelves
				ModBlocks.SHELF.get(WoodType.OAK),
				ModBlocks.SHELF.get(WoodType.BIRCH),
				ModBlocks.SHELF.get(WoodType.SPRUCE),
				ModBlocks.SHELF.get(WoodType.JUNGLE),
				ModBlocks.SHELF.get(WoodType.ACACIA),
				ModBlocks.SHELF.get(WoodType.DARK_OAK),
				ModBlocks.SHELF.get(WoodType.CRIMSON),
				ModBlocks.SHELF.get(WoodType.WARPED),
				//Card Displays
				ModBlocks.CARD_DISPLAY.get(WoodType.OAK),
				ModBlocks.CARD_DISPLAY.get(WoodType.BIRCH),
				ModBlocks.CARD_DISPLAY.get(WoodType.SPRUCE),
				ModBlocks.CARD_DISPLAY.get(WoodType.JUNGLE),
				ModBlocks.CARD_DISPLAY.get(WoodType.ACACIA),
				ModBlocks.CARD_DISPLAY.get(WoodType.DARK_OAK),
				ModBlocks.CARD_DISPLAY.get(WoodType.CRIMSON),
				ModBlocks.CARD_DISPLAY.get(WoodType.WARPED)
			).build(null));
		
		ARMOR_TRADER = ModRegistries.BLOCK_ENTITIES.register("armor_trader", () -> BlockEntityType.Builder.of(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY.get()).build(null));
		
		FREEZER_TRADER = ModRegistries.BLOCK_ENTITIES.register("freezer_trader", () -> BlockEntityType.Builder.of(FreezerTraderBlockEntity::new, ModBlocks.FREEZER.get()).build(null));
		
		TICKET_TRADER = ModRegistries.BLOCK_ENTITIES.register("ticket_trader", () -> BlockEntityType.Builder.of(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK.get()).build(null));
		
		UNIVERSAL_ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_item_trader", () -> BlockEntityType.Builder.of(UniversalItemTraderBlockEntity::new, ModBlocks.ITEM_TRADER_SERVER_SMALL.get(), ModBlocks.ITEM_TRADER_SERVER_MEDIUM.get(), ModBlocks.ITEM_TRADER_SERVER_LARGE.get(), ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE.get()).build(null));
		
		ITEM_INTERFACE = ModRegistries.BLOCK_ENTITIES.register("item_interface", () -> BlockEntityType.Builder.of(ItemInterfaceBlockEntity::new,
				//Vending Machine 1
				ModBlocks.VENDING_MACHINE.get(Color.WHITE),
				ModBlocks.VENDING_MACHINE.get(Color.ORANGE),
				ModBlocks.VENDING_MACHINE.get(Color.MAGENTA),
				ModBlocks.VENDING_MACHINE.get(Color.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE.get(Color.YELLOW),
				ModBlocks.VENDING_MACHINE.get(Color.LIME),
				ModBlocks.VENDING_MACHINE.get(Color.PINK),
				ModBlocks.VENDING_MACHINE.get(Color.GRAY),
				ModBlocks.VENDING_MACHINE.get(Color.LIGHTGRAY),
				ModBlocks.VENDING_MACHINE.get(Color.CYAN),
				ModBlocks.VENDING_MACHINE.get(Color.PURPLE),
				ModBlocks.VENDING_MACHINE.get(Color.BLUE),
				ModBlocks.VENDING_MACHINE.get(Color.BROWN),
				ModBlocks.VENDING_MACHINE.get(Color.GREEN),
				ModBlocks.VENDING_MACHINE.get(Color.RED),
				ModBlocks.VENDING_MACHINE.get(Color.BLACK),
				//Vending Machine 2
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.WHITE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.ORANGE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.MAGENTA),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.YELLOW),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIME),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.PINK),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.GRAY),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.LIGHTGRAY),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.CYAN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.PURPLE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BLUE),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BROWN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.GREEN),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.RED),
				ModBlocks.VENDING_MACHINE_LARGE.get(Color.BLACK),
				//Armor Display
				ModBlocks.ARMOR_DISPLAY.get(),
				//Freezer
				ModBlocks.FREEZER.get()
			).build(null));
		
		TRADER_INTERFACE_ITEM = ModRegistries.BLOCK_ENTITIES.register("trader_interface_item", () -> BlockEntityType.Builder.of(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE.get()).build(null));
		
		CASH_REGISTER = ModRegistries.BLOCK_ENTITIES.register("cash_register", () -> BlockEntityType.Builder.of(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER.get()).build(null));
		
		COIN_MINT = ModRegistries.BLOCK_ENTITIES.register("coin_mint", () -> BlockEntityType.Builder.of(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT.get()).build(null));
		
		TICKET_MACHINE = ModRegistries.BLOCK_ENTITIES.register("ticket_machine", () -> BlockEntityType.Builder.of(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE.get()).build(null));
		
		PAYGATE = ModRegistries.BLOCK_ENTITIES.register("paygate", () -> BlockEntityType.Builder.of(PaygateBlockEntity::new, ModBlocks.PAYGATE.get()).build(null));
		
		COIN_JAR = ModRegistries.BLOCK_ENTITIES.register("coin_jar", () -> BlockEntityType.Builder.of(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK.get(), ModBlocks.COINJAR_BLUE.get()).build(null));
	}
	
	//Item Trader
	public static final RegistryObject<BlockEntityType<ItemTraderBlockEntity>> ITEM_TRADER;
	//Armor variant of the trader
	public static final RegistryObject<BlockEntityType<ArmorDisplayTraderBlockEntity>> ARMOR_TRADER;
	//Freezer variant of the trader
	public static final RegistryObject<BlockEntityType<FreezerTraderBlockEntity>> FREEZER_TRADER;
	//Ticket variant of the trader
	public static final RegistryObject<BlockEntityType<TicketTraderBlockEntity>> TICKET_TRADER;
	
	//Universal Item Trader
	public static final RegistryObject<BlockEntityType<UniversalItemTraderBlockEntity>> UNIVERSAL_ITEM_TRADER;
	
	//Item Interface for multi-block traders
	public static final RegistryObject<BlockEntityType<ItemInterfaceBlockEntity>> ITEM_INTERFACE;
	
	//Trader Interface Terminal
	public static final RegistryObject<BlockEntityType<ItemTraderInterfaceBlockEntity>> TRADER_INTERFACE_ITEM;
	
	//Cash Register
	public static final RegistryObject<BlockEntityType<CashRegisterBlockEntity>> CASH_REGISTER;
	
	//Coin Mint
	public static final RegistryObject<BlockEntityType<CoinMintBlockEntity>> COIN_MINT;
	//Ticket Machine
	public static final RegistryObject<BlockEntityType<TicketMachineBlockEntity>> TICKET_MACHINE;
	
	//Paygate
	public static final RegistryObject<BlockEntityType<PaygateBlockEntity>> PAYGATE;
	
	//Coin Jars
	public static final RegistryObject<BlockEntityType<CoinJarBlockEntity>> COIN_JAR;
	
}
