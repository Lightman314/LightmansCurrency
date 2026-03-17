package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU,LightmansCurrency.MODID);
	
	public static final Supplier<MenuType<ATMMenu>> ATM = registerValidated("atm",ATMMenu::new);
	
	public static final Supplier<MenuType<MintMenu>> MINT = registerBE("coinmint",CoinMintBlockEntity.class,MintMenu::new);

	public static final Supplier<MenuType<TerminalMenu>> NETWORK_TERMINAL = registerValidated("network_terminal",TerminalMenu::new);
	
	//Any Trader
	public static final Supplier<MenuType<TraderMenu>> TRADER = registerTrader("trader",TraderMenu::new);
	public static final Supplier<MenuType<TraderMenuBlockSource>> TRADER_BLOCK = register("trader_block",(id, inventory, data) -> new TraderMenuBlockSource(id, inventory, data.readBlockPos(), MenuValidator.decode(data)));
	public static final Supplier<MenuType<TraderMenuAllNetwork>> TRADER_NETWORK_ALL = registerValidated("trader_network_all",TraderMenuAllNetwork::new);

	//Slot Machine
	public static final Supplier<MenuType<SlotMachineMenu>> SLOT_MACHINE = registerTrader("slot_machine",SlotMachineMenu::new);
	//Gacha Machine
	public static final Supplier<MenuType<GachaMachineMenu>> GACHA_MACHINE = registerTrader("gacha_machine",GachaMachineMenu::new);

	//Any Trader Storage
	public static final Supplier<MenuType<TraderStorageMenu>> TRADER_STORAGE = registerTrader("trader_storage",TraderStorageMenu::new);
	
	public static final Supplier<MenuType<WalletMenu>> WALLET = registerItem("wallet",WalletMenu::new);
	public static final Supplier<MenuType<WalletBankMenu>> WALLET_BANK = registerItem("wallet_bank",WalletBankMenu::new);
	
	public static final Supplier<MenuType<TicketStationMenu>> TICKET_MACHINE = registerBE("ticket_machine",TicketStationBlockEntity.class,TicketStationMenu::new);
	
	public static final Supplier<MenuType<TraderInterfaceMenu>> TRADER_INTERFACE = registerBE("trader_interface",TraderInterfaceBlockEntity.class,TraderInterfaceMenu::new);
	
	public static final Supplier<MenuType<EjectionRecoveryMenu>> EJECTION_RECOVERY = registerSimple("trader_recovery",EjectionRecoveryMenu::new);

	public static final Supplier<MenuType<PlayerTradeMenu>> PLAYER_TRADE = register("player_trading",(id, inventory, data) -> new PlayerTradeMenu(id, inventory, data.readInt(), ClientPlayerTrade.STREAM_CODEC.decode(data)));

	public static final Supplier<MenuType<CoinChestMenu>> COIN_CHEST = registerBE("coin_chest",CoinChestBlockEntity.class,CoinChestMenu::new);

	public static final Supplier<MenuType<TaxCollectorMenu>> TAX_COLLECTOR = registerTrader("tax_collector",TaxCollectorMenu::new);

	public static final Supplier<MenuType<TeamManagementMenu>> TEAM_MANAGEMENT = registerSimple("team_management",TeamManagementMenu::new);

	public static final Supplier<MenuType<NotificationMenu>> NOTIFICATIONS = registerSimple("notifications",NotificationMenu::new);

	public static final Supplier<MenuType<ATMCardMenu>> ATM_CARD = registerItem("atm_card",ATMCardMenu::new);

	public static final Supplier<MenuType<BlockVariantSelectMenu>> VARIANT_SELECT_BLOCK = register("variant_select",(window,inventory,data) -> new BlockVariantSelectMenu(window,inventory,data.readBlockPos()));
	public static final Supplier<MenuType<ItemVariantSelectMenu>> VARIANT_SELECT_ITEM = registerSimple("item_variant_select",ItemVariantSelectMenu::new);

    public static final Supplier<MenuType<ItemFilterMenu>> ITEM_FILTER = registerItem("item_trade_filter",ItemFilterMenu::new);

    public static final Supplier<MenuType<TransactionRegisterMenu>> TRANSACTION_REGISTER = registerItem("transaction_register",TransactionRegisterMenu::new);

    public static <T extends AbstractContainerMenu,B extends BlockEntity> DeferredHolder<MenuType<?>,MenuType<T>> registerBE(String id, Class<B> beClass,Function3<Integer,Inventory,B,T> factory) {
        return register(id,(menu,inventory,data) -> {
            BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
            if(beClass.isInstance(be))
                return factory.apply(menu,inventory,beClass.cast(be));
            return null;
        });
    }
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> registerItem(String id,Function3<Integer,Inventory,Integer,T> factory) {
        return register(id,(menu,inventory,data) -> factory.apply(menu,inventory,data.readInt()));
    }
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> registerTrader(String id, Function4<Integer,Inventory,Long,MenuValidator,T> factory) {
        return register(id,(menu,inventory,data) -> factory.apply(menu,inventory,data.readLong(),MenuValidator.decode(data)));
    }
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> registerValidated(String id,Function3<Integer,Inventory,MenuValidator,T> factory) {
        return register(id,(menu,inventory,data) -> factory.apply(menu,inventory,MenuValidator.decode(data)));
    }
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> registerSimple(String id, BiFunction<Integer,Inventory,T> factory) {
        return register(id,(menu,inventory,data) -> factory.apply(menu,inventory));
    }
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> register(String id,IContainerFactory<T> factory) {
        return REGISTER.register(id,() -> new MenuType<>(factory,FeatureFlagSet.of()));
    }

}
