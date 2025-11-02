package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NotificationType<T extends Notification> {

    public final ResourceLocation type;
    private final NonNullSupplier<T> generator;

    public NotificationType(ResourceLocation type, NonNullSupplier<T> generator) { this.type = type; this.generator = generator; }

    public T load(CompoundTag tag)
    {
        T notification = this.generator.get();
        notification.load(tag);
        return notification;
    }

    @Override
    public final String toString() { return this.type.toString(); }

}
