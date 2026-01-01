package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RegisterTradeRuleTabsEvent extends Event implements IModBusEvent {

    private final Map<TradeRuleType<?>,Function<TradeRulesClientTab<?>,TradeRulesClientSubTab>> results = new HashMap<>();

    public RegisterTradeRuleTabsEvent() {}

    public Map<TradeRuleType<?>,Function<TradeRulesClientTab<?>,TradeRulesClientSubTab>> getTabBuilders() { return ImmutableMap.copyOf(this.results); }

    public void register(TradeRuleType<?> type,Function<TradeRulesClientTab<?>,TradeRulesClientSubTab> builder)
    {
        if(this.results.put(type,builder) != null)
            LightmansCurrency.LogWarning("Duplicate trade rule tab builder was registered for " + type);
    }

}