package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.SmallTabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class TradeRulesClientTab<T extends TradeRulesTab> extends TraderStorageClientTab<T> {

    private int selectedTab = 0;
    private final List<TradeRulesClientSubTab> tabs = new ArrayList<>();
    private final List<SmallTabButton> tabButtons = new ArrayList<>();
    private LazyWidgetPositioner widgetPositioner = null;

    private TradeRulesClientSubTab getCurrentTab() {
        if(this.selectedTab < 0 || this.selectedTab >= this.tabs.size())
            this.selectedTab = 0;
        if(!this.tabs.isEmpty())
            return this.tabs.get(this.selectedTab);
        return null;
    }

    protected TradeRulesClientTab(Object screen, T commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADE_RULES; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.tabs.clear();
        this.tabButtons.clear();
        this.widgetPositioner = null;
        if(firstOpen)
            this.selectedTab = 0;

        //Collect tabs
        this.refreshTabs(firstOpen);

        //Set up the "Current Tab"
        this.getCurrentTab().onOpen();

        this.tick();

    }

    public void refreshTabs(boolean fullReset)
    {

        TradeRulesClientSubTab oldTab = null;
        if(!this.tabs.isEmpty())
        {
            oldTab = this.getCurrentTab();
            if(oldTab != null)
                oldTab.onClose();
        }

        //Collect tabs
        this.tabs.clear();
        this.tabs.add(new RuleToggleTab(this));
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null)
        {
            for(TradeRule rule : host.getRules())
            {
                try{
                    this.tabs.add(rule.createTab(this));
                } catch(Throwable t) {
                    LightmansCurrency.LogError("Trade Rule of type '" + rule.type + "' encountered an error creating its tab. Trade Rule will not be editable!", t);
                }
            }
        }

        if(this.widgetPositioner == null)
            this.widgetPositioner = this.addChild(LazyWidgetPositioner.create(this.screen,LazyWidgetPositioner.createTopdown(WidgetRotation.RIGHT),ScreenPosition.of(this.screen.getArea().width,0), SmallTabButton.SIZE));
        else
            this.widgetPositioner.clear();

        //Create Tab buttons
        for(SmallTabButton button : this.tabButtons)
            this.removeChild(button);
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;

            TradeRulesClientSubTab tab = this.tabs.get(tabIndex);
            SmallTabButton button = this.addChild(SmallTabButton.builder()
                    .pressAction(() -> this.openTab(tabIndex))
                    .tab(tab)
                    .addon(EasyAddonHelper.visibleCheck(tab::isVisible))
                    .addon(EasyAddonHelper.activeCheck(() -> tabIndex != this.selectedTab))
                    .build());
            this.tabButtons.add(button);
            this.widgetPositioner.addWidget(button);
        }

        if(oldTab == null || this.selectedTab >= this.tabs.size() || oldTab.getClass() != this.getCurrentTab().getClass())
            this.selectedTab = 0;

        this.getCurrentTab().onOpen();

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

        //Check if tab count is correct, and if not refresh the tabs
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null && this.tabs.size() != host.getRules().size() + 1)
            this.refreshTabs(false);

        this.getCurrentTab().tick();

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderBG(gui); }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderAfterWidgets(gui); }

    @Override
    public boolean showRightEdgeButtons() { return false; }

    public static class Trader extends TradeRulesClientTab<TradeRulesTab.Trader>
    {

        public Trader(Object screen, TradeRulesTab.Trader commonTab) { super(screen, commonTab); }

        @Override
        public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_TRADE_RULES_TRADER.get(); }

    }

    public static class Trade extends TradeRulesClientTab<TradeRulesTab.Trade>
    {
        public Trade(Object screen, TradeRulesTab.Trade commonTab) { super(screen, commonTab); }

        @Override
        public boolean tabVisible() { return false; }

        @Override
        public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE.get(); }

    }

}
