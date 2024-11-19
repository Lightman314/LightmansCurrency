package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trade_rules.TradeRulesTab;
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

        //Collect tabs
        this.tabs.clear();
        this.tabs.add(new RuleToggleTab(this));
        //Reset Selected Tabs
        if(firstOpen)
            this.selectedTab = 0;
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

        LazyWidgetPositioner tabPositioner = this.addChild(LazyWidgetPositioner.create(this.screen,LazyWidgetPositioner.createTopdown(WidgetRotation.RIGHT),ScreenPosition.of(screenArea.width,0),25));

        //Create Tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;

            TradeRulesClientSubTab tab = this.tabs.get(tabIndex);
            TabButton button = this.addChild(TabButton.builder()
                    .pressAction(() -> this.openTab(tabIndex))
                    .tab(tab)
                    .addon(EasyAddonHelper.visibleCheck(tab::isVisible))
                    .addon(EasyAddonHelper.activeCheck(() -> tabIndex != this.selectedTab))
                    .build());
            tabPositioner.addWidget(button);
        }

        //Set up the "Current Tab"
        this.getCurrentTab().onOpen();

        this.tick();

        //Hide Coin Slots
        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void closeAction() {
        //Show Coin Slots
        this.menu.SetCoinSlotsActive(true);
    }

    public void refreshTabs()
    {
        //Collect tabs
        this.tabs.clear();
        this.tabs.add(new RuleToggleTab(this));
        //Reset Selected Tabs
        this.selectedTab = 0;
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

        this.getCurrentTab().tick();

    }

    protected boolean hasBackButton() { return false; }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderBG(gui); }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderAfterWidgets(gui); }

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
