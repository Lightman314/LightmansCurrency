package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class NotificationCategoryType<T extends NotificationCategory> {

    public final ResourceLocation type;
    private final BiFunction<CompoundTag,HolderLookup.Provider,T> generator;

    public NotificationCategoryType(@Nonnull ResourceLocation type, @Nonnull BiFunction<CompoundTag,HolderLookup.Provider,T> generator) { this.type = type; this.generator = generator; }

    @Nonnull
    public T load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { return this.generator.apply(tag,lookup); }
    @Override
    public String toString() { return this.type.toString(); }

}
