package io.github.lightman314.lightmanscurrency.api.traders.data.templates;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.NetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of {@link NormalTraderData} but supporting network trader data
 */
public abstract class NetworkSupportingTraderData extends NormalTraderData {

    //Creation from Type
    protected NetworkSupportingTraderData() { }
    protected NetworkSupportingTraderData(Map<TraderNodeType<?>,Object> args) { super(args); }

    //Creation from Block Entity
    protected NetworkSupportingTraderData(boolean alwaysNetwork,Level level, BlockPos pos) { this(new HashMap<>(),alwaysNetwork,level,pos); }
    protected NetworkSupportingTraderData(Map<TraderNodeType<?>,Object> args, boolean alwaysNetwork,Level level, BlockPos pos) { super(assembleArgs(args,alwaysNetwork),level,pos); }

    //Loading from Codec
    protected NetworkSupportingTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { this(new HashMap<>(),id,nodes); }
    protected NetworkSupportingTraderData(Map<TraderNodeType<?>,Object> args, long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(args,id, nodes); }

    private static Map<TraderNodeType<?>,Object> assembleArgs(Map<TraderNodeType<?>,Object> args,boolean alwaysNetwork)
    {
        args.put(NetworkNode.TYPE,alwaysNetwork);
        return args;
    }

    @Override
    public void addDefaultNodes(NodeCollector collector) {
        super.addDefaultNodes(collector);
        collector.addNode(NetworkNode.TYPE);
    }

}
