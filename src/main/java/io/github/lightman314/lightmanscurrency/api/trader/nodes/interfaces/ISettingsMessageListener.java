package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.world.entity.player.Player;

public interface ISettingsMessageListener {

    void handleSettingsChange(Player player,FancyPacketMap message);

}
