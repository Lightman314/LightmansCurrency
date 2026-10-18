package io.github.lightman314.lightmanscurrency.api.notifications.category;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public abstract class SingletonNotificationCategory extends NotificationCategory {

    //Text is publicly available for translation purposes
    public final TextEntry text;
    protected SingletonNotificationCategory() { this.text = TextEntry.delayedNotificationCategory(this::getType); }

    public static <T extends SingletonNotificationCategory> NotificationCategoryType<T> buildType(T instance) { return new NotificationCategoryType<>(MapCodec.unit(instance),StreamCodec.unit(instance)); }

    @Override
    public final Component getName() { return this.text.get(); }
    @Override
    public final boolean equals(NotificationCategory other) { return other == this; }
    @Override
    protected final int hash() { return 0; }

}