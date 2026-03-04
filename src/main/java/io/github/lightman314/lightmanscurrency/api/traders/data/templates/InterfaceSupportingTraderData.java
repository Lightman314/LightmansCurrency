package io.github.lightman314.lightmanscurrency.api.traders.data.templates;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class InterfaceSupportingTraderData extends NetworkSupportingTraderData{

    protected InterfaceSupportingTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id, nodes); }
    protected InterfaceSupportingTraderData(boolean alwaysNetwork, Level level, BlockPos pos) { super(alwaysNetwork, level, pos); }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        super.initializeAllyPermissions(defaultConsumer);
        defaultConsumer.accept(Permissions.INTERACTION_LINK,0);
    }
}
