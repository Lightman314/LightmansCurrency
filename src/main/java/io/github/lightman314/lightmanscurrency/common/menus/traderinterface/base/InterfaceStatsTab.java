package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.InterfaceStatsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class InterfaceStatsTab extends TraderInterfaceTab {

    public InterfaceStatsTab(TraderInterfaceMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new InterfaceStatsClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void clearStats(boolean fullClear)
    {
        this.menu.getBE().statTracker.clear(fullClear);
        this.menu.getBE().setStatsDirty();
        if(this.menu.isClient())
            this.menu.SendMessage(LazyPacketData.builder().setBoolean("ClearStats",fullClear));
    }

    @Override
    public void handleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("ClearStats"))
            this.clearStats(message.getBoolean("ClearStats"));
    }

}