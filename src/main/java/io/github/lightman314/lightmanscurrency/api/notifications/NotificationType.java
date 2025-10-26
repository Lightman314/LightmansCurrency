package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class NotificationType<T extends Notification> {

    public final ResourceLocation type;
    private final Supplier<T> generator;

    public NotificationType(ResourceLocation type, Supplier<T> generator) { this.type = type; this.generator = generator; }

    public T load(CompoundTag tag, HolderLookup.Provider lookup)
    {
        T notification = this.generator.get();
        notification.load(tag, lookup);
        return notification;
    }

    @Override
    public final String toString() { return this.type.toString(); }

}
