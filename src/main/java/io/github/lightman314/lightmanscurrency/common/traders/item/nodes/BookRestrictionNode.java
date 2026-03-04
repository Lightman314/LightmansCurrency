package io.github.lightman314.lightmanscurrency.common.traders.item.nodes;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.BookRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ITradeRestrictionSource;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;

public class BookRestrictionNode extends UnitNode implements ITradeRestrictionSource {

    public static final TraderNodeType<BookRestrictionNode> TYPE = TraderNodeType.unit(BookRestrictionNode::new);

    private BookRestrictionNode() { }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public ItemTradeRestriction getTradeRestriction(int index) { return BookRestriction.INSTANCE; }

}
