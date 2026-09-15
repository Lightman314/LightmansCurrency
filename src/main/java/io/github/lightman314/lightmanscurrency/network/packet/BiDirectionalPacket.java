package io.github.lightman314.lightmanscurrency.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class BiDirectionalPacket extends ServerToClientPacket {

    public BiDirectionalPacket(Type<?> type) { super(type); }

    public final void sendToServer() { ClientPacketDistributor.sendToServer(this); }

    public static abstract class WithSplitHandlers extends BiDirectionalPacket {
        public WithSplitHandlers(Type<?> type) {
            super(type);
        }

        @Override
        protected final void handle(IPayloadContext context, Player player) {
            if(context.flow() == PacketFlow.CLIENTBOUND)
                this.handleOnClient(context,player);
            else
                this.handleOnServer(context,player);
        }
        protected abstract void handleOnClient(IPayloadContext context,Player player);
        protected abstract void handleOnServer(IPayloadContext context,Player player);
    }

}
