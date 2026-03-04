package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public abstract class NotificationType<T extends Notification> {

    public static final Codec<NotificationType<?>> CODEC = LCRegistries.NOTIFICATION_TYPES.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationType<?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.NOTIFICATION_TYPE_KEY);

    protected abstract T createNew();
    @Deprecated
    public T loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        T notification = this.createNew();
        notification.loadOld(tag, lookup);
        return notification;
    }

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<RegistryFriendlyByteBuf,T> streamCodec();

    @Override
    public String toString() { return "NotificationType[" + LCRegistries.NOTIFICATION_TYPES.getKey(this) + "]"; }

}
