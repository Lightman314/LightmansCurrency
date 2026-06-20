package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeGroup;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemTradesNode extends TradingNode<ItemTradeData> {

    private static final MapCodec<ItemTradesNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.optionalFieldOf("baseCount",1).forGetter(n -> n.baseCount),
            ItemTradeData.CODEC.listOf().fieldOf("trades").forGetter(ItemTradesNode::getMutableTrades)
    ).apply(builder,ItemTradesNode::new));
    public static final TraderNodeType<ItemTradesNode> TYPE = TraderNodeType.simple(ItemTradesNode::new,MAP_CODEC);
    public static final TraderNodeGroup<ItemTradesNode> GROUP = new TraderNodeGroup<>(TYPE);

    private int baseCount = 1;
    private final List<ItemTradeData> trades = new ArrayList<>();

    private ItemTradesNode() {}
    private ItemTradesNode(int count,List<ItemTradeData> trades)
    {
        this.baseCount = count;
        this.trades.addAll(trades);
        this.attachTrades();
    }

    @Override
    public void updateArgument(Optional<Object> argument) {
        if(argument.isPresent() && argument.get() instanceof Number n)
            this.baseCount = Math.clamp(n.intValue(),1,TraderData.GLOBAL_TRADE_LIMIT);
    }

    @Override
    protected ItemTradeData createNewTrade() { return new ItemTradeData(); }

    @Override
    protected List<ItemTradeData> getMutableTrades() { return this.trades; }

    @Override
    public Component getSetLabel() {
        //TODO translate
        return Component.literal("Item Trades");
    }

    @Override
    public TradeResult executeTrade(TradeContext context, int tradeIndex) throws TradeFailedException {
        ItemTradeData trade = this.getTrade(tradeIndex);
        if(trade == null)
            return TradeResult.FAIL_NULL;
        return TradeResult.FAIL_NOT_SUPPORTED;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

}