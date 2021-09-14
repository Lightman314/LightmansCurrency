package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.containers.*;
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
	
	public static final MenuType<ATMContainer> ATM = register("atm", ATMContainer::new);
	
	public static final MenuType<MintContainer> MINT = register("coinmint", MintContainer::new);
	
	public static final MenuType<ItemTraderContainer> ITEMTRADER = register("item_trader", (IContainerFactory<ItemTraderContainer>)(windowId, playerInventory, data)->{
		
		ItemTraderBlockEntity blockEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderContainer(windowId, playerInventory, blockEntity);
		
	});
	public static final MenuType<ItemTraderStorageContainer> ITEMTRADERSTORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageContainer>)(windowId, playerInventory, data)->{
		
		ItemTraderBlockEntity blockEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderStorageContainer(windowId, playerInventory, blockEntity);
		
	});
	public static final MenuType<ItemTraderContainerCR> ITEMTRADERCR = register("item_trader_cr", (IContainerFactory<ItemTraderContainerCR>)(windowId, playerInventory, data)->{
		
		ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		CashRegisterBlockEntity registerEntity = (CashRegisterBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderContainerCR(windowId, playerInventory, traderEntity, registerEntity);
		
	});
	
	public static final MenuType<UniversalItemTraderContainer> UNIVERSAL_ITEMTRADER = register("universal_item_trader", (IContainerFactory<UniversalItemTraderContainer>)(windowId, playerInventory, data)->{
		
		return new UniversalItemTraderContainer(windowId, playerInventory, data.readUUID(), data.readNbt());
		
	});
	
	public static final MenuType<UniversalItemTraderStorageContainer> UNIVERSAL_ITEMTRADERSTORAGE = register("universal_item_trader_storage", (IContainerFactory<UniversalItemTraderStorageContainer>)(windowId, playerInventory, data)->{
		
		return new UniversalItemTraderStorageContainer(windowId, playerInventory, data.readUUID(), data.readNbt());
		
	});
	
	public static final MenuType<WalletContainer> WALLET = register("wallet", (IContainerFactory<WalletContainer>)(windowId, playerInventory, data) ->{
		
		return new WalletContainer(windowId, playerInventory, data.readInt());
		
	});
	
	public static final MenuType<PaygateContainer> PAYGATE = register("paygate", (IContainerFactory<PaygateContainer>)(windowId, playerInventory, data)->{
		
		PaygateBlockEntity blockEntity = (PaygateBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new PaygateContainer(windowId, playerInventory, blockEntity);
	});
	
	public static final MenuType<TicketMachineContainer> TICKET_MACHINE = register("ticket_machine", TicketMachineContainer::new);
	
	
	public static final MenuType<ItemEditContainer> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditContainer>)(windowId, playerInventory, data)->{
		ItemTraderBlockEntity blockEntity = (ItemTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemEditContainer(windowId, playerInventory, () -> blockEntity, data.readInt());
	});
	
	public static final MenuType<UniversalItemEditContainer> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditContainer>)(windowId, playerInventory, data)->{
		
		data.readUUID();
		UniversalItemTraderData traderData = new UniversalItemTraderData(data.readNbt());
		return new UniversalItemEditContainer(windowId, playerInventory, () -> traderData, data.readInt());
		
	});
	
	//Code
	private static <T extends AbstractContainerMenu> MenuType<T> register(String key, MenuType.MenuSupplier<T> factory)
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
