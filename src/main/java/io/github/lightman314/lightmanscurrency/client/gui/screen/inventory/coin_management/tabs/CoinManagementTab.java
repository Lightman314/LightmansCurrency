package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.CoinManagementScreen;

import javax.annotation.Nonnull;

public abstract class CoinManagementTab extends EasyTab {


    protected final CoinManagementScreen screen;
    protected CoinManagementTab(@Nonnull CoinManagementScreen screen) { super(screen); this.screen = screen; }

    @Override
    public final int getColor() { return 0xFFFFFF; }

    @Override
    public final boolean blockInventoryClosing() { return true; }

}
