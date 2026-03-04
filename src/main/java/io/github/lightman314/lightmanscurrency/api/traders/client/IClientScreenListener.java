package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.client.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;

import java.util.function.Consumer;

public interface IClientScreenListener {

    default void onScreenInit(TraderData trader, ITraderScreen screen, Consumer<Object> addWidget) {}
    default void onStorageScreenInit(TraderData trader, ITraderStorageScreen screen, Consumer<Object> addWidget) {}

}
