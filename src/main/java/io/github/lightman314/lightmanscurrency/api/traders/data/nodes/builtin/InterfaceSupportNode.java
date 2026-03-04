package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

import java.util.function.BiConsumer;

public class InterfaceSupportNode extends UnitNode {

    public static final TraderNodeType<InterfaceSupportNode> TYPE = TraderNodeType.unit(InterfaceSupportNode::new);

    private InterfaceSupportNode() {}
    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.INTERACTION_LINK,0);
    }

}
