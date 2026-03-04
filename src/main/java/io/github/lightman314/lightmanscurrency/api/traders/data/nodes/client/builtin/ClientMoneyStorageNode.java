package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

public class ClientMoneyStorageNode extends ClientTraderNode<MoneyStorageNode> implements IClientPermissionProvider {

    public ClientMoneyStorageNode(MoneyStorageNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.COLLECT_COINS);
        builder.addSimple(Permissions.STORE_COINS);
    }

}
