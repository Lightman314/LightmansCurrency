package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;

public interface IPlayerTrade {

    int ITEM_COUNT = 12;
    long PENDING_DURATION = TimeUtil.DURATION_MINUTE * 5;

    boolean isCompleted();

    boolean isHost(PlayerEntity player);

    ITextComponent getHostName();
    ITextComponent getGuestName();

    CoinValue getHostMoney();
    CoinValue getGuestMoney();

    IInventory getHostItems();
    IInventory getGuestItems();

    int getHostState();
    int getGuestState();

}