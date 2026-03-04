package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.*;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LightmansCurrency.MODID);

	//Item Trader
	public static final Supplier<BlockEntityType<ItemTraderBlockEntity>> ITEM_TRADER = register("item_trader",ItemTraderBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE));
	//Armor variant of the trader
	public static final Supplier<BlockEntityType<ArmorDisplayTraderBlockEntity>> ARMOR_TRADER = register("armor_trader",ArmorDisplayTraderBlockEntity::new,() -> toArray(ModBlocks.ARMOR_DISPLAY));
	//Freezer variant of the trader
	public static final Supplier<BlockEntityType<FreezerTraderBlockEntity>> FREEZER_TRADER = register("freezer_trader", FreezerTraderBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.FREEZER_TRADER_TYPE));
	//Ticket variant of the trader
	public static final Supplier<BlockEntityType<TicketTraderBlockEntity>> TICKET_TRADER = register("ticket_trader",TicketTraderBlockEntity::new,() -> toArray(ModBlocks.TICKET_KIOSK));
	//Book variant of the trader
	public static final Supplier<BlockEntityType<BookTraderBlockEntity>> BOOK_TRADER = register("book_trader",BookTraderBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.BOOKSHELF_TRADER_TYPE));
	//Slot Machine variant of the trader
	public static final Supplier<BlockEntityType<SlotMachineTraderBlockEntity>> SLOT_MACHINE_TRADER = register("slot_machine_trader",SlotMachineTraderBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.SLOT_MACHINE_TRADER_TYPE));
	public static final Supplier<BlockEntityType<GachaMachineBlockEntity>> GACHA_MACHINE = register("gacha_machine",GachaMachineBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.GACHA_MACHINE_TYPE));

    public static final Supplier<BlockEntityType<CapabilityInterfaceBlockEntity>> CAPABILITY_INTERFACE = register("capability_interface",CapabilityInterfaceBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntities(BlockEntityBlockHelper.CAPABILITY_INTERFACE_TYPE));

	//Trader Interface Terminal
	public static final Supplier<BlockEntityType<ItemTraderInterfaceBlockEntity>> TRADER_INTERFACE_ITEM = register("trader_interface_item",ItemTraderInterfaceBlockEntity::new,() -> toArray(ModBlocks.ITEM_TRADER_INTERFACE));
	
	//Cash Register
	public static final Supplier<BlockEntityType<CashRegisterBlockEntity>> CASH_REGISTER = register("cash_register",CashRegisterBlockEntity::new,() -> toArray(ModBlocks.CASH_REGISTER));
	
	//Coin Mint
	public static final Supplier<BlockEntityType<CoinMintBlockEntity>> COIN_MINT = register("coin_mint",CoinMintBlockEntity::new,() -> toArray(ModBlocks.COIN_MINT));
	//Ticket Machine
	public static final Supplier<BlockEntityType<TicketStationBlockEntity>> TICKET_MACHINE = register("ticket_machine",TicketStationBlockEntity::new,() -> toArray(ModBlocks.TICKET_STATION));

	//Paygate
	public static final Supplier<BlockEntityType<PaygateBlockEntity>> PAYGATE = register("paygate",PaygateBlockEntity::new,() -> toArray(ModBlocks.PAYGATE));

	//Command Trader
	public static final Supplier<BlockEntityType<CommandTraderBlockEntity>> COMMAND_TRADER = register("command_trader",CommandTraderBlockEntity::new,() -> toArray(ModBlocks.COMMAND_TRADER));

	//Tax Block
	public static final Supplier<BlockEntityType<TaxBlockEntity>> TAX_BLOCK = register("tax_block",TaxBlockEntity::new,() -> toArray(ModBlocks.TAX_COLLECTOR));
	
	//Coin Jars
	public static final Supplier<BlockEntityType<CoinJarBlockEntity>> COIN_JAR = register("coin_jar",CoinJarBlockEntity::new,() -> toArray(ModBlocks.PIGGY_BANK.get(),ModBlocks.COINJAR_BLUE.get(),ModBlocks.SUS_JAR.get()));
	public static final Supplier<BlockEntityType<MoneyBagBlockEntity>> MONEY_BAG = register("money_bag",MoneyBagBlockEntity::new,() -> toArray(ModBlocks.MONEY_BAG.get()));

	//Auction Stand
	public static final Supplier<BlockEntityType<AuctionStandBlockEntity>> AUCTION_STAND = register("auction_stand",AuctionStandBlockEntity::new,() -> BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.AUCTION_STAND_TYPE));

	public static final Supplier<BlockEntityType<CoinChestBlockEntity>> COIN_CHEST = register("coin_chest",CoinChestBlockEntity::new,() -> toArray(ModBlocks.COIN_CHEST));


    public static Block[] toArray(Block... validBlocks) { return validBlocks; }
    public static Block[] toArray(Supplier<? extends Block> validBlock) { return toArray(validBlock.get()); }

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>,BlockEntityType<T>> register(String id,BlockEntityType.BlockEntitySupplier<T> factory,Supplier<Block[]> validBlocks)
    {
        return REGISTER.register(id,() -> BlockEntityType.Builder.of(factory,validBlocks.get()).build(null));
    }

}
