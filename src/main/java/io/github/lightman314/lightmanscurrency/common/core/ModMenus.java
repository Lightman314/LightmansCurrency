package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.menus.*;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu.*;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;

public class ModMenus {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		ATM = ModRegistries.MENUS.register("atm", () -> new ContainerType<>((IContainerFactory<ATMMenu>)(id, inventory, data) -> new ATMMenu(id, inventory)));
		
		MINT = ModRegistries.MENUS.register("coinmint", () -> new ContainerType<>((IContainerFactory<MintMenu>)(id, playerInventory, data)->{
			CoinMintBlockEntity tileEntity = (CoinMintBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new MintMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER = ModRegistries.MENUS.register("trader", () -> new ContainerType<>((IContainerFactory<TraderMenu>)(id, playerInventory, data) -> new TraderMenu(id, playerInventory, data.readLong())));
		
		TRADER_BLOCK = ModRegistries.MENUS.register("trader_block", () -> new ContainerType<>((IContainerFactory<TraderMenuBlockSource>)(id, playerInventory, data) -> new TraderMenuBlockSource(id, playerInventory, data.readBlockPos())));
		
		TRADER_NETWORK_ALL = ModRegistries.MENUS.register("trader_network_all", () -> new ContainerType<>((IContainerFactory<TraderMenuAllNetwork>)(id, playerInventory,data) -> new TraderMenuAllNetwork(id, playerInventory)));
		
		TRADER_STORAGE = ModRegistries.MENUS.register("trader_storage", () -> new ContainerType<>((IContainerFactory<TraderStorageMenu>)(id, playerInventory, data) -> new TraderStorageMenu(id, playerInventory, data.readLong())));
		
		WALLET = ModRegistries.MENUS.register("wallet", () -> new ContainerType<>((IContainerFactory<WalletMenu>)(id, playerInventory, data) -> new WalletMenu(id, playerInventory, data.readInt())));
		
		WALLET_BANK = ModRegistries.MENUS.register("wallet_bank", () -> new ContainerType<>((IContainerFactory<WalletBankMenu>)(id, playerInventory, data) -> new WalletBankMenu(id, playerInventory, data.readInt())));
		
		TICKET_MACHINE = ModRegistries.MENUS.register("ticket_machine", () -> new ContainerType<>((IContainerFactory<TicketMachineMenu>)(id, playerInventory, data)->{
			TicketMachineBlockEntity tileEntity = (TicketMachineBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TicketMachineMenu(id, playerInventory, tileEntity);
		}));
		
		TRADER_INTERFACE = ModRegistries.MENUS.register("trader_interface", () -> new ContainerType<>((IContainerFactory<TraderInterfaceMenu>)(id, playerInventory, data) ->{
			TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
			return new TraderInterfaceMenu(id, playerInventory, blockEntity);
		}));
		
		TRADER_RECOVERY = ModRegistries.MENUS.register("trader_recovery", () -> new ContainerType<>((IContainerFactory<TraderRecoveryMenu>)(id, playerInventory, data) -> new TraderRecoveryMenu(id, playerInventory)));

		PLAYER_TRADE = ModRegistries.MENUS.register("player_trading", () -> new ContainerType<>((IContainerFactory<PlayerTradeMenu>)(id, playerInventory, data) -> new PlayerTradeMenu(id, playerInventory, data.readInt(), ClientPlayerTrade.decode(data))));

	}
	
	public static final RegistryObject<ContainerType<ATMMenu>> ATM;
	
	public static final RegistryObject<ContainerType<MintMenu>> MINT;
	
	//Any Trader
	public static final RegistryObject<ContainerType<TraderMenu>> TRADER;
	public static final RegistryObject<ContainerType<TraderMenuBlockSource>> TRADER_BLOCK;
	public static final RegistryObject<ContainerType<TraderMenuAllNetwork>> TRADER_NETWORK_ALL;
	
	//Any Trader Storage
	public static final RegistryObject<ContainerType<TraderStorageMenu>> TRADER_STORAGE;
	
	public static final RegistryObject<ContainerType<WalletMenu>> WALLET;
	public static final RegistryObject<ContainerType<WalletBankMenu>> WALLET_BANK;
	
	public static final RegistryObject<ContainerType<TicketMachineMenu>> TICKET_MACHINE;
	
	public static final RegistryObject<ContainerType<TraderInterfaceMenu>> TRADER_INTERFACE;
	
	public static final RegistryObject<ContainerType<TraderRecoveryMenu>> TRADER_RECOVERY;

	public static final RegistryObject<ContainerType<PlayerTradeMenu>> PLAYER_TRADE;
	
}