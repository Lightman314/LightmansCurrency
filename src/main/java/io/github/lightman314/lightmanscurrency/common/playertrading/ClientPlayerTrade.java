package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class ClientPlayerTrade implements IPlayerTrade {


    @Override
    public boolean isCompleted() { return false; }

    private final UUID hostID;
    @Override
    public boolean isHost(PlayerEntity player) { return player.getUUID().equals(this.hostID); }

    private final ITextComponent hostName;
    @Override
    public ITextComponent getHostName() { return this.hostName; }
    private final ITextComponent guestName;
    @Override
    public ITextComponent getGuestName() { return this.guestName; }

    private final CoinValue hostMoney;
    @Override
    public CoinValue getHostMoney() { return this.hostMoney; }
    private final CoinValue guestMoney;
    @Override
    public CoinValue getGuestMoney() { return this.guestMoney; }

    private final IInventory hostItems;
    @Override
    public IInventory getHostItems() { return this.hostItems; }

    private final IInventory guestItems;
    @Override
    public IInventory getGuestItems() { return this.guestItems; }

    private final int hostState;
    @Override
    public int getHostState() { return this.hostState; }
    private final int guestState;
    @Override
    public int getGuestState() { return this.guestState; }

    public ClientPlayerTrade(UUID hostID, ITextComponent hostName, ITextComponent guestName, CoinValue hostMoney, CoinValue guestMoney, IInventory hostItems, IInventory guestItems, int hostState, int guestState) {
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

    public final void encode(PacketBuffer data) {
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

    public static ClientPlayerTrade decode(PacketBuffer data) {
        UUID hostID = data.readUUID();
        ITextComponent hostName = data.readComponent();
        ITextComponent guestName = data.readComponent();
        CoinValue hostMoney = CoinValue.decode(data);
        CoinValue guestMoney = CoinValue.decode(data);
        IInventory hostItems = InventoryUtil.decodeItems(data);
        IInventory guestItems = InventoryUtil.decodeItems(data);
        int hostState = data.readInt();
        int guestState = data.readInt();
        return new ClientPlayerTrade(hostID, hostName, guestName, hostMoney, guestMoney, hostItems, guestItems, hostState, guestState);
    }

}