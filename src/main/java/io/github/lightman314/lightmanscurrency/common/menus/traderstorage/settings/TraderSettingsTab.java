package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

public class TraderSettingsTab extends TraderStorageTab {


    public TraderSettingsTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new TraderSettingsClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    @Deprecated(since = "2.1.2.4")
    public void SendSettingsMessage(CompoundTag settingsUpdate)
    {
        if(this.menu.isClient())
            this.menu.SendMessage(LazyPacketData.simpleTag("SettingsUpdate", settingsUpdate));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void receiveMessage(LazyPacketData message) {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            if(message.contains("SettingsUpdate", LazyPacketData.TYPE_NBT))
                trader.receiveNetworkMessage(this.menu.player, message.getNBT("SettingsUpdate"));
            else
                trader.handleSettingsChange(this.menu.player, message);
        }
    }

}
