package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.blockentity.*;
import io.github.lightman314.lightmanscurrency.blockentity.old.item.*;
import io.github.lightman314.lightmanscurrency.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.trader.TicketTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.util.BlockEntityBlockHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("deprecation")
public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("item_trader", () -> BlockEntityType.Builder.of(ItemTraderBlockEntity::new,
				BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null));
		
		ARMOR_TRADER = ModRegistries.BLOCK_ENTITIES.register("armor_trader", () -> BlockEntityType.Builder.of(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY.get()).build(null));
		
		FREEZER_TRADER = ModRegistries.BLOCK_ENTITIES.register("freezer_trader", () -> BlockEntityType.Builder.of(FreezerTraderBlockEntity::new, ModBlocks.FREEZER.get()).build(null));
		
		TICKET_TRADER = ModRegistries.BLOCK_ENTITIES.register("ticket_trader", () -> BlockEntityType.Builder.of(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK.get()).build(null));
		
		CAPABILITY_INTERFACE = ModRegistries.BLOCK_ENTITIES.register("capability_interface", () -> BlockEntityType.Builder.of(CapabilityInterfaceBlockEntity::new,
				BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.CAPABILITY_INTERFACE_TYPE)).build(null));
		
		TRADER_INTERFACE_ITEM = ModRegistries.BLOCK_ENTITIES.register("trader_interface_item", () -> BlockEntityType.Builder.of(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE.get()).build(null));
		
		CASH_REGISTER = ModRegistries.BLOCK_ENTITIES.register("cash_register", () -> BlockEntityType.Builder.of(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER.get()).build(null));
		
		COIN_MINT = ModRegistries.BLOCK_ENTITIES.register("coin_mint", () -> BlockEntityType.Builder.of(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT.get()).build(null));
		
		TICKET_MACHINE = ModRegistries.BLOCK_ENTITIES.register("ticket_machine", () -> BlockEntityType.Builder.of(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE.get()).build(null));
		
		PAYGATE = ModRegistries.BLOCK_ENTITIES.register("paygate", () -> BlockEntityType.Builder.of(PaygateBlockEntity::new, ModBlocks.PAYGATE.get()).build(null));
		
		COIN_JAR = ModRegistries.BLOCK_ENTITIES.register("coin_jar", () -> BlockEntityType.Builder.of(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK.get(), ModBlocks.COINJAR_BLUE.get()).build(null));
		
		OLD_ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("trader", () -> BlockEntityType.Builder.of(OldItemTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null));
		
		UNIVERSAL_ITEM_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_item_trader", () -> BlockEntityType.Builder.of(UniversalItemTraderBlockEntity::new,
				ModBlocks.ITEM_NETWORK_TRADER_1.get(),
				ModBlocks.ITEM_NETWORK_TRADER_2.get(),
				ModBlocks.ITEM_NETWORK_TRADER_3.get(),
				ModBlocks.ITEM_NETWORK_TRADER_4.get()).build(null));
		
	}
	
	//Item Trader
	public static final RegistryObject<BlockEntityType<ItemTraderBlockEntity>> ITEM_TRADER;
	//Armor variant of the trader
	public static final RegistryObject<BlockEntityType<ArmorDisplayTraderBlockEntity>> ARMOR_TRADER;
	//Freezer variant of the trader
	public static final RegistryObject<BlockEntityType<FreezerTraderBlockEntity>> FREEZER_TRADER;
	//Ticket variant of the trader
	public static final RegistryObject<BlockEntityType<TicketTraderBlockEntity>> TICKET_TRADER;
	
	//Item Interface for multi-block traders
	public static final RegistryObject<BlockEntityType<CapabilityInterfaceBlockEntity>> CAPABILITY_INTERFACE;
	
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
	
	//Old Item Trader ID
	@Deprecated
	public static final RegistryObject<BlockEntityType<OldItemTraderBlockEntity>> OLD_ITEM_TRADER;
	//Network Item Trader (for conversion)
	@Deprecated
	public static final RegistryObject<BlockEntityType<UniversalItemTraderBlockEntity>> UNIVERSAL_ITEM_TRADER;
	
	
}