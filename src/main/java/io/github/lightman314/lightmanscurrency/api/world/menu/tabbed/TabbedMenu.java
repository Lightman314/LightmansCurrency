package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.debug.DebugHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class TabbedMenu<X extends TabbedMenu<X,T>,T extends MenuTab<X>> extends MessageMenu implements ITickerServer {

    private final int inventorySlots;

    private final Map<Integer,T> tabs;
    public final Map<Integer,T> getTabs() { return this.tabs; }

    public Identifier getMenuKey() { return BuiltInRegistries.MENU.getKey(this.getType()); }

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
        //Keep track of how many inventory slots are present
        this.inventorySlots = this.slots.size();
        this.addBonusSlots();
        //Add Tab Slots
        for(MenuTab<?> tab : this.getTabs().values())
            tab.addMenuSlots(this::addSlot);

        this.debugTabs();
        //this.debugSlotCount();

        //Flag the first tab as opened
        this.tabs.get(this.currentTab).onTabOpened(FancyPacketMap.EMPTY);

    }

    protected final void debugTabs() {
        LightmansCurrency.LogDebug(this.getClass().getSimpleName() + " on the " + DebugHelper.sideName(this) + " has the following tabs:\n" + DebugHelper.debugMap(this.tabs,Object::toString,DebugHelper::simpleClassName));
    }

    protected abstract void addInventorySlots(Inventory inventory);

    protected void addBonusSlots() {}

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
        if(newTab == null || (!newTab.canOpen() && tabSlot != this.defaultTab)) {
            LightmansCurrency.LogWarning("Failed to open tab " + tabSlot + " on the " + DebugHelper.sideName(this));
            return; //No new tab to open in that slot
        }
        this.tabs.get(this.currentTab).onTabClosed();
        this.currentTab = tabSlot;
        newTab.onTabOpened(message);
        this.tabChangeListener.accept(tabSlot,message);
        //LightmansCurrency.LogDebug("Changed tab to " + tabSlot + " on the " + DebugHelper.sideName(this));
        if(sendPacket)
        {
            this.send(FancyPacketMap.map().setMap("changeTab",FancyPacketMap.map()
                    .setInt("slot",tabSlot)
                    .setOptionalMap("additional",message)));
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("changeTab"))
        {
            FancyPacketMap entry = message.getMap("changeTab");
            int slot = entry.getInt("slot");
            FancyPacketMap additional = entry.getMap("additional");
            //Call the same method on both sides, but don't send the packet when it was received *from* a packet
            this.changeTab(slot,additional,false);
        }
        //Send to the current tab
        this.getCurrentTab().handleMessage(message);
    }

    @Override
    public final ItemStack quickMoveStack(Player player,int slotIndex) {
        Optional<ItemStack> result = this.getCurrentTab().quickMoveStack(player,slotIndex);
        if(result.isPresent())
            return result.get();
        return this.quickMoveAction(player,slotIndex);
    }

    protected ItemStack quickMoveAction(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if(slot != null && slot.hasItem())
        {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();
            if(slotIndex < this.inventorySlots)
            {
                //If the slot is within the inventory slot range, quick move from the players inventory into the available bonus slots
                if(!this.moveItemStackTo(stack,this.inventorySlots,this.slots.size(),false))
                    return ItemStack.EMPTY;
            }
            else {
                //Otherwise quick-move from the bonus slot back into the players inventory
                if(!this.moveItemStackTo(stack,0,this.inventorySlots,true))
                    return ItemStack.EMPTY;
            }
            if(stack.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return clicked;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void serverTick() {
        if(!this.getCurrentTab().canOpen())
            this.changeTab(this.defaultTab);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void removed(Player player) {
        super.removed(player);
        for(MenuTab<X> tab : this.tabs.values())
            tab.onMenuClosed();
    }

    public static int sortTabs(Map.Entry<?,? extends MenuTab<?>> tabA, Map.Entry<?,? extends MenuTab<?>> tabB) {
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
        public boolean stillValid(Player player) { return MenuValidator.stillValid(player,this.validators); }

    }

}