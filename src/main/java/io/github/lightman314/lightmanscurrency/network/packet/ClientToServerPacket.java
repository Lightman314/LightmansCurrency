package io.github.lightman314.lightmanscurrency.network.packet;

import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public abstract class ClientToServerPacket extends CustomPacket {

    protected ClientToServerPacket(@Nonnull Type<?> type) { super(type); }

    public final void send() { PacketDistributor.sendToServer(this); }

}
