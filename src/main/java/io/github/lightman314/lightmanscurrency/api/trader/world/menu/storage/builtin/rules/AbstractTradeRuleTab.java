package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.PreviousTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;

public abstract class AbstractTradeRuleTab extends TraderStorageTab {

    public static final TextEntry TOOLTIP_TRADER_TRADE_RULES_TRADER = TextEntry.tooltip(LCApi.MODID,"trader.trade_rules.trader");
    public static final TextEntry TOOLTIP_TRADER_TRADE_RULES_TRADE = TextEntry.tooltip(LCApi.MODID,"trader.trade_rules.trade");
    public static final TextEntry TOOLTIP_TRADE_RULES_MANAGER = TextEntry.tooltip(LCApi.MODID,"trade_rule.manager");
    public static final TextEntry GUI_TRADE_RULES_LIST = TextEntry.gui(LCApi.MODID,"trade_rule.list");

    public AbstractTradeRuleTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public int getTabSortPriority() { return 0; }

    @Nullable
    public abstract TradeRuleHolder getRuleHolder();

    public boolean hasSpecialOpenRequirement() { return false; }

    @Nullable
    public PreviousTab getPreviousTab() { return null; }

    public final void goBack() {
        PreviousTab previousTab = this.getPreviousTab();
        if(previousTab != null)
            previousTab.open(this.getMenu());
    }

    public final boolean hasRule(TradeRuleType<?> type) {
        TradeRuleHolder holder = this.getRuleHolder();
        if(holder == null || type == null)
            return false;
        return holder.getRuleOfType(type) != null;
    }

    public final <T extends TradeRule> T getRule(TradeRuleType<T> type) {
        Objects.requireNonNull(type,"Cannot get a Trade Rule with no type given!");
        TradeRuleHolder holder = this.getRuleHolder();
        if(holder == null)
            return type.createNew();
        try {
            T result = (T)holder.getRuleOfType(type);
            if(result != null)
                return result;
        } catch (ClassCastException e) { LightmansCurrency.LogError("Error casting Trade Rule of type " + type,e); }
        T dummy = type.createNew();
        dummy.attach(holder);
        return dummy;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean canOpen() { return this.hasRequiredPermissions(); }

    protected boolean hasRequiredPermissions() { return this.getPermission(BuiltInPermissions.EDIT_TRADE_RULES); }

    public final void requestRuleChange(TradeRuleType<?> type,FancyPacketMap request) {
        if(this.isClient()) {
            this.sendToServer(FancyPacketMap.map().setMap("ruleRequest",FancyPacketMap.map()
                    .setRegistryEntry("type",LCRegistries.Trader.TRADE_RULE_TYPE,type)
                    .setMap("request",request)));
        }
        if(this.hasRequiredPermissions()) {
            TradeRule rule = this.getRule(type);
            if(rule != null)
                rule.handlePlayerRequest(this.getPlayer(),request);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("ruleRequest")) {
            FancyPacketMap entry = message.getMap("ruleRequest");
            TradeRuleType<?> type = entry.getRegistryEntry("type",LCRegistries.Trader.TRADE_RULE_TYPE);
            this.requestRuleChange(type,entry.getMap("request"));
        }
    }

}