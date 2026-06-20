package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;

public interface INetworkController {

    boolean visibleToNetwork();

    static boolean visibleToNetwork(TraderData trader)
    {
        for(INetworkController node : trader.getNodes(INetworkController.class))
        {
            if(node.visibleToNetwork())
                return true;
        }
        return false;
    }

}