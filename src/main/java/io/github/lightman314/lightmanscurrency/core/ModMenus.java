package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.menus.*;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuAllUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.TraderStorageMenuUniversal;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
	
	private static final List<MenuType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final MenuType<ATMMenu> ATM = register("atm", (id, inventory, data) -> new ATMMenu(id, inventory));
	
	public static final MenuType<MintMenu> MINT = register("coinmint", (IContainerFactory<MintMenu>)(id, playerInventory, data)->{
		
		CoinMintBlockEntity tileEntity = (CoinMintBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new MintMenu(id, playerInventory, tileEntity);

	});
	
	//Any Trader
	public static final MenuType<TraderMenu> TRADER = register("trader", (IContainerFactory<TraderMenu>)(id, playerInventory,data) ->{
		return new TraderMenu(id, playerInventory, data.readBlockPos());
	});
	public static final MenuType<TraderMenuUniversal> TRADER_UNIVERSAL = register("trader_universal", (IContainerFactory<TraderMenuUniversal>)(id, playerInventory,data) ->{
		return new TraderMenuUniversal(id, playerInventory, data.readUUID());
	});
	public static final MenuType<TraderMenuAllUniversal> TRADER_UNIVERSAL_ALL = register("trader_universal_all", (IContainerFactory<TraderMenuAllUniversal>)(id, playerInventory,data) ->{
		return new TraderMenuAllUniversal(id, playerInventory);
	});
	
	//Any Trader Storage
	public static final MenuType<TraderStorageMenu> TRADER_STORAGE = register("trader_storage", (IContainerFactory<TraderStorageMenu>)(id, playerInventory,data) ->{
		return new TraderStorageMenu(id, playerInventory, data.readBlockPos());
	});
	public static final MenuType<TraderStorageMenuUniversal> TRADER_STORAGE_UNIVERSAL = register("trader_storage_universal", (IContainerFactory<TraderStorageMenuUniversal>)(id, playerInventory,data) ->{
		return new TraderStorageMenuUniversal(id, playerInventory, data.readUUID());
	});
	
	//Item Trader (Deprecated as of v1.1.0.0)
	/*public static final MenuType<ItemTraderMenu> ITEM_TRADER = register("item_trader", (IContainerFactory<ItemTraderMenu>)(id, playerInventory, data)->{
		return new ItemTraderMenu(id, playerInventory, data.readBlockPos());
	});
	public static final MenuType<ItemTraderMenuCR> ITEM_TRADER_CR = register("item_trader_cr", (IContainerFactory<ItemTraderMenuCR>)(id, playerInventory, data)->{
		BlockPos traderPos = data.readBlockPos();
		CashRegisterBlockEntity registerEntity = (CashRegisterBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderMenuCR(id, playerInventory, traderPos, registerEntity);
	});
	public static final MenuType<ItemTraderMenuUniversal> ITEM_TRADER_UNIVERSAL = register("universal_item_trader", (IContainerFactory<ItemTraderMenuUniversal>)(id, playerInventory, data)->{
		return new ItemTraderMenuUniversal(id, playerInventory, data.readUUID());
	});
	
	public static final MenuType<ItemTraderStorageMenu> ITEM_TRADER_STORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageMenu>)(id, playerInventory, data)->{
		return new ItemTraderStorageMenu(id, playerInventory, data.readBlockPos());
	});
	public static final MenuType<ItemTraderStorageMenuUniversal> ITEM_TRADER_STORAGE_UNIVERSAL = register("universal_item_trader_storage", (IContainerFactory<ItemTraderStorageMenuUniversal>)(id, playerInventory, data)->{
		return new ItemTraderStorageMenuUniversal(id, playerInventory, data.readUUID());
	});*/
	
	public static final MenuType<WalletMenu> WALLET = register("wallet", (IContainerFactory<WalletMenu>)(id, playerInventory, data) ->{
		return new WalletMenu(id, playerInventory, data.readInt());
	});
	
	public static final MenuType<PaygateMenu> PAYGATE = register("paygate", (IContainerFactory<PaygateMenu>)(id, playerInventory, data)->{
		PaygateBlockEntity tileEntity = (PaygateBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new PaygateMenu(id, playerInventory, tileEntity);
	});
	
	public static final MenuType<TicketMachineMenu> TICKET_MACHINE = register("ticket_machine", (IContainerFactory<TicketMachineMenu>)(id, playerInventory, data)->{
		TicketMachineBlockEntity tileEntity = (TicketMachineBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new TicketMachineMenu(id, playerInventory, tileEntity);
	});
	
	/*public static final MenuType<ItemEditMenu> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditMenu>)(id, playerInventory, data)->{
		return new ItemEditMenu(id, playerInventory, data.readBlockPos(), data.readInt());
	});
	
	public static final MenuType<UniversalItemEditMenu> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditMenu>)(id, playerInventory, data)->{
		return new UniversalItemEditMenu(id, playerInventory, data.readUUID(), data.readInt());
	});*/
	
	public static final MenuType<ItemInterfaceMenu> ITEM_INTERFACE = register("item_interface", (IContainerFactory<ItemInterfaceMenu>)(id, playerInventory, data) ->{
		UniversalItemTraderInterfaceBlockEntity blockEntity = (UniversalItemTraderInterfaceBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemInterfaceMenu(id, playerInventory, blockEntity);
	});
	
	//Code
	private static <T extends AbstractContainerMenu> MenuType<T> register(String key, IContainerFactory<T> factory)
	{
		MenuType<T> type = new MenuType<>(factory);
		type.setRegistryName(key);
		CONTAINER_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<MenuType<?>> event)
	{
		CONTAINER_TYPES.forEach(type -> event.getRegistry().register(type));
		CONTAINER_TYPES.clear();
	}
	
}
