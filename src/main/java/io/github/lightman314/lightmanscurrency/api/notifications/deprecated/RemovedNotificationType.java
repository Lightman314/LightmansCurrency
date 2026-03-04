package io.github.lightman314.lightmanscurrency.api.notifications.deprecated;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Alternative {@link NotificationType} for notifications that should be replaced with a new notification.<br>
 * Must be registered to {@link LCRegistries#NOTIFICATION_TYPES LCRegistries#NOTIFICATION_TYPES} just like any other notification type.<br>
 * If the notification that has been removed existed after notifications were re-written to utilize Codecs, please also override {@link RemovedNotificationType#codec()} and provide a codec<br>
 * Stream Codec is not needed, as no Notification should ever return this in {@link Notification#getType()}
 * @param <T>
 */
public abstract class RemovedNotificationType<T extends Notification> extends NotificationType<T> {

    @Override
    public final T loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        return this.loadAndConvert(tag,lookup);
    }
    protected abstract T loadAndConvert(CompoundTag tag, HolderLookup.Provider lookup);
    @Override
    public MapCodec<T> codec() { return MapCodec.unit(this::createNew); }
    @Override
    public final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() { return StreamHelper.unit(this::createNew); }

}
