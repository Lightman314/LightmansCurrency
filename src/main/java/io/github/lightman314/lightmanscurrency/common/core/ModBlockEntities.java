package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.*;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("item_trader", () -> TileEntityType.Builder.of(ItemTraderBlockEntity::new,
				BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null));
		
		ARMOR_TRADER = ModRegistries.BLOCK_ENTITIES.register("armor_trader", () -> TileEntityType.Builder.of(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY.get()).build(null));

		FREEZER_TRADER = ModRegistries.BLOCK_ENTITIES.register("freezer_trader", () -> TileEntityType.Builder.of(FreezerTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.FREEZER_TRADER_TYPE)).build(null));
		
		TICKET_TRADER = ModRegistries.BLOCK_ENTITIES.register("ticket_trader", () -> TileEntityType.Builder.of(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK.get()).build(null));
		
		CAPABILITY_INTERFACE = ModRegistries.BLOCK_ENTITIES.register("capability_interface", () -> TileEntityType.Builder.of(CapabilityInterfaceBlockEntity::new,
				BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.CAPABILITY_INTERFACE_TYPE)).build(null));
		
		TRADER_INTERFACE_ITEM = ModRegistries.BLOCK_ENTITIES.register("trader_interface_item", () -> TileEntityType.Builder.of(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE.get()).build(null));
		
		CASH_REGISTER = ModRegistries.BLOCK_ENTITIES.register("cash_register", () -> TileEntityType.Builder.of(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER.get()).build(null));
		
		COIN_MINT = ModRegistries.BLOCK_ENTITIES.register("coin_mint", () -> TileEntityType.Builder.of(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT.get()).build(null));
		
		TICKET_MACHINE = ModRegistries.BLOCK_ENTITIES.register("ticket_machine", () -> TileEntityType.Builder.of(TicketMachineBlockEntity::new, ModBlocks.TICKET_STATION.get()).build(null));
		
		PAYGATE = ModRegistries.BLOCK_ENTITIES.register("paygate", () -> TileEntityType.Builder.of(PaygateBlockEntity::new, ModBlocks.PAYGATE.get()).build(null));
		
		COIN_JAR = ModRegistries.BLOCK_ENTITIES.register("coin_jar", () -> TileEntityType.Builder.of(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK.get(), ModBlocks.COINJAR_BLUE.get()).build(null));

		AUCTION_STAND = ModRegistries.BLOCK_ENTITIES.register("auction_stand", () -> TileEntityType.Builder.of(AuctionStandBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.AUCTION_STAND_TYPE)).build(null));

		OLD_ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("trader", () -> TileEntityType.Builder.of(ItemTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null));
		
		UNIVERSAL_ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_item_trader", () -> TileEntityType.Builder.of(ItemTraderBlockEntity::new,
				ModBlocks.ITEM_NETWORK_TRADER_1.get(),
				ModBlocks.ITEM_NETWORK_TRADER_2.get(),
				ModBlocks.ITEM_NETWORK_TRADER_3.get(),
				ModBlocks.ITEM_NETWORK_TRADER_4.get()).build(null));
		
	}
	
	//Item Trader
	public static final RegistryObject<TileEntityType<ItemTraderBlockEntity>> ITEM_TRADER;
	//Armor variant of the trader
	public static final RegistryObject<TileEntityType<ArmorDisplayTraderBlockEntity>> ARMOR_TRADER;
	//Freezer variant of the trader
	public static final RegistryObject<TileEntityType<FreezerTraderBlockEntity>> FREEZER_TRADER;
	//Ticket variant of the trader
	public static final RegistryObject<TileEntityType<TicketTraderBlockEntity>> TICKET_TRADER;
	
	//Item Interface for multi-block traders
	public static final RegistryObject<TileEntityType<CapabilityInterfaceBlockEntity>> CAPABILITY_INTERFACE;
	
	//Trader Interface Terminal
	public static final RegistryObject<TileEntityType<ItemTraderInterfaceBlockEntity>> TRADER_INTERFACE_ITEM;
	
	//Cash Register
	public static final RegistryObject<TileEntityType<CashRegisterBlockEntity>> CASH_REGISTER;
	
	//Coin Mint
	public static final RegistryObject<TileEntityType<CoinMintBlockEntity>> COIN_MINT;
	//Ticket Machine
	public static final RegistryObject<TileEntityType<TicketMachineBlockEntity>> TICKET_MACHINE;
	
	//Paygate
	public static final RegistryObject<TileEntityType<PaygateBlockEntity>> PAYGATE;
	
	//Coin Jars
	public static final RegistryObject<TileEntityType<CoinJarBlockEntity>> COIN_JAR;

	//Auction Stand
	public static final RegistryObject<TileEntityType<AuctionStandBlockEntity>> AUCTION_STAND;
	
	//Old Item Trader ID
	@Deprecated
	public static final RegistryObject<TileEntityType<ItemTraderBlockEntity>> OLD_ITEM_TRADER;
	//Network Item Trader (for conversion)
	@Deprecated
	public static final RegistryObject<TileEntityType<ItemTraderBlockEntity>> UNIVERSAL_ITEM_TRADER;
	
	
}