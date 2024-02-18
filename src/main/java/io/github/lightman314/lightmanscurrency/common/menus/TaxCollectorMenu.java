package io.github.lightman314.lightmanscurrency.common.menus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class TaxCollectorMenu extends LazyMessageMenu {

    public final long entryID;
    public final TaxEntry getEntry() { return TaxSaveData.GetTaxEntry(this.entryID, this.isClient()); }

    private Consumer<Integer> tabChangeListener = i -> {};
    public void setTabChangeListener(@Nonnull Consumer<Integer> listener) { this.tabChangeListener = listener; }

    private int currentTab = 0;
    private final List<TaxCollectorTab> tabs = Lists.newArrayList(new BasicSettingsTab(this), new LogTab(this), new InfoTab(this), new OwnershipTab(this), new AdminTab(this));
    public final List<TaxCollectorTab> getAllTabs() { return ImmutableList.copyOf(this.tabs); }
    private TaxCollectorTab getCurrentTabInternal() { if(this.currentTab >= 0 && this.currentTab < this.tabs.size()) return this.tabs.get(this.currentTab); return null; }
    public final TaxCollectorTab getCurrentTab() {
        if(this.currentTab < 0 || this.currentTab >= this.tabs.size())
            this.ChangeTab(0, true);
        return this.getCurrentTabInternal();
    }


    public TaxCollectorMenu(int id, Inventory inventory, long entryID, MenuValidator validator) {
        super(ModMenus.TAX_COLLECTOR.get(), id, inventory, validator);
        this.entryID = entryID;
        this.addValidator(this::hasAccess);

        //Add item slots (in case any of the tabs actually need them)
        for(TaxCollectorTab tab : this.tabs)
            tab.addMenuSlots(this::addSlot);
    }

    public void ChangeTab(int newTabIndex, boolean sendMessage)
    {
        if(newTabIndex < 0 || newTabIndex >= this.tabs.size())
            return;
        if(newTabIndex != this.currentTab)
        {
            TaxCollectorTab oldTab = this.getCurrentTabInternal();
            if(oldTab != null)
                oldTab.onTabClose();
            this.currentTab = newTabIndex;
            TaxCollectorTab newTab = this.getCurrentTabInternal();
            if(newTab != null)
                newTab.onTabOpen();
            //Send tab change packet to the client
            if(sendMessage)
                this.SendMessage(LazyPacketData.builder().setInt("ChangeTab", this.currentTab));
            //Sync with screen (if client menu changed tab)
            this.tabChangeListener.accept(this.currentTab);
        }
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    public void CollectStoredMoney()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null && this.hasAccess())
        {
            MoneyStorage amountToGive = entry.getStoredMoney();
            if(!amountToGive.isEmpty())
                entry.getStoredMoney().GiveToPlayer(this.player);
            if(this.isClient())
                this.SendMessageToServer(LazyPacketData.simpleFlag("CollectStoredMoney"));
        }
    }

    @Override
    protected void onValidationTick(@Nonnull Player player) {
        TaxCollectorTab tab = this.getCurrentTab();
        if(tab != null && !tab.canBeAccessed() && this.currentTab != 0)
            this.ChangeTab(0, true);
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
    public void HandleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("ChangeTab"))
            this.ChangeTab(message.getInt("ChangeTab"), false);
        else if(message.contains("CollectStoredMoney"))
            this.CollectStoredMoney();
        else
            this.getCurrentTab().receiveMessage(message);
    }

}
