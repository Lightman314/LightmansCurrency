package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintMenu;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.WalletMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LCMenuTypes {
    private LCMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU,LCApi.MODID);

    public static final DeferredHolder<MenuType<?>,MenuType<WalletMenu>> WALLET = register("wallet",() ->
            (id,inv,data) -> new WalletMenu(id,inv.player,data.readInt()));

    public static final DeferredHolder<MenuType<?>,MenuType<CoinMintMenu>> COIN_MINT = register("coin_mint",() ->
            (id,inv,data) -> new CoinMintMenu(id,inv.player,(CoinMintBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos())));

    public static final DeferredHolder<MenuType<?>,MenuType<ATMMenu>> ATM = register("atm",() -> (id,inv,data) -> new ATMMenu(id,inv.player));

    public static final DeferredHolder<MenuType<?>,MenuType<AbstractTabbedCustomerMenu>> TRADER_DIRECT = register("trader_direct",() ->
            (id,inv,data) -> new AbstractTabbedCustomerMenu.Direct(id,inv.player,data.readLong(),MenuValidator.STREAM_CODEC.decode(data)));
    public static final DeferredHolder<MenuType<?>,MenuType<AbstractTabbedCustomerMenu>> TRADER_BLOCK_ENTITY = register("trader_block",() ->
            (id,inv,data) -> new AbstractTabbedCustomerMenu.Block(id,inv.player,data.readBlockPos()));

    public static final DeferredHolder<MenuType<?>,MenuType<TraderStorageMenu>> TRADER_STORAGE = register("trader_storage",() ->
            (id,inv,data) -> new TraderStorageMenu(id,inv.player,data.readLong(),MenuValidator.STREAM_CODEC.decode(data)));

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> register(String name,Supplier<IContainerFactory<T>> factory)
    {
        return REGISTER.register(name,() -> new MenuType<>(factory.get(),FeatureFlagSet.of()));
    }

}
