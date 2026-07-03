package io.github.lightman314.lightmanscurrency.api.trader.client.nodes;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;

public interface IStorageScreenListener {

    default void onStorageScreenInit(TraderStorageScreen screen) {}
    default void onStorageScreenRender(TraderStorageScreen screen,FancyGuiExtractor gui, ScreenArea area) {}

}
