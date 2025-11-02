package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.rules.RuleSubNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TradeSettings<T extends TraderData> extends EasyTraderSettingsNode<T> {

    public TradeSettings(String key, T trader) { super(key, trader); }

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
    protected abstract ITradeRuleHost getRuleHost(int tradeIndex);
    protected abstract SettingsSubNode<?> createTradeNode(int tradeIndex);

}