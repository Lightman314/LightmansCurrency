package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class EasyTabbedMenuScreen<M extends EasyTabbedMenu<M,T>,T extends EasyMenuTab<M,T>,S extends EasyTabbedMenuScreen<M,T,S>> extends EasyMenuScreen<M> {

    private int currentTabIndex = 0;
    private final Map<Integer,EasyMenuClientTab<? extends T,M,T,S,?>> clientTabs = new HashMap<>();
    protected EasyMenuClientTab<? extends T,M,T,S,?> currentTab() { return this.clientTabs.get(this.currentTabIndex); }

    private IWidgetPositioner tabButtonPositioner;

    public EasyTabbedMenuScreen(@Nonnull M menu, @Nonnull Inventory inventory) { this(menu,inventory, EasyText.empty()); }
    public EasyTabbedMenuScreen(@Nonnull M menu, @Nonnull Inventory inventory, @Nonnull Component title) {
        super(menu, inventory, title);
        this.menu.setMessageListener(this::HandleClientMessage);
        this.menu.getAllTabs().forEach((key,tab) -> {
            Object ct = tab.createClientTab(this);
            try {
                this.clientTabs.put(key,(EasyMenuClientTab<? extends T,M,T,S,?>)Objects.requireNonNull(ct,"Tab at slot " + key + " returned a null client tab!"));
                LightmansCurrency.LogDebug("Created client tab for slot " + key);
            } catch (ClassCastException e) {
                throw new IllegalStateException("Tab at slot " + key + " did not return a valid client tab!",e);
            }
        });
    }

    @Override
    protected final void initialize(ScreenArea screenArea) {

        //Initialize the tab buttons
        this.tabButtonPositioner = this.getTabButtonPositioner();
        this.tabButtonPositioner.clear();
        this.addChild(this.tabButtonPositioner);
        this.clientTabs.forEach((key,tab) -> {
            TabButton button = new TabButton(b -> this.ChangeTab(key),tab)
                    .withAddons(EasyAddonHelper.activeCheck(() -> this.currentTabIndex != key),
                            EasyAddonHelper.visibleCheck(() -> tab.tabVisible() && tab.commonTab.canOpen(this.menu.player)));
            this.addChild(button);
            this.tabButtonPositioner.addWidget(button);
        });
        //Initialize screen-related widgets
        this.init(screenArea);
        //Initialize the currently opened tab
        this.currentTab().onOpen();
    }


    @Nonnull
    protected abstract IWidgetPositioner getTabButtonPositioner();

    protected abstract void init(ScreenArea screenArea);

    public final void ChangeTab(int newTab) { this.ChangeTab(newTab,null); }
    public final void ChangeTab(int newTab, @Nullable LazyPacketData.Builder data) { this.ChangeTab(newTab, data == null ? null : data.build(), true); }
    private void ChangeTab(int newTab, @Nullable LazyPacketData data, boolean sendPacket) {
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
        if(sendPacket)
            this.menu.ChangeTab(newTab,data);
        //If sendPacket = false, then this was *from* a packet and thus the menu already knows about the tab being changed
    }

    @Override
    protected final void renderBG(@Nonnull EasyGuiGraphics gui) {
        this.renderBackground(gui);
        this.currentTab().renderBG(gui);
    }
    protected abstract void renderBackground(@Nonnull EasyGuiGraphics gui);

    @Override
    protected final void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        this.renderLate(gui);
        this.currentTab().renderAfterWidgets(gui);
    }
    protected void renderLate(@Nonnull EasyGuiGraphics gui) {}

    protected final void HandleClientMessage(@Nonnull LazyPacketData message)
    {
        if(message.contains("ChangeTab"))
        {
            this.ChangeTab(message.getInt("ChangeTab"),message,false);
        }
    }

    @Override
    public boolean blockInventoryClosing() { return this.currentTab() != null && this.currentTab().blockInventoryClosing(); }

}
