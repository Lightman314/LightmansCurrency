package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class ClientPlayerTrade implements IPlayerTrade {

    public static final StreamCodec<RegistryFriendlyByteBuf,ClientPlayerTrade> STREAM_CODEC = StreamHelper.composite(
            UUIDUtil.STREAM_CODEC,t -> t.hostID,
            UUIDUtil.STREAM_CODEC,t -> t.guestID,
            ComponentSerialization.STREAM_CODEC,t -> t.hostName,
            ComponentSerialization.STREAM_CODEC,t -> t.guestName,
            MoneyValue.STREAM_CODEC,t -> t.hostMoney,
            MoneyValue.STREAM_CODEC,t -> t.guestMoney,
            LCItemStackHandler.STREAM_CODEC,t -> t.hostItems,
            LCItemStackHandler.STREAM_CODEC,t -> t.guestItems,
            ByteBufCodecs.INT,t -> t.hostState,
            ByteBufCodecs.INT,t -> t.guestState,
            ClientPlayerTrade::new);

    @Override
    public boolean isCompleted() { return false; }

    private final UUID hostID;
    private final UUID guestID;
    @Override
    public UUID getHostID() { return this.hostID; }
    @Override
    public UUID getGuestID() { return this.guestID; }

    private final Component hostName;
    @Override
    public Component getHostName() { return this.hostName; }
    private final Component guestName;
    @Override
    public Component getGuestName() { return this.guestName; }

    private final MoneyValue hostMoney;
    @Override
    public MoneyValue getHostMoney() { return this.hostMoney; }
    private final MoneyValue guestMoney;
    @Override
    public MoneyValue getGuestMoney() { return this.guestMoney; }

    private final LCItemStackHandler hostItems;
    @Override
    public LCItemStackHandler getHostItems() { return this.hostItems; }

    private final LCItemStackHandler guestItems;
    @Override
    public LCItemStackHandler getGuestItems() { return this.guestItems; }

    private final int hostState;
    @Override
    public int getHostState() { return this.hostState; }
    private final int guestState;
    @Override
    public int getGuestState() { return this.guestState; }

    public ClientPlayerTrade(UUID hostID, UUID guestID, Component hostName, Component guestName, MoneyValue hostMoney, MoneyValue guestMoney, LCItemStackHandler hostItems, LCItemStackHandler guestItems, int hostState, int guestState) {
        this.hostID = hostID;
        this.guestID = guestID;
        this.hostName = hostName;
        this.guestName = guestName;
        this.hostMoney = hostMoney;
        this.guestMoney = guestMoney;
        this.hostItems = hostItems;
        this.guestItems = guestItems;
        this.hostState = hostState;
        this.guestState = guestState;
    }

}
