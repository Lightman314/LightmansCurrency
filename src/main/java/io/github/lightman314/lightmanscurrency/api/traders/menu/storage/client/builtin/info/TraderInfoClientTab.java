package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientInfoTabProvider;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.SmallTabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TraderInfoTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TraderInfoClientTab extends TraderStorageClientTab<TraderInfoTab> {

    //private LazyWidgetPositioner tabPositioner;
    private int selectedTab = 0;
    private final List<InfoSubTab> tabs = new ArrayList<>();

    private InfoSubTab getCurrentTab() {
        if(this.selectedTab < 0 || this.selectedTab >= this.tabs.size())
            this.selectedTab = 0;
        if(!this.tabs.isEmpty())
            return this.tabs.get(this.selectedTab);
        return null;
    }

    private int getFirstSelectableTab() {
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            if(this.tabs.get(i).canOpen())
                return i;
        }
        return 0;
    }

    public TraderInfoClientTab(Object screen, TraderInfoTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_INFO.get(); }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        LazyWidgetPositioner tabPositioner = this.addChild(LazyWidgetPositioner.create(this.screen,LazyWidgetPositioner.createClockwiseWraparound(screenArea,WidgetRotation.RIGHT),SmallTabButton.SIZE));

        this.tabs.clear();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.tabs.addAll(IClientInfoTabProvider.getInfoSubtabs(trader,this));

        if(firstOpen)
            this.selectedTab = this.getFirstSelectableTab();

        //Create Tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;
            InfoSubTab tab = this.tabs.get(i);
            SmallTabButton newButton = this.addChild(SmallTabButton.builder()
                    .pressAction(() -> this.openTab(tabIndex))
                    .tab(tab)
                    .addon(EasyAddonHelper.visibleCheck(tab::canOpen))
                    .addon(EasyAddonHelper.activeCheck(() -> this.selectedTab != tabIndex))
                    .build());
            tabPositioner.addWidget(newButton);
        }

        //Set up the "Current Tab"
        this.getCurrentTab().onOpen();

        this.tick();

    }

    public void openTab(int index)
    {
        if(index == this.selectedTab || index < 0 || index >= this.tabs.size())
            return;
        this.getCurrentTab().onClose();
        this.selectedTab = index;
        this.getCurrentTab().onOpen();
    }

    @Override
    public void tick() {

        //Force close the tab if they don't have access to it anymore
        if(!this.getCurrentTab().canOpen() && this.selectedTab != 0)
            this.openTab(0);

        this.getCurrentTab().tick();

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        this.getCurrentTab().renderBG(gui);
    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) { this.getCurrentTab().renderAfterWidgets(gui); }
    @Override
    public boolean shouldRenderInventoryText() { return this.getCurrentTab().shouldRenderInventoryText(); }
    @Override
    public boolean showRightEdgeButtons() { return false; }

}
