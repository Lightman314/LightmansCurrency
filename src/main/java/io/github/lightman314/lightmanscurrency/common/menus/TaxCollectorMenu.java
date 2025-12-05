package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.data.types.TaxDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaxCollectorMenu extends EasyTabbedMenu<TaxCollectorMenu,TaxCollectorTab> {

    public final long entryID;
    public final TaxEntry getEntry() { return TaxDataCache.TYPE.get(this).getEntry(this.entryID); }

    public TaxCollectorMenu(int id, Inventory inventory, long entryID, MenuValidator validator) {
        super(ModMenus.TAX_COLLECTOR.get(), id, inventory, validator);
        this.entryID = entryID;
        this.addValidator(this::hasAccess);

        this.initializeTabs();
    }

    @Override
    protected void registerTabs() {
        this.addTab(new BasicSettingsTab(this));
        this.addTab(new LogTab(this));
        this.addTab(new InfoTab(this));
        this.addTab(new OwnershipTab(this));
        this.addTab(new AdminTab(this));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    public void CollectStoredMoney()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null && this.hasAccess())
        {
            MoneyStorage amountToGive = entry.getStoredMoney();
            if(!amountToGive.isEmpty())
                entry.getStoredMoney().GiveToPlayer(this.player);
            if(this.isClient())
                this.SendMessageToServer(this.builder().setFlag("CollectStoredMoney"));
        }
    }

    public boolean isServerEntry() {
        TaxEntry entry = this.getEntry();
        if(entry != null)
            return entry.isServerEntry();
        return false;
    }

    public boolean hasAccess()
    {
        TaxEntry entry = this.getEntry();
        if(entry == null)
            return false;
        return entry.canAccess(this.player);
    }

    public boolean isOwner()
    {
        TaxEntry entry = this.getEntry();
        if(entry == null)
            return false;
        return entry.getOwner().isAdmin(this.player);
    }

    public boolean isAdmin() { return LCAdminMode.isAdminPlayer(this.player); }

    @Override
    protected void HandleMessages(LazyPacketData message) {
        if(message.contains("CollectStoredMoney"))
            this.CollectStoredMoney();
    }
}