package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;

import javax.annotation.Nonnull;

public abstract class EasyMenuClientTab<X extends T,M extends EasyTabbedMenu<M,T>,T extends EasyMenuTab<M,T>,S extends EasyTabbedMenuScreen<M,T,S>,C extends EasyMenuClientTab<X,M,T,S,C>> extends EasyTab {

    protected final S screen;
    protected final M menu;
    protected final X commonTab;

    public EasyMenuClientTab(@Nonnull Object screen, @Nonnull X commonTab) {
        super((S)screen);
        this.screen = (S)screen;
        this.menu = this.screen.getMenu();
        this.commonTab = commonTab;
    }

    public boolean tabVisible() { return true; }

    protected void OpenMessage(@Nonnull LazyPacketData clientData) {}

}
