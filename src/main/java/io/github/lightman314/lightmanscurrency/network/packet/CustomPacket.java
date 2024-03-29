package io.github.lightman314.lightmanscurrency.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class CustomPacket {

    public abstract void encode(@Nonnull FriendlyByteBuf buffer);

    public static abstract class Handler<T extends CustomPacket>
    {
        @Nonnull
        public abstract T decode(@Nonnull FriendlyByteBuf buffer);
        public final void handlePacket(@Nonnull T message, @Nonnull Supplier<NetworkEvent.Context> supplier)
        {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> this.handle(message, context.getSender()));
            context.setPacketHandled(true);
        }
        protected abstract void handle(@Nonnull T message, @Nullable ServerPlayer sender);
    }

    public static abstract class SimpleHandler<T extends CustomPacket> extends Handler<T>
    {
        protected final T instance;
        protected SimpleHandler(@Nonnull T instance) { this.instance = instance; }
        @Nonnull
        @Override
        public final  T decode(@Nonnull FriendlyByteBuf buffer) { return this.instance; }
    }

}
