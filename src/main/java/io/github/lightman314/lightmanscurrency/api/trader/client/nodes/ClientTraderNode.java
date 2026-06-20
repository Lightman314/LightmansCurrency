package io.github.lightman314.lightmanscurrency.api.trader.client.nodes;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientTraderNode {

    public static final ClientPairedRegistry<TraderNodeType<?>,ClientTraderNode> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Trader.TRADER_NODE_TYPE,new ClientTraderNode());

    protected ClientTraderNode() {}

    public static <T> List<T> getClientNodes(@Nullable TraderData trader, Class<T> nodeClass) {
        if(trader == null)
            return new ArrayList<>();
        List<T> list = new ArrayList<>();
        for(TraderNode node : trader.getAllNodes())
        {
            ClientTraderNode clientNode = REGISTRY.getValue(node.getType());
            if(nodeClass.isInstance(clientNode))
                list.add(nodeClass.cast(clientNode));
        }
        return list;
    }

    public static <T> List<T> getClientNodes(@Nullable TraderSource source,Class<T> nodeClass) {
        if(source == null)
            return new ArrayList<>();
        List<T> list = new ArrayList<>();
        Set<TraderNodeType<?>> foundTypes = new HashSet<>();
        for(TraderData trader : source.getTraders())
        {
            for(TraderNode node : trader.getNodes(n -> !foundTypes.contains(n.getType())))
            {
                foundTypes.add(node.getType());
                ClientTraderNode clientNode = REGISTRY.getValue(node.getType());
                if(nodeClass.isInstance(clientNode))
                    list.add(nodeClass.cast(clientNode));
            }
        }
        return list;
    }

}