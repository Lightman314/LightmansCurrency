package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientPlayerTrade implements IPlayerTrade {


    @Override
    public boolean isCompleted() { return false; }

    private final UUID hostID;
    @Override
    public boolean isHost(Player player) { return player.getUUID().equals(this.hostID); }

    private final Component hostName;
    @Override
    public Component getHostName() { return this.hostName; }
    private final Component guestName;
    @Override
    public Component getGuestName() { return this.guestName; }

    private final CoinValue hostMoney;
    @Override
    public CoinValue getHostMoney() { return this.hostMoney; }
    private final CoinValue guestMoney;
    @Override
    public CoinValue getGuestMoney() { return this.guestMoney; }

    private Container hostItems;
    @Override
    public Container getHostItems() { return this.hostItems; }

    private Container guestItems;
    @Override
    public Container getGuestItems() { return this.guestItems; }

    private final int hostState;
    @Override
    public int getHostState() { return this.hostState; }
    private final int guestState;
    @Override
    public int getGuestState() { return this.guestState; }

    public ClientPlayerTrade(UUID hostID, Component hostName, Component guestName, CoinValue hostMoney, CoinValue guestMoney, Container hostItems, Container guestItems, int hostState, int guestState) {
        this.hostID = hostID;
        this.hostName = hostName;
        this.guestName = guestName;
        this.hostMoney = hostMoney;
        this.guestMoney = guestMoney;
        this.hostItems = hostItems;
        this.guestItems = guestItems;
        this.hostState = hostState;
        this.guestState = guestState;
    }

    public final void encode(FriendlyByteBuf data) {
        data.writeUUID(this.hostID);
        data.writeComponent(this.hostName);
        data.writeComponent(this.guestName);
        this.hostMoney.encode(data);
        this.guestMoney.encode(data);
        InventoryUtil.encodeItems(this.hostItems, data);
        InventoryUtil.encodeItems(this.guestItems, data);
        data.writeInt(this.hostState);
        data.writeInt(this.guestState);
    }

    public static ClientPlayerTrade decode(FriendlyByteBuf data) {
        UUID hostID = data.readUUID();
        Component hostName = data.readComponent();
        Component guestName = data.readComponent();
        CoinValue hostMoney = CoinValue.decode(data);
        CoinValue guestMoney = CoinValue.decode(data);
        Container hostItems = InventoryUtil.decodeItems(data);
        Container guestItems = InventoryUtil.decodeItems(data);
        int hostState = data.readInt();
        int guestState = data.readInt();
        return new ClientPlayerTrade(hostID, hostName, guestName, hostMoney, guestMoney, hostItems, guestItems, hostState, guestState);
    }

}
