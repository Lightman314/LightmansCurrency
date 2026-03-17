package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasyItemHandlerSlot;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.UpgradesClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UpgradesTab extends TraderStorageTab {

    public static final ResourceLocation KEY = LightmansCurrency.id("upgrade_slots");

    public UpgradesTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }


    List<EasySlot> slots = new ArrayList<>();
    public List<? extends Slot> getSlots() { return this.slots; }

    @Override
    public Object createClientTab(Object screen) { return new UpgradesClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.hasPermission(player,Permissions.OPEN_STORAGE) && !(trader instanceof IPersistentTrader pt && pt.isPersistent());
    }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            UpgradeStackHandler upgrades = trader.getUpgrades();
            int limit = upgrades.getSlots();
            int nextCount = limit;
            int xPos = 103 - (9 * Math.min(9,limit));
            int yPos = 54 - (9 * Math.max(1,(limit + 8) / 9));
            for(int i = 1; i <= limit; ++i)
            {
                EasySlot upgradeSlot = new EasyItemHandlerSlot(upgrades, i, xPos, yPos);
                upgradeSlot.setActive(false);
                addSlot.apply(upgradeSlot);
                this.slots.add(upgradeSlot);
                if(limit % 9 == 8)
                {
                    nextCount -= 9;
                    xPos = 103 - (9 * Math.min(9,limit));
                    yPos += 18;
                }
                else
                    xPos += 18;
            }
        }
    }

    @Override
    public void onTabOpen() { EasySlot.SetActive(this.slots,true); }

    @Override
    public void onTabClose() { EasySlot.SetActive(this.slots,false); }

    @Override
    public boolean quickMoveStack(ItemStack stack) {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.quickInsertUpgrade(stack);
    }

    @Override
    public void receiveMessage(LazyPacketData message) { }

}
