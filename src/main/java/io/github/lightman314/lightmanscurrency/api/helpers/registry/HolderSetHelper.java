package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class HolderSetHelper {

    private HolderSetHelper() {}

    public static abstract class SingleRegistryType<E> implements HolderSetType {

        protected abstract ResourceKey<Registry<E>> getTargetRegistry();
        protected abstract MapCodec<? extends ICustomHolderSet<E>> unsafeCodec();
        protected abstract StreamCodec<RegistryFriendlyByteBuf,? extends ICustomHolderSet<E>> unsafeStream();

        @Override
        public final <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<Holder<T>> codec, boolean b) {
            return this.isType(resourceKey) ? (MapCodec<? extends ICustomHolderSet<T>>)this.unsafeCodec() : MapCodec.unit(new EmptySet<>(this));
        }

        @Override
        public final <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
            return this.isType(resourceKey) ? (StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>>)this.unsafeStream() : StreamCodec.unit(new EmptySet<>(this));
        }

        private boolean isType(ResourceKey<?> resourceKey) { return resourceKey == this.getTargetRegistry(); }

    }

    private static final class EmptySet<T> implements ICustomHolderSet<T> {
        private final SingleRegistryType<?> type;
        private EmptySet(SingleRegistryType<?> type) { this.type = type; }
        @Override
        public HolderSetType type() { return this.type; }
        @Override
        public Stream<Holder<T>> stream() { return Stream.empty(); }
        @Override
        public int size() { return 0; }
        @Override
        public boolean isBound() { return true; }
        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() { return Either.right(List.of()); }
        @Override
        public Optional<Holder<T>> getRandomElement(RandomSource random) { return Optional.empty(); }
        @Override
        public Holder<T> get(int index) { return null; }
        @Override
        public boolean contains(Holder<T> value) { return false; }
        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) { return false; }
        @Override
        public Optional<TagKey<T>> unwrapKey() { return Optional.empty(); }
        @Nonnull
        @Override
        public Iterator<Holder<T>> iterator() { return this.stream().iterator(); }
    }

}