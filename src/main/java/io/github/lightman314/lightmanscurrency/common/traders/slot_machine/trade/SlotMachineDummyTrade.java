package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.DummyTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SlotMachineDummyTrade extends DummyTrade {

    public static final StreamCodec<ByteBuf,SlotMachineDummyTrade> STREAM_CODEC = StreamHelper.unit(SlotMachineDummyTrade::new);

    public SlotMachineDummyTrade() { }

    @Override
    public int getStock(TradeContext context) {
        SlotMachineNode node = this.getNode(SlotMachineNode.TYPE);
        if(node != null && node.isValidSetup())
        {
            int maxStock = Integer.MAX_VALUE;
            for(SlotMachineEntry entry : node.getValidEntries())
            {
                int stock = entry.getStock(this.trader);
                if(stock < maxStock)
                    maxStock = stock;
            }
            return maxStock;
        }
        return 0;
    }

    @Override
    public MoneyValue getCost() {
        SlotMachineNode node = this.getNode(SlotMachineNode.TYPE);
        return node == null ? MoneyValue.empty() : node.getPrice();
    }

    @Override
    public boolean isValid() { return this.trader.findNodeValue(SlotMachineNode.TYPE,SlotMachineNode::isValidSetup,false); }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

    @Override
    public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return ImmutableList.of(); }

    //No Storage Menu Interactions for this Trade Type
    @Override
    public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }

    @Override
    public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }

    @Override
    public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { }

}
