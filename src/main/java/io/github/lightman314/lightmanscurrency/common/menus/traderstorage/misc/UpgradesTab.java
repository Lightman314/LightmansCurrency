package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.misc;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.slot.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.misc.UpgradesClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UpgradesTab extends TraderStorageTab {

    private final int slotLimit;
    public UpgradesTab(@Nonnull ITraderStorageMenu menu) { this(menu,0); }
    public UpgradesTab(@Nonnull ITraderStorageMenu menu, int slotLimit) {
        super(menu);
        this.slotLimit = slotLimit;
    }


    List<EasySlot> slots = new ArrayList<>();
    public List<? extends Slot> getSlots() { return this.slots; }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new UpgradesClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.hasPermission(player,Permissions.OPEN_STORAGE) && !trader.isPersistent();
    }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            Container upgrades = trader.getUpgrades();
            int limit = this.slotLimit > 0 ? Math.min(this.slotLimit,upgrades.getContainerSize()) : upgrades.getContainerSize();
            int xStart = 103 - 9 * limit;
            for(int i = 0; i < limit; ++i)
            {
                EasySlot upgradeSlot = new UpgradeInputSlot(upgrades, i, xStart + 18 * i, 54, trader);
                upgradeSlot.active = false;
                addSlot.apply(upgradeSlot);
                this.slots.add(upgradeSlot);
            }
        }
    }

    @Override
    public void onTabOpen() { EasySlot.SetActive(this.slots); }

    @Override
    public void onTabClose() { EasySlot.SetInactive(this.slots); }

    @Override
    public boolean quickMoveStack(ItemStack stack) {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.quickInsertUpgrade(stack);
    }

    @Override
    public void receiveMessage(LazyPacketData message) { }

}
