package io.github.lightman314.lightmanscurrency.network.packet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CustomPacket {

    public abstract void encode(FriendlyByteBuf buffer);

    public static abstract class Handler<T extends CustomPacket>
    {
        public abstract T decode(FriendlyByteBuf buffer);
        public final void handlePacket(T message, Supplier<NetworkEvent.Context> supplier)
        {
            NetworkEvent.Context context = supplier.get();
            boolean useClientPlayer = context.getDirection().getReceptionSide().isClient();
            context.enqueueWork(() -> this.handle(message, useClientPlayer ? LightmansCurrency.getProxy().getLocalPlayer() : context.getSender()));
            context.setPacketHandled(true);
        }
        protected abstract void handle(T message, Player player);
    }

    public static abstract class SimpleHandler<T extends CustomPacket> extends Handler<T>
    {
        protected final T instance;
        protected SimpleHandler(T instance) { this.instance = instance; }
        
        @Override
        public final  T decode(FriendlyByteBuf buffer) { return this.instance; }
    }

}
