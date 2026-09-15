package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;

import java.util.List;

public class ItemTradesNode extends AbstractItemTradesNode<ItemTradeData> {

    private static final MapCodec<ItemTradesNode> MAP_CODEC = buildCodec(ItemTradeData.CODEC,ItemTradesNode::new);

    public static final TraderNodeType<ItemTradesNode> TYPE = TraderNodeType.simple(ItemTradesNode::new,MAP_CODEC);

    public static final TextEntry SECTION_TITLE = sectionName(TYPE);

    protected ItemTradesNode() {}
    protected ItemTradesNode(int count,int upgradeCount,List<ItemTradeData> trades) { super(count,upgradeCount,trades); }

    @Override
    protected ItemTradeData createNewTrade() { return new ItemTradeData(); }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

}