package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.util.SafeTradingOffice;
import io.github.lightman314.lightmanscurrency.menus.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
	
	private static final List<MenuType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final MenuType<InventoryWalletMenu> INVENTORY_WALLET = register("inventory_wallet", (id, inventory, data) -> new InventoryWalletMenu(id, inventory));
	
	public static final MenuType<ATMMenu> ATM = register("atm", (id, inventory, data) -> new ATMMenu(id, inventory));
	
	public static final MenuType<MintMenu> MINT = register("coinmint", (IContainerFactory<MintMenu>)(id, playerInventory, data)->{
		
		CoinMintBlockEntity tileEntity = (CoinMintBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new MintMenu(id, playerInventory, tileEntity);

	});
	
	public static final MenuType<ItemTraderMenu> ITEMTRADER = register("item_trader", (IContainerFactory<ItemTraderMenu>)(id, playerInventory, data)->{
		
		ItemTraderBlockEntity tileEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderMenu(id, playerInventory, tileEntity);
		
	});
	public static final MenuType<ItemTraderStorageMenu> ITEMTRADERSTORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageMenu>)(id, playerInventory, data)->{
		
		ItemTraderBlockEntity tileEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderStorageMenu(id, playerInventory, tileEntity);
		
	});
	public static final MenuType<ItemTraderMenuCR> ITEMTRADERCR = register("item_trader_cr", (IContainerFactory<ItemTraderMenuCR>)(id, playerInventory, data)->{
		
		ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		CashRegisterBlockEntity registerEntity = (CashRegisterBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderMenuCR(id, playerInventory, traderEntity, registerEntity);
		
	});
	
	public static final MenuType<UniversalItemTraderMenu> UNIVERSAL_ITEMTRADER = register("universal_item_trader", (IContainerFactory<UniversalItemTraderMenu>)(id, playerInventory, data)->{
		
		return new UniversalItemTraderMenu(id, playerInventory, data.readUUID());
		
	});
	
	public static final MenuType<UniversalItemTraderStorageMenu> UNIVERSAL_ITEMTRADERSTORAGE = register("universal_item_trader_storage", (IContainerFactory<UniversalItemTraderStorageMenu>)(id, playerInventory, data)->{
		
		return new UniversalItemTraderStorageMenu(id, playerInventory, data.readUUID());
		
	});
	
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
	
	
	public static final MenuType<ItemEditMenu> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditMenu>)(id, playerInventory, data)->{
		ItemTraderBlockEntity tileEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemEditMenu(id, playerInventory, () -> tileEntity, data.readInt());
	});
	
	public static final MenuType<UniversalItemEditMenu> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditMenu>)(id, playerInventory, data)->{
		UUID traderID = data.readUUID();
		return new UniversalItemEditMenu(id, playerInventory, () -> (UniversalItemTraderData)SafeTradingOffice.getData(traderID), data.readInt());
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
