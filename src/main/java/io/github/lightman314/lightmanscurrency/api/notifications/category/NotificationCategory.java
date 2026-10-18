package io.github.lightman314.lightmanscurrency.api.notifications.category;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.UnfilteredCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public abstract class NotificationCategory implements ISidedContext.Mutable<NotificationCategory> {

    public static final Codec<NotificationCategory> CODEC = LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE.byNameCodec()
            .dispatch(NotificationCategory::getType,NotificationCategoryType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationCategory> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE_KEY)
            .dispatch(NotificationCategory::getType,NotificationCategoryType::streamCodec);

    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    @Override
    public final NotificationCategory setSidedContext(ISidedContext context) {
        this.context = context;
        return this;
    }
    @Override
    public final boolean isClient() { return this.context.isClient(); }
    @Override
    public final boolean isServer() { return Mutable.super.isServer(); }

    public abstract Component getName();
    public abstract IconData getIcon();
    public abstract NotificationCategoryType<?> getType();

    @Override
    public final int hashCode() { return Objects.hash(this.getType(),this.hash()); }
    protected abstract int hash();
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof NotificationCategory cat)
            return this.equals(cat);
        return false;
    }
    protected abstract boolean equals(NotificationCategory other);

    public final boolean notGeneral() { return this != UnfilteredCategory.INSTANCE; }

    @Override
    public final String toString() { return RegistryHelper.toString("NotificationCategory",LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE,this.getType()); }

}