package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.CoinManagementMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class CoinManagementScreen extends EasyMenuScreen<CoinManagementMenu> {

    public CoinManagementScreen(CoinManagementMenu menu, Inventory inventory, Component ignored) {
        super(menu, inventory);
        //Collect editable money data
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

    }

}