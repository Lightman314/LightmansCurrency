package io.github.lightman314.lightmanscurrency.datagen.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An {@link Holder} class that can contain objects that do not currently exist.<br><br>
 * Intended to <b>ONLY</b> be used during datagen to allow reference to enchantments that don't exist during the datagen phase.
 */
public class EmptyHolder<T> implements Holder<T> {

    private final ResourceKey<T> key;
    public EmptyHolder(@Nonnull ResourceKey<T> key) { this.key = key; }

    @Nonnull
    @Override
    public T value() { throw new RuntimeException("Attempted to get value of an empty key!"); }
    @Override
    public boolean isBound() { return false; }
    @Override
    public boolean is(@Nonnull ResourceLocation id) { return this.key.location().equals(id); }
    @Override
    public boolean is(@Nonnull ResourceKey<T> key) { return this.key.equals(key); }
    @Override
    public boolean is(@Nonnull Predicate<ResourceKey<T>> predicate) { return predicate.test(this.key); }
    @Override
    public boolean is(@Nonnull TagKey<T> tagKey) { return false; }
    @Override
    public boolean is(@Nonnull Holder<T> holder) { return holder.is(this.key); }
    @Nonnull
    @Override
    public Stream<TagKey<T>> tags() { return Stream.empty(); }
    @Nonnull
    @Override
    public Either<ResourceKey<T>, T> unwrap() { return Either.left(this.key); }
    @Nonnull
    @Override
    public Optional<ResourceKey<T>> unwrapKey() { return Optional.of(this.key); }
    @Nonnull
    @Override
    public Kind kind() { return Kind.REFERENCE; }
    @Override
    public boolean canSerializeIn(@Nonnull HolderOwner<T> owner) { return true; }
}
