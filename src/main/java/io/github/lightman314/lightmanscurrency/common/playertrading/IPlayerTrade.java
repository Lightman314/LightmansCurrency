package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public interface IPlayerTrade {

    int ITEM_COUNT = 12;
    long PENDING_DURATION = TimeUtil.DURATION_MINUTE * 5;

    boolean isCompleted();

    boolean isHost(Player player);

    Component getHostName();
    Component getGuestName();

    CoinValue getHostMoney();
    CoinValue getGuestMoney();

    Container getHostItems();
    Container getGuestItems();

    int getHostState();
    int getGuestState();

}