package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.IEasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EasyTabbedMenu<M extends IEasyTabbedMenu<T>,T extends EasyMenuTab<M,T>> extends LazyMessageMenu implements IEasyTabbedMenu<T> {

    private boolean tabsLocked = false;
    private int currentTabIndex = -1;
    private int currentAddIndex = 0;
    public final T currentTab() { return this.menuTabs.get(this.currentTabIndex); }
    private Map<Integer,T> menuTabs = null;
    public final Map<Integer,T> getAllTabs() { return this.menuTabs == null ? ImmutableMap.of() : ImmutableMap.copyOf(this.menuTabs); }

    private IEasyTabbedMenuScreen<M,T,?> screen = null;
    public void setScreen(IEasyTabbedMenuScreen<M,T,?> screen) { this.screen = screen; }
    private Consumer<LazyPacketData> messageListener = null;

    public EasyTabbedMenu(MenuType<?> type, int id, Inventory inventory, MenuValidator validator) { super(type, id, inventory, validator); }
    public EasyTabbedMenu(MenuType<?> type, int id, Inventory inventory) { super(type, id, inventory); }

    public final void setMessageListener(Consumer<LazyPacketData> listener) { this.messageListener = listener; }

    /**
     * To be called during init to trigger the initialization of the tabs<br>
     * Not done in this classes init as certain variables may not yet be set up
     */
    protected final void initializeTabs()
    {
        if(this.tabsLocked)
            throw new IllegalStateException("Cannot initialize the menus tabs when they've already be initialized!");
        this.menuTabs = new HashMap<>();
        this.registerTabs();
        if(!this.menuTabs.containsKey(0))
            throw new IllegalArgumentException("EasyTabbedMenu#registerTabs did not register a tab for key 0!");
        this.tabsLocked = true;
        //Add all slots added by the tabs
        for(T tab : this.menuTabs.values())
            tab.addStorageMenuSlots(this::addSlot);
        //Change to the default tab
        this.ChangeTab(0,null,false);
    }

    /**
     * Called during {@link #initializeTabs()} to let the subclass know that the menu is ready to register menu tabs
     */
    protected abstract void registerTabs();

    /**
     * Simpler version of {@link #setTab(int, EasyMenuTab)} but for when the key is considered irrelevant and can be generated automatically
     */
    public final void addTab(T tab)
    {
        if(this.tabsLocked || this.menuTabs == null)
            this.setTab(this.currentAddIndex,tab);
        else
            this.setTab(this.currentAddIndex++,tab);
    }

    /**
     * Called by subclass during {@link #registerTabs()} to register the relevant tabs
     */
    public final void setTab(int key, T tab)
    {
        if(this.tabsLocked)
        {
            LightmansCurrency.LogError("Attempted to define a tab for the menu after the registration has been locked!");
            return;
        }
        if(this.menuTabs == null)
        {
            LightmansCurrency.LogError("Attempted to register a tab for the menu outside of the #registerTabs function!");
            return;
        }
        this.menuTabs.put(key,tab);
    }

    /**
     * Called by subclass during {@link #registerTabs()} to remove a registered tab
     */
    public final void clearTab(int key)
    {
        if(this.tabsLocked)
        {
            LightmansCurrency.LogError("Attempted to clear a tab for the menu after the registration has been locked!");
            return;
        }
        if(this.menuTabs == null)
        {
            LightmansCurrency.LogError("Attempted to clear a tab for the menu outside of the #registerTabs function!");
            return;
        }
        if(key == 0)
        {
            LightmansCurrency.LogError("Attempted to clear the tab for the root key!");
            return;
        }
        this.menuTabs.remove(key);
    }

    public final void ChangeTab(int newTab) { this.ChangeTab(newTab,null,true); }
    public final void ChangeTab(int newTab,@Nullable LazyPacketData.Builder data) { this.ChangeTab(newTab,data == null ? null : data.build()); }
    public final void ChangeTab(int newTab,@Nullable LazyPacketData data) { this.ChangeTab(newTab,data,true); }
    private void ChangeTab(int newTab,@Nullable LazyPacketData data,boolean sendPacket)
    {
        if(newTab == this.currentTabIndex)
            return;
        if(!this.menuTabs.containsKey(newTab))
        {
            LightmansCurrency.LogError("Attempted to open tab " + newTab + ", but no tab with that key is present in the menu!");
            return;
        }
        if(this.currentTab() != null)
            this.currentTab().onTabClose();
        this.currentTabIndex = newTab;
        if(data != null && data.size("ChangeTab") > 0)
            this.currentTab().OpenMessage(data);
        this.currentTab().onTabOpen();
        this.onTabChanged(this.currentTab());
        if(sendPacket)
        {
            LazyPacketData.Builder builder;
            if(data != null)
                builder = data.copyToBuilder();
            else
                builder = this.builder();
            builder.setInt("ChangeTab",newTab);
            LightmansCurrency.LogDebug("Sending Change Tab message from the " + DebugUtil.getSideText(this) + "\n" + builder);
            this.SendMessage(builder);
        }
        //Force screen to be synced with the menu
        if(this.screen != null && this.screen.getCurrentTabIndex() != this.currentTabIndex)
            this.screen.ChangeTab(this.currentTabIndex,data,false);
    }

    protected void onTabChanged(T newTab) { }

    @Override
    protected final void HandleMessage(LazyPacketData message) {
        if(message.contains("ChangeTab"))
        {
            LightmansCurrency.LogDebug("Handling Change Tab message on the " + DebugUtil.getSideText(this) + "\n" + message);
            this.ChangeTab(message.getInt("ChangeTab"),message,false);
        }

        this.currentTab().receiveMessage(message);
        this.HandleMessages(message);
        if(this.messageListener != null)
            this.messageListener.accept(message);
    }

    protected void HandleMessages(LazyPacketData message) {}

    @Override
    public void removed(Player player) {
        super.removed(player);
        for(T tab : this.menuTabs.values())
            tab.onMenuClose();
    }
}