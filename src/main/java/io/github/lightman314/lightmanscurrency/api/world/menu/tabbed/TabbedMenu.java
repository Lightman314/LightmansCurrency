package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class TabbedMenu<X extends TabbedMenu<X,T>,T extends MenuTab<X>> extends MessageMenu implements ITickerServer {

    private final Map<Integer,T> tabs;
    public final Map<Integer,T> getTabs() { return this.tabs; }

    private final int defaultTab;
    private int currentTab;
    public int getCurrentTabKey() { return this.currentTab; }
    public T getCurrentTab() { return this.tabs.get(this.currentTab); }

    private BiConsumer<Integer,FancyPacketMap> tabChangeListener = (k,p) -> {};

    protected TabbedMenu(@Nullable MenuType<?> menuType,int containerId,Player player) {
        super(menuType,containerId,player);
        Map<Integer,T> temp = new HashMap<>();
        this.collectTabs(TabBuilder.forMap(temp));
        this.tabs = ImmutableMap.copyOf(temp);
        if(this.tabs.isEmpty())
            throw new IllegalStateException("Tabbed Menu of type " + BuiltInRegistries.MENU.getKey(this.getType()) + " does not have any tabs registered!");
        //Calculate the default tab
        this.defaultTab = this.currentTab = this.calculateDefaultTab();
        //Add Slots
        this.addInventorySlots(player.getInventory());
        //Add Tab Slots
        for(MenuTab<?> tab : this.getTabs().values())
            tab.addMenuSlots(this::addSlot);

        //Flag the first tab as opened
        this.tabs.get(this.currentTab).onTabOpened(FancyPacketMap.EMPTY);
    }

    protected abstract void addInventorySlots(Inventory inventory);

    //Seperate method so that children can define this seperately
    protected int calculateDefaultTab() {
        //Calculate the default tab
        if(this.tabs.containsKey(0))
            return 0;
        else
        {
            //Just grab the first tab in the sorting priority (if we know how they're sorted on the common side of things)
            List<Map.Entry<Integer,T>> tabArray = new ArrayList<>(this.tabs.entrySet());
            tabArray.sort(TabbedMenu::sortTabs);
            return tabArray.getFirst().getKey();
        }
    }

    public final void addClientListener(BiConsumer<Integer,FancyPacketMap> tabChangeListener) { this.tabChangeListener = tabChangeListener; }

    protected abstract void collectTabs(TabBuilder<X,T> builder);

    public final void changeTab(int tabSlot) { this.changeTab(tabSlot,FancyPacketMap.EMPTY); }
    public final void changeTab(int tabSlot,FancyPacketMap message) { this.changeTab(tabSlot,message,true); }
    private void changeTab(int tabSlot,FancyPacketMap message,boolean sendPacket)
    {
        if(this.currentTab == tabSlot)
            return; //Nothing to change here!
        T newTab = this.tabs.get(tabSlot);
        if(newTab == null || (!newTab.canOpen() && tabSlot != this.defaultTab))
            return; //No new tab to open in that slot
        this.tabs.get(this.currentTab).onTabClosed();
        this.currentTab = tabSlot;
        newTab.onTabOpened(message);
        this.tabChangeListener.accept(tabSlot,message);
        if(sendPacket)
        {
            this.send(FancyPacketMap.newMutable().setMap("changeTab",FancyPacketMap.newMutable()
                    .setInt("slot",tabSlot)
                    .setOptionalMap("additional",message)));
        }
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("changeTab"))
        {
            FancyPacketMap entry = message.getMap("changeTab");
            int slot = message.getInt("slot");
            FancyPacketMap additional = message.getMap("additional");
            //Call the same method on both sides, but don't send the packet when it was received *from* a packet
            this.changeTab(slot,additional,false);
        }
    }

    @Override
    public final ItemStack quickMoveStack(Player player,int slotIndex) {
        if(this.getCurrentTab().quickMoveStack(player,slotIndex))
            return ItemStack.EMPTY;
        return this.quickMoveAction(player,slotIndex);
    }
    protected ItemStack quickMoveAction(Player player, int slotIndex) { return ItemStack.EMPTY; }

    @Override
    public void serverTick() {
        if(!this.getCurrentTab().canOpen())
            this.changeTab(this.defaultTab);
    }

    public static int sortTabs(Map.Entry<?,? extends MenuTab<?>> tabA,Map.Entry<?,? extends MenuTab<?>> tabB) {
        return sortTabs(tabA.getValue(),tabB.getValue());
    }
    public static int sortTabs(MenuTab<?> tabA, MenuTab<?> tabB) {
        return Integer.compare(ISortedTab.getTabSortPriority(tabA),ISortedTab.getTabSortPriority(tabB));
    }

    public abstract static class Validated<X extends TabbedMenu.Validated<X,T>,T extends MenuTab<X>> extends TabbedMenu<X,T> implements IValidatedMenu
    {

        private final MenuValidator mainValidator;
        private final List<MenuValidator> validators = new ArrayList<>();

        protected Validated(@Nullable MenuType<?> menuType, int containerId, Player player, MenuValidator validator) {
            super(menuType, containerId, player);
            this.mainValidator = validator;
            this.validators.add(this.mainValidator);
        }

        @Override
        public MenuValidator getValidator() { return this.mainValidator; }

        @Override
        public void addValidator(MenuValidator validator) {
            if(!this.validators.contains(validator))
                this.validators.add(validator);
        }

        @Override
        public boolean stillValid(Player player) {
            for(MenuValidator v : new ArrayList<>(this.validators))
            {
                if(!v.stillValid(player))
                    return false;
            }
            return true;
        }
    }

}