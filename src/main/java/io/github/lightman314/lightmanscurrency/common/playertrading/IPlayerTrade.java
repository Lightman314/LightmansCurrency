package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IPlayerTrade {

    int ITEM_COUNT = 12;
    long PENDING_DURATION = TimeUtil.DURATION_MINUTE * 5;

    boolean isCompleted();

    default boolean isHost(@Nonnull Player player) { return player.getUUID().equals(this.getHostID()); }
    default boolean isGuest(@Nonnull Player player) { return player.getUUID().equals(this.getGuestID()); }

    @Nonnull
    UUID getHostID();
    @Nonnull
    UUID getGuestID();

    @Nonnull
    Component getHostName();
    @Nonnull
    Component getGuestName();

    @Nonnull
    MoneyValue getHostMoney();
    @Nonnull
    MoneyValue getGuestMoney();

    @Nonnull
    Container getHostItems();
    @Nonnull
    Container getGuestItems();

    int getHostState();
    int getGuestState();

}
