package io.github.lightman314.lightmanscurrency.network.packet;

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
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class CustomPacket implements CustomPacketPayload {

    private final Type<?> type;
    protected CustomPacket(@Nonnull Type<?> type) { this.type = type;}

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() { return this.type; }

    protected static CompoundTag readNBT(@Nonnull FriendlyByteBuf buffer) { return (CompoundTag)buffer.readNbt(NbtAccounter.unlimitedHeap()); }

    protected static void writeItem(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull ItemStack stack)
    {
        buffer.writeBoolean(stack.isEmpty());
        if(!stack.isEmpty())
        {
            ItemStack.STREAM_CODEC.encode(buffer,stack.copyWithCount(1));
            buffer.writeInt(stack.getCount());
        }
    }

    protected static ItemStack readItem(@Nonnull RegistryFriendlyByteBuf buffer) {
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

    public static <T extends CustomPacket> StreamCodec<FriendlyByteBuf,T> simpleCodec(@Nonnull T instance) { return easyCodec((b,p) -> {},b -> instance); }

    public static abstract class AbstractHandler<T extends CustomPacket> implements IPayloadHandler<T>
    {
        public final Type<T> type;
        public final StreamCodec<? super RegistryFriendlyByteBuf,T> codec;
        protected AbstractHandler(@Nonnull Type<T> type, @Nonnull StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { this.type = type; this.codec = codec; }
    }

    public static abstract class Handler<T extends CustomPacket> extends AbstractHandler<T>
    {
        protected Handler(@Nonnull Type<T> type, @Nonnull StreamCodec<? super RegistryFriendlyByteBuf, T> codec) { super(type, codec); }
        @Override
        public final void handle(@Nonnull T payload, @Nonnull IPayloadContext context) { this.handle(payload, context, context.player()); }
        protected abstract void handle(@Nonnull T message, @Nonnull IPayloadContext context, @Nonnull Player player);
    }

    public static abstract class ConfigHandler<T extends CustomPacket> extends AbstractHandler<T>
    {
        public final StreamCodec<? super FriendlyByteBuf,T> configCodec;
        protected ConfigHandler(@Nonnull Type<T> type, @Nonnull StreamCodec<? super FriendlyByteBuf,T> codec) { super(type,codec); this.configCodec = codec; }
    }

    public static abstract class SimpleHandler<T extends CustomPacket> extends Handler<T>
    {
        protected SimpleHandler(@Nonnull Type<T> type, @Nonnull T instance) { super(type, simpleCodec(instance)); }
    }

}
