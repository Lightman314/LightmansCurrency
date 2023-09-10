package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trade_rules.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class TradeRulesClientTab<T extends TradeRulesTab> extends TraderStorageClientTab<T> {

    private int selectedTab = 0;
    private final List<TabButton> tabButtons = new ArrayList<>();
    private final List<TradeRulesClientSubTab> tabs = new ArrayList<>();

    private TradeRulesClientSubTab getCurrentTab() {
        if(this.selectedTab < 0 || this.selectedTab >= this.tabs.size())
            this.selectedTab = 0;
        if(this.tabs.size() > 0)
            return this.tabs.get(this.selectedTab);
        return null;
    }

    protected TradeRulesClientTab(Object screen, T commonTab) { super(screen, commonTab); }


    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TRADE_RULES; }

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

        //Remove Existing Tab Buttons
        for(TabButton b : this.tabButtons)
            this.removeChild(b);
        this.tabButtons.clear();

        //Create Tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;
            this.tabButtons.add(this.addChild(new TabButton(b -> this.openTab(tabIndex), this.tabs.get(tabIndex))));
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

        //Update Tab Button visiblity
        for(int i = 0; i < this.tabs.size() && i < this.tabButtons.size(); ++i)
        {
            this.tabButtons.get(i).visible = this.tabs.get(i).isVisible();
            this.tabButtons.get(i).active = this.selectedTab != i;
        }

        //Reposition buttons
        ScreenArea screenArea = this.screen.getArea();
        int yPos = 0;
        for(TabButton button : this.tabButtons)
        {
            if(button.visible)
            {
                button.reposition(screenArea.pos.offset(screenArea.width, yPos), 1);
                yPos += TabButton.SIZE;
            }
        }

        this.getCurrentTab().tick();

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderBG(gui); }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) { this.getCurrentTab().renderAfterWidgets(gui); }

    public static class Trader extends TradeRulesClientTab<TradeRulesTab.Trader>
    {

        public Trader(Object screen, TradeRulesTab.Trader commonTab) { super(screen, commonTab); }

        @Override
        public MutableComponent getTooltip() { return IconAndButtonUtil.TOOLTIP_TRADE_RULES_TRADER; }

    }

    public static class Trade extends TradeRulesClientTab<TradeRulesTab.Trade>
    {
        public Trade(Object screen, TradeRulesTab.Trade commonTab) { super(screen, commonTab); }

        @Override
        public boolean tabButtonVisible() { return false; }

        @Override
        public MutableComponent getTooltip() { return IconAndButtonUtil.TOOLTIP_TRADE_RULES_TRADE; }

        @Override
        public void receiveSelfMessage(CompoundTag message) { this.commonTab.receiveMessage(message); }
    }

}
