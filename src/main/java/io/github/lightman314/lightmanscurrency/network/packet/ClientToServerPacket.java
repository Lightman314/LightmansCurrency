package io.github.lightman314.lightmanscurrency.network.packet;

import net.neoforged.neoforge.network.PacketDistributor;

public abstract class ClientToServerPacket extends CustomPacket {

    public ClientToServerPacket(Type<?> type) { super(type); }

    public final void sendToServer() { PacketDistributor.sendToServer(this); }

}
