package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NotificationCategoryType<T extends NotificationCategory> {

    public final ResourceLocation type;
    private final BiFunction<CompoundTag,HolderLookup.Provider,T> generator;

    public NotificationCategoryType(ResourceLocation type, BiFunction<CompoundTag,HolderLookup.Provider,T> generator) { this.type = type; this.generator = generator; }

    public T load(CompoundTag tag, HolderLookup.Provider lookup) { return this.generator.apply(tag,lookup); }
    @Override
    public String toString() { return this.type.toString(); }

}
