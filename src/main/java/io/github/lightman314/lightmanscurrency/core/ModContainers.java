package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.containers.*;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer.UniversalItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer.ItemTraderContainerCR;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer.ItemTraderContainerUniversal;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer.ItemTraderStorageContainerUniversal;
import io.github.lightman314.lightmanscurrency.tileentity.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
	
	private static final List<ContainerType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final ContainerType<ATMContainer> ATM = register("atm",ATMContainer::new);
	
	public static final ContainerType<MintContainer> MINT = register("coinmint", (IContainerFactory<MintContainer>)(windowId, playerInventory, data)->{
		
		CoinMintTileEntity tileEntity = (CoinMintTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new MintContainer(windowId, playerInventory, tileEntity);

	});
	
	public static final ContainerType<ItemTraderContainer> ITEM_TRADER = register("item_trader", (IContainerFactory<ItemTraderContainer>)(windowId, playerInventory, data)->{
		return new ItemTraderContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<ItemTraderContainerCR> ITEM_TRADER_CR = register("item_trader_cr", (IContainerFactory<ItemTraderContainerCR>)(windowId, playerInventory, data)->{
		BlockPos traderPos = data.readBlockPos();
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new ItemTraderContainerCR(windowId, playerInventory, traderPos, registerEntity);
	});
	public static final ContainerType<ItemTraderContainerUniversal> ITEM_TRADER_UNIVERSAL = register("universal_item_trader", (IContainerFactory<ItemTraderContainerUniversal>)(windowId, playerInventory, data)->{
		return new ItemTraderContainerUniversal(windowId, playerInventory, data.readUniqueId());
	});
	
	public static final ContainerType<ItemTraderStorageContainer> ITEM_TRADER_STORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageContainer>)(windowId, playerInventory, data)->{
		return new ItemTraderStorageContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<ItemTraderStorageContainerUniversal> ITEM_TRADER_STORAGE_UNIVERSAL = register("universal_item_trader_storage", (IContainerFactory<ItemTraderStorageContainerUniversal>)(windowId, playerInventory, data)->{
		return new ItemTraderStorageContainerUniversal(windowId, playerInventory, data.readUniqueId());
	});
	
	public static final ContainerType<WalletContainer> WALLET = register("wallet", (IContainerFactory<WalletContainer>)(windowId, playerInventory, data) ->{
		return new WalletContainer(windowId, playerInventory, data.readInt());
	});
	
	public static final ContainerType<PaygateContainer> PAYGATE = register("paygate", (IContainerFactory<PaygateContainer>)(windowId, playerInventory, data)->{
		PaygateTileEntity tileEntity = (PaygateTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new PaygateContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final ContainerType<TicketMachineContainer> TICKET_MACHINE = register("ticket_machine", (IContainerFactory<TicketMachineContainer>)(windowId, playerInventory, data)->{
		TicketMachineTileEntity tileEntity = (TicketMachineTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new TicketMachineContainer(windowId, playerInventory, tileEntity);
	});
	
	
	public static final ContainerType<ItemEditContainer> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditContainer>)(windowId, playerInventory, data)->{
		return new ItemEditContainer(windowId, playerInventory, data.readBlockPos(), data.readInt());
	});
	public static final ContainerType<UniversalItemEditContainer> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditContainer>)(windowId, playerInventory, data)->{
		return new UniversalItemEditContainer(windowId, playerInventory, data.readUniqueId(), data.readInt());
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
