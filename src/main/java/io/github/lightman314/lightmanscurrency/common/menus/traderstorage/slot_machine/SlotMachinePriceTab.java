package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachinePriceClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SlotMachinePriceTab extends TraderStorageTab {


    public SlotMachinePriceTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new SlotMachinePriceClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    public void SetPrice(MoneyValue newPrice)
    {
        if(this.menu.hasPermission(Permissions.EDIT_TRADES) && this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            trader.setPrice(newPrice);
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("SetPrice", newPrice));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetPrice"))
        {
            this.SetPrice(message.getMoneyValue("SetPrice"));
        }
    }

}