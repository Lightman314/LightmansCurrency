package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public interface StorageTabBuilder {

    void addTab(Function<TraderStorageMenu,TraderStorageTab> tab);
    void removeTab(Identifier tabKey);
    boolean hasTab(Identifier tabKey);

}