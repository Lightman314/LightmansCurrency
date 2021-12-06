package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.containers.*;
import io.github.lightman314.lightmanscurrency.tileentity.*;
import io.github.lightman314.lightmanscurrency.util.SafeTradingOffice;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
	
	private static final List<MenuType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final MenuType<PlayerInventoryWalletContainer> INVENTORY_WALLET = register("inventory_wallet", (id, inventory, data) -> new PlayerInventoryWalletContainer(id, inventory));
	
	public static final MenuType<ATMContainer> ATM = register("atm", (id, inventory, data) -> new ATMContainer(id, inventory));
	
	public static final MenuType<MintContainer> MINT = register("coinmint", (IContainerFactory<MintContainer>)(id, playerInventory, data)->{
		
		CoinMintTileEntity tileEntity = (CoinMintTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new MintContainer(id, playerInventory, tileEntity);

	});
	
	public static final MenuType<ItemTraderContainer> ITEMTRADER = register("item_trader", (IContainerFactory<ItemTraderContainer>)(id, playerInventory, data)->{
		
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderContainer(id, playerInventory, tileEntity);
		
	});
	public static final MenuType<ItemTraderStorageContainer> ITEMTRADERSTORAGE = register("item_trader_storage", (IContainerFactory<ItemTraderStorageContainer>)(id, playerInventory, data)->{
		
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderStorageContainer(id, playerInventory, tileEntity);
		
	});
	public static final MenuType<ItemTraderContainerCR> ITEMTRADERCR = register("item_trader_cr", (IContainerFactory<ItemTraderContainerCR>)(id, playerInventory, data)->{
		
		ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemTraderContainerCR(id, playerInventory, traderEntity, registerEntity);
		
	});
	
	public static final MenuType<UniversalItemTraderContainer> UNIVERSAL_ITEMTRADER = register("universal_item_trader", (IContainerFactory<UniversalItemTraderContainer>)(id, playerInventory, data)->{
		
		return new UniversalItemTraderContainer(id, playerInventory, data.readUUID());
		
	});
	
	public static final MenuType<UniversalItemTraderStorageContainer> UNIVERSAL_ITEMTRADERSTORAGE = register("universal_item_trader_storage", (IContainerFactory<UniversalItemTraderStorageContainer>)(id, playerInventory, data)->{
		
		return new UniversalItemTraderStorageContainer(id, playerInventory, data.readUUID());
		
	});
	
	public static final MenuType<WalletContainer> WALLET = register("wallet", (IContainerFactory<WalletContainer>)(id, playerInventory, data) ->{
		
		return new WalletContainer(id, playerInventory, data.readInt());
		
	});
	
	public static final MenuType<PaygateContainer> PAYGATE = register("paygate", (IContainerFactory<PaygateContainer>)(id, playerInventory, data)->{
		
		PaygateTileEntity tileEntity = (PaygateTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new PaygateContainer(id, playerInventory, tileEntity);
	});
	
	public static final MenuType<TicketMachineContainer> TICKET_MACHINE = register("ticket_machine", (IContainerFactory<TicketMachineContainer>)(id, playerInventory, data)->{
		TicketMachineTileEntity tileEntity = (TicketMachineTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new TicketMachineContainer(id, playerInventory, tileEntity);
		
	});
	
	
	public static final MenuType<ItemEditContainer> ITEM_EDIT = register("item_edit", (IContainerFactory<ItemEditContainer>)(id, playerInventory, data)->{
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new ItemEditContainer(id, playerInventory, () -> tileEntity, data.readInt());
	});
	
	public static final MenuType<UniversalItemEditContainer> UNIVERSAL_ITEM_EDIT = register("universal_item_edit", (IContainerFactory<UniversalItemEditContainer>)(id, playerInventory, data)->{
		UUID traderID = data.readUUID();
		return new UniversalItemEditContainer(id, playerInventory, () -> (UniversalItemTraderData)SafeTradingOffice.getData(traderID), data.readInt());
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
