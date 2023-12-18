package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public final class NotificationType<T extends Notification> {

    public final ResourceLocation type;
    private final NonNullSupplier<T> generator;

    public NotificationType(@Nonnull ResourceLocation type, @Nonnull NonNullSupplier<T> generator) { this.type = type; this.generator = generator; }

    @Nonnull
    public T load(@Nonnull CompoundTag tag)
    {
        T notification = this.generator.get();
        notification.load(tag);
        return notification;
    }

    @Override
    public String toString() { return this.type.toString(); }

}
