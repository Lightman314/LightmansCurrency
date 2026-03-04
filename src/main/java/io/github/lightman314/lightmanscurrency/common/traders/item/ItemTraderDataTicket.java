package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.TicketRestrictionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ItemTraderDataTicket extends ItemTraderData {

	public static final TraderType<ItemTraderDataTicket> TYPE = TraderType.simple(ItemTraderDataTicket::new,ItemTraderDataTicket::new);
	
	private ItemTraderDataTicket() { }
	public ItemTraderDataTicket(int tradeCount, Level level, BlockPos pos) { super(tradeCount,false,level,pos); }
    private ItemTraderDataTicket(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public void addCustomNodes(NodeCollector collector) {
        super.addCustomNodes(collector);
        collector.addNode(TicketRestrictionNode.TYPE);
    }
}
