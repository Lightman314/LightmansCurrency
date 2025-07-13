package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.rules;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RuleSubNode extends SettingsSubNode<SettingsNode> {

    private final ITradeRuleHost host;
    private final Predicate<LoadContext> loadable;
    private final MutableComponent name;

    public RuleSubNode(SettingsNode parent, ITradeRuleHost host, Predicate<LoadContext> loadable, MutableComponent name)
    {
        super(parent);
        this.host = host;
        this.loadable = loadable;
        this.name = name;
    }

    @Override
    public String getSubKey() { return "trade_rules"; }

    @Override
    public MutableComponent getName() { return this.name.copy(); }

    @Override
    public boolean allowLoading(LoadContext context) {
        return this.loadable.test(context);
    }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        for(TradeRule rule : this.host.getRules())
        {
            if(rule.isActive() && rule instanceof ICopySupportingRule csr)
            {
                SavedSettingData.MutableNodeAccess ruleNode = data.forSubNode(rule.type.type.toString());
                csr.writeSettings(ruleNode);
            }
        }
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        for(TradeRule rule : this.host.getRules())
        {
            if(rule instanceof ICopySupportingRule csr)
            {
                SavedSettingData.NodeAccess ruleNode = data.forSubNode(rule.type.type.toString());
                if(ruleNode.isEmpty())
                {
                    //If no data is present for this rule, reset the rule to inactive and default settings
                    rule.setActive(false);
                    csr.resetToDefaultState();
                }
                else
                {
                    //If data *is* present for this rule, activate it and load the settings
                    rule.setActive(true);
                    csr.loadSettings(ruleNode);
                }
            }
        }
    }

    @Override
    public void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        int count = 0;
        for(TradeRule rule : this.host.getRules())
        {
            SavedSettingData.NodeAccess ruleNode = data.forSubNode(rule.type.type.toString());
            if(!ruleNode.isEmpty())
                count++;
        }
        lineWriter.accept(LCText.DATA_ENTRY_RULES_COUNT.get(count));
    }

}
