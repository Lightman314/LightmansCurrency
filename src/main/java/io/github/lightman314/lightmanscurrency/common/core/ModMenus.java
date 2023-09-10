package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.menus.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu.*;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ATM = ModRegistries.MENUS.register("atm", () -> new MenuType<>((IContainerFactory<ATMMenu>)(id, inventory, data) -> new ATMMenu(id, inventory, MenuValidator.decode(data))));
		
		MINT = ModRegistries.MENUS.register("coinmint", () -> new MenuType<>((IContainerFactory<MintMenu>)(id, inventory, data)->{
			CoinMintBlockEntity blockEntity = (CoinMintBlockEntity)inventory.player.level.getBlockEntity(data.readBlockPos());
			return new MintMenu(id, inventory, blockEntity);
		}));

		NETWORK_TERMINAL = ModRegistries.MENUS.register("network_terminal", () -> new MenuType<>((IContainerFactory<TerminalMenu>)(id,inventory,data) -> new TerminalMenu(id, inventory, MenuValidator.decode(data))));
		
		TRADER = ModRegistries.MENUS.register("trader", () -> new MenuType<>((IContainerFactory<TraderMenu>)(id, inventory, data) -> new TraderMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));
		
		TRADER_BLOCK = ModRegistries.MENUS.register("trader_block", () -> new MenuType<>((IContainerFactory<TraderMenuBlockSource>)(id, inventory, data) -> new TraderMenuBlockSource(id, inventory, data.readBlockPos(), MenuValidator.decode(data))));
		
		TRADER_NETWORK_ALL = ModRegistries.MENUS.register("trader_network_all", () -> new MenuType<>((IContainerFactory<TraderMenuAllNetwork>)(id, inventory,data) -> new TraderMenuAllNetwork(id, inventory, MenuValidator.decode(data))));

		SLOT_MACHINE = ModRegistries.MENUS.register("slot_machine", () -> new MenuType<>((IContainerFactory<SlotMachineMenu>)(id, inventory, data) -> new SlotMachineMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));

		TRADER_STORAGE = ModRegistries.MENUS.register("trader_storage", () -> new MenuType<>((IContainerFactory<TraderStorageMenu>)(id, inventory, data) -> new TraderStorageMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));
		
		WALLET = ModRegistries.MENUS.register("wallet", () -> new MenuType<>((IContainerFactory<WalletMenu>)(id, inventory, data) -> new WalletMenu(id, inventory, data.readInt())));
		
		WALLET_BANK = ModRegistries.MENUS.register("wallet_bank", () -> new MenuType<>((IContainerFactory<WalletBankMenu>)(id, inventory, data) -> new WalletBankMenu(id, inventory, data.readInt())));
		
		TICKET_MACHINE = ModRegistries.MENUS.register("ticket_machine", () -> new MenuType<>((IContainerFactory<TicketMachineMenu>)(id, inventory, data)->{
			TicketMachineBlockEntity blockEntity = (TicketMachineBlockEntity)inventory.player.level.getBlockEntity(data.readBlockPos());
			return new TicketMachineMenu(id, inventory, blockEntity);
		}));
		
		TRADER_INTERFACE = ModRegistries.MENUS.register("trader_interface", () -> new MenuType<>((IContainerFactory<TraderInterfaceMenu>)(id, inventory, data) ->{
			TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)inventory.player.level.getBlockEntity(data.readBlockPos());
			return new TraderInterfaceMenu(id, inventory, blockEntity);
		}));
		
		TRADER_RECOVERY = ModRegistries.MENUS.register("trader_recovery", () -> new MenuType<>((IContainerFactory<TraderRecoveryMenu>)(id, inventory, data) -> new TraderRecoveryMenu(id, inventory)));

		PLAYER_TRADE = ModRegistries.MENUS.register("player_trading", () -> new MenuType<>((IContainerFactory<PlayerTradeMenu>)(id, inventory, data) -> new PlayerTradeMenu(id, inventory, data.readInt(), ClientPlayerTrade.decode(data))));

		COIN_CHEST = ModRegistries.MENUS.register("coin_chest", () -> new MenuType<>((IContainerFactory<CoinChestMenu>)(id,inventory,data) -> {
			CoinChestBlockEntity blockEntity = (CoinChestBlockEntity)inventory.player.level.getBlockEntity(data.readBlockPos());
			return new CoinChestMenu(id, inventory, blockEntity);
		}));

		TAX_COLLECTOR = ModRegistries.MENUS.register("tax_collector", () -> new MenuType<>((IContainerFactory<TaxCollectorMenu>)(id, inventory, data) -> new TaxCollectorMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));

	}
	
	public static final RegistryObject<MenuType<ATMMenu>> ATM;
	
	public static final RegistryObject<MenuType<MintMenu>> MINT;

	public static final RegistryObject<MenuType<TerminalMenu>> NETWORK_TERMINAL;
	
	//Any Trader
	public static final RegistryObject<MenuType<TraderMenu>> TRADER;
	public static final RegistryObject<MenuType<TraderMenuBlockSource>> TRADER_BLOCK;
	public static final RegistryObject<MenuType<TraderMenuAllNetwork>> TRADER_NETWORK_ALL;

	//Slot Machine
	public static final RegistryObject<MenuType<SlotMachineMenu>> SLOT_MACHINE;
	
	//Any Trader Storage
	public static final RegistryObject<MenuType<TraderStorageMenu>> TRADER_STORAGE;
	
	public static final RegistryObject<MenuType<WalletMenu>> WALLET;
	public static final RegistryObject<MenuType<WalletBankMenu>> WALLET_BANK;
	
	public static final RegistryObject<MenuType<TicketMachineMenu>> TICKET_MACHINE;
	
	public static final RegistryObject<MenuType<TraderInterfaceMenu>> TRADER_INTERFACE;
	
	public static final RegistryObject<MenuType<TraderRecoveryMenu>> TRADER_RECOVERY;

	public static final RegistryObject<MenuType<PlayerTradeMenu>> PLAYER_TRADE;

	public static final RegistryObject<MenuType<CoinChestMenu>> COIN_CHEST;

	public static final RegistryObject<MenuType<TaxCollectorMenu>> TAX_COLLECTOR;
	
}