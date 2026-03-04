package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ArmorRestrictionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ItemTraderDataArmor extends ItemTraderData {

	public static final TraderType<ItemTraderDataArmor> TYPE = TraderType.simple(ItemTraderDataArmor::new,ItemTraderDataArmor::new);

    private ItemTraderDataArmor() {}
	public ItemTraderDataArmor(Level level, BlockPos pos) { super(4, false,level, pos); }
    private ItemTraderDataArmor(long id,Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public void addCustomNodes(NodeCollector collector) {
        super.addCustomNodes(collector);
        collector.addNode(ArmorRestrictionNode.TYPE);
    }
	
}
