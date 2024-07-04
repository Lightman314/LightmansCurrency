package io.github.lightman314.lightmanscurrency.api.ownership;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class OwnerType {

    private final ResourceLocation type;
    private final BiFunction<CompoundTag,HolderLookup.Provider,Owner> deserializer;
    private OwnerType(@Nonnull ResourceLocation type, @Nonnull BiFunction<CompoundTag,HolderLookup.Provider,Owner> deserializer)
    {
        this.type = type;
        this.deserializer = deserializer;
    }
    public static OwnerType create(@Nonnull ResourceLocation type, @Nonnull BiFunction<CompoundTag,HolderLookup.Provider,Owner> deserializer) { return new OwnerType(type,deserializer); }

    @Nonnull
    public ResourceLocation getID() { return this.type; }
    @Nonnull
    public Owner load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { return this.deserializer.apply(tag,lookup); }

}
