package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.menus.*;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuAllUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.TraderStorageMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.menus.wallet.WalletMenu;
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
	
	public static final MenuType<WalletMenu> WALLET = register("wallet", (IContainerFactory<WalletMenu>)(id, playerInventory, data) ->{
		return new WalletMenu(id, playerInventory, data.readInt());
	});
	
	public static final MenuType<WalletBankMenu> WALLET_BANK = register("wallet_bank", (IContainerFactory<WalletBankMenu>)(id, playerInventory, data) ->{
		return new WalletBankMenu(id, playerInventory, data.readInt());
	});
	
	public static final MenuType<TicketMachineMenu> TICKET_MACHINE = register("ticket_machine", (IContainerFactory<TicketMachineMenu>)(id, playerInventory, data)->{
		TicketMachineBlockEntity tileEntity = (TicketMachineBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new TicketMachineMenu(id, playerInventory, tileEntity);
	});
	
	public static final MenuType<TraderInterfaceMenu> TRADER_INTERFACE = register("trader_interface", (IContainerFactory<TraderInterfaceMenu>)(id, playerInventory, data) ->{
		TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new TraderInterfaceMenu(id, playerInventory, blockEntity);
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
