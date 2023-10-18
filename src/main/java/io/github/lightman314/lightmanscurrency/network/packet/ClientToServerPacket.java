package io.github.lightman314.lightmanscurrency.network.packet;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public abstract class ClientToServerPacket extends CustomPacket {

    public final void send() { LightmansCurrencyPacketHandler.instance.send(this, PacketDistributor.SERVER.noArg()); }

    public static class Simple extends ClientToServerPacket
    {
        @Override
        public void encode(@Nonnull FriendlyByteBuf buffer) { }
    }

}
