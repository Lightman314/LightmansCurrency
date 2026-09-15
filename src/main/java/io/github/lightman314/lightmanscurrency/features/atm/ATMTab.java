package io.github.lightman314.lightmanscurrency.features.atm;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.IEasySlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class ATMTab extends MenuTab<ATMMenu> {

    public ATMTab(ATMMenu menu) { super(menu); }

    protected final boolean isQuarantined() { return LCApi.getQuarantineAPI().isQuarantined(this.getPlayer().level()); }

    @Override
    public boolean canOpen() { return !this.isQuarantined(); }

    public ResourceHandler<ItemResource> getMoneyStorage() { return this.getMenu().getMoneyStorage(); }
    public MoneyResourceHandler getMoneyResource() { return this.getMenu().getMoneyResources(); }

    public abstract boolean usesMoneySlots();

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onTabOpened(FancyPacketMap additional) {
        IEasySlot.setActive(this.getMenu().getMoneySlots(),this.usesMoneySlots());
        LightmansCurrency.LogDebug("Set " + this.getMenu().getMoneySlots().size() + " money slots as " + (this.usesMoneySlots() ? "active" : "inactive"));
    }

    public static abstract class SafeAccess extends ATMTab {

        public SafeAccess(ATMMenu menu) { super(menu); }
        @Override
        public boolean canOpen() { return true; }

    }

}
