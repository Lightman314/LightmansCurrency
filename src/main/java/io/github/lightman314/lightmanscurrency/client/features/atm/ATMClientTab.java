package io.github.lightman314.lightmanscurrency.client.features.atm;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

public abstract class ATMClientTab<T extends ATMTab> extends ClientMenuTab<ATMMenu,T,ATMTab,ATMScreen> {

    protected ATMClientTab(ATMMenu menu, T commonTab, ATMScreen screen) { super(menu, commonTab, screen); }

    public BankReference getSelectedAccount() { return this.getMenu().getSelectedAccount(); }
    @Nullable
    public BankAccount getBankAccount() { return this.getSelectedAccount().get(); }

    public ResourceHandler<ItemResource> getMoneyStorage() { return this.getMenu().getMoneyStorage(); }
    public MoneyResourceHandler getMoneyResource() { return this.getMenu().getMoneyResources(); }
    public MoneyResourceHandler getPlayerAndMoneyResources() { return this.getMenu().getPlayerAndMoneyResources(); }
    public MoneyResourceHandler getMoneyAndPlayerResources() { return this.getMenu().getMoneyAndPlayerResources(); }

}
