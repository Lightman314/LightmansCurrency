package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TraderSettingsClientTab extends TraderStorageClientTab<TraderSettingsTab> {

    private int selectedTab = 0;
    private final List<TabButton> tabButtons = new ArrayList<>();
    private final List<SettingsSubTab> tabs = new ArrayList<>();

    private SettingsSubTab getCurrentTab() {
        if(this.selectedTab < 0 || this.selectedTab >= this.tabs.size())
            this.selectedTab = 0;
        if(!this.tabs.isEmpty())
            return this.tabs.get(this.selectedTab);
        return null;
    }

    public TraderSettingsClientTab(Object screen, TraderSettingsTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_SETTINGS; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS.get(); }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.tabButtons.clear();
        this.refreshTabs();

        //Set up the "Current Tab"
        this.getCurrentTab().onOpen();

        this.tick();

        //Hide Coin Slots
        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void closeAction() {
        //Reveal Coin Slots
        this.menu.SetCoinSlotsActive(true);
    }

    public void refreshTabs()
    {
        //Collect tabs
        this.tabs.clear();
        //Reset Selected Tabs
        this.selectedTab = 0;
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.tabs.addAll(trader.getSettingsTabs(this));

        //Remove Existing Tab Buttons
        for(TabButton b : this.tabButtons)
            this.screen.removeChild(b);
        this.tabButtons.clear();

        //Create Tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;
            SettingsSubTab tab = this.tabs.get(i);
            TabButton newButton = this.addChild(new TabButton(b -> this.openTab(tabIndex), tab)
                    .withAddons(EasyAddonHelper.visibleCheck(tab::canOpen),
                            EasyAddonHelper.activeCheck(() -> this.selectedTab != tabIndex)));
            newButton.visible = tab.canOpen();
            this.tabButtons.add(newButton);
        }

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

        //Position buttons
        ScreenPosition corner = this.screen.getCorner();
        int yPos = 0;
        for(TabButton button : this.tabButtons)
        {
            if(button.visible)
            {
                button.reposition(corner.offset(this.screen.getXSize(), yPos), 1);
                yPos += TabButton.SIZE;
            }
        }

        //Force close the tab if they don't have access to it anymore
        if(!this.getCurrentTab().canOpen() && this.selectedTab != 0)
            this.openTab(0);

        this.getCurrentTab().tick();

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        this.getCurrentTab().renderBG(gui);
    }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        this.getCurrentTab().renderAfterWidgets(gui);
    }
    @Override
    public boolean shouldRenderInventoryText() { return this.getCurrentTab().shouldRenderInventoryText(); }

}
