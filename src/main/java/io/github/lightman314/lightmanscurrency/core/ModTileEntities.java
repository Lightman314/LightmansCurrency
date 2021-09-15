package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.tileentity.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {
	
	private static final List<TileEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();
	
	//Item Trader
	public static final TileEntityType<ItemTraderTileEntity> ITEM_TRADER = buildType("trader", TileEntityType.Builder.create(ItemTraderTileEntity::new,
			//Display Case
			ModBlocks.DISPLAY_CASE.block,
			//Vending Machine 1
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.WHITE),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.ORANGE),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.MAGENTA),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.LIGHTBLUE),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.YELLOW),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.LIME),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.PINK),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.GRAY),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.LIGHTGRAY),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.CYAN),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.PURPLE),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.BLUE),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.BROWN),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.GREEN),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.RED),
			ModBlocks.VENDING_MACHINE1.getBlock(Colors.BLACK),
			//Vending Machine 2
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.WHITE),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.ORANGE),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.MAGENTA),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.LIGHTBLUE),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.YELLOW),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.LIME),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.PINK),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.GRAY),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.LIGHTGRAY),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.CYAN),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.PURPLE),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.BLUE),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.BROWN),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.GREEN),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.RED),
			ModBlocks.VENDING_MACHINE2.getBlock(Colors.BLACK),
			//Wooden Shelves
			ModBlocks.SHELF.getBlock(WoodType.OAK),
			ModBlocks.SHELF.getBlock(WoodType.BIRCH),
			ModBlocks.SHELF.getBlock(WoodType.SPRUCE),
			ModBlocks.SHELF.getBlock(WoodType.JUNGLE),
			ModBlocks.SHELF.getBlock(WoodType.ACACIA),
			ModBlocks.SHELF.getBlock(WoodType.DARK_OAK),
			ModBlocks.SHELF.getBlock(WoodType.CRIMSON),
			ModBlocks.SHELF.getBlock(WoodType.WARPED),
			//Card Displays
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.OAK),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.BIRCH),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.SPRUCE),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.JUNGLE),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.ACACIA),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.DARK_OAK),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.CRIMSON),
			ModBlocks.CARD_DISPLAY.getBlock(WoodType.WARPED)
			
	));
	//Armor variant of the trader
	public static final TileEntityType<ArmorDisplayTraderTileEntity> ARMOR_TRADER = buildType("armor_trader", TileEntityType.Builder.create(ArmorDisplayTraderTileEntity::new, ModBlocks.ARMOR_DISPLAY.block));
	//Freezer variant of the trader
	public static final TileEntityType<FreezerTraderTileEntity> FREEZER_TRADER = buildType("freezer_trader", TileEntityType.Builder.create(FreezerTraderTileEntity::new, ModBlocks.FREEZER.block));
	
	//Universal Item Trader
	public static final TileEntityType<UniversalItemTraderTileEntity> UNIVERSAL_ITEM_TRADER = buildType("universal_item_trader", TileEntityType.Builder.create(UniversalItemTraderTileEntity::new, ModBlocks.ITEM_TRADER_SERVER_SMALL.block, ModBlocks.ITEM_TRADER_SERVER_MEDIUM.block, ModBlocks.ITEM_TRADER_SERVER_LARGE.block));
	
	//Cash Register
	public static final TileEntityType<CashRegisterTileEntity> CASH_REGISTER = buildType("cash_register", TileEntityType.Builder.create(CashRegisterTileEntity::new, ModBlocks.CASH_REGISTER.block));
	
	//Paygate
	public static final TileEntityType<PaygateTileEntity> PAYGATE = buildType("paygate", TileEntityType.Builder.create(PaygateTileEntity::new, ModBlocks.PAYGATE.block));
	
	//Coin Jars
	public static final TileEntityType<CoinJarTileEntity> COIN_JAR = buildType("coin_jar", TileEntityType.Builder.create(CoinJarTileEntity::new, ModBlocks.PIGGY_BANK.block));
	
	//Code
	private static <T extends TileEntity> TileEntityType<T> buildType(String id, TileEntityType.Builder<T> builder)
	{
		TileEntityType<T> type = builder.build(null);
		type.setRegistryName(LightmansCurrency.MODID,id);
		TILE_ENTITY_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<TileEntityType<?>> event)
	{
		TILE_ENTITY_TYPES.forEach(type -> event.getRegistry().register(type));
		TILE_ENTITY_TYPES.clear();
	}
	
	
	
}
