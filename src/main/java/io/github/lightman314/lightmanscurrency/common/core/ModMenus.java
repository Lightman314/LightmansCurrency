package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.menus.*;
import io.github.lightman314.lightmanscurrency.common.menus.gacha_machine.GachaMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.variant.BlockVariantSelectMenu;
import io.github.lightman314.lightmanscurrency.common.menus.variant.ItemVariantSelectMenu;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu.*;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

import java.util.function.Supplier;

public class ModMenus {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {

		ATM = ModRegistries.MENUS.register("atm", () -> CreateType((IContainerFactory<ATMMenu>)(id, inventory, data) -> new ATMMenu(id, inventory, MenuValidator.decode(data))));
		
		MINT = ModRegistries.MENUS.register("coinmint", () -> CreateType((IContainerFactory<MintMenu>)(id, inventory, data)->{
			CoinMintBlockEntity blockEntity = (CoinMintBlockEntity)inventory.player.level().getBlockEntity(data.readBlockPos());
			return new MintMenu(id, inventory, blockEntity);
		}));

		NETWORK_TERMINAL = ModRegistries.MENUS.register("network_terminal", () -> CreateType((IContainerFactory<TerminalMenu>)(id,inventory,data) -> new TerminalMenu(id, inventory, MenuValidator.decode(data))));
		
		TRADER = ModRegistries.MENUS.register("trader", () -> CreateType((IContainerFactory<TraderMenu>)(id, inventory,data) -> new TraderMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));
		
		TRADER_BLOCK = ModRegistries.MENUS.register("trader_block", () -> CreateType((IContainerFactory<TraderMenuBlockSource>)(id, inventory, data) -> new TraderMenuBlockSource(id, inventory, data.readBlockPos(), MenuValidator.decode(data))));
		
		TRADER_NETWORK_ALL = ModRegistries.MENUS.register("trader_network_all", () -> CreateType((IContainerFactory<TraderMenuAllNetwork>)(id, inventory,data) -> new TraderMenuAllNetwork(id, inventory, MenuValidator.decode(data))));

		SLOT_MACHINE = ModRegistries.MENUS.register("slot_machine", () -> CreateType((IContainerFactory<SlotMachineMenu>)(id, inventory, data) -> new SlotMachineMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));

		GACHA_MACHINE = ModRegistries.MENUS.register("gacha_machine", () -> CreateType((IContainerFactory<GachaMachineMenu>)(id,inventory,data) -> new GachaMachineMenu(id,inventory,data.readLong(), MenuValidator.decode(data))));

		TRADER_STORAGE = ModRegistries.MENUS.register("trader_storage", () -> CreateType((IContainerFactory<TraderStorageMenu>)(id, inventory,data) -> new TraderStorageMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));
		
		WALLET = ModRegistries.MENUS.register("wallet", () -> CreateType((IContainerFactory<WalletMenu>)(id, inventory, data) -> new WalletMenu(id, inventory, data.readInt())));
		
		WALLET_BANK = ModRegistries.MENUS.register("wallet_bank", () -> CreateType((IContainerFactory<WalletBankMenu>)(id, inventory, data) -> new WalletBankMenu(id, inventory, data.readInt())));
		
		TICKET_MACHINE = ModRegistries.MENUS.register("ticket_machine", () -> CreateType((IContainerFactory<TicketStationMenu>)(id, inventory, data)->{
			TicketStationBlockEntity blockEntity = (TicketStationBlockEntity)inventory.player.level().getBlockEntity(data.readBlockPos());
			return new TicketStationMenu(id, inventory, blockEntity);
		}));
		
		TRADER_INTERFACE = ModRegistries.MENUS.register("trader_interface", () -> CreateType((IContainerFactory<TraderInterfaceMenu>)(id, inventory, data) ->{
			TraderInterfaceBlockEntity blockEntity = (TraderInterfaceBlockEntity)inventory.player.level().getBlockEntity(data.readBlockPos());
			return new TraderInterfaceMenu(id, inventory, blockEntity);
		}));
		
		EJECTION_RECOVERY = ModRegistries.MENUS.register("trader_recovery", () -> CreateType((IContainerFactory<EjectionRecoveryMenu>)(id, inventory, data) -> new EjectionRecoveryMenu(id, inventory)));

		PLAYER_TRADE = ModRegistries.MENUS.register("player_trading", () -> CreateType((IContainerFactory<PlayerTradeMenu>)(id, inventory, data) -> new PlayerTradeMenu(id, inventory, data.readInt(), ClientPlayerTrade.decode(data))));

