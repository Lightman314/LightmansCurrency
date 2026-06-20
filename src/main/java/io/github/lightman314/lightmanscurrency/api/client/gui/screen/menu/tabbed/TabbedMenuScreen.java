package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.MessageMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.ISortedTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TabbedMenuScreen<M extends TabbedMenu<M,T>,T extends MenuTab<M>,C extends ClientMenuTab<M,? extends T,T,?>> extends MessageMenuScreen<M> {

    private final Map<Integer,C> clientTabs;

    protected boolean initialized = false;
    private int currentTab;
    public C getCurrentTab() { return this.clientTabs.get(this.currentTab); }

    private IWidgetPositioner tabPositioner;
    public final IWidgetPositioner getTabPositioner() { return this.tabPositioner; }

    public TabbedMenuScreen(M menu, Inventory inventory) { this(menu,inventory,Component.empty()); }
    public TabbedMenuScreen(M menu, Inventory inventory, Component title) { this(menu, inventory, title,176,166); }
    public TabbedMenuScreen(M menu, Inventory inventory, int width, int height) { this(menu,inventory,Component.empty(),width, height); }
    public TabbedMenuScreen(M menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
        this.menu.addClientListener(this::onTabChanged);
        this.currentTab = this.getMenu().getCurrentTabKey();
        ImmutableMap.Builder<Integer,C> builder = ImmutableMap.builder();
        this.menu.getTabs().forEach((key,tab) -> {
            try {
                C newTab = (C)ClientMenuTab.create(this.getMenu(),tab,this);
                if(newTab == null)
                    LightmansCurrency.LogError("No Client Tab Builder was registered for " + tab.getClientTabKey() + "!");
                builder.put(key,newTab);
            } catch (ClassCastException exception) {
                LightmansCurrency.LogError("Error adding client tab to the screen!",exception);
            }
        });
        this.clientTabs = builder.build();
        if(!this.clientTabs.containsKey(this.currentTab))
            throw new IllegalStateException("Screen did not properly construct the default client tab!");
    }

    private void changeTab(C tab)
    {
        this.clientTabs.forEach((slot,t) -> {
            if(t == tab)
                this.menu.changeTab(slot);
        });
    }

    private void onTabChanged(int newSlot,FancyPacketMap packet)
    {
        if(this.currentTab == newSlot || !this.clientTabs.containsKey(newSlot))
            return;
        C oldTab = this.getCurrentTab();
        oldTab.onTabClosed();
        this.currentTab = newSlot;
        C newTab = this.getCurrentTab();
        newTab.onTabOpened(packet);
    }

    protected abstract IWidgetPositioner createTabPositioner();

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        this.getCurrentTab().extractBackground(gui,area);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void initialize(ScreenArea area) {
        //Add the tab buttons
        this.tabPositioner = this.createTabPositioner();
        this.addChild(this.tabPositioner);
        List<C> tabs = new ArrayList<>(this.clientTabs.values());
        tabs.sort(TabbedMenuScreen::sortTabs);
        for(final C tab : tabs)
        {
            TabButton button = this.addChild(TabButton.builder()
                    .forTab(tab)
                    .active(() -> tab == this.getCurrentTab())
                    .onPress(() -> this.changeTab(tab))
                    .build());
            this.tabPositioner.addWidget(button);
        }
        //Initialize the current tab
        this.getCurrentTab().onTabOpened(FancyPacketMap.EMPTY);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void handleMessage(FancyPacketMap message) {
        super.handleMessage(message);
        //Pass the message along to the current tab
        this.getCurrentTab().handleMessage(message);
    }

    public static int sortTabs(ClientMenuTab<?,?,?,?> tabA,ClientMenuTab<?,?,?,?> tabB) {
        return Integer.compare(getSortPriority(tabA),getSortPriority(tabB));
    }

    private static int getSortPriority(ClientMenuTab<?,?,?,?> tab) { return ISortedTab.getTabSortPriority(tab,tab.getCommonTab()); }

}
