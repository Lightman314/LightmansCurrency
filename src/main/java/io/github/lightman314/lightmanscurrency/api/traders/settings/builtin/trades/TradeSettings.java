package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.rules.RuleSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TradeSettings<T extends TradeData,N extends TradeOfferSourceNode<T>> extends EasyTraderNodeSettings<TraderData,N> {

    public TradeSettings(String key, TraderData trader, N node) { super(key,trader,node); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_TRADES.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_TRADES; }

    @Override
    public List<SettingsSubNode<?>> getSubNodes() {
        List<SettingsSubNode<?>> subNodes = new ArrayList<>();
        for(int i = 0; i < this.getTradeCount(); ++i)
        {
            subNodes.add(this.createTradeNode(i));
            ITradeRuleHost host = this.getRuleHost(i);
            if(host != null)
                subNodes.add(new RuleSubNode(this,host,c -> this.allowLoading(c) && c.hasPermission(Permissions.EDIT_TRADE_RULES), LCText.DATA_CATEGORY_RULES_TRADE.get(i + 1),i));
        }
        return subNodes;
    }

    protected int getTradeCount() { return this.trader.getTradeCount(); }
    @Nullable
    protected ITradeRuleHost getRuleHost(int tradeIndex)
    {
        T trade = this.node.getTrade(tradeIndex);
        if(trade instanceof ITradeRuleHost h)
            return h;
        return null;
    }
    protected abstract SettingsSubNode<?> createTradeNode(int tradeIndex);

}
