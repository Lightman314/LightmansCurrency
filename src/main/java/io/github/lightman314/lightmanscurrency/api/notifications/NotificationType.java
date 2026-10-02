package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class NotificationType<T extends Notification> extends AbstractType.Serializable<T,NotificationType<?>> {

    public NotificationType(MapCodec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<NotificationType<?>> getRegistry() { return LCRegistries.Notifications.NOTIFICATION_TYPE; }
    @Override
    protected String getName() { return "NotificationType"; }

}