package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Menu Screen for menus that only have tabs on the client-side.<br>
 * @see EasyTabbedMenuScreen
 * @see io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu EasyTabbedMenu
 */
public abstract class EasyClientTabbedMenuScreen<M extends AbstractContainerMenu,S extends EasyClientTabbedMenuScreen<M,S,T>,T extends EasyClientTab<M,S,T>> extends EasyMenuScreen<M> {

    private boolean tabsLocked = false;
    private int currentTabIndex = -1;
    private int currentAddIndex = 0;
    @Nonnull
    public final T currentTab() { return this.menuTabs.get(this.currentTabIndex); }
    private Map<Integer,T> menuTabs = null;
    public final Map<Integer,T> getAllTabs() { return this.menuTabs == null ? ImmutableMap.of() : ImmutableMap.copyOf(this.menuTabs); }

    private IWidgetPositioner tabButtonPositioner;

    protected EasyClientTabbedMenuScreen(M menu,Inventory inventory) { super(menu,inventory); }
    protected EasyClientTabbedMenuScreen(M menu,Inventory inventory,Component title) { super(menu,inventory,title); }

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
            throw new IllegalArgumentException("EasyClientTabbedMenuScreen#registerTabs did not register a tab for key 0!");
        this.tabsLocked = true;
        //Change to the default tab
        this.ChangeTab(0);
    }

    @Override
    protected final void initialize(ScreenArea screenArea) {
        //Initialize screen-related widgets
        this.init(screenArea);
        //Initialize the tab buttons
        this.tabButtonPositioner = this.getTabButtonPositioner();
        this.tabButtonPositioner.clear();
        this.addChild(this.tabButtonPositioner);
        this.menuTabs.forEach((key,tab) -> {
            TabButton button = TabButton.builder()
                    .pressAction(() -> this.ChangeTab(key))
                    .tab(tab)
                    .addon(EasyAddonHelper.activeCheck(() -> this.currentTabIndex != key))
                    .addon(EasyAddonHelper.visibleCheck(tab::tabVisible))
                    .build();
            this.addChild(button);
            this.tabButtonPositioner.addWidget(button);
        });
        //Initialize the currently opened tab
        this.currentTab().onOpen();
    }

    @Nonnull
    protected abstract IWidgetPositioner getTabButtonPositioner();

    protected abstract void init(ScreenArea screenArea);

    /**
     * Called during {@link #initializeTabs()} to let the subclass know that the menu is ready to register menu tabs
     */
    protected abstract void registerTabs();

    /**
     * Simpler version of {@link #setTab(int, T)} but for when the key is considered irrelevant and can be generated automatically
     */
    public final void addTab(@Nonnull T tab)
    {
        if(this.tabsLocked || this.menuTabs == null)
            this.setTab(this.currentAddIndex,tab);
        else
            this.setTab(this.currentAddIndex++,tab);
    }

    /**
     * Called by subclass during {@link #registerTabs()} to register the relevant tabs
     */
    public final void setTab(int key, @Nonnull T tab)
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

    private void ChangeTab(int newTab)
    {
        if(newTab == this.currentTabIndex)
            return;
        if(!this.menuTabs.containsKey(newTab))
        {
            LightmansCurrency.LogError("Attempted to open tab " + newTab + ", but no tab with that key is present in the menu!");
            return;
        }
        if(this.currentTab() != null)
            this.currentTab().onClose();
        this.currentTabIndex = newTab;
        this.currentTab().onOpen();
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

    @Override
    public boolean blockInventoryClosing() { return this.currentTab() != null && this.currentTab().blockInventoryClosing(); }

    @Nullable
    @Override
    public Pair<ItemStack,ScreenArea> getHoveredItem(@Nonnull ScreenPosition mousePos) { return this.currentTab().getHoveredItem(mousePos); }
    @Nullable
    @Override
    public Pair<FluidStack,ScreenArea> getHoveredFluid(@Nonnull ScreenPosition mousePos) { return this.currentTab().getHoveredFluid(mousePos); }

}