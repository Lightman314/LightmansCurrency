package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.menus.*;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu.*;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {

		ATM = ModRegistries.MENUS.register("atm", () -> CreateType((IContainerFactory<ATMMenu>)(id, inventory, data) -> new ATMMenu(id, inventory)));
		
		MINT = ModRegistries.MENUS.register("coinmint", () -> CreateType((IContainerFactory<MintMenu>)(id, playerInventory, data)->{
			CoinMintBlockEntity tileEntity = (CoinMintBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new MintMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER = ModRegistries.MENUS.register("trader", () -> CreateType((IContainerFactory<TraderMenu>)(id, playerInventory,data) -> new TraderMenu(id, playerInventory, data.readLong())));
		
		TRADER_BLOCK = ModRegistries.MENUS.register("trader_block", () -> CreateType((IContainerFactory<TraderMenuBlockSource>)(id, playerInventory, data) -> new TraderMenuBlockSource(id, playerInventory, data.readBlockPos())));
		
		TRADER_NETWORK_ALL = ModRegistries.MENUS.register("trader_network_all", () -> CreateType((IContainerFactory<TraderMenuAllNetwork>)(id, playerInventory,data) -> new TraderMenuAllNetwork(id, playerInventory)));

		SLOT_MACHINE = ModRegistries.MENUS.register("slot_machine", () -> CreateType((IContainerFactory<SlotMachineMenu>)(id, playerInventory, data) -> new SlotMachineMenu(id, playerInventory, data.readLong())));

		TRADER_STORAGE = ModRegistries.MENUS.register("trader_storage", () -> CreateType((IContainerFactory<TraderStorageMenu>)(id, playerInventory,data) -> new TraderStorageMenu(id, playerInventory, data.readLong())));
		
		WALLET = ModRegistries.MENUS.register("wallet", () -> CreateType((IContainerFactory<WalletMenu>)(id, playerInventory, data) -> new WalletMenu(id, playerInventory, data.readInt())));
		
		WALLET_BANK = ModRegistries.MENUS.register("wallet_bank", () -> CreateType((IContainerFactory<WalletBankMenu>)(id, playerInventory, data) -> new WalletBankMenu(id, playerInventory, data.readInt())));
		
		TICKET_MACHINE = ModRegistries.MENUS.register("ticket_machine", () -> CreateType((IContainerFactory<TicketMachineMenu>)(id, playerInventory, data)->{
			TicketMachineBlockEntity tileEntity = (TicketMachineBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TicketMachineMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER_INTERFACE = ModRegistries.MENUS.register("trader_interface", () -> CreateType((IContainerFactory<TraderInterfaceMenu>)(id, playerInventory, data) ->{
			TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TraderInterfaceMenu(id, playerInventory, blockEntity);
		}));
		
		TRADER_RECOVERY = ModRegistries.MENUS.register("trader_recovery", () -> CreateType((IContainerFactory<TraderRecoveryMenu>)(id, playerInventory, data) -> new TraderRecoveryMenu(id, playerInventory)));

		PLAYER_TRADE = ModRegistries.MENUS.register("player_trading", () -> CreateType((IContainerFactory<PlayerTradeMenu>)(id, playerInventory, data) -> new PlayerTradeMenu(id, playerInventory, data.readInt(), ClientPlayerTrade.decode(data))));

		COIN_CHEST = ModRegistries.MENUS.register("coin_chest", () -> CreateType((IContainerFactory<CoinChestMenu>)(id,playerInventory,data) -> {
			CoinChestBlockEntity blockEntity = (CoinChestBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new CoinChestMenu(id, playerInventory, blockEntity);
		}));

	}
	
	public static final RegistryObject<MenuType<ATMMenu>> ATM;
	
	public static final RegistryObject<MenuType<MintMenu>> MINT;
	
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

	private static <T extends AbstractContainerMenu> MenuType<T> CreateType(MenuType.MenuSupplier<T> supplier){ return new MenuType<>(supplier, FeatureFlagSet.of()); }

}
