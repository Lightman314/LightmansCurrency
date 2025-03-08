package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.gatcha;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.gatcha.GachaPriceClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.gatcha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaPriceTab extends TraderStorageTab {

    public GachaPriceTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new GachaPriceClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    public void setPrice(MoneyValue price) {
        if(this.menu.getTrader() instanceof GachaTrader trader)
        {
            trader.setPrice(this.menu.getPlayer(),price);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("ChangePrice",price));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangePrice"))
            this.setPrice(message.getMoneyValue("ChangePrice"));
    }

}
