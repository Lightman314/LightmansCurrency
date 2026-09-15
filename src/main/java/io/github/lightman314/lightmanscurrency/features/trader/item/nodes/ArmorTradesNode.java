package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ArmorTradeData;

import java.util.List;

public class ArmorTradesNode extends AbstractItemTradesNode<ArmorTradeData> {

    private static final MapCodec<ArmorTradesNode> MAP_CODEC = buildCodec(ArmorTradeData.CODEC,ArmorTradesNode::new);

    public static final TraderNodeType<ArmorTradesNode> TYPE = TraderNodeType.simple(ArmorTradesNode::new,MAP_CODEC);

    protected ArmorTradesNode() {}
    protected ArmorTradesNode(int baseCount,int upgradeCount,List<ArmorTradeData> trades) { super(baseCount,upgradeCount,trades); }

    @Override
    protected ArmorTradeData createNewTrade() { return new ArmorTradeData(); }
    @Override
    public TraderNodeType<?> getType() { return TYPE; }

}
