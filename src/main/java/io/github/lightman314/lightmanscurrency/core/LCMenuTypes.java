package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
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

    public static final DeferredHolder<MenuType<?>,MenuType<TraderStorageMenu>> TRADER_STORAGE = register("trader_storage",() -> (id,inv,data) ->
            new TraderStorageMenu(id,inv.player,data.readLong(),IValidatedMenu.extract(data)));

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<T>> register(String name,Supplier<IContainerFactory<T>> factory)
    {
        return REGISTER.register(name,() -> new MenuType<>(factory.get(),FeatureFlagSet.of()));
    }

}
