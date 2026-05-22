package io.github.lightman314.lightmanscurrency.common.attachments;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class EasyAttachment<T extends EasyAttachment<T>> implements IClientTracker {

    @Nullable
    private IAttachmentHolder holder;
    protected IAttachmentHolder getHolder() { return Objects.requireNonNull(this.holder,"Attempted to get the holder before it was assigned!"); }
    @Override
    public boolean isClient() { return isClient(this.holder); }
    @Override
    public final boolean isServer() { return IClientTracker.super.isServer(); }

    protected abstract AttachmentType<T> getType();
    public void setChanged()
    {
        if(this.holder != null)
            this.holder.setData(this.getType(),(T)this);
    }

    void assignHolder(IAttachmentHolder holder) { this.holder = holder; this.afterHolderAssigned(); }
    protected void afterHolderAssigned() {}

    public static boolean isClient(@Nullable IAttachmentHolder holder)
    {
        if(holder == null)
            return true;
        if(holder instanceof Entity e)
            return e.level().isClientSide;
        if(holder instanceof Level level)
            return level.isClientSide;
        if(holder instanceof LevelChunk chunk)
            return chunk.getLevel().isClientSide;
        return true;
    }

    public static <T extends EasyAttachment<T>,B extends ByteBuf> AttachmentType.Builder<T> buildType(Supplier<T> factory, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec, UnaryOperator<T> copier) { return buildType(factory,codec,streamCodec,copier,true); }
    public static <T extends EasyAttachment<T>,B extends ByteBuf> AttachmentType.Builder<T> buildType(Supplier<T> factory, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec, UnaryOperator<T> copier, boolean copyOnDeath)
    {
        AttachmentType.Builder<T> builder = AttachmentType.builder(holder -> {
                    T data = factory.get();
                    data.assignHolder(holder);
                    return data;
                })
                .serialize(new Serializer<>(codec))
                .sync(new Syncer<>(streamCodec))
                .copyHandler(new Copier<>(copier));
        if(copyOnDeath)
            builder.copyOnDeath();
        return builder;
    }

    public static <T extends EasyAttachment<T>,B extends ByteBuf> AttachmentType.Builder<T> buildType(Supplier<T> factory, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec,UnaryOperator<T> copier,Predicate<T> shouldWrite,boolean copyOnDeath)
    {
        AttachmentType.Builder<T> builder = AttachmentType.builder(holder -> {
                    T data = factory.get();
                    data.assignHolder(holder);
                    return data;
                })
                .serialize(new AdvancedSerializer<>(codec,shouldWrite))
                .sync(new Syncer<>(streamCodec))
                .copyHandler(new Copier<>(copier));
        if(copyOnDeath)
            builder.copyOnDeath();
        return builder;
    }

    protected record Factory<T extends EasyAttachment<T>>(Supplier<T> factory) implements Function<IAttachmentHolder,T>
    {
        @Override
        public T apply(IAttachmentHolder holder) {
            T data = this.factory.get();
            data.assignHolder(holder);
            return data;
        }
    }

    protected static class Serializer<T extends EasyAttachment<T>> implements IAttachmentSerializer<Tag,T>
    {

        private final Codec<T> codec;
        protected Serializer(Codec<T> codec) { this.codec = codec; }
        protected boolean shouldWrite(T value) { return true; }

        @Override
        public T read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
            T data = DataContext.createNBT(provider).read(tag,this.codec);
            data.assignHolder(holder);
            return data;
        }
        @Override
        @Nullable
        public Tag write(T attachment, HolderLookup.Provider provider) {
            if(this.shouldWrite(attachment))
                return DataContext.createNBT(provider).write(attachment,this.codec);
            return null;
        }
    }

    protected static class AdvancedSerializer<T extends EasyAttachment<T>> extends Serializer<T>
    {

        private final Predicate<T> shouldWrite;
        protected AdvancedSerializer(Codec<T> codec, Predicate<T> shouldWrite) {
            super(codec);
            this.shouldWrite = shouldWrite;
        }
        @Override
        protected boolean shouldWrite(T value) { return this.shouldWrite.test(value); }
    }

    protected record Syncer<T extends EasyAttachment<T>>(StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements AttachmentSyncHandler<T>
    {
        @Override
        public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
            this.streamCodec.encode(buf,attachment);
        }
        @Override
        @Nullable
        public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
            T data = this.streamCodec.decode(buf);
            data.assignHolder(holder);
            return data;
        }
    }

    protected record Copier<T extends EasyAttachment<T>>(UnaryOperator<T> copier) implements IAttachmentCopyHandler<T>
    {
        @Override
        @Nullable
        public T copy(T attachment, IAttachmentHolder holder, HolderLookup.Provider provider) {
            T data = this.copier.apply(attachment);
            data.assignHolder(holder);
            return data;
        }
    }

}
