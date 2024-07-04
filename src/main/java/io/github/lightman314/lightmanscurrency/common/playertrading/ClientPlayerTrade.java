package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ClientPlayerTrade implements IPlayerTrade {


    @Override
    public boolean isCompleted() { return false; }

    private final UUID hostID;
    private final UUID guestID;

    @Nonnull
    @Override
    public UUID getHostID() { return this.hostID; }
    @Nonnull
    @Override
    public UUID getGuestID() { return this.guestID; }

    private final Component hostName;
    @Nonnull
    @Override
    public Component getHostName() { return this.hostName; }
    private final Component guestName;
    @Nonnull
    @Override
    public Component getGuestName() { return this.guestName; }

    private final MoneyValue hostMoney;
    @Nonnull
    @Override
    public MoneyValue getHostMoney() { return this.hostMoney; }
    private final MoneyValue guestMoney;
    @Nonnull
    @Override
    public MoneyValue getGuestMoney() { return this.guestMoney; }

    private final Container hostItems;
    @Nonnull
    @Override
    public Container getHostItems() { return this.hostItems; }

    private final Container guestItems;
    @Nonnull
    @Override
    public Container getGuestItems() { return this.guestItems; }

    private final int hostState;
    @Override
    public int getHostState() { return this.hostState; }
    private final int guestState;
    @Override
    public int getGuestState() { return this.guestState; }

    public ClientPlayerTrade(UUID hostID, UUID guestID, Component hostName, Component guestName, MoneyValue hostMoney, MoneyValue guestMoney, Container hostItems, Container guestItems, int hostState, int guestState) {
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

    public final void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(this.hostID);
        buffer.writeUUID(this.guestID);
        ComponentSerialization.STREAM_CODEC.encode(buffer,this.hostName);
        ComponentSerialization.STREAM_CODEC.encode(buffer,this.guestName);
        this.hostMoney.encode(buffer);
        this.guestMoney.encode(buffer);
        InventoryUtil.encodeItems(this.hostItems, buffer);
        InventoryUtil.encodeItems(this.guestItems, buffer);
        buffer.writeInt(this.hostState);
        buffer.writeInt(this.guestState);
    }

    public static ClientPlayerTrade decode(RegistryFriendlyByteBuf buffer) {
        UUID hostID = buffer.readUUID();
        UUID guestID = buffer.readUUID();
        Component hostName = ComponentSerialization.STREAM_CODEC.decode(buffer);
        Component guestName = ComponentSerialization.STREAM_CODEC.decode(buffer);
        MoneyValue hostMoney = MoneyValue.decode(buffer);
        MoneyValue guestMoney = MoneyValue.decode(buffer);
        Container hostItems = InventoryUtil.decodeItems(buffer);
        Container guestItems = InventoryUtil.decodeItems(buffer);
        int hostState = buffer.readInt();
        int guestState = buffer.readInt();
        return new ClientPlayerTrade(hostID, guestID, hostName, guestName, hostMoney, guestMoney, hostItems, guestItems, hostState, guestState);
    }

}
