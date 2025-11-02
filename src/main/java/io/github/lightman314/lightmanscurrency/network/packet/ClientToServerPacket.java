package io.github.lightman314.lightmanscurrency.network.packet;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ClientToServerPacket extends CustomPacket {

    public final void send() { LightmansCurrencyPacketHandler.instance.sendToServer(this); }

    public static class Simple extends ClientToServerPacket
    {
        @Override
        public void encode(FriendlyByteBuf buffer) { }
    }

}