		COIN_CHEST = ModRegistries.MENUS.register("coin_chest", () -> CreateType((IContainerFactory<CoinChestMenu>)(id,inventory,data) -> {
			CoinChestBlockEntity blockEntity = (CoinChestBlockEntity)inventory.player.level().getBlockEntity(data.readBlockPos());
			return new CoinChestMenu(id, inventory, blockEntity);
		}));

		TAX_COLLECTOR = ModRegistries.MENUS.register("tax_collector", () -> CreateType((IContainerFactory<TaxCollectorMenu>)(id, inventory, data) -> new TaxCollectorMenu(id, inventory, data.readLong(), MenuValidator.decode(data))));

		TEAM_MANAGEMENT = ModRegistries.MENUS.register("team_management", () -> CreateType((IContainerFactory<TeamManagementMenu>)(id,inventory,data) -> new TeamManagementMenu(id,inventory)));

		NOTIFICATIONS = ModRegistries.MENUS.register("notifications", () -> CreateType((IContainerFactory<NotificationMenu>)(id,inventory,data) -> new NotificationMenu(id,inventory)));

		ATM_CARD = ModRegistries.MENUS.register("atm_card", () -> CreateType((IContainerFactory<ATMCardMenu>)(id, inventory, data) -> new ATMCardMenu(id,inventory,data.readInt())));

		VARIANT_SELECT_BLOCK = ModRegistries.MENUS.register("variant_select", () -> CreateType((IContainerFactory<BlockVariantSelectMenu>)(id, inventory, data) -> new BlockVariantSelectMenu(id,inventory,data.readBlockPos())));
		VARIANT_SELECT_ITEM = ModRegistries.MENUS.register("item_variant_select", () -> CreateType((IContainerFactory<ItemVariantSelectMenu>)(id, inventory, data) -> new ItemVariantSelectMenu(id,inventory)));

        ITEM_FILTER = ModRegistries.MENUS.register("item_trade_filter", () -> CreateType((IContainerFactory<ItemFilterMenu>)(id,inventory,data) -> new ItemFilterMenu(id,inventory,data.readInt())));

        TRANSACTION_REGISTER = ModRegistries.MENUS.register("transaction_register", () -> CreateType((IContainerFactory<TransactionRegisterMenu>)(id,inventory,data) -> new TransactionRegisterMenu(id,inventory,data.readInt())));

	}
	
	public static final Supplier<MenuType<ATMMenu>> ATM;
	
	public static final Supplier<MenuType<MintMenu>> MINT;

	public static final Supplier<MenuType<TerminalMenu>> NETWORK_TERMINAL;
	
	//Any Trader
	public static final Supplier<MenuType<TraderMenu>> TRADER;
	public static final Supplier<MenuType<TraderMenuBlockSource>> TRADER_BLOCK;
	public static final Supplier<MenuType<TraderMenuAllNetwork>> TRADER_NETWORK_ALL;

	//Slot Machine
	public static final Supplier<MenuType<SlotMachineMenu>> SLOT_MACHINE;
	//Gacha Machine
	public static final Supplier<MenuType<GachaMachineMenu>> GACHA_MACHINE;

	//Any Trader Storage
	public static final Supplier<MenuType<TraderStorageMenu>> TRADER_STORAGE;
	
	public static final Supplier<MenuType<WalletMenu>> WALLET;
	public static final Supplier<MenuType<WalletBankMenu>> WALLET_BANK;
	
	public static final Supplier<MenuType<TicketStationMenu>> TICKET_MACHINE;
	
	public static final Supplier<MenuType<TraderInterfaceMenu>> TRADER_INTERFACE;
	
	public static final Supplier<MenuType<EjectionRecoveryMenu>> EJECTION_RECOVERY;

	public static final Supplier<MenuType<PlayerTradeMenu>> PLAYER_TRADE;

	public static final Supplier<MenuType<CoinChestMenu>> COIN_CHEST;

	public static final Supplier<MenuType<TaxCollectorMenu>> TAX_COLLECTOR;

	public static final Supplier<MenuType<TeamManagementMenu>> TEAM_MANAGEMENT;

	public static final Supplier<MenuType<NotificationMenu>> NOTIFICATIONS;

	public static final Supplier<MenuType<ATMCardMenu>> ATM_CARD;

	public static final Supplier<MenuType<BlockVariantSelectMenu>> VARIANT_SELECT_BLOCK;
	public static final Supplier<MenuType<ItemVariantSelectMenu>> VARIANT_SELECT_ITEM;

    public static final Supplier<MenuType<ItemFilterMenu>> ITEM_FILTER;

    public static final Supplier<MenuType<TransactionRegisterMenu>> TRANSACTION_REGISTER;

	private static <T extends AbstractContainerMenu> MenuType<T> CreateType(MenuType.MenuSupplier<T> supplier){ return new MenuType<>(supplier, FeatureFlagSet.of()); }

}
