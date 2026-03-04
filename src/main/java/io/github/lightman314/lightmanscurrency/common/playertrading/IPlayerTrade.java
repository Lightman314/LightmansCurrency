package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IPlayerTrade {

    int ITEM_COUNT = 12;
    long PENDING_DURATION = TimeUtil.DURATION_MINUTE * 5;

    boolean isCompleted();

    default boolean isHost(Player player) { return player.getUUID().equals(this.getHostID()); }
    default boolean isGuest(Player player) { return player.getUUID().equals(this.getGuestID()); }

    UUID getHostID();
    UUID getGuestID();

    Component getHostName();
    Component getGuestName();

    MoneyValue getHostMoney();
    MoneyValue getGuestMoney();

    LCItemStackHandler getHostItems();
    LCItemStackHandler getGuestItems();

    int getHostState();
    int getGuestState();

}
