package io.github.lightman314.lightmanscurrency.network.packet;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CustomPacket implements CustomPacketPayload {

    private final Type<?> type;
    protected CustomPacket(Type<?> type) { this.type = type;}

    @Override
    public Type<? extends CustomPacketPayload> type() { return this.type; }

    protected static CompoundTag readNBT(FriendlyByteBuf buffer) { return (CompoundTag)buffer.readNbt(NbtAccounter.unlimitedHeap()); }

    protected static void writeItem(RegistryFriendlyByteBuf buffer, ItemStack stack)
    {
        buffer.writeBoolean(stack.isEmpty());
        if(!stack.isEmpty())
        {
            ItemStack.STREAM_CODEC.encode(buffer,stack.copyWithCount(1));
            buffer.writeInt(stack.getCount());
        }
    }

    protected static ItemStack readItem(RegistryFriendlyByteBuf buffer) {
        boolean empty = buffer.readBoolean();
        if(empty)
            return ItemStack.EMPTY;
        else
        {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            stack.setCount(buffer.readInt());
            return stack;
        }
    }

    public static <T extends CustomPacket> StreamCodec<FriendlyByteBuf,T> easyCodec(BiConsumer<FriendlyByteBuf,T> encoder, Function<FriendlyByteBuf, T> decoder) { return StreamCodec.of(encoder::accept,decoder::apply); }
    public static <T extends CustomPacket> StreamCodec<? super RegistryFriendlyByteBuf,T> fancyCodec(BiConsumer<RegistryFriendlyByteBuf,T> encoder, Function<RegistryFriendlyByteBuf,T> decoder) { return StreamCodec.of(encoder::accept,decoder::apply); }

    public static <T extends CustomPacket> StreamCodec<FriendlyByteBuf,T> simpleCodec(T instance) { return easyCodec((b,p) -> {},b -> instance); }

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
        public final void handle(T payload, IPayloadContext context) { this.handle(payload, context, context.player()); }
        protected abstract void handle(T message, IPayloadContext context, Player player);
    }

    public static abstract class ConfigHandler<T extends CustomPacket> extends AbstractHandler<T>
    {
        public final StreamCodec<? super FriendlyByteBuf,T> configCodec;
        protected ConfigHandler(Type<T> type, StreamCodec<? super FriendlyByteBuf,T> codec) { super(type,codec); this.configCodec = codec; }
    }

    public static abstract class SimpleHandler<T extends CustomPacket> extends Handler<T>
    {
        protected SimpleHandler(Type<T> type, T instance) { super(type, simpleCodec(instance)); }
    }

}
