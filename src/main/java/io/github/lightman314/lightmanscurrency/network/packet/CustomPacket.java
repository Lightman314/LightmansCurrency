package io.github.lightman314.lightmanscurrency.network.packet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public abstract class CustomPacket implements CustomPacketPayload {

    public static <T extends CustomPacket> Type<T> bType(String id) { return bType(LightmansCurrency.MODID,id); }
    public static <T extends CustomPacket> Type<T> bType(String modid,String id) { return new Type<>(ResourceLocation.fromNamespaceAndPath(modid,"b_" + id)); }
    public static <T extends CustomPacket> Type<T> cType(String id) { return cType(LightmansCurrency.MODID,"c_" + id); }
    public static <T extends CustomPacket> Type<T> cType(String modid,String id) { return new Type<>(ResourceLocation.fromNamespaceAndPath(modid,"c_" + id)); }
    public static <T extends CustomPacket> Type<T> sType(String id) { return sType(LightmansCurrency.MODID,id); }
    public static <T extends CustomPacket> Type<T> sType(String modid,String id) { return new Type<>(ResourceLocation.fromNamespaceAndPath(modid,"s_" + id)); }

    private final Type<?> type;
    protected CustomPacket(Type<?> type) { this.type = type;}

    @Override
    public Type<? extends CustomPacketPayload> type() { return this.type; }

    public static abstract class AbstractHandler<T extends CustomPacket> implements IPayloadHandler<T>
    {
        public final Type<T> type;
        public final StreamCodec<? super RegistryFriendlyByteBuf,T> codec;
        protected AbstractHandler(Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { this.type = type; this.codec = codec; }
    }

    public static abstract class Handler<T extends CustomPacket> extends AbstractHandler<T>
    {
        protected Handler(Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) { super(type, codec); }
        @Override
        public final void handle(T payload,IPayloadContext context) { context.enqueueWork(() -> this.handle(payload,context,context.player())); }
        protected abstract void handle(T message, IPayloadContext context,Player player);
    }

    public static abstract class ConfigHandler<T extends CustomPacket> extends AbstractHandler<T>
    {
        public final StreamCodec<? super FriendlyByteBuf,T> configCodec;
        protected ConfigHandler(Type<T> type, StreamCodec<? super FriendlyByteBuf,T> codec) { super(type,codec); this.configCodec = codec; }
    }

    public static abstract class SimpleHandler<T extends CustomPacket> extends Handler<T>
    {
        protected SimpleHandler(Type<T> type,T instance) { super(type,StreamCodec.unit(instance)); }
    }

}
