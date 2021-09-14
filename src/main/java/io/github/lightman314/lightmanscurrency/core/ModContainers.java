package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.containers.*;
import io.github.lightman314.lightmanscurrency.tileentity.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
	
	private static final List<ContainerType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final ContainerType<ATMContainer> ATM = register("atm",ATMContainer::new);
	
	public static final ContainerType<MintContainer> MINT = register("coinmint", MintContainer::new);
	
	public static final ContainerType<ItemTraderContainer> ITEMTRADER = register("item_trader", (IContainerFactory<ItemTraderContainer>)(windowId, playerInventory, data)->{
		
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new ItemTraderContainer(windowId, playerInventory, tileEntity);
		
	});
	public static final ContainerType<ItemTraderStorageContainer> ITEMTRADERSTORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageContainer>)(windowId, playerInventory, data)->{
		
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new ItemTraderStorageContainer(windowId, playerInventory, tileEntity);
		
	});
	public static final ContainerType<ItemTraderContainerCR> ITEMTRADERCR = register("item_trader_cr", (IContainerFactory<ItemTraderContainerCR>)(windowId, playerInventory, data)->{
		
		ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new ItemTraderContainerCR(windowId, playerInventory, traderEntity, registerEntity);
		
	});
	
	public static final ContainerType<UniversalItemTraderContainer> UNIVERSAL_ITEMTRADER = register("universal_item_trader", (IContainerFactory<UniversalItemTraderContainer>)(windowId, playerInventory, data)->{
		
		return new UniversalItemTraderContainer(windowId, playerInventory, data.readUniqueId(), data.readCompoundTag());
		
	});
	
	public static final ContainerType<UniversalItemTraderStorageContainer> UNIVERSAL_ITEMTRADERSTORAGE = register("universal_item_trader_storage", (IContainerFactory<UniversalItemTraderStorageContainer>)(windowId, playerInventory, data)->{
		
		return new UniversalItemTraderStorageContainer(windowId, playerInventory, data.readUniqueId(), data.readCompoundTag());
		
	});
	
	public static final ContainerType<WalletContainer> WALLET = register("wallet", (IContainerFactory<WalletContainer>)(windowId, playerInventory, data) ->{
		
		return new WalletContainer(windowId, playerInventory, data.readInt());
		
	});
	
	public static final ContainerType<PaygateContainer> PAYGATE = register("paygate", (IContainerFactory<PaygateContainer>)(windowId, playerInventory, data)->{
		
		PaygateTileEntity tileEntity = (PaygateTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new PaygateContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final ContainerType<TicketMachineContainer> TICKET_MACHINE = register("ticket_machine", TicketMachineContainer::new);
	
	
	public static final ContainerType<ItemEditContainer> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditContainer>)(windowId, playerInventory, data)->{
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new ItemEditContainer(windowId, playerInventory, () -> tileEntity, data.readInt());
	});
	
	public static final ContainerType<UniversalItemEditContainer> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditContainer>)(windowId, playerInventory, data)->{
		
		data.readUniqueId();
		UniversalItemTraderData traderData = new UniversalItemTraderData(data.readCompoundTag());
		return new UniversalItemEditContainer(windowId, playerInventory, () -> traderData, data.readInt());
		
	});
	
	//Code
	private static <T extends Container> ContainerType<T> register(String key, ContainerType.IFactory<T> factory)
	{
		ContainerType<T> type = new ContainerType<>(factory);
		type.setRegistryName(key);
		CONTAINER_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<ContainerType<?>> event)
	{
		CONTAINER_TYPES.forEach(type -> event.getRegistry().register(type));
		CONTAINER_TYPES.clear();
	}
	
}
