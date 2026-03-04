package io.github.lightman314.lightmanscurrency.common.traders.item.nodes;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.tabs.ticket.ItemTradeTicketEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.IItemTradeMode;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;

public class TicketRestrictionNode extends UnitNode implements IItemTradeMode {

    public static final TraderNodeType<TicketRestrictionNode> TYPE = TraderNodeType.unit(TicketRestrictionNode::new);

    private TicketRestrictionNode() {}

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public ItemTradeType<?> expectedTradeType() { return TicketItemTrade.TYPE; }

    @Override
    public void applyLateStorageTabs(ITraderStorageMenu menu) {
        //Add ItemTradeTicketEditTab in the "late" phase so that it overrides the normal ItemTradeEdit tab
        menu.addTab(new ItemTradeTicketEditTab(menu));
    }

}
