package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

public abstract class AbstractType<T extends AbstractType<T>> implements TypedInstance<T> {

    private Holder<T> holder = null;

    protected T getEntry() { return (T)this; }
    protected abstract Registry<T> getRegistry();
    protected abstract String getName();

    @Override
    public Holder<T> typeHolder() {
        if(this.holder == null)
            this.holder = this.getRegistry().wrapAsHolder(this.getEntry());
        return this.holder;
    }

    @Nullable
    public final Identifier getKey() { return this.getRegistry().getKey(this.getEntry()); }
    @Nullable
    public final ResourceKey<T> getResourceKey() { return this.getRegistry().getResourceKey(this.getEntry()).orElse(null); }
    public final int getSyncID() { return this.getRegistry().getId(this.getEntry()); }

    @Override
    public final int hashCode() { return RegistryHelper.hash(this.getRegistry(),this.getEntry()); }
    @Override
    public final String toString() { return RegistryHelper.toString(this.getName(),this.getRegistry(),this.getEntry()); }

    public static abstract class WithCodec<X,T extends WithCodec<?,T>> extends AbstractType<T> {

        private final MapCodec<X> codec;
        public final MapCodec<X> codec() { return this.codec; }
        public WithCodec(MapCodec<X> codec) { this.codec = codec; }

    }

    public static abstract class Serializable<X,T extends Serializable<?,T>> extends WithCodec<X,T> {

        private final StreamCodec<? super RegistryFriendlyByteBuf,X> streamCodec;
        public final StreamCodec<? super RegistryFriendlyByteBuf,X> streamCodec() { return this.streamCodec; }
        public Serializable(MapCodec<X> codec, StreamCodec<? super RegistryFriendlyByteBuf,X> streamCodec) {
            super(codec);
            this.streamCodec = streamCodec;
        }

    }

}