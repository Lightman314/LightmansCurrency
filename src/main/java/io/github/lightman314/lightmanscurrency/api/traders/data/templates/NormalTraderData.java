package io.github.lightman314.lightmanscurrency.api.traders.data.templates;

import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Base {@link TraderData} class that automatically provides all the base, built-in nodes that most traders utilize
 */
public abstract class NormalTraderData extends TraderData {

    //Creation from Type
    protected NormalTraderData() {}
    protected NormalTraderData(Map<TraderNodeType<?>,Object> args) { super(args); }

    //Creation from Block Entity
    protected NormalTraderData(Level level, BlockPos pos) { this(new HashMap<>(),level,pos); }
    protected NormalTraderData(Map<TraderNodeType<?>,Object> args, Level level, BlockPos pos) {
        super(args);
        WorldStateNode node = this.getNode(WorldStateNode.TYPE);
        if(node != null)
        {
            node.setPosition(WorldPosition.of(level.dimension(),pos));
            node.setTraderBlock(level.getBlockState(pos).getBlock().asItem());
        }
    }

    //Loading from Codec
    protected NormalTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }
    protected NormalTraderData(Map<TraderNodeType<?>,Object> args, long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(args,id,nodes); }

    @Override
    public void addDefaultNodes(NodeCollector collector) {
        collector.addNode(OwnerNode.TYPE);
        collector.addNode(AlliesNode.TYPE);
        collector.addNode(LoggerNode.TYPE);
        collector.addNode(DisplayNode.TYPE);
        collector.addNode(WorldStateNode.TYPE);
        collector.addNode(InteractionNode.TYPE);
        collector.addNode(MoneyStorageNode.TYPE);
        collector.addNode(TraderRulesNode.TYPE);
        collector.addNode(BankNode.TYPE);
        collector.addNode(TaxesNode.TYPE);
        this.addCustomNodes(collector);
    }

    protected abstract void addCustomNodes(NodeCollector collector);

}
