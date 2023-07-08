package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
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

    public void SendSettingsMessage(CompoundTag settingsUpdate)
    {
        if(this.menu.isClient())
        {
            CompoundTag message = new CompoundTag();
            message.put("SettingsUpdate", settingsUpdate);
            this.menu.sendMessage(message);
        }
    }

    @Override
    public void receiveMessage(CompoundTag message) {

        if(message.contains("SettingsUpdate"))
        {
            TraderData trader = this.menu.getTrader();
            if(trader != null)
                trader.receiveNetworkMessage(this.menu.player, message.getCompound("SettingsUpdate"));
        }

    }

}
