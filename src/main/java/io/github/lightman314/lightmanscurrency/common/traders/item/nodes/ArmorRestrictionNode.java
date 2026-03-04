package io.github.lightman314.lightmanscurrency.common.traders.item.nodes;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ITradeRestrictionSource;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;

public class ArmorRestrictionNode extends UnitNode implements ITradeRestrictionSource {

    public static final TraderNodeType<ArmorRestrictionNode> TYPE = TraderNodeType.unit(ArmorRestrictionNode::new);

    private ArmorRestrictionNode() { }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public ItemTradeRestriction getTradeRestriction(int index) {
        return switch (index % 4) {
            case 0 -> EquipmentRestriction.HEAD;
            case 1 -> EquipmentRestriction.CHEST;
            case 2 -> EquipmentRestriction.LEGS;
            default -> EquipmentRestriction.FEET;
        };
    }

}
