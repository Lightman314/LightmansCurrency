package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A piece of data that stores data that can be displayed as a notification.<br>
 * @see NotificationStack
 */
@Immutable
public abstract class Notification implements ISidedContext.Mutable<Notification> {

    public static final Codec<Notification> CODEC = LCRegistries.Notifications.NOTIFICATION_TYPE.byNameCodec()
            .dispatch(Notification::getType,NotificationType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,Notification> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Notifications.NOTIFICATION_TYPE_KEY)
            .dispatch(Notification::getType,NotificationType::streamCodec);

    public static final TextEntry NOTIFICATION_FORMAT_GENERAL = new TextEntry("lightmanscurrency.notifications.format.general");
    public static final TextEntry NOTIFICATION_FORMAT_CHAT = new TextEntry("lightmanscurrency.notifications.format.chat");
    public static final TextEntry NOTIFICATION_FORMAT_CHAT_TITLE = new TextEntry("lightmanscurrency.notifications.format.chat.title");
    public static final TextEntry NOTIFICATION_TIMESTAMP = new TextEntry("lightmanscurrency.notifications.timestamp");
    public static final TextEntry TOOLTIP_DELETE = TextEntry.tooltip(LCApi.MODID,"notifications.delete");

    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    @Override
    public final Notification setSidedContext(ISidedContext context) {
        this.context = context;
        return this;
    }

    @Override
    public final boolean isClient() { return this.context.isClient(); }
    @Override
    public final boolean isServer() { return Mutable.super.isServer(); }

    public abstract NotificationType<?> getType();
    public abstract NotificationCategory getCategory();

    public abstract List<Component> getMessageLines();
    public List<Component> getGeneralMessage() { return this.getModifiedMessage(line -> NOTIFICATION_FORMAT_GENERAL.get(this.getCategory().getName(),line)); }
    public List<Component> getChatMessage() { return this.getModifiedMessage(line -> NOTIFICATION_FORMAT_CHAT.get(NOTIFICATION_FORMAT_CHAT_TITLE.get(this.getCategory().getName()).withStyle(ChatFormatting.GOLD),line)); }

    public Component getTimeStampMessage(long timestamp) { return NOTIFICATION_TIMESTAMP.get(TimeHelper.formatTime(timestamp)); }

    public final List<Component> getModifiedMessage(UnaryOperator<Component> edit) {
        List<Component> message = new ArrayList<>(this.getMessageLines());
        if(message.isEmpty())
            return message;
        message.set(0,edit.apply(message.getFirst()));
        return message;
    }

    public boolean canMerge(Notification other) { return this.equals(other); }

    protected abstract boolean equals(Notification other);

    @Override
    public final boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof Notification n)
            return this.equals(n);
        return false;
    }

    protected abstract int hash();

    @Override
    public final int hashCode() { return Objects.hash(this.getType(),this.hash()); }

    @Override
    public final String toString() { return RegistryHelper.toString("Notification",LCRegistries.Notifications.NOTIFICATION_TYPE,this.getType()); }

}
