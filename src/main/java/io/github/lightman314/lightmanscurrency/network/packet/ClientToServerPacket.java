package io.github.lightman314.lightmanscurrency.network.packet;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nonnull;

public abstract class ClientToServerPacket extends CustomPacket {

    protected ClientToServerPacket(@Nonnull Type<?> type) { super(type); }

    public final void send() { ClientPacketDistributor.sendToServer(this); }

}