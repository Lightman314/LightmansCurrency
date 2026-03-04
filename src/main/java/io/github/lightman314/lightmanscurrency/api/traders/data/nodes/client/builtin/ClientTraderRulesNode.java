package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.TraderRulesNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

public class ClientTraderRulesNode extends ClientTraderNode<TraderRulesNode> implements IClientPermissionProvider {

    public ClientTraderRulesNode(TraderRulesNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.EDIT_TRADE_RULES);
    }
}
