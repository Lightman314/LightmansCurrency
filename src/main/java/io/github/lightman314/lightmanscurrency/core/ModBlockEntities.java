package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;

public class ModBlockEntities {
	
	private static final List<BlockEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();
	
	public static final BlockEntityType<DummyBlockEntity> DUMMY = buildType("dummy", BlockEntityType.Builder.of(DummyBlockEntity::new));
	
	//Item Trader
	public static final BlockEntityType<ItemTraderBlockEntity> ITEM_TRADER = buildType("trader", BlockEntityType.Builder.of(ItemTraderBlockEntity::new,
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
			
	));
	//Armor variant of the trader
	public static final BlockEntityType<ArmorDisplayTraderBlockEntity> ARMOR_TRADER = buildType("armor_trader", BlockEntityType.Builder.of(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY));
	//Freezer variant of the trader
	public static final BlockEntityType<FreezerTraderBlockEntity> FREEZER_TRADER = buildType("freezer_trader", BlockEntityType.Builder.of(FreezerTraderBlockEntity::new, ModBlocks.FREEZER));
	//Ticket variant of the trader
	public static final BlockEntityType<TicketTraderBlockEntity> TICKET_TRADER = buildType("ticket_trader", BlockEntityType.Builder.of(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK));
	
	//Universal Item Trader
	public static final BlockEntityType<UniversalItemTraderBlockEntity> UNIVERSAL_ITEM_TRADER = buildType("universal_item_trader", BlockEntityType.Builder.of(UniversalItemTraderBlockEntity::new, ModBlocks.ITEM_TRADER_SERVER_SMALL, ModBlocks.ITEM_TRADER_SERVER_MEDIUM, ModBlocks.ITEM_TRADER_SERVER_LARGE, ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE));
	
	//Item Interface for multi-block traders
	public static final BlockEntityType<ItemInterfaceBlockEntity> ITEM_INTERFACE = buildType("item_interface", BlockEntityType.Builder.of(ItemInterfaceBlockEntity::new,
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
			
	));
	
	public static final BlockEntityType<ItemTraderInterfaceBlockEntity> TRADER_INTERFACE_ITEM = buildType("trader_interface_item", BlockEntityType.Builder.of(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE));
	
	//Cash Register
	public static final BlockEntityType<CashRegisterBlockEntity> CASH_REGISTER = buildType("cash_register", BlockEntityType.Builder.of(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER));
	
	//Coin Mint
	public static final BlockEntityType<CoinMintBlockEntity> COIN_MINT = buildType("coin_mint",BlockEntityType.Builder.of(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT));
	//Ticket Machine
	public static final BlockEntityType<TicketMachineBlockEntity> TICKET_MACHINE = buildType("ticket_machine", BlockEntityType.Builder.of(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE));
	
	//Paygate
	public static final BlockEntityType<PaygateBlockEntity> PAYGATE = buildType("paygate", BlockEntityType.Builder.of(PaygateBlockEntity::new, ModBlocks.PAYGATE));
	
	//Coin Jars
	public static final BlockEntityType<CoinJarBlockEntity> COIN_JAR = buildType("coin_jar", BlockEntityType.Builder.of(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK));
	
	//Code
	private static <T extends BlockEntity> BlockEntityType<T> buildType(String id, BlockEntityType.Builder<T> builder)
	{
		BlockEntityType<T> type = builder.build(null);
		type.setRegistryName(LightmansCurrency.MODID,id);
		TILE_ENTITY_TYPES.add(type);
		return type;
	}
	
	//@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<BlockEntityType<?>> event)
	{
		TILE_ENTITY_TYPES.forEach(type -> event.getRegistry().register(type));
		TILE_ENTITY_TYPES.clear();
	}
	
	
	
}
