package io.github.lightman314.lightmanscurrency.api.traders.rules.client;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TradeRulesClientSubTab extends EasyTab {

    public final TradeRulesClientTab<?> parent;
    public final TradeRulesTab commonTab;
    public final ITraderStorageScreen screen;
    public final ITraderStorageMenu menu;

    private final List<Object> children = new ArrayList<>();

    public List<TradeRule> getTradeRules()
    {
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null)
            return host.getRules();
        return new ArrayList<>();
    }

    public List<TradeRule> getFilteredRules() { return this.filterRules(this.getTradeRules()); }

    /**
     * Hides trade rules that cannot be activated in the trader/trades current state.
     */
    protected final  List<TradeRule> filterRules(List<TradeRule> rules) { return rules.stream().filter(r -> r.canPlayerActivate(this.menu.getPlayer())).collect(Collectors.toList()); }

    protected TradeRulesClientSubTab(TradeRulesClientTab<?> parent)
    {
        super(parent.screen);
        this.parent = parent;
        this.commonTab = this.parent.commonTab;
        this.screen = this.parent.screen;
        this.menu = this.parent.menu;
    }

    public abstract boolean isVisible();

    @Override
    public <T> T addChild(T child)
    {
        if(!this.children.contains(child))
            this.children.add(child);
        return this.parent.addChild(child);
    }

    @Override
    public void removeChild(Object child)
    {
        this.children.remove(child);
        this.parent.removeChild(child);
    }

    @Override
    protected final void closeAction() {
        this.children.forEach(this.parent::removeChild);
        this.children.clear();
        this.onSubtabClose();
    }

    protected void onSubtabClose() {}

}
