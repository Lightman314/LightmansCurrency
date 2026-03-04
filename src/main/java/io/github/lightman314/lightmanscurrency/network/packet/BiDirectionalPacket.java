package io.github.lightman314.lightmanscurrency.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BiDirectionalPacket extends ServerToClientPacket {

    public BiDirectionalPacket(Type<?> type) { super(type); }

    public final void sendToServer() { PacketDistributor.sendToServer(this); }

    public static abstract class DirectionalHandler<T extends CustomPacket> extends Handler<T>
    {
        protected DirectionalHandler(Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf, T> codec) { super(type, codec); }
        @Override
        protected final void handle(T message, IPayloadContext context, Player player) {
            if(context.flow() == PacketFlow.CLIENTBOUND)
                this.handleOnClient(message,context,player);
            else
                this.handleOnServer(message,context,player);
        }
        protected abstract void handleOnClient(T message, IPayloadContext context, Player player);
        protected abstract void handleOnServer(T message, IPayloadContext context, Player player);
    }

}
