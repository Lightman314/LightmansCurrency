package io.github.lightman314.lightmanscurrency.network.packet;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public abstract class ClientToServerPacket extends CustomPacket {

    public ClientToServerPacket(Type<?> type) { super(type); }

    public final void send() { ClientPacketDistributor.sendToServer(this); }

}