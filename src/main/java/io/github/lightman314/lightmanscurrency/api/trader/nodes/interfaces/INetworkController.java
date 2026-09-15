package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

public interface INetworkController {

    boolean visibleToNetwork();

    static boolean visibleToNetwork(INodeAccess trader) {
        return trader.getNodes(INetworkController.class).stream().anyMatch(INetworkController::visibleToNetwork);
    }

}