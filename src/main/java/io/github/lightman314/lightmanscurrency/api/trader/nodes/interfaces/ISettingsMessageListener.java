package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import net.minecraft.world.entity.player.Player;

public interface ISettingsMessageListener {

    void handleSettingsChange(Player player,FancyPacketMap message);

    static void handleSettingsChange(INodeAccess trader,Player player,FancyPacketMap message) {
        if(message.contains("node")) {
            TraderNodeType<?> type = LCRegistries.Trader.TRADER_NODE_TYPE.getValue(message.getIdentifier("node"));
            if(type != null) {
                TraderNode node = trader.getNode(type);
                if(node instanceof ISettingsMessageListener l)
                    l.handleSettingsChange(player,message);
            }
        }
    }

}
