package io.github.lightman314.lightmanscurrency.api.trader.data.templates;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;

public abstract class NormalTraderType extends TraderType {

    @Override
    @OverridingMethodsMustInvokeSuper
    public void addNodes(NodeCollector collector) {
        collector.addNode(OwnerNode.TYPE);
        collector.addNode(WorldNode.TYPE);
        collector.addNode(DisplayNode.TYPE);
        collector.addNode(AlliesNode.TYPE);
        collector.addNode(NotificationNode.TYPE);
        collector.addNode(MoneyStorageNode.TYPE);
        collector.addNode(UpgradeNode.TYPE,5);
        collector.addNode(TradeRulesNode.TYPE);
        collector.addNode(SettingsNode.TYPE);
        collector.addNode(TraderStatsNode.TYPE);
        this.addAdditionalNodes(collector);
    }

    protected abstract void addAdditionalNodes(NodeCollector collector);

}