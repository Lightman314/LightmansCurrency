package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;
import java.util.function.Consumer;

public abstract class TraderStorageClientTabWithSubTabs<T extends TraderStorageTab,S extends TraderStorageClientTab<T>> extends TraderStorageClientTab<T> {

    private boolean locked = false;
    private int currentTab = 0;
    private final List<S> subTabs = new ArrayList<>();
    protected final int getCurrentTabSlot() { return this.currentTab; }
    @Nullable
    protected final S getCurrentTab() {
        if(this.subTabs.isEmpty())
            return null;
        return this.subTabs.get(this.currentTab);
    }

    protected final int getSubtabCount() { return this.subTabs.size(); }
    protected final boolean hasSubtabs() { return !this.subTabs.isEmpty(); }

    protected final void assertSubtabsLoaded() {
        if(!this.locked) {
            this.collectSubtabs(this::addTab);
            this.locked = true;
        }
    }

    private IWidgetPositioner positioner;

    protected TraderStorageClientTabWithSubTabs(TraderStorageMenu menu,T commonTab,TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    public final void setTab(int tab) {
        if(tab == this.currentTab || tab < 0 || tab >= this.subTabs.size())
            return;
        S oldTab = this.getCurrentTab();
        oldTab.removeAllChildren();
        oldTab.onTabClosed();
        this.currentTab = tab;
        S newTab = this.getCurrentTab();
        this.addChild(newTab);
        newTab.onTabOpened(FancyPacketMap.EMPTY);
    }

    private void addTab(S tab) {
        if(this.locked)
            throw new IllegalStateException("Cannot add more tabs after it has been locked!");
        this.subTabs.add(Objects.requireNonNull(tab));
    }

    protected abstract void collectSubtabs(Consumer<S> builder);

    protected IWidgetPositioner getTabButtonPositioner() { return WidgetPositioner.rightEdge(this.getScreen(),TabButton.SIZE); }

    @Override
    public boolean showRightEdgeWidgets() { return false; }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        this.assertSubtabsLoaded();

        //Add tab buttons
        this.positioner = this.addChild(this.getTabButtonPositioner());
        for(int i = 0; i < this.subTabs.size(); ++i) {
            final int index = i;
            TabButton button = this.addChild(TabButton.builder()
                    .forTab(this.subTabs.get(i))
                    .onPress(() -> this.setTab(index))
                    .active(() -> this.currentTab != index)
                    .build());
            this.positioner.addWidgets(button);
        }

        //Initialize the current tab
        S currentTab = this.getCurrentTab();
        if(currentTab != null) {
            this.addChild(currentTab);
            currentTab.onTabOpened(message);
        }
    }

    //Protected method for easy scroll arrow addition,
    // but not forced into the normal initialize method so that it's left optional
    //Should be called after the normal initialize method so that it applies to the correct widget positioner
    protected final void addScrollArrows(ScreenArea area) {
        if(this.positioner instanceof IScrollable scrollable) {
            //Up arrow at the top-right
            this.addChild(IconButton.builder()
                    .atPos(area.cornerTopRight().offset(0,IconButton.SIZE * -1))
                    .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_up")))
                    .onPress(scrollable::decrementScroll)
                    .visible(scrollable::showScrollability)
                    .build());
            //Down arrow at the botton-right
            this.addChild(IconButton.builder()
                    .atPos(area.cornerBottomRight())
                    .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_down")))
                    .onPress(scrollable::incrementScroll)
                    .visible(scrollable::showScrollability)
                    .build());
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        S tab = this.getCurrentTab();
        if(tab != null)
            tab.extractBackground(gui,area);
    }

    //Reset the selected tab when the tab is closed
    @Override
    protected void afterTabClosed() { super.afterTabClosed(); this.currentTab = 0; }

    public static abstract class SubTab<C extends TraderStorageTab> extends TraderStorageClientTab<C> {

        protected SubTab(TraderStorageClientTabWithSubTabs<C,?> parent) { super(parent.getMenu(),parent.getCommonTab(),parent.getScreen()); }

        @Override
        protected final void initialize(ScreenArea area, FancyPacketMap message) { this.initialize(area); }
        protected abstract void initialize(ScreenArea area);

    }

}
