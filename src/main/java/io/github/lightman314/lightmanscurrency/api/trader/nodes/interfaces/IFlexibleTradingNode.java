package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import net.minecraft.world.entity.player.Player;

public interface IFlexibleTradingNode {

    boolean showTradeCountButtons(Player player);
    boolean canAddTrade(Player player);
    boolean canRemoveTrade(Player player);

    boolean tryAddTrade(Player player);
    boolean tryRemoveTrade(Player player);

}