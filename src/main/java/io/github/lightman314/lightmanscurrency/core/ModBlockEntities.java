package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class ModBlockEntities {
	
	public static void init() {
		
		ModRegistries.BLOCK_ENTITIES.register("trader", () -> BlockEntityType.Builder.of(ItemTraderBlockEntity::new,
				//Display Case
				ModBlocks.DISPLAY_CASE,
				//Vending Machine 1
				ModBlocks.VENDING_MACHINE,
				ModBlocks.VENDING_MACHINE_ORANGE,
				ModBlocks.VENDING_MACHINE_MAGENTA,
				ModBlocks.VENDING_MACHINE_LIGHTBLUE,
				ModBlocks.VENDING_MACHINE_YELLOW,
				ModBlocks.VENDING_MACHINE_LIME,
				ModBlocks.VENDING_MACHINE_PINK,
				ModBlocks.VENDING_MACHINE_GRAY,
				ModBlocks.VENDING_MACHINE_LIGHTGRAY,
				ModBlocks.VENDING_MACHINE_CYAN,
				ModBlocks.VENDING_MACHINE_PURPLE,
				ModBlocks.VENDING_MACHINE_BLUE,
				ModBlocks.VENDING_MACHINE_BROWN,
				ModBlocks.VENDING_MACHINE_GREEN,
				ModBlocks.VENDING_MACHINE_RED,
				ModBlocks.VENDING_MACHINE_BLACK,
				//Vending Machine 2
				ModBlocks.VENDING_MACHINE_LARGE,
				ModBlocks.VENDING_MACHINE_LARGE_ORANGE,
				ModBlocks.VENDING_MACHINE_LARGE_MAGENTA,
				ModBlocks.VENDING_MACHINE_LARGE_LIGHTBLUE,
				ModBlocks.VENDING_MACHINE_LARGE_YELLOW,
				ModBlocks.VENDING_MACHINE_LARGE_LIME,
				ModBlocks.VENDING_MACHINE_LARGE_PINK,
				ModBlocks.VENDING_MACHINE_LARGE_GRAY,
				ModBlocks.VENDING_MACHINE_LARGE_LIGHTGRAY,
				ModBlocks.VENDING_MACHINE_LARGE_CYAN,
				ModBlocks.VENDING_MACHINE_LARGE_PURPLE,
				ModBlocks.VENDING_MACHINE_LARGE_BLUE,
				ModBlocks.VENDING_MACHINE_LARGE_BROWN,
				ModBlocks.VENDING_MACHINE_LARGE_GREEN,
				ModBlocks.VENDING_MACHINE_LARGE_RED,
				ModBlocks.VENDING_MACHINE_LARGE_BLACK,
				//Wooden Shelves
				ModBlocks.SHELF_OAK,
				ModBlocks.SHELF_BIRCH,
				ModBlocks.SHELF_SPRUCE,
				ModBlocks.SHELF_JUNGLE,
				ModBlocks.SHELF_ACACIA,
				ModBlocks.SHELF_DARK_OAK,
				ModBlocks.SHELF_CRIMSON,
				ModBlocks.SHELF_WARPED,
				//Card Displays
				ModBlocks.CARD_DISPLAY_OAK,
				ModBlocks.CARD_DISPLAY_BIRCH,
				ModBlocks.CARD_DISPLAY_SPRUCE,
				ModBlocks.CARD_DISPLAY_JUNGLE,
				ModBlocks.CARD_DISPLAY_ACACIA,
				ModBlocks.CARD_DISPLAY_DARK_OAK,
				ModBlocks.CARD_DISPLAY_CRIMSON,
				ModBlocks.CARD_DISPLAY_WARPED
			).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("armor_trader", () -> BlockEntityType.Builder.of(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("freezer_trader", () -> BlockEntityType.Builder.of(FreezerTraderBlockEntity::new, ModBlocks.FREEZER).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("ticket_trader", () -> BlockEntityType.Builder.of(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("universal_item_trader", () -> BlockEntityType.Builder.of(UniversalItemTraderBlockEntity::new, ModBlocks.ITEM_TRADER_SERVER_SMALL, ModBlocks.ITEM_TRADER_SERVER_MEDIUM, ModBlocks.ITEM_TRADER_SERVER_LARGE, ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("item_interface", () -> BlockEntityType.Builder.of(ItemInterfaceBlockEntity::new,
				//Vending Machine 1
				ModBlocks.VENDING_MACHINE,
				ModBlocks.VENDING_MACHINE_ORANGE,
				ModBlocks.VENDING_MACHINE_MAGENTA,
				ModBlocks.VENDING_MACHINE_LIGHTBLUE,
				ModBlocks.VENDING_MACHINE_YELLOW,
				ModBlocks.VENDING_MACHINE_LIME,
				ModBlocks.VENDING_MACHINE_PINK,
				ModBlocks.VENDING_MACHINE_GRAY,
				ModBlocks.VENDING_MACHINE_LIGHTGRAY,
				ModBlocks.VENDING_MACHINE_CYAN,
				ModBlocks.VENDING_MACHINE_PURPLE,
				ModBlocks.VENDING_MACHINE_BLUE,
				ModBlocks.VENDING_MACHINE_BROWN,
				ModBlocks.VENDING_MACHINE_GREEN,
				ModBlocks.VENDING_MACHINE_RED,
				ModBlocks.VENDING_MACHINE_BLACK,
				//Vending Machine 2
				ModBlocks.VENDING_MACHINE_LARGE,
				ModBlocks.VENDING_MACHINE_LARGE_ORANGE,
				ModBlocks.VENDING_MACHINE_LARGE_MAGENTA,
				ModBlocks.VENDING_MACHINE_LARGE_LIGHTBLUE,
				ModBlocks.VENDING_MACHINE_LARGE_YELLOW,
				ModBlocks.VENDING_MACHINE_LARGE_LIME,
				ModBlocks.VENDING_MACHINE_LARGE_PINK,
				ModBlocks.VENDING_MACHINE_LARGE_GRAY,
				ModBlocks.VENDING_MACHINE_LARGE_LIGHTGRAY,
				ModBlocks.VENDING_MACHINE_LARGE_CYAN,
				ModBlocks.VENDING_MACHINE_LARGE_PURPLE,
				ModBlocks.VENDING_MACHINE_LARGE_BLUE,
				ModBlocks.VENDING_MACHINE_LARGE_BROWN,
				ModBlocks.VENDING_MACHINE_LARGE_GREEN,
				ModBlocks.VENDING_MACHINE_LARGE_RED,
				ModBlocks.VENDING_MACHINE_LARGE_BLACK,
				//Armor Display
				ModBlocks.ARMOR_DISPLAY,
				//Freezer
				ModBlocks.FREEZER
			).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("trader_interface_item", () -> BlockEntityType.Builder.of(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("cash_register", () -> BlockEntityType.Builder.of(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("coin_mint", () -> BlockEntityType.Builder.of(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("ticket_machine", () -> BlockEntityType.Builder.of(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("paygate", () -> BlockEntityType.Builder.of(PaygateBlockEntity::new, ModBlocks.PAYGATE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("coin_jar", () -> BlockEntityType.Builder.of(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK, ModBlocks.COINJAR_BLUE).build(null));
		
	}
	
	//Item Trader
	@ObjectHolder("trader")
	public static final BlockEntityType<ItemTraderBlockEntity> ITEM_TRADER = null;
	//Armor variant of the trader
	public static final BlockEntityType<ArmorDisplayTraderBlockEntity> ARMOR_TRADER = null;
	//Freezer variant of the trader
	public static final BlockEntityType<FreezerTraderBlockEntity> FREEZER_TRADER = null;
	//Ticket variant of the trader
	public static final BlockEntityType<TicketTraderBlockEntity> TICKET_TRADER = null;
	
	//Universal Item Trader
	public static final BlockEntityType<UniversalItemTraderBlockEntity> UNIVERSAL_ITEM_TRADER = null;
	
	//Item Interface for multi-block traders
	public static final BlockEntityType<ItemInterfaceBlockEntity> ITEM_INTERFACE = null;
	
	public static final BlockEntityType<ItemTraderInterfaceBlockEntity> TRADER_INTERFACE_ITEM = null;
	
	//Cash Register
	public static final BlockEntityType<CashRegisterBlockEntity> CASH_REGISTER = null;
	
	//Coin Mint
	public static final BlockEntityType<CoinMintBlockEntity> COIN_MINT = null;
	//Ticket Machine
	public static final BlockEntityType<TicketMachineBlockEntity> TICKET_MACHINE = null;
	
	//Paygate
	public static final BlockEntityType<PaygateBlockEntity> PAYGATE = null;
	
	//Coin Jars
	public static final BlockEntityType<CoinJarBlockEntity> COIN_JAR = null;
	
}
