package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.BookRestrictionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ItemTraderDataBook extends ItemTraderData {

    public static final TraderType<ItemTraderDataBook> TYPE = TraderType.simple(ItemTraderDataBook::new,ItemTraderDataBook::new);

    private ItemTraderDataBook() { super(); }
    public ItemTraderDataBook(int tradeCount, Level level, BlockPos pos) { super(tradeCount,false,level,pos); }
    private ItemTraderDataBook(long id,Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public void addCustomNodes(NodeCollector collector) {
        super.addCustomNodes(collector);
        collector.addNode(BookRestrictionNode.TYPE);
    }

}
