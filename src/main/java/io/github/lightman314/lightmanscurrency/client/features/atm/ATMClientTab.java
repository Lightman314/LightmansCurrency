package io.github.lightman314.lightmanscurrency.client.features.atm;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;

public abstract class ATMClientTab<T extends ATMTab> extends ClientMenuTab<ATMMenu,T,ATMTab,ATMScreen> {

    protected ATMClientTab(ATMMenu menu, T commonTab, ATMScreen screen) { super(menu, commonTab, screen); }



}
