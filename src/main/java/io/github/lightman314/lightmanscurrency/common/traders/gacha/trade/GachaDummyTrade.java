package io.github.lightman314.lightmanscurrency.common.traders.gacha.trade;

import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.DummyTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GachaDummyTrade extends DummyTrade {

    public static final StreamCodec<ByteBuf,GachaDummyTrade> STREAM_CODEC = StreamHelper.unit(GachaDummyTrade::new);

    public GachaDummyTrade() { }

    @Override
    public MoneyValue getCost() {
        GachaNode node = this.getNode(GachaNode.TYPE);
        return node == null ? MoneyValue.empty() : node.getPrice();
    }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public int getStock(TradeContext context) {
        GachaStorageNode node = this.getNode(GachaStorageNode.TYPE);
        return node == null ? 0 : node.getStorage().getItemCount();
    }

    @Override
    public TradeComparisonResult compare(TradeData expectedTrade) { return new TradeComparisonResult(); }
    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }
    @Override
    public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return List.of(); }

    @Override
    public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }
    @Override
    public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }
    @Override
    public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { }

}
