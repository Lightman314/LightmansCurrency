package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.PreviousTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TradeTradeRuleTab extends AbstractTradeRuleTab {

    public static final Identifier KEY = LCApi.id("trade_trade_rules");

    private int nodeIndex = -1;
    private int tradeIndex = -1;
    @Nullable
    private PreviousTab previousTab;

    public TradeTradeRuleTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public boolean hasSpecialOpenRequirement() { return true; }

    @Nullable
    @Override
    public PreviousTab getPreviousTab() { return this.previousTab; }

    public static void open(TraderStorageMenu menu,int nodeIndex,int tradeIndex) { open(menu,nodeIndex,tradeIndex,Optional.empty());}
    public static void open(TraderStorageMenu menu,int nodeIndex,int tradeIndex,@Nullable PreviousTab previousTab) { open(menu,nodeIndex,tradeIndex,Optional.ofNullable(previousTab)); }
    public static void open(TraderStorageMenu menu,int nodeIndex,int tradeIndex,Optional<PreviousTab> previousTab) {
        menu.changeTab(KEY,FancyPacketMap.map()
                .setInt("trade_set",nodeIndex)
                .setInt("trade_index",tradeIndex)
                .setOptionalMap("previous",previousTab.map(PreviousTab::asMap)));
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) {
        this.nodeIndex = additional.getInt("trade_set",-1);
        this.tradeIndex = additional.getInt("trade_index",-1);
        this.previousTab = PreviousTab.parseMap(additional.getMap("previous"));
    }

    @Override
    public void onTabClosed() {
        this.nodeIndex = -1;
        this.tradeIndex = -1;
        this.previousTab = null;
    }

    @Nullable
    @Override
    public TradeRuleHolder getRuleHolder() {
        List<TradingNode<?>> tradingNodes = this.getTradingNodes();
        if(this.nodeIndex < 0 || this.nodeIndex >= tradingNodes.size())
            return null;
        TradingNode<?> node = tradingNodes.get(this.nodeIndex);
        TradeData trade = node.getTrade(this.tradeIndex);
        if(trade instanceof TradeRuleHolder holder)
            return holder;
        return null;
    }

    @Override
    public Identifier getKey() { return KEY; }

}
