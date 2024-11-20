package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.InterfaceStatsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class InterfaceStatsTab extends TraderInterfaceTab {

    public InterfaceStatsTab(TraderInterfaceMenu menu) { super(menu); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new InterfaceStatsClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    public void clearStats(boolean fullClear)
    {
        this.menu.getBE().statTracker.clear(fullClear);
        this.menu.getBE().setStatsDirty();
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setBoolean("ClearStats",fullClear));
    }

    @Override
    public void handleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("ClearStats"))
            this.clearStats(message.getBoolean("ClearStats"));
    }

}