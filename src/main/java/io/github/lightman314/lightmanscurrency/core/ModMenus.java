package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.menus.*;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuAllUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu.TraderMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.TraderStorageMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.blockentity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ATM = ModRegistries.MENUS.register("atm", () -> new MenuType<>((IContainerFactory<ATMMenu>)(id, inventory, data) -> new ATMMenu(id, inventory)));
		
		MINT = ModRegistries.MENUS.register("coinmint", () -> new MenuType<>((IContainerFactory<MintMenu>)(id, playerInventory, data)->{
			CoinMintBlockEntity tileEntity = (CoinMintBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new MintMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER = ModRegistries.MENUS.register("trader", () -> new MenuType<>((IContainerFactory<TraderMenu>)(id, playerInventory,data) ->{
			return new TraderMenu(id, playerInventory, data.readBlockPos());
		}));
		
		TRADER_UNIVERSAL = ModRegistries.MENUS.register("trader_universal", () -> new MenuType<>((IContainerFactory<TraderMenuUniversal>)(id, playerInventory,data) ->{
			return new TraderMenuUniversal(id, playerInventory, data.readUUID());
		}));
		
		TRADER_UNIVERSAL_ALL = ModRegistries.MENUS.register("trader_universal_all", () -> new MenuType<>((IContainerFactory<TraderMenuAllUniversal>)(id, playerInventory,data) ->{
			return new TraderMenuAllUniversal(id, playerInventory);
		}));
		
		TRADER_STORAGE = ModRegistries.MENUS.register("trader_storage", () -> new MenuType<>((IContainerFactory<TraderStorageMenu>)(id, playerInventory,data) ->{
			return new TraderStorageMenu(id, playerInventory, data.readBlockPos());
		}));
		
		TRADER_STORAGE_UNIVERSAL = ModRegistries.MENUS.register("trader_storage_universal", () -> new MenuType<>((IContainerFactory<TraderStorageMenuUniversal>)(id, playerInventory,data) ->{
			return new TraderStorageMenuUniversal(id, playerInventory, data.readUUID());
		}));
		
		WALLET = ModRegistries.MENUS.register("wallet", () -> new MenuType<>((IContainerFactory<WalletMenu>)(id, playerInventory, data) ->{
			return new WalletMenu(id, playerInventory, data.readInt());
		}));
		
		WALLET_BANK = ModRegistries.MENUS.register("wallet_bank", () -> new MenuType<>((IContainerFactory<WalletBankMenu>)(id, playerInventory, data) ->{
			return new WalletBankMenu(id, playerInventory, data.readInt());
		}));
		
		TICKET_MACHINE = ModRegistries.MENUS.register("ticket_machine", () -> new MenuType<>((IContainerFactory<TicketMachineMenu>)(id, playerInventory, data)->{
			TicketMachineBlockEntity tileEntity = (TicketMachineBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TicketMachineMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER_INTERFACE = ModRegistries.MENUS.register("trader_interface", () -> new MenuType<>((IContainerFactory<TraderInterfaceMenu>)(id, playerInventory, data) ->{
			TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TraderInterfaceMenu(id, playerInventory, blockEntity);
		}));
		
		TRADER_RECOVERY = ModRegistries.MENUS.register("trader_recovery", () -> new MenuType<>((IContainerFactory<TraderRecoveryMenu>)(id, playerInventory, data) -> {
			return new TraderRecoveryMenu(id, playerInventory);
		}));
		
	}
	
	public static final RegistryObject<MenuType<ATMMenu>> ATM;
	
	public static final RegistryObject<MenuType<MintMenu>> MINT;
	
	//Any Trader
	public static final RegistryObject<MenuType<TraderMenu>> TRADER;
	public static final RegistryObject<MenuType<TraderMenuUniversal>> TRADER_UNIVERSAL;
	public static final RegistryObject<MenuType<TraderMenuAllUniversal>> TRADER_UNIVERSAL_ALL;
	
	//Any Trader Storage
	public static final RegistryObject<MenuType<TraderStorageMenu>> TRADER_STORAGE;
	public static final RegistryObject<MenuType<TraderStorageMenuUniversal>> TRADER_STORAGE_UNIVERSAL;
	
	public static final RegistryObject<MenuType<WalletMenu>> WALLET;
	public static final RegistryObject<MenuType<WalletBankMenu>> WALLET_BANK;
	
	public static final RegistryObject<MenuType<TicketMachineMenu>> TICKET_MACHINE;
	
	public static final RegistryObject<MenuType<TraderInterfaceMenu>> TRADER_INTERFACE;
	
	public static final RegistryObject<MenuType<TraderRecoveryMenu>> TRADER_RECOVERY;
	
}
