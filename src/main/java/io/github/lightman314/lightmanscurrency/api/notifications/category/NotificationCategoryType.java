package io.github.lightman314.lightmanscurrency.api.notifications.category;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class NotificationCategoryType<T extends NotificationCategory> extends AbstractType.Serializable<T,NotificationCategoryType<?>> {

    public NotificationCategoryType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<NotificationCategoryType<?>> getRegistry() { return LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE; }

    @Override
    protected String getName() { return "NotificationCategoryType"; }

}