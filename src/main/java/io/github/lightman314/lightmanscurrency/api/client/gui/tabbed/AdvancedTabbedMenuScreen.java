package io.github.lightman314.lightmanscurrency.api.client.gui.tabbed;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.IEasyTabbedMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AdvancedTabbedMenuScreen<X extends IEasyTabbedMenu<T>,M extends EasyTabbedMenu<X,T>,T extends EasyMenuTab<X,T>,S extends IEasyTabbedMenuScreen<X,T,S>> extends EasyMenuScreen<M> implements IEasyTabbedMenuScreen<X,T,S> {

    private int currentTabIndex = 0;
    @Override
    public final int getCurrentTabIndex() { return this.currentTabIndex; }
    private final Map<Integer,EasyMenuClientTab<? extends T,X,T,S,?>> clientTabs = new HashMap<>();
    public final EasyMenuClientTab<? extends T,X,T,S,?> currentTab() { return this.clientTabs.get(this.currentTabIndex); }

    @Override
    public final X getMenuInterface() { return (X)this.menu; }

    public AdvancedTabbedMenuScreen(M menu, Inventory inventory) { this(menu,inventory, EasyText.empty()); }
    public AdvancedTabbedMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.menu.setScreen(this);
        this.menu.setMessageListener(this::HandleClientMessage);
        this.menu.getAllTabs().forEach((key,tab) -> {
            Object ct = tab.createClientTab(this);
            try {
                this.clientTabs.put(key,(EasyMenuClientTab<? extends T,X,T,S,?>)Objects.requireNonNull(ct,"Tab at slot " + key + " returned a null client tab!"));
                LightmansCurrency.LogDebug("Created client tab for slot " + key);
            } catch (ClassCastException e) {
                throw new IllegalStateException("Tab at slot " + key + " did not return a valid client tab!",e);
            }
        });
    }

    @Override
    protected final void initialize(ScreenArea screenArea) {

        //Initialize screen-related widgets (done first just in case we get resized)
        this.init(screenArea);
        //Initialize the tab buttons
        IWidgetPositioner tabButtonPositioner = this.getTabButtonPositioner();
        tabButtonPositioner.clear();
        this.addChild(tabButtonPositioner);
        //Sort the tab buttons so that they'll be ordered by the positioner correctly
        List<Map.Entry<Integer,EasyMenuClientTab<? extends T,X,T,S,?>>> sortedTabs = new ArrayList<>(this.clientTabs.entrySet());
        sortedTabs.sort(ISortableTab::sortEntries);
        //Create the tab buttons
        for(var entry : sortedTabs)
        {
            TabButton button = TabButton.builder()
                    .pressAction(() -> this.ChangeTab(entry.getKey()))
                    .tab(entry.getValue())
                    .addon(EasyAddonHelper.activeCheck(() -> this.currentTabIndex != entry.getKey()))
                    .addon(EasyAddonHelper.visibleCheck(() -> entry.getValue().tabVisible() && entry.getValue().commonTab.canOpen(this.menu.player)))
                    .build();
            this.addChild(button);
            tabButtonPositioner.addWidget(button);
        }
        //Initialize the currently opened tab
        this.currentTab().onOpen();
    }

    protected abstract IWidgetPositioner getTabButtonPositioner();

    protected abstract void init(ScreenArea screenArea);

    public final void ChangeTab(int newTab) { this.ChangeTab(newTab,null); }
    public final void ChangeTab(int newTab, @Nullable LazyPacketData.Builder data) { this.ChangeTab(newTab, data == null ? null : data.build(), true); }
    public final void ChangeTab(int newTab, @Nullable LazyPacketData data, boolean sendPacket) {
        if(newTab == this.currentTabIndex)
            return;
        if(!this.clientTabs.containsKey(newTab))
        {
            LightmansCurrency.LogError("Attempted to open tab " + newTab + " on the client, but no tab with that key is present in the menu!");
            return;
        }
        this.currentTab().onClose();
        this.currentTabIndex = newTab;
        if(data != null && data.size("ChangeTab") > 0)
            this.currentTab().OpenMessage(data);
        this.currentTab().onOpen();
        LightmansCurrency.LogDebug("Client Tab Changed to " + this.currentTabIndex);
        if(sendPacket)
            this.menu.ChangeTab(newTab,data);
        //If sendPacket = false, then this was *from* a packet and thus the menu already knows about the tab being changed
    }

    @Override
    protected final void renderBG(EasyGuiGraphics gui) {
        this.renderBackground(gui);
        this.currentTab().renderBG(gui);
    }
    protected abstract void renderBackground(EasyGuiGraphics gui);

    @Override
    protected final void renderAfterWidgets(EasyGuiGraphics gui) {
        this.renderLate(gui);
        this.currentTab().renderAfterWidgets(gui);
    }
    protected void renderLate(EasyGuiGraphics gui) {}

    protected final void HandleClientMessage(LazyPacketData message)
    {
        if(message.contains("ChangeTab"))
            this.ChangeTab(message.getInt("ChangeTab"),message,false);
    }

    @Override
    public boolean blockInventoryClosing() { return this.currentTab() != null && this.currentTab().blockInventoryClosing(); }

    @Nullable
    @Override
    public Pair<ItemStack,ScreenArea> getHoveredItem(ScreenPosition mousePos) {
        EasyMenuClientTab<? extends T,X,T,S,?> tab = this.currentTab();
        if(tab != null)
            return tab.getHoveredItem(mousePos);
        return super.getHoveredItem(mousePos);
    }

    @Nullable
    @Override
    public Pair<FluidStack,ScreenArea> getHoveredFluid(ScreenPosition mousePos) {
        EasyMenuClientTab<? extends T,X,T,S,?> tab = this.currentTab();
        if(tab != null)
            return tab.getHoveredFluid(mousePos);
        return super.getHoveredFluid(mousePos);
    }

}