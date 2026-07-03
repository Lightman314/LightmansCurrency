package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.AdvancedItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTradesNode extends AbstractItemTradesNode<ItemTradeData> {

    private static final MapCodec<ItemTradesNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.optionalFieldOf("baseCount",1).forGetter(n -> n.baseCount),
            Codec.INT.optionalFieldOf("upgradeCount",0).forGetter(n -> n.upgradeCount),
            ItemTradeData.CODEC.listOf().fieldOf("trades").forGetter(ItemTradesNode::getMutableTrades)
    ).apply(builder,ItemTradesNode::new));
    public static final TraderNodeType<ItemTradesNode> TYPE = TraderNodeType.simple(ItemTradesNode::new,MAP_CODEC);

    protected ItemTradesNode() {}
    protected ItemTradesNode(int count,int upgradeCount,List<ItemTradeData> trades) {
        super(count,upgradeCount,trades);
    }

    @Override
    protected ItemTradeData createNewTrade() { return new ItemTradeData(); }

    @Nullable
    @Override
    public Identifier advancedEditTabKey() { return AdvancedItemTradeEditTab.KEY; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        super.addTabs(builder);
        builder.addTab(AdvancedItemTradeEditTab::new);
    }

}